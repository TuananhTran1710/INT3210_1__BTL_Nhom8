package com.example.wink.ui.features.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController // Changed from NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wink.data.model.Answer
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.navigation.BottomNavItem
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.quiz.QuizListScreen
import com.example.wink.ui.features.dashboard.DashboardScreen
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
    navController: NavHostController, // For bottom navigation
    mainNavController: NavHostController // For main app navigation (logout)
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen(navController = navController) // Use main nav controller for navigation
        }
        composable(BottomNavItem.Message.route) {
            ChatListScreen(navController = navController)
        }
        composable(
            route = "message/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) {
            MessageScreen(navController = navController)
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = navController) // Use main nav controller for logout
        }
        composable(
            route = Screen.UserDetail.route,
            // Không cần arguments = listOf(...) vì NavHost tự parse {userId}
        ) { backStackEntry ->
            // Hilt sẽ tự inject ViewModel và lấy userId từ SavedStateHandle
            UserDetailScreen(navController = navController)
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
        composable(Screen.Friends.route) {
            FriendsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController,
                onLogout = {
                    mainNavController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }

        composable(Screen.ChangeIcon.route) {
            IconShopScreen(navController = navController)
        }
    }
}
