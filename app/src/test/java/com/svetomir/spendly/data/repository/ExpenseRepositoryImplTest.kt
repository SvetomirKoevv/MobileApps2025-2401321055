package com.svetomir.spendly.data.repository

import app.cash.turbine.test
import com.svetomir.spendly.data.FakeExpenseDao
import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpenseRepositoryImplTest {

    private lateinit var repository: ExpenseRepositoryImpl

    @Before
    fun setUp() {
        repository = ExpenseRepositoryImpl(FakeExpenseDao())
    }

    private fun expense(title: String, amount: Double = 5.0) =
        Expense(title = title, amount = amount, category = Category.FOOD, date = 1_000L)

    @Test
    fun addExpense_appearsInList() = runTest {
        repository.addExpense(expense("Lunch"))

        val list = repository.getExpenses().first()

        assertEquals(1, list.size)
        assertEquals("Lunch", list.first().title)
    }

    @Test
    fun addExpense_returnsGeneratedId() = runTest {
        val id = repository.addExpense(expense("Lunch"))

        assertTrue(id > 0)
        assertEquals("Lunch", repository.getExpense(id).first()?.title)
    }

    @Test
    fun updateExpense_changesAmount() = runTest {
        val id = repository.addExpense(expense("Lunch", amount = 5.0))

        repository.updateExpense(expense("Lunch", amount = 12.5).copy(id = id))

        assertEquals(12.5, repository.getExpense(id).first()?.amount ?: 0.0, 0.001)
    }

    @Test
    fun deleteExpense_removesIt() = runTest {
        val id = repository.addExpense(expense("Lunch"))
        val saved = requireNotNull(repository.getExpense(id).first())

        repository.deleteExpense(saved)

        assertNull(repository.getExpense(id).first())
    }

    @Test
    fun getExpense_unknownId_returnsNull() = runTest {
        assertNull(repository.getExpense(42L).first())
    }

    @Test
    fun searchExpenses_filtersByTitle() = runTest {
        repository.addExpense(expense("Coffee"))
        repository.addExpense(expense("Taxi"))

        val result = repository.searchExpenses("tax").first()

        assertEquals(listOf("Taxi"), result.map { it.title })
    }

    @Test
    fun getExpenses_emitsOnEveryChange() = runTest {
        repository.getExpenses().test {
            assertEquals(0, awaitItem().size)
            repository.addExpense(expense("Lunch"))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
