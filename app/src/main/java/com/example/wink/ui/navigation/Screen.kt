package com.example.wink.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash") // Màn hình khởi động app

    // Auth Screens
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Onboarding : Screen("onboarding") // Màn hình lựa chọn giới tính, gu,.. sau khi đăng ký
    // Main Screens
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Explore : Screen("explore")
    // --- CÁC MÀN HÌNH TÍNH NĂNG (EXPLORE) ---
    object Tips : Screen("tips_screen")       // Bí kíp
    object Quiz : Screen("quiz")
    // 🔮 Tarot / Bói tình yêu
    object TarotHub : Screen("tarot_hub")          // Hub – 3 lựa chọn
    object TarotName : Screen("tarot_name")        // Bói theo tên
    object TarotZodiac : Screen("tarot_zodiac")    // Bói theo cung hoàng đạo
    object TarotCard : Screen("tarot_card")        // Bói bài tây (rút bài)
    object TarotZodiacResult : Screen("tarot_zodiac_result")

    // ⭐️ MÀN KẾT QUẢ BÓI THEO TÊN
    object TarotNameResult : Screen("tarot_name_result/{yourName}/{crushName}") {
        fun buildRoute(yourName: String, crushName: String): String {
            return "tarot_name_result/${
                Uri.encode(yourName)
            }/${
                Uri.encode(crushName)
            }"
        }
    }


    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }

    object ChangeIcon : Screen("change_icon")
    object SecretBook : Screen("secret_book")
    object Tarot : Screen("tarot")
    object Friends : Screen("friends")
    object Settings : Screen("settings")

    object HumanAiGame : Screen("human_ai_game")
    companion object {
        const val AUTH_GRAPH_ROUTE = "auth_graph"
        const val MAIN_GRAPH_ROUTE = "main_graph"
    }
}