package com.example.wink.ui.features.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wink.ui.MainViewModel // Import MainViewModel vừa tạo
import com.example.wink.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomNavigation(
    navController: NavController,
    // Inject ViewModel ngay tại đây để lấy số liệu global
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val exploreSubRoutes = listOf(
            Screen.Tips.route,
            Screen.Quiz.route,
            Screen.TarotHub.route
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
                icon = {
                    // --- LOGIC HIỂN THỊ BADGE ---
                    if (item == BottomNavItem.Message && unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red, // Yêu cầu màu đỏ
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        // Các item khác hoặc khi không có tin nhắn mới
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}