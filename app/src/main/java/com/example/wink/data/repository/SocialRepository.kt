package com.example.wink.data.repository

import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    // Lấy luồng bài viết (Realtime)
    fun getSocialFeed(): Flow<List<SocialPost>>

    // Đăng bài mới
    suspend fun createPost(content: String, user: User): Result<Unit>

    // Like / Unlike (trả về like count mới hoặc lỗi)
    suspend fun toggleLike(postId: String, userId: String, currentLikes: List<String>): Result<Unit>

    // Lấy bình luận của 1 bài (Realtime)
    fun getComments(postId: String): Flow<List<Comment>>

    // Gửi bình luận
    suspend fun sendComment(postId: String, content: String, user: User): Result<Unit>

    // Lấy bảng xếp hạng
    suspend fun getLeaderboard(): Result<List<User>>
}