package com.svetomir.spendly.ui.list

import com.svetomir.spendly.MainDispatcherRule
import com.svetomir.spendly.data.FakeExpenseDao
import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ExpenseRepositoryImpl
    private lateinit var viewModel: ExpenseListViewModel

    @Before
    fun setUp() {
        repository = ExpenseRepositoryImpl(FakeExpenseDao())
        viewModel = ExpenseListViewModel(repository)
    }

    private fun expense(title: String, amount: Double) =
        Expense(title = title, amount = amount, category = Category.FOOD, date = 1_000L)

    /** uiState only updates while someone collects it (WhileSubscribed), like the screen does. */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun emptyRepository_showsEmptyList() = runTest {
        collectUiState()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.expenses.isEmpty())
        assertEquals(0.0, state.total, 0.001)
    }

    @Test
    fun expenses_areShownWithTotal() = runTest {
        repository.addExpense(expense("Coffee", 3.5))
        repository.addExpense(expense("Taxi", 12.0))
        collectUiState()

        val state = viewModel.uiState.value
        assertEquals(2, state.expenses.size)
        assertEquals(15.5, state.total, 0.001)
    }

    @Test
    fun newExpense_updatesListAutomatically() = runTest {
        collectUiState()

        repository.addExpense(expense("Lunch", 8.0))

        assertEquals(1, viewModel.uiState.value.expenses.size)
        assertEquals(8.0, viewModel.uiState.value.total, 0.001)
    }

    @Test
    fun deleteExpense_removesItAndRemembersIt() = runTest {
        val id = repository.addExpense(expense("Coffee", 3.5))
        collectUiState()

        viewModel.deleteExpense(id)

        assertTrue(viewModel.uiState.value.expenses.isEmpty())
        assertEquals("Coffee", viewModel.recentlyDeleted.value?.title)
    }

    @Test
    fun deleteExpense_unknownId_doesNothing() = runTest {
        repository.addExpense(expense("Coffee", 3.5))
        collectUiState()

        viewModel.deleteExpense(99L)

        assertEquals(1, viewModel.uiState.value.expenses.size)
        assertNull(viewModel.recentlyDeleted.value)
    }

    @Test
    fun undoDelete_restoresExpenseWithSameId() = runTest {
        val id = repository.addExpense(expense("Coffee", 3.5))
        collectUiState()
        viewModel.deleteExpense(id)

        viewModel.undoDelete()

        val expenses = viewModel.uiState.value.expenses
        assertEquals(1, expenses.size)
        assertEquals(id, expenses.first().id)
        assertNull(viewModel.recentlyDeleted.value)
    }

    @Test
    fun undoDelete_withoutDelete_doesNothing() = runTest {
        collectUiState()

        viewModel.undoDelete()

        assertTrue(viewModel.uiState.value.expenses.isEmpty())
    }

    @Test
    fun clearRecentlyDeleted_keepsExpenseDeleted() = runTest {
        val id = repository.addExpense(expense("Coffee", 3.5))
        collectUiState()
        viewModel.deleteExpense(id)

        viewModel.clearRecentlyDeleted()
        viewModel.undoDelete()

        assertNull(viewModel.recentlyDeleted.value)
        assertTrue(viewModel.uiState.value.expenses.isEmpty())
    }
}
