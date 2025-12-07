package com.example.wink.ui.features.tarot.name.results

data class TarotNameResultState(
    val yourName: String = "",
    val crushName: String = "",
    val score: Int = 0,
    val message: String = "",
    val initialized: Boolean = false,

    // xử lý Rizz
    val isProcessing: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val showNotEnoughDialog: Boolean = false
)
