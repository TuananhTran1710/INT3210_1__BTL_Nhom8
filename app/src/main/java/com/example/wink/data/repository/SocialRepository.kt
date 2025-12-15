package com.example.wink.data.repository

import android.net.Uri
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    // Lấy luồng bài viết (Realtime)
    suspend fun getSocialFeed(): Result<List<SocialPost>>
    fun listenForNewPosts(latestTimestamp: Long): Flow<Boolean>

    // Đăng bài mới
    suspend fun createPost(content: String, imageUrls: List<String>, user: User): Result<Unit>

    // Like / Unlike (trả về like count mới hoặc lỗi)
    suspend fun toggleLike(postId: String, userId: String, currentLikes: List<String>): Result<Unit>

    // Lấy bình luận của 1 bài (Realtime)
    fun getComments(postId: String): Flow<List<Comment>>

    // Gửi bình luận
    suspend fun sendComment(postId: String, content: String, user: User): Result<Unit>

    // Like / Unlike comment
    suspend fun toggleCommentLike(postId: String, commentId: String, userId: String, currentLikes: List<String>): Result<Unit>

    // Sửa comment
    suspend fun editComment(postId: String, commentId: String, userId: String, newContent: String): Result<Unit>

    // Xóa bài viết
    suspend fun deletePost(postId: String, userId: String): Result<Unit>

    // Sửa bài viết
    suspend fun editPost(postId: String, userId: String, newContent: String, newImageUrls: List<String>): Result<Unit>

    // Retweet / Untweet bài viết
    suspend fun toggleRetweet(postId: String, userId: String, userName: String, userAvatar: String?, currentRetweets: List<String>): Result<Unit>

    // Lấy bài viết của một user (bao gồm retweet)
    fun getUserPosts(userId: String): Flow<List<SocialPost>>

    // Lấy bảng xếp hạng
    suspend fun getLeaderboard(): Result<List<User>>
    suspend fun uploadImage(uri: Uri): Result<String>
}