package com.example.wink.ui.features.social

import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User

data class SocialState(
    val feedList: List<SocialPost> = emptyList(),
    val leaderboardList: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,

    // --- THÊM MỚI ---
    val isCreatingPost: Boolean = false, // Trạng thái hiện dialog soạn bài
    val newPostContent: String = ""      // Nội dung đang nhập
)