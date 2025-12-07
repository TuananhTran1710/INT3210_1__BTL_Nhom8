package com.example.wink.data.repository

import android.net.Uri
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
}