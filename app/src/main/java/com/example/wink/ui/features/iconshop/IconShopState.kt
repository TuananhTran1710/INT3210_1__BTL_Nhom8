package com.example.wink.ui.features.iconshop

data class IconShopState(
    val rizzPoints: Int = 0,
    val icons: List<IconItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
