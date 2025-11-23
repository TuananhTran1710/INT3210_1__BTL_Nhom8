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
fun MainScreen() { // Removed navController parameter, as it will be created internally
    val navController = rememberNavController() // Create NavHostController
    Scaffold(
        bottomBar = { MainBottomNavigation(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainNavHost(navController) // Pass the created NavHostController
        }
    }
}