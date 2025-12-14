package com.example.wink.ui.features.iconshop

import androidx.compose.ui.graphics.Color

data class IconItemUi(
    val id: String,
    val price: Int,
    val isOwned: Boolean,
    val isSelected: Boolean,
    val iconResId: Int // Đổi từ 'color: Color' thành 'iconResId: Int'
)
