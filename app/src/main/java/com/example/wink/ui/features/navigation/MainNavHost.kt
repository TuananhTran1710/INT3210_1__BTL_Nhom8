package com.example.wink.ui.features.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController // Changed from NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wink.ui.features.navigation.BottomNavItem



@Composable
fun MainNavHost(navController: NavHostController) { // Changed parameter type
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route
    ) {
        composable(BottomNavItem.Dashboard.route) {
            // Placeholder Dashboard
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Dashboard Screen")
            }
        }
        composable(BottomNavItem.Profile.route) {
            // Placeholder Profile
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Profile Screen")
            }
        }
    }
}
