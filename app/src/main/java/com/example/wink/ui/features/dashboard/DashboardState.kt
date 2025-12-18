package com.example.wink.ui.features.dashboard

import com.example.wink.data.model.DailyTask
import com.example.wink.data.model.FriendRequest
import com.example.wink.data.model.Notification

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
    val dailyTasks: List<DailyTask>,
    
    // Error states
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    
    // Friend requests
    val pendingFriendRequests: List<FriendRequest> = emptyList(),
    val showFriendRequestsDialog: Boolean = false,
    
    // Notifications - Tất cả thông báo tổng hợp
    val notifications: List<Notification> = emptyList(),
    val showNotificationsDialog: Boolean = false,
    
    // Accepted friend request notification
    val acceptedFriendNotification: String? = null,

    val completedTaskNotification: String? = null,

    val aiCrushName: String = "Lan Anh",
    val aiCrushAvatar: String? = null,
)