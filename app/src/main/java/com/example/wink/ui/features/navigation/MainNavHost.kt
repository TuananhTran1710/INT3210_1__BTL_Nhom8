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
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.social.SocialScreen


@Composable
fun MainNavHost(
    navController: NavHostController, // For bottom navigation
    mainNavController: NavHostController // For main app navigation (logout)
) {
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
            ProfileScreen(navController = mainNavController) // Use main nav controller for logout
        }
        composable(BottomNavItem.Social.route) {
            SocialScreen(navController = mainNavController)
        }
    }
}
