package com.svetomir.spendly.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svetomir.spendly.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RatesState {
    data object Loading : RatesState
    data class Success(val date: String, val rates: Map<String, Double>) : RatesState
    data object Error : RatesState
}

data class CurrencyUiState(
    val ratesState: RatesState = RatesState.Loading,
    val selectedCurrency: String = CurrencyViewModel.DEFAULT_CURRENCY,
)

@HiltViewModel
class CurrencyViewModel @Inject constructor(private val repository: CurrencyRepository,) :
    ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    init {
        loadRates()
    }

    fun retry() = loadRates()

    fun selectCurrency(code: String) = _uiState.update { it.copy(selectedCurrency = code) }

    private fun loadRates() {
        _uiState.update { it.copy(ratesState = RatesState.Loading) }
        viewModelScope.launch {
            val result = repository.getRates()
            val newState = result.fold(
                onSuccess = { response ->
                    val supported = SUPPORTED_CURRENCIES
                        .mapNotNull { code -> response.rates[code]?.let { code to it } }
                        .toMap()
                    RatesState.Success(date = response.date, rates = supported)
                },
                onFailure = { RatesState.Error },
            )
            _uiState.update { it.copy(ratesState = newState) }
        }
    }

    companion object {
        const val DEFAULT_CURRENCY = "USD"
        val SUPPORTED_CURRENCIES = listOf("USD", "GBP", "CHF", "JPY", "TRY", "RON")
    }
}
