package com.example.wink.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.wink.ui.features.dashboard.DashboardScreen

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    composable(Screen.MAIN_GRAPH_ROUTE) {
        MainScreen(navController = navController)
    }
}