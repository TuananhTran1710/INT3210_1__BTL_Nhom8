package com.example.wink.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatGptRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatGptMessage>,
    @SerialName("max_tokens")
    val maxTokens: Int = 200
)

@Serializable
data class ChatGptMessage(
    val role: String, // "system", "user", or "assistant"
    val content: String
)

@Serializable
data class ChatGptResponse(
    val choices: List<ChatGptChoice>
)

@Serializable
data class ChatGptChoice(
    val message: ChatGptMessage,
    // Thêm dòng này để biết tại sao model dừng lại
    @SerialName("finish_reason")
    val finishReason: String? = null
)
