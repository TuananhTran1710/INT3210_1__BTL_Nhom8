package com.example.wink.data.repository

import com.example.wink.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FakeAuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    override suspend fun login(email: String, pass: String): AuthResult {
        if (email.isNotBlank()) {
            _currentUser.value = User(
                uid = "676767",
                email = email,
                username = "dangduat",
                gender = "male",
                preference = "female",
                rizzPoints = 67,
                avatarUrl = ""
            )
            return Result.success(Unit)
        }
        return Result.failure(Exception("Đăng nhập thất bại"))
    }

    override suspend fun signup(email: String, pass: String, username: String): AuthResult {
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}