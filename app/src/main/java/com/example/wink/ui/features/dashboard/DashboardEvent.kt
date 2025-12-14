package com.example.wink.ui.features.dashboard

sealed class DashboardEvent {
    // Daily check-in events
    object OnDailyCheckIn : DashboardEvent()
    
    // AI Chat features
    object OnStartAIChat : DashboardEvent()
    
    // Daily tasks
    object OnCompleteTask : DashboardEvent()
    object OnClaimTaskReward : DashboardEvent()
    
    // Gaming events
    object OnPlayGame : DashboardEvent()
    
    // Rewards and achievements
    object OnClaimReward : DashboardEvent()
    
    // Navigation events
    object OnNavigateToProfile : DashboardEvent()
    object OnNavigateToLeaderboard : DashboardEvent()
    object OnNavigateToShop : DashboardEvent()
    object OnNavigateToSettings : DashboardEvent()
    
    // UI events
    object OnRefresh : DashboardEvent()
    data class OnShowDetails(val itemId: String) : DashboardEvent()
    
    // Friend request events
    object OnOpenFriendRequests : DashboardEvent()
    object OnCloseFriendRequests : DashboardEvent()
    data class OnAcceptFriendRequest(val requestId: String) : DashboardEvent()
    data class OnRejectFriendRequest(val requestId: String) : DashboardEvent()
    object OnClearAcceptedNotification : DashboardEvent()
    
    // Notification events - Thông báo tổng hợp
    object OnOpenNotifications : DashboardEvent()
    object OnCloseNotifications : DashboardEvent()
    data class OnMarkNotificationRead(val notificationId: String) : DashboardEvent()
    object OnClearAllNotifications : DashboardEvent()
}