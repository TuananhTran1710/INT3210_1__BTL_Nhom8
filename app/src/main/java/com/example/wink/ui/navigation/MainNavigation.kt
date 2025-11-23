package com.example.wink.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.wink.ui.features.dashboard.DashboardScreen

//fun NavGraphBuilder.mainGraph(navController: NavController) {
//    navigation(
//        route = Screen.MAIN_GRAPH_ROUTE,
//        startDestination = Screen.Dashboard.route
//    ) {
//        composable(Screen.Dashboard.route) {
//            DashboardScreen(navController = navController)
//        }
//        composable(Screen.Profile.route) {
////            ProfileScreen(navController = navController)
//            androidx.compose.material3.Text("TODO: Profile Screen")
//        }
//    }
//}

fun NavGraphBuilder.mainGraph(navController: NavController) {
    composable(Screen.MAIN_GRAPH_ROUTE) {
        MainScreen() // MainScreen now creates its own NavController
    }
}