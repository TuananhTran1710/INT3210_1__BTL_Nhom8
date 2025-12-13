package com.example.wink.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    // URL này sẽ được cấu hình trong DI
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String = "",
        @Header("X-Title") title: String = "Wink App",
        @Body request: ChatGptRequest
    ): ChatGptResponse
}