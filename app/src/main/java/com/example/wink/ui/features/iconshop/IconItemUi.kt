package com.example.wink.ui.features.iconshop

import androidx.compose.ui.graphics.Color

data class IconItemUi(
    val id: String,
    val price: Int,
    val isOwned: Boolean,
    val isSelected: Boolean,
    val color: Color
)
