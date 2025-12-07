// File: data/repository/FakeUserRepositoryImpl.kt
package com.example.wink.data.repository

import com.example.wink.data.model.User
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepositoryImpl @Inject constructor() : UserRepository {
    private var last: User? = null
    private var rizz: Int = 200
    private val tarotFreeMap = mutableMapOf<String, Long>()

    override suspend fun saveUserProfile(user: User) {
        delay(200)
        last = user
    }

    override suspend fun getCurrentUid(): String? = last?.uid ?: "fake-uid"
    override suspend fun getCurrentUserEmail(): String? = last?.email ?: "fake@example.com"

    override suspend fun loadRizzPoints(): Int = rizz
    override suspend fun canSpendRizz(amount: Int): Boolean = rizz >= amount
    override suspend fun spendRizz(amount: Int): Boolean {
        return if (rizz >= amount) {
            rizz -= amount
            true
        } else false
    }

    override suspend fun getTarotFreeUsage(): Map<String, Long> = tarotFreeMap.toMap()

    override suspend fun markTarotFreeUsedToday(featureKey: String, todayEpochDay: Long) {
        tarotFreeMap[featureKey] = todayEpochDay
    }
}
