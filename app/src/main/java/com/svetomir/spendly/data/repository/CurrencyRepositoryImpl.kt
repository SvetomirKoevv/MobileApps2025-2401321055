package com.svetomir.spendly.data.repository

import com.svetomir.spendly.data.remote.FrankfurterApi
import com.svetomir.spendly.data.remote.RatesResponse
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class CurrencyRepositoryImpl @Inject constructor(
    private val api: FrankfurterApi,
) : CurrencyRepository {

    override suspend fun getRates(): Result<RatesResponse> = try {
        Result.success(api.getLatestRates(base = BASE_CURRENCY))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: SerializationException) {
        Result.failure(e)
    }

    private companion object {
        const val BASE_CURRENCY = "EUR"
    }
}