package com.example.wink.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.wink.ui.features.login.LoginScreen
import com.example.wink.ui.features.signup.SignupScreen

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
//            OnboardingScreen(navController = navController)
            androidx.compose.material3.Text("TODO: Onboarding Screen")
        }
    }
}