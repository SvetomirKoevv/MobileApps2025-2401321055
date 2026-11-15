package com.svetomir.spendly.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditUiState(
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.FOOD,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val titleError: Boolean = false,
    val amountError: Boolean = false,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>(ARG_EXPENSE_ID) ?: NEW_EXPENSE_ID

    private val _uiState = MutableStateFlow(
        AddEditUiState(isEditing = expenseId != NEW_EXPENSE_ID),
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (expenseId != NEW_EXPENSE_ID) {
            loadExpense()
        }
    }

    private fun loadExpense() {
        viewModelScope.launch {
            val expense = repository.getExpense(expenseId).first() ?: return@launch
            _uiState.update {
                it.copy(
                    title = expense.title,
                    amount = expense.amount.toString(),
                    category = expense.category,
                    date = expense.date,
                    note = expense.note,
                )
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value, titleError = false)
    }

    fun onAmountChange(value: String) = _uiState.update {
        it.copy(amount = value, amountError = false)
    }

    fun onCategoryChange(value: Category) = _uiState.update { it.copy(category = value) }

    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
        val titleError = state.title.isBlank()

        if (titleError || amount == null) {
            _uiState.update { it.copy(titleError = titleError, amountError = amount == null) }
            return
        }

        val expense = Expense(
            id = expenseId,
            title = state.title.trim(),
            amount = amount,
            category = state.category,
            date = state.date,
            note = state.note.trim(),
        )
        viewModelScope.launch {
            if (state.isEditing) {
                repository.updateExpense(expense)
            } else {
                repository.addExpense(expense)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        const val ARG_EXPENSE_ID = "expenseId"
        const val NEW_EXPENSE_ID = 0L
    }
}
