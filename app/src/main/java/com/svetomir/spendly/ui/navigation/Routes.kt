package com.svetomir.spendly.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object ExpenseListRoute

@Serializable
data class ExpenseDetailRoute(val expenseId: Long)

@Serializable
data class AddEditExpenseRoute(val expenseId: Long = 0L)
