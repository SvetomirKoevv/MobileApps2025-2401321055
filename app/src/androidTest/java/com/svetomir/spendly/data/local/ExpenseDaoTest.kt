package com.svetomir.spendly.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.expenseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun expense(title: String, date: Long = 1_000L) =
        Expense(title = title, amount = 10.0, category = Category.FOOD, date = date)

    @Test
    fun getAll_returnsNewestFirst() = runTest {
        dao.insert(expense("Old", date = 1_000L))
        dao.insert(expense("New", date = 2_000L))

        val titles = dao.getAll().first().map { it.title }

        assertEquals(listOf("New", "Old"), titles)
    }

    @Test
    fun update_changesStoredExpense() = runTest {
        val id = dao.insert(expense("Coffee"))

        dao.update(expense("Tea").copy(id = id))

        assertEquals("Tea", dao.getById(id).first()?.title)
    }

    @Test
    fun delete_removesExpense() = runTest {
        val id = dao.insert(expense("Coffee"))
        val saved = requireNotNull(dao.getById(id).first())

        dao.delete(saved)

        assertNull(dao.getById(id).first())
    }

    @Test
    fun search_matchesPartOfTitle() = runTest {
        dao.insert(expense("Coffee"))
        dao.insert(expense("Bus ticket"))

        val result = dao.search("off").first()

        assertEquals(listOf("Coffee"), result.map { it.title })
    }

    @Test
    fun getAll_emitsAgainAfterInsert() = runTest {
        dao.getAll().test {
            assertEquals(0, awaitItem().size)
            dao.insert(expense("Lunch"))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
