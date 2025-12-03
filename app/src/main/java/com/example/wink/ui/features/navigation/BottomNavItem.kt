package com.example.wink.ui.features.navigation

// File: BottomNavItem.kt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Home", Icons.Filled.Home)
    object Social : BottomNavItem("social", "Social", Icons.Filled.Public) // Dùng icon Ngôi sao hoặc People
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Social,
    BottomNavItem.Profile
)
