package com.svetomir.spendly.ui.detail

import androidx.lifecycle.SavedStateHandle
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

data class ExpenseDetailUiState(val expense: Expense? = null, val isLoading: Boolean = true,)

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>(ARG_EXPENSE_ID) ?: 0L

    val uiState: StateFlow<ExpenseDetailUiState> = repository.getExpense(expenseId)
        .map { expense -> ExpenseDetailUiState(expense = expense, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ExpenseDetailUiState(),
        )

    companion object {
        const val ARG_EXPENSE_ID = "expenseId"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
