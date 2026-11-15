package com.svetomir.spendly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svetomir.spendly.ui.addedit.AddEditExpenseScreen
import com.svetomir.spendly.ui.detail.ExpenseDetailScreen
import com.svetomir.spendly.ui.list.ExpenseListScreen
import com.svetomir.spendly.ui.list.ExpenseListViewModel

private const val DELETE_REQUEST_KEY = "delete_request_id"

@Composable
fun SpendlyNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ExpenseListRoute) {
        composable<ExpenseListRoute> { backStackEntry ->
            val viewModel: ExpenseListViewModel = hiltViewModel()
            val savedState = backStackEntry.savedStateHandle
            val deleteRequest by savedState
                .getStateFlow<Long?>(DELETE_REQUEST_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(deleteRequest) {
                deleteRequest?.let { id ->
                    viewModel.deleteExpense(id)
                    savedState.set<Long?>(DELETE_REQUEST_KEY, null)
                }
            }

            ExpenseListScreen(
                onAddClick = { navController.navigate(AddEditExpenseRoute()) },
                onExpenseClick = { id -> navController.navigate(ExpenseDetailRoute(id)) },
                viewModel = viewModel,
            )
        }
        composable<ExpenseDetailRoute> {
            ExpenseDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(AddEditExpenseRoute(id)) },
                onDelete = { id ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(DELETE_REQUEST_KEY, id)
                    navController.popBackStack()
                },
            )
        }
        composable<AddEditExpenseRoute> {
            AddEditExpenseScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
