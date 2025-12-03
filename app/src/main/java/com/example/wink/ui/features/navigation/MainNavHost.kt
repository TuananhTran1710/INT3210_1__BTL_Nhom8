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
import com.example.wink.data.model.Answer
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.navigation.BottomNavItem
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.quiz.QuizListScreen
import com.example.wink.ui.features.dashboard.DashboardScreen
import com.example.wink.ui.features.social.SocialScreen
import com.example.wink.ui.features.tips.TipsScreen
import com.example.wink.ui.navigation.Screen


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
            DashboardScreen(navController = mainNavController) // Use main nav controller for navigation
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = mainNavController) // Use main nav controller for logout
        }
        composable(BottomNavItem.Social.route) {
            SocialScreen(navController = mainNavController)
        }
        composable(BottomNavItem.Explore.route) {
            ExploreScreen(navController = navController)
        }
        composable(Screen.Tips.route) {
            TipsScreen(navController = navController)
        }

        composable(Screen.Quiz.route) {
            QuizFeatureNavHost()
        }
    }
}
