package com.svetomir.spendly.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svetomir.spendly.ui.addedit.AddEditExpenseScreen
import com.svetomir.spendly.ui.list.ExpenseListScreen

@Composable
fun SpendlyNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ExpenseListRoute) {
        composable<ExpenseListRoute> {
            ExpenseListScreen(
                onAddClick = { navController.navigate(AddEditExpenseRoute()) },
                onExpenseClick = { id -> navController.navigate(ExpenseDetailRoute(id)) },
            )
        }
        composable<ExpenseDetailRoute> {
            PlaceholderScreen("Expense details")
        }
        composable<AddEditExpenseRoute> {
            AddEditExpenseScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
