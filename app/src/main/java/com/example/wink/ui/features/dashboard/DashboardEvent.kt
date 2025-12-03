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
}