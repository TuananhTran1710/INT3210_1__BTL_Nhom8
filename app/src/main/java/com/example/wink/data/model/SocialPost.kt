package com.example.wink.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class SocialPost(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val isLikedByMe: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    // Retweet fields
    val isRepost: Boolean = false,  // Đánh dấu đây là 1 retweet
    val originalPostId: String? = null,  // ID của bài viết gốc
    val originalUserId: String? = null,  // UID của người đăng bài gốc
    val originalUsername: String? = null,  // Tên người đăng bài gốc
    val retweetedBy: List<String> = emptyList(),  // Danh sách UID người retweet
    val retweetCount: Int = 0,  // Số lượng retweet
    val isRetweetedByMe: Boolean = false,  // Người dùng hiện tại có retweet không
    val canDelete: Boolean = false,  // Chỉ tác giả mới có thể xoá
    val canEdit: Boolean = false  // Chỉ tác giả mới có thể sửa
)