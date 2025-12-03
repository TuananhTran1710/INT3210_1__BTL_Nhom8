package com.example.wink.ui.features.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wink.ui.navigation.Screen

@Composable
fun MainBottomNavigation(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val exploreSubRoutes = listOf(
            Screen.Tips.route,
        )

        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route ||
                    (item == BottomNavItem.Explore && exploreSubRoutes.contains(currentRoute))

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(bottomNavItems.first().route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}
