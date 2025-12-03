package com.example.wink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.profile.UserDetailScreen

/**
 * Navigation example showing how to integrate ProfileScreen with UserDetailScreen
 * This demonstrates the navigation setup for the friend profile viewing feature
 */
@Composable
fun WinkNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "profile"
    ) {
        composable("profile") {
            // Note: In real implementation, you would pass the actual navController
            // ProfileScreen(
            //     navController = navController,
            //     onNavigateToUserDetail = { userId ->
            //         navController.navigate("user_detail/$userId")
            //     }
            // )
        }
        

    }
}

/**
 * Route definitions for type-safe navigation
 */
object WinkRoutes {
    const val PROFILE = "profile"
    const val USER_DETAIL = "user_detail/{userId}"
    
    fun userDetail(userId: String) = "user_detail/$userId"
}