package com.example.wink.ui.features.navigation

// File: BottomNavItem.kt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Home", Icons.Filled.Home)
    object Message : BottomNavItem("message", "Message", Icons.Filled.Email)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Message,
    BottomNavItem.Profile
)
