package com.example.wink.ui.navigation

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
    object Tarot : Screen("tarot")
    object ChangeIcon : Screen("change_icon")
    object SecretBook : Screen("secret_book")
    companion object {
        const val AUTH_GRAPH_ROUTE = "auth_graph"
        const val MAIN_GRAPH_ROUTE = "main_graph"
    }
}