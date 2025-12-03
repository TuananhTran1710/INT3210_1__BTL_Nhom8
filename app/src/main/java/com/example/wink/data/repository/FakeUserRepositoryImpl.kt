package com.example.wink.data.repository

import com.example.wink.data.model.User
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepositoryImpl @Inject constructor(): UserRepository {
    private var last: User? = null

    override suspend fun saveUserProfile(user: User) {
        delay(200)
        last = user
    }

    override suspend fun getCurrentUid(): String? = last?.uid ?: "fake-uid"
    override suspend fun getCurrentUserEmail(): String? = last?.email ?: "fake@example.com"
}