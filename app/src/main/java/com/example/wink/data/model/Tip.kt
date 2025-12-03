package com.example.wink.data.model

data class Tip(
    val id: String,
    val title: String,
    val description: String,
    val content: String, // Nội dung chi tiết (HTML/Markdown)
    val price: Int = 0,  // Giá 0 = Miễn phí
    val isLocked: Boolean = true, // Trạng thái khóa
    val imageUrl: String? = null // Ảnh minh họa
)