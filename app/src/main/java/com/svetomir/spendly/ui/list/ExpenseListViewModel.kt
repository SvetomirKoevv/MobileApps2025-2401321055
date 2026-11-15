package com.svetomir.spendly.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val hasAnyExpenses: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<Category?>(null)

    val uiState: StateFlow<ExpenseListUiState> = combine(
        repository.getExpenses(),
        searchQuery,
        selectedCategory,
    ) { expenses, query, category ->
        val filtered = expenses.filter { expense ->
            val matchesQuery = query.isBlank() ||
                    expense.title.contains(query.trim(), ignoreCase = true)
            val matchesCategory = category == null || expense.category == category
            matchesQuery && matchesCategory
        }
        ExpenseListUiState(
            expenses = filtered,
            total = filtered.sumOf { it.amount },
            searchQuery = query,
            selectedCategory = category,
            hasAnyExpenses = expenses.isNotEmpty(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ExpenseListUiState(),
    )

    private val _recentlyDeleted = MutableStateFlow<Expense?>(null)
    val recentlyDeleted: StateFlow<Expense?> = _recentlyDeleted.asStateFlow()

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: Category?) {
        selectedCategory.update { current -> if (current == category) null else category }
    }

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