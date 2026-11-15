package com.svetomir.spendly.ui.addedit

import androidx.lifecycle.SavedStateHandle
import com.svetomir.spendly.MainDispatcherRule
import com.svetomir.spendly.data.FakeExpenseDao
import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ExpenseRepositoryImpl

    @Before
    fun setUp() {
        repository = ExpenseRepositoryImpl(FakeExpenseDao())
    }

    private fun newExpenseViewModel() = AddEditExpenseViewModel(repository, SavedStateHandle())

    private fun editExpenseViewModel(id: Long) = AddEditExpenseViewModel(
        repository,
        SavedStateHandle(mapOf(AddEditExpenseViewModel.ARG_EXPENSE_ID to id)),
    )

    private suspend fun savedExpenses() = repository.getExpenses().first()

    @Test
    fun newExpense_startsEmptyAndNotEditing() {
        val state = newExpenseViewModel().uiState.value

        assertFalse(state.isEditing)
        assertEquals("", state.title)
        assertEquals("", state.amount)
    }

    @Test
    fun save_blankTitle_showsTitleErrorAndSavesNothing() = runTest {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("   ")
        viewModel.onAmountChange("5")

        viewModel.save()

        assertTrue(viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.amountError)
        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(savedExpenses().isEmpty())
    }

    @Test
    fun save_textAmount_showsAmountError() = runTest {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("Coffee")
        viewModel.onAmountChange("abc")

        viewModel.save()

        assertTrue(viewModel.uiState.value.amountError)
        assertTrue(savedExpenses().isEmpty())
    }

    @Test
    fun save_zeroAmount_showsAmountError() {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("Coffee")
        viewModel.onAmountChange("0")

        viewModel.save()

        assertTrue(viewModel.uiState.value.amountError)
    }

    @Test
    fun save_negativeAmount_showsAmountError() {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("Coffee")
        viewModel.onAmountChange("-4")

        viewModel.save()

        assertTrue(viewModel.uiState.value.amountError)
    }

    @Test
    fun save_emptyForm_showsBothErrors() {
        val viewModel = newExpenseViewModel()

        viewModel.save()

        assertTrue(viewModel.uiState.value.titleError)
        assertTrue(viewModel.uiState.value.amountError)
    }

    @Test
    fun typing_clearsErrors() {
        val viewModel = newExpenseViewModel()
        viewModel.save()

        viewModel.onTitleChange("C")
        viewModel.onAmountChange("1")

        assertFalse(viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.amountError)
    }

    @Test
    fun save_validInput_addsExpenseAndMarksSaved() = runTest {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("  Coffee  ")
        viewModel.onAmountChange("3.5")
        viewModel.onCategoryChange(Category.SHOPPING)
        viewModel.onDateChange(2_000L)
        viewModel.onNoteChange(" with milk ")

        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        val saved = savedExpenses().single()
        assertEquals("Coffee", saved.title)
        assertEquals(3.5, saved.amount, 0.001)
        assertEquals(Category.SHOPPING, saved.category)
        assertEquals(2_000L, saved.date)
        assertEquals("with milk", saved.note)
    }

    @Test
    fun save_acceptsCommaAsDecimalSeparator() = runTest {
        val viewModel = newExpenseViewModel()
        viewModel.onTitleChange("Coffee")
        viewModel.onAmountChange("3,50")

        viewModel.save()

        assertEquals(3.5, savedExpenses().single().amount, 0.001)
    }

    @Test
    fun editExpense_loadsExistingValues() = runTest {
        val id = repository.addExpense(
            Expense(
                title = "Taxi",
                amount = 12.0,
                category = Category.TRANSPORT,
                date = 5_000L,
                note = "Airport",
            ),
        )

        val state = editExpenseViewModel(id).uiState.value

        assertTrue(state.isEditing)
        assertEquals("Taxi", state.title)
        assertEquals("12.0", state.amount)
        assertEquals(Category.TRANSPORT, state.category)
        assertEquals(5_000L, state.date)
        assertEquals("Airport", state.note)
    }

    @Test
    fun editExpense_saveUpdatesInsteadOfAdding() = runTest {
        val id = repository.addExpense(
            Expense(title = "Taxi", amount = 12.0, category = Category.TRANSPORT, date = 5_000L),
        )
        val viewModel = editExpenseViewModel(id)
        viewModel.onAmountChange("15")

        viewModel.save()

        val saved = savedExpenses().single()
        assertEquals(id, saved.id)
        assertEquals(15.0, saved.amount, 0.001)
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
