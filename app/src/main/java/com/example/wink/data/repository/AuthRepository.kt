package com.example.wink.data.repository

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
}