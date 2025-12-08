package com.example.wink.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController // Import NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.example.wink.ui.features.navigation.MainBottomNavigation
import com.example.wink.ui.features.navigation.MainNavHost


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(navController: NavHostController) {  // nhận từ ngoài cho logout navigation
    // Tạo NavController riêng cho bottom navigation
    val bottomNavController = rememberAnimatedNavController()
    
    Scaffold(
        bottomBar = { MainBottomNavigation(bottomNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainNavHost(bottomNavController, mainNavController = navController)   // pass both controllers
        }
    }
}