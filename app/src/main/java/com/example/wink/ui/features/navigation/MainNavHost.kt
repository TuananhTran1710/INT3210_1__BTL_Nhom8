package com.example.wink.ui.features.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wink.ui.features.dashboard.DashboardScreen
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.iconshop.IconShopScreen
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.social.SocialScreen
import com.example.wink.ui.features.tarot.card.TarotCardScreen
import com.example.wink.ui.features.tarot.TarotHubScreen
import com.example.wink.ui.features.tarot.name.TarotNameScreen
import com.example.wink.ui.features.tarot.name.results.TarotNameResultScreen
import com.example.wink.ui.features.tarot.zodiac.TarotZodiacScreen
import com.example.wink.ui.features.tarot.zodiac.results.TarotZodiacResultScreen
import com.example.wink.ui.features.tips.TipsScreen
import com.example.wink.ui.navigation.Screen

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainNavController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen(navController = mainNavController)
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = mainNavController)
        }
        composable(BottomNavItem.Social.route) {
            SocialScreen(navController = mainNavController)
        }
        composable(BottomNavItem.Explore.route) {
            ExploreScreen(navController = navController)
        }
        composable(Screen.Tips.route) {
            TipsScreen(navController = navController)
        }

        // 🔮 Tarot hub + 3 màn con
        composable(Screen.TarotHub.route) {
            TarotHubScreen(navController = navController)
        }
        composable(Screen.TarotName.route) {
            TarotNameScreen(navController = navController)
        }
        composable(Screen.TarotZodiac.route) {
            TarotZodiacScreen(navController = navController)
        }
        composable(Screen.TarotCard.route) {
            TarotCardScreen(navController = navController)
        }

        // ⭐️ MÀN KẾT QUẢ BÓI THEO TÊN
        composable(
            route = Screen.TarotNameResult.route,
            arguments = listOf(
                navArgument("yourName") { type = NavType.StringType },
                navArgument("crushName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val yourName = backStackEntry.arguments?.getString("yourName") ?: ""
            val crushName = backStackEntry.arguments?.getString("crushName") ?: ""
            TarotNameResultScreen(
                navController = navController,
                yourName = yourName,
                crushName = crushName
            )
        }
        composable(Screen.TarotZodiacResult.route) {
            TarotZodiacResultScreen(navController = navController)
        }
        composable(Screen.Quiz.route) {
            QuizFeatureNavHost()
        }

        composable(Screen.ChangeIcon.route) {
            IconShopScreen(navController = navController)
        }
    }
}
