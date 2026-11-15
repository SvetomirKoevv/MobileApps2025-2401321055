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

    private fun expense(title: String, amount: Double, category: Category = Category.FOOD) =
        Expense(title = title, amount = amount, category = category, date = 1_000L)

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

    @Test
    fun search_filtersByTitleIgnoringCase() = runTest {
        repository.addExpense(expense("Coffee", 3.5))
        repository.addExpense(expense("Taxi", 12.0))
        collectUiState()

        viewModel.onSearchQueryChange("cOf")

        val state = viewModel.uiState.value
        assertEquals("cOf", state.searchQuery)
        assertEquals(listOf("Coffee"), state.expenses.map { it.title })
    }

    @Test
    fun search_blankQuery_showsEverything() = runTest {
        repository.addExpense(expense("Coffee", 3.5))
        repository.addExpense(expense("Taxi", 12.0))
        collectUiState()

        viewModel.onSearchQueryChange("   ")

        assertEquals(2, viewModel.uiState.value.expenses.size)
    }

    @Test
    fun categoryFilter_showsOnlyThatCategory() = runTest {
        repository.addExpense(expense("Coffee", 3.5, Category.FOOD))
        repository.addExpense(expense("Taxi", 12.0, Category.TRANSPORT))
        collectUiState()

        viewModel.onCategorySelected(Category.TRANSPORT)

        val state = viewModel.uiState.value
        assertEquals(Category.TRANSPORT, state.selectedCategory)
        assertEquals(listOf("Taxi"), state.expenses.map { it.title })
    }

    @Test
    fun categoryFilter_selectingSameCategoryAgain_clearsFilter() = runTest {
        repository.addExpense(expense("Coffee", 3.5, Category.FOOD))
        repository.addExpense(expense("Taxi", 12.0, Category.TRANSPORT))
        collectUiState()

        viewModel.onCategorySelected(Category.TRANSPORT)
        viewModel.onCategorySelected(Category.TRANSPORT)

        assertNull(viewModel.uiState.value.selectedCategory)
        assertEquals(2, viewModel.uiState.value.expenses.size)
    }

    @Test
    fun categoryFilter_null_showsAllCategories() = runTest {
        repository.addExpense(expense("Coffee", 3.5, Category.FOOD))
        repository.addExpense(expense("Taxi", 12.0, Category.TRANSPORT))
        collectUiState()
        viewModel.onCategorySelected(Category.FOOD)

        viewModel.onCategorySelected(null)

        assertEquals(2, viewModel.uiState.value.expenses.size)
    }

    @Test
    fun searchAndCategory_areCombined() = runTest {
        repository.addExpense(expense("Bus ticket", 2.0, Category.TRANSPORT))
        repository.addExpense(expense("Taxi", 12.0, Category.TRANSPORT))
        repository.addExpense(expense("Bus snack", 1.5, Category.FOOD))
        collectUiState()

        viewModel.onSearchQueryChange("bus")
        viewModel.onCategorySelected(Category.TRANSPORT)

        assertEquals(listOf("Bus ticket"), viewModel.uiState.value.expenses.map { it.title })
    }

    @Test
    fun total_onlyCountsFilteredExpenses() = runTest {
        repository.addExpense(expense("Coffee", 3.5, Category.FOOD))
        repository.addExpense(expense("Taxi", 12.0, Category.TRANSPORT))
        collectUiState()

        viewModel.onCategorySelected(Category.FOOD)

        assertEquals(3.5, viewModel.uiState.value.total, 0.001)
    }

    @Test
    fun noMatches_stillReportsThatExpensesExist() = runTest {
        repository.addExpense(expense("Coffee", 3.5))
        collectUiState()

        viewModel.onSearchQueryChange("pizza")

        val state = viewModel.uiState.value
        assertTrue(state.expenses.isEmpty())
        assertTrue(state.hasAnyExpenses)
    }

    @Test
    fun emptyRepository_hasNoExpenses() = runTest {
        collectUiState()

        assertFalse(viewModel.uiState.value.hasAnyExpenses)
    }
}
