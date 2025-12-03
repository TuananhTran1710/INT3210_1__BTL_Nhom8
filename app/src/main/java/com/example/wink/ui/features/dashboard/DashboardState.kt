package com.example.wink.ui.features.dashboard

data class DashboardTask(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val isCompleted: Boolean = false
)

data class DashboardReward(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val icon: String
)

data class DashboardState(
    val userEmail: String = "Đang tải...",
    val username: String = "",
    val rizzPoints: Int = 0,
    val dailyStreak: Int = 0,
    val isLoading: Boolean = true,
    
    // Daily check-in
    val hasDailyCheckIn: Boolean = false,
    
    // AI features
    val isAIUnlocked: Boolean = true,
    
    // Tasks
    val dailyTasks: List<DashboardTask> = listOf(
        DashboardTask("task1", "Nhiệm vụ hôm nay", "+50 RIZZ", 50),
        DashboardTask("task2", "Game: AI hay thật?", "+20 RIZZ", 20)
    ),
    
    // Rewards and achievements  
    val availableRewards: List<DashboardReward> = listOf(
        DashboardReward("reward1", "Bí kíp đàng khóa", "Cần thêm 300 RIZZ để mở 'Nghệ thuật bắt chuyện'", 300, "🎯"),
        DashboardReward("reward2", "Góc Tiên Tri", "", 0, "✨")
    ),
    
    // Error states
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)