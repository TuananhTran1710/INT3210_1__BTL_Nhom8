package com.example.wink.data.repository

import android.net.Uri
import com.example.wink.data.model.FriendRequest
import com.example.wink.data.model.User
import kotlinx.coroutines.flow.Flow

// Dùng Result<T> để xử lý thành công/thất bại
typealias AuthResult = Result<Unit>

interface AuthRepository {
    // Lấy thông tin user hiện tại (nếu có)
    val currentUser: Flow<User?>

    suspend fun login(email: String, pass: String): AuthResult
    suspend fun signup(email: String, pass: String, username: String): AuthResult
    suspend fun logout()

    suspend fun hasLoggedInUser() : Boolean
    suspend fun performDailyCheckIn() : AuthResult

    suspend fun uploadAvatar(uri: Uri): Result<String>

    suspend fun updateUserProfile(uid: String, username: String, avatarUrl: String): Result<Unit>
    suspend fun updateEmail(newEmail: String): Result<Unit>
    suspend fun updateUserPreferences(
        gender: String,
        preference: String,
        personalities: List<String>
    ): AuthResult
    suspend fun getUserById(userId: String): User?
    suspend fun getUsersByIds(userIds: List<String>): List<User>

    // Lắng nghe lời mời kết bạn real-time từ Firestore
    fun getFriendRequestsStream(): Flow<List<FriendRequest>>
    suspend fun completeQuizAndAwardPoints(
        quizId: String,
        firstTimeAward: Int = 50,
        isPerfectScore: Boolean
    ): Int
    suspend fun unlockQuiz(quizId: String, cost: Int): Boolean
}