package com.svetomir.spendly.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(private val repository: ExpenseRepository,) :
    ViewModel() {

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

    private val _recentlyDeleted = MutableStateFlow<Expense?>(null)
    val recentlyDeleted: StateFlow<Expense?> = _recentlyDeleted.asStateFlow()

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            val expense = repository.getExpense(id).first() ?: return@launch
            repository.deleteExpense(expense)
            _recentlyDeleted.value = expense
        }
    }

    fun undoDelete() {
        val expense = _recentlyDeleted.value ?: return
        _recentlyDeleted.value = null
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeleted.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
