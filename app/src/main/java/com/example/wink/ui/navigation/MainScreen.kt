package com.example.wink.ui.navigation

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController // Import NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.wink.ui.MainViewModel
import com.example.wink.ui.features.navigation.MainBottomNavigation
import com.example.wink.ui.features.navigation.MainNavHost


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(navController: NavHostController, mainViewModel: MainViewModel = hiltViewModel()) {  // nhận từ ngoài cho logout navigation
    // Tạo NavController riêng cho bottom navigation
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(true) {
        mainViewModel.globalMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        bottomBar = { MainBottomNavigation(bottomNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainNavHost(bottomNavController, mainNavController = navController)   // pass both controllers
        }
    }
}