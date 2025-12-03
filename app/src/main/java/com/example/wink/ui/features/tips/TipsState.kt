package com.example.wink.ui.features.tips

import com.example.wink.data.model.Tip

data class TipsState(
    val tips: List<Tip> = emptyList(),
    val userRizzPoints: Int = 0, // Điểm hiện tại của user
    val isLoading: Boolean = false,

    // Quản lý Dialog mở khóa
    val selectedTipToUnlock: Tip? = null,
    val unlockError: String? = null // Ví dụ: "Bạn không đủ điểm"
)