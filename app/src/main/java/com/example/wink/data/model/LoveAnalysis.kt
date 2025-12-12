package com.example.wink.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoveAnalysisResponse(
    val score: Int,
    val comment: String
)