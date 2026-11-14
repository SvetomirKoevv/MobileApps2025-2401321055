package com.svetomir.spendly.data.repository

import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.data.local.ExpenseDao
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl @Inject constructor(private val dao: ExpenseDao,) : ExpenseRepository {
    override fun getExpenses(): Flow<List<Expense>> = dao.getAll()

    override fun getExpense(id: Long): Flow<Expense?> = dao.getById(id)

    override fun searchExpenses(query: String): Flow<List<Expense>> = dao.search(query)

    override suspend fun addExpense(expense: Expense): Long = dao.insert(expense)

    override suspend fun updateExpense(expense: Expense) = dao.update(expense)

    override suspend fun deleteExpense(expense: Expense) = dao.delete(expense)
}
