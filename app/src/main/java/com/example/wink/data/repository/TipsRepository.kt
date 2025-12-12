package com.example.wink.data.repository

import com.example.wink.data.model.Tip

interface TipsRepository {
    suspend fun getTips(): Result<List<Tip>>
    suspend fun unlockTip(userId: String, tipId: String, price: Int): Result<Unit>
}