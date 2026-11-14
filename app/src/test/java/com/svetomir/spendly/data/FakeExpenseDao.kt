package com.svetomir.spendly.data

import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory replacement for Room's DAO, used by unit tests. */
class FakeExpenseDao : ExpenseDao {

    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<Expense>> =
        expenses.map { list -> list.sortedByDescending { it.date } }

    override fun getById(id: Long): Flow<Expense?> =
        expenses.map { list -> list.find { it.id == id } }

    override fun search(query: String): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.title.contains(query, ignoreCase = true) }
            .sortedByDescending { it.date }
    }

    override suspend fun insert(expense: Expense): Long {
        val id = if (expense.id == 0L) nextId++ else expense.id
        expenses.value = expenses.value + expense.copy(id = id)
        return id
    }

    override suspend fun update(expense: Expense) {
        expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun delete(expense: Expense) {
        expenses.value = expenses.value.filterNot { it.id == expense.id }
    }
}
