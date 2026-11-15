package com.svetomir.spendly.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(repository: ExpenseRepository,) : ViewModel() {

    val uiState: StateFlow<ExpenseListUiState> = repository.getExpenses()
        .map { expenses ->
            ExpenseListUiState(
                expenses = expenses,
                total = expenses.sumOf { it.amount },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ExpenseListUiState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
