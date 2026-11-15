package com.svetomir.spendly.ui.detail

import androidx.lifecycle.SavedStateHandle
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
class ExpenseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ExpenseRepositoryImpl

    private val coffee =
        Expense(title = "Coffee", amount = 3.5, category = Category.FOOD, date = 1L)

    @Before
    fun setUp() {
        repository = ExpenseRepositoryImpl(FakeExpenseDao())
    }

    private fun viewModelFor(id: Long) = ExpenseDetailViewModel(
        repository,
        SavedStateHandle(mapOf(ExpenseDetailViewModel.ARG_EXPENSE_ID to id)),
    )

    private fun TestScope.collect(viewModel: ExpenseDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModelFor(1L).uiState.value.isLoading)
    }

    @Test
    fun existingExpense_isShown() = runTest {
        val id = repository.addExpense(coffee)
        val viewModel = viewModelFor(id)
        collect(viewModel)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Coffee", state.expense?.title)
    }

    @Test
    fun unknownId_showsNotFound() = runTest {
        val viewModel = viewModelFor(42L)
        collect(viewModel)

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.expense)
    }

    @Test
    fun editedExpense_updatesDetailsAutomatically() = runTest {
        val id = repository.addExpense(coffee)
        val viewModel = viewModelFor(id)
        collect(viewModel)

        repository.updateExpense(coffee.copy(id = id, amount = 4.2))

        assertEquals(4.2, viewModel.uiState.value.expense?.amount ?: 0.0, 0.001)
    }

    @Test
    fun deletedExpense_becomesNotFound() = runTest {
        val id = repository.addExpense(coffee)
        val viewModel = viewModelFor(id)
        collect(viewModel)

        repository.deleteExpense(coffee.copy(id = id))

        assertNull(viewModel.uiState.value.expense)
    }
}
