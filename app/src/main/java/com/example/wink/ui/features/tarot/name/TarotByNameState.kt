package com.example.wink.ui.features.tarot.name
data class TarotNameState(
    val yourName: String = "",
    val crushName: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false // Thêm trạng thái loading
)