package com.example.wink.ui.features.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wink.data.model.Tip
import com.example.wink.ui.features.chat.ChatListScreen
import com.example.wink.ui.features.chat.MessageScreen
import com.example.wink.ui.features.chat.MessageScreenForAI
import com.example.wink.ui.features.chat.NewChatScreen
import com.example.wink.ui.features.dashboard.DashboardScreen
import com.example.wink.ui.features.explore.ExploreScreen
import com.example.wink.ui.features.friends.FriendsScreen
import com.example.wink.ui.features.games.humanai.HumanAiGameScreen
import com.example.wink.ui.features.iconshop.IconShopScreen
import com.example.wink.ui.features.profile.ProfileScreen
import com.example.wink.ui.features.profile.SettingsScreen
import com.example.wink.ui.features.profile.UserDetailScreen
import com.example.wink.ui.features.setting.AiSettingsScreen
import com.example.wink.ui.features.social.SocialScreen
import com.example.wink.ui.features.tarot.TarotHubScreen
import com.example.wink.ui.features.tarot.card.TarotCardScreen
import com.example.wink.ui.features.tarot.name.TarotNameScreen
import com.example.wink.ui.features.tarot.name.results.TarotNameResultScreen
import com.example.wink.ui.features.tarot.zodiac.TarotZodiacScreen
import com.example.wink.ui.features.tarot.zodiac.results.TarotZodiacResultScreen
import com.example.wink.ui.features.tips.TipDetailScreen
import com.example.wink.ui.features.tips.TipsScreen
import com.example.wink.ui.navigation.Screen

@Composable
fun MainNavHost(
    navController: NavHostController, // For bottom navigation
    mainNavController: NavHostController, // For main app navigation (logout)
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        enterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    },
        // 2. Màn hình cũ trượt sang Trái rồi biến mất
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }, // Trượt hết sang trái
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        // 3. (Khi bấm Back) Màn hình cũ trượt từ Trái về lại
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        // 4. (Khi bấm Back) Màn hình hiện tại trượt sang Phải biến mất
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
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
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            if (chatId == "ai_chat") {
                MessageScreenForAI(navController = navController)
            } else {
                MessageScreen(navController = navController)
            }
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
            SocialScreen(navController = navController)
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
            QuizFeatureNavHost(onBack = {
                navController.popBackStack()
            })
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
        composable(Screen.NewChat.route) {
            NewChatScreen(navController = navController)
        }
        composable(Screen.ChangeIcon.route) {
            IconShopScreen(navController = navController)
        }

        composable(Screen.HumanAiGame.route) {
            HumanAiGameScreen(navController = navController)
        }

        composable(Screen.AiSettings.route) {
            AiSettingsScreen(navController = navController)
        }
    }
}
