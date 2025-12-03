package com.example.wink.data.repository

import com.example.wink.data.model.User

interface UserRepository {
    suspend fun saveUserProfile(user: User)
    suspend fun getCurrentUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
