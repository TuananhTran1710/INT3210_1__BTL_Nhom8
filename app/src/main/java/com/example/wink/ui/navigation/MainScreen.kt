package com.example.wink.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // Import rememberNavController
import androidx.navigation.NavHostController // Import NavHostController
import com.example.wink.ui.features.navigation.MainBottomNavigation
import com.example.wink.ui.features.navigation.MainNavHost


@Composable
fun MainScreen(navController: NavHostController) {  // nhận từ ngoài cho logout navigation
    // Tạo NavController riêng cho bottom navigation
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = { MainBottomNavigation(bottomNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainNavHost(bottomNavController, mainNavController = navController)   // pass both controllers
        }
    }
}