package com.svetomir.spendly.data.repository

import com.svetomir.spendly.data.local.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(): Flow<List<Expense>>
    fun getExpense(id: Long): Flow<Expense?>
    fun searchExpenses(query: String): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}
