package com.svetomir.spendly.ui.currency

import com.svetomir.spendly.MainDispatcherRule
import com.svetomir.spendly.data.remote.RatesResponse
import com.svetomir.spendly.data.repository.CurrencyRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CurrencyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Fake repository: answers with the next queued result, or waits on [pending] if set. */
    private class FakeCurrencyRepository(results: List<Result<RatesResponse>> = emptyList()) :
        CurrencyRepository {
        private val queue = results.toMutableList()
        var pending: CompletableDeferred<Result<RatesResponse>>? = null
        var calls = 0

        override suspend fun getRates(): Result<RatesResponse> {
            calls++
            pending?.let { return it.await() }
            return queue.removeAt(0)
        }
    }

    private val ratesResponse = RatesResponse(
        base = "EUR",
        date = "2026-11-13",
        rates = mapOf("GBP" to 0.83559, "USD" to 1.1052, "XYZ" to 9.9),
    )

    private val success = Result.success(ratesResponse)
    private val noInternet = Result.failure<RatesResponse>(IOException("offline"))

    @Test
    fun whileRequestRuns_stateIsLoading() {
        val repository = FakeCurrencyRepository().apply { pending = CompletableDeferred() }

        val viewModel = CurrencyViewModel(repository)

        assertEquals(RatesState.Loading, viewModel.uiState.value.ratesState)
    }

    @Test
    fun success_showsRatesAndDate() {
        val viewModel = CurrencyViewModel(FakeCurrencyRepository(listOf(success)))

        val state = viewModel.uiState.value.ratesState as RatesState.Success
        assertEquals("2026-11-13", state.date)
        assertEquals(1.1052, state.rates.getValue("USD"), 0.0001)
    }

    @Test
    fun success_keepsOnlySupportedCurrenciesInFixedOrder() {
        val viewModel = CurrencyViewModel(FakeCurrencyRepository(listOf(success)))

        val state = viewModel.uiState.value.ratesState as RatesState.Success
        assertEquals(listOf("USD", "GBP"), state.rates.keys.toList())
    }

    @Test
    fun defaultCurrency_isUsd() {
        val viewModel = CurrencyViewModel(FakeCurrencyRepository(listOf(success)))

        assertEquals(CurrencyViewModel.DEFAULT_CURRENCY, viewModel.uiState.value.selectedCurrency)
        assertEquals("USD", viewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun selectCurrency_changesSelection() {
        val viewModel = CurrencyViewModel(FakeCurrencyRepository(listOf(success)))

        viewModel.selectCurrency("GBP")

        assertEquals("GBP", viewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun failure_showsError() {
        val viewModel = CurrencyViewModel(FakeCurrencyRepository(listOf(noInternet)))

        assertEquals(RatesState.Error, viewModel.uiState.value.ratesState)
    }

    @Test
    fun retry_afterFailure_loadsRates() {
        val repository = FakeCurrencyRepository(listOf(noInternet, success))
        val viewModel = CurrencyViewModel(repository)

        viewModel.retry()

        assertTrue(viewModel.uiState.value.ratesState is RatesState.Success)
        assertEquals(2, repository.calls)
    }

    @Test
    fun retry_showsLoadingWhileWaiting() {
        val repository = FakeCurrencyRepository(listOf(noInternet))
        val viewModel = CurrencyViewModel(repository)
        repository.pending = CompletableDeferred()

        viewModel.retry()

        assertEquals(RatesState.Loading, viewModel.uiState.value.ratesState)
    }
}
