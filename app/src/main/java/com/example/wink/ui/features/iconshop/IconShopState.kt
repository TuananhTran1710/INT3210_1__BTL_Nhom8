package com.example.wink.ui.features.iconshop

data class IconShopState(
    val rizzPoints: Int = 0,
    val icons: List<IconItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showRestartDialog: Boolean = false, // Có hiện dialog không?
    val pendingIconId: String? = null     // Icon user vừa bấm chọn là gì?
)
