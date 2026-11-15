package com.svetomir.spendly.data.repository

import com.svetomir.spendly.data.remote.FrankfurterApi
import com.svetomir.spendly.data.remote.RatesResponse
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class CurrencyRepositoryImplTest {

    /** Fake API that either returns [response] or throws [error], without any network. */
    private class FakeFrankfurterApi(
        private val response: RatesResponse? = null,
        private val error: Exception? = null,
    ) : FrankfurterApi {
        var requestedBase: String? = null

        override suspend fun getLatestRates(base: String): RatesResponse {
            requestedBase = base
            error?.let { throw it }
            return requireNotNull(response)
        }
    }

    private val sampleResponse = RatesResponse(
        base = "EUR",
        date = "2026-11-13",
        rates = mapOf("USD" to 1.1052, "GBP" to 0.83559),
    )

    @Test
    fun getRates_success_returnsRates() = runTest {
        val repository = CurrencyRepositoryImpl(FakeFrankfurterApi(response = sampleResponse))

        val result = repository.getRates()

        assertTrue(result.isSuccess)
        assertEquals(1.1052, result.getOrThrow().rates.getValue("USD"), 0.0001)
        assertEquals("2026-11-13", result.getOrThrow().date)
    }

    @Test
    fun getRates_asksForEuroRates() = runTest {
        val api = FakeFrankfurterApi(response = sampleResponse)

        CurrencyRepositoryImpl(api).getRates()

        assertEquals("EUR", api.requestedBase)
    }

    @Test
    fun getRates_noInternet_returnsFailure() = runTest {
        val api = FakeFrankfurterApi(error = IOException("No internet"))

        val result = CurrencyRepositoryImpl(api).getRates()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun getRates_serverError_returnsFailure() = runTest {
        val serverError = HttpException(Response.error<RatesResponse>(500, "".toResponseBody()))
        val api = FakeFrankfurterApi(error = serverError)

        val result = CurrencyRepositoryImpl(api).getRates()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }
}
