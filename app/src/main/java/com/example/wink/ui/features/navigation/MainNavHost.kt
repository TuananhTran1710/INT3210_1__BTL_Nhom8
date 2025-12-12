package com.example.wink.ui.features.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
<<<<<<< HEAD
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wink.ui.features.dashboard.DashboardScreen
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.friend_requests.FriendRequestScreen
import com.example.wink.ui.features.friends.FriendsScreen
import com.example.wink.ui.features.chat.ChatListScreen
import com.example.wink.ui.features.chat.MessageScreen
import com.example.wink.ui.features.profile.ProfileScreen
=======
import androidx.navigation.navArgument
import com.example.wink.data.model.Tip
import com.example.wink.ui.features.chat.ChatListScreen
import com.example.wink.ui.features.chat.MessageScreen
import com.example.wink.ui.features.chat.MessageScreenForAI
import com.example.wink.ui.features.dashboard.DashboardScreen
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.friends.FriendsScreen
import com.example.wink.ui.features.iconshop.IconShopScreen
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.profile.SettingsScreen
>>>>>>> main
import com.example.wink.ui.features.profile.UserDetailScreen
//import com.example.wink.ui.features.quiz.QuizFeatureNavHost
import com.example.wink.ui.features.social.SocialScreen
<<<<<<< HEAD
=======
import com.example.wink.ui.features.tarot.TarotHubScreen
import com.example.wink.ui.features.tarot.card.TarotCardScreen
import com.example.wink.ui.features.tarot.name.TarotNameScreen
import com.example.wink.ui.features.tarot.name.results.TarotNameResultScreen
import com.example.wink.ui.features.tarot.zodiac.TarotZodiacScreen
import com.example.wink.ui.features.tarot.zodiac.results.TarotZodiacResultScreen
import com.example.wink.ui.features.tips.TipDetailScreen
>>>>>>> main
import com.example.wink.ui.features.tips.TipsScreen
import com.example.wink.ui.navigation.Screen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    navController: NavHostController
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
    ) {
        // Bottom Navigation Screens
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        composable(BottomNavItem.Message.route) {
            ChatListScreen(navController = navController)
        }
        
        composable(
            route = "message/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
<<<<<<< HEAD
            MessageScreen(navController = navController)
=======
            val chatId = backStackEntry.arguments?.getString("chatId")
            if (chatId == "ai_chat") {
                MessageScreenForAI(navController = navController)
            } else {
                MessageScreen(navController = navController)
            }
>>>>>>> main
        }
        
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        // Friend Requests Screen
        composable(route = "friend_requests") {
            FriendRequestScreen(navController = navController)
        }
        
        // User Detail Screen
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(navController = navController)
        }
        
        composable(BottomNavItem.Social.route) {
            SocialScreen(navController = navController)
        }
        
        composable(BottomNavItem.Explore.route) {
            ExploreScreen(navController = navController)
        }
        
        composable(Screen.Tips.route) {
            TipsScreen(navController = navController)
        }

        composable(Screen.Quiz.route) {
            QuizFeatureNavHost(onBack = {
                navController.popBackStack()
            })
        }
        
        composable(Screen.Friends.route) {
            FriendsScreen(navController = navController)
        }
<<<<<<< HEAD
=======
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController,
                onLogout = {
                    mainNavController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }
        composable("tip_detail_screen") {
            // Lấy dữ liệu từ màn hình trước đó gửi sang
            val tip = navController.previousBackStackEntry?.savedStateHandle?.get<Tip>("selectedTip")

            if (tip != null) {
                // Gọi màn hình hiển thị
                TipDetailScreen(
                    tip = tip, // Truyền nguyên object Tip vào
                    navController = navController
                )
            }
        }

        composable(Screen.ChangeIcon.route) {
            IconShopScreen(navController = navController)
        }
>>>>>>> main
    }
}
