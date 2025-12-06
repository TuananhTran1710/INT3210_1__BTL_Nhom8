package com.example.wink.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.wink.ui.features.login.LoginScreen
import com.example.wink.ui.features.onboarding.OnboardingScreen
import com.example.wink.ui.features.profile.UserDetailScreen
import com.example.wink.ui.features.signup.SignupScreen
//import com.example.wink.ui.features.explore.ChangeIconScreen

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        route = Screen.AUTH_GRAPH_ROUTE,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
//        composable(Screen.ChangeIcon.route) {
//            ChangeIconScreen(navController = navController)
//        }
    }
}