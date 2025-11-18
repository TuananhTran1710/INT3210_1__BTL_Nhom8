package com.example.wink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = Screen.AUTH_GRAPH_ROUTE

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Đồ thị (Graph) cho luồng Đăng nhập/Đăng ký
        authGraph(navController)

        // Đồ thị (Graph) cho các tính năng chính (sau khi đăng nhập)
        mainGraph(navController)
    }
}