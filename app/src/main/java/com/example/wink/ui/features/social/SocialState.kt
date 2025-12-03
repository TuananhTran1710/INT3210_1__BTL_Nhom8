package com.example.wink.ui.features.social

import android.net.Uri
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
data class SocialState(
    val feedList: List<SocialPost> = emptyList(),
    val leaderboardList: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,

    val isCreatingPost: Boolean = false,
    val newPostContent: String = "",

    val activePostId: String? = null, // ID bài viết đang mở comment (null = đóng)
    val commentsForActivePost: List<Comment> = emptyList(), // List comment của bài đó
    val newCommentContent: String = "", // Nội dung đang nhập bình luận
    val selectedImageUris: List<Uri> = emptyList()
)