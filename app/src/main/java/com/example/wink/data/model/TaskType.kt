package com.example.wink.data.model

enum class TaskType(val title: String, val target: Int, val reward: Int) {
    CHAT_AI("Tâm sự với AI", 5, 20),
    CHAT_FRIEND("Nhắn tin cho bạn bè", 3, 30),
    PLAY_GAME("Chơi minigames", 1, 50),
    POST_FEED("Đăng bài viết", 1, 40),
    LIKE_POST("Thả tim dạo", 5, 15),
    COMMENT_POST("Bình luận vào bài viết", 2, 25),
    COMPLETE_QUIZ("Thực hiện quiz", 1, 30),
    DRAW_TAROT("Thực hiện bói tarot", 1, 20),
    READ_TIP("Đọc bí kíp", 1, 10);

    // Helper để tìm kiếm
    companion object {
        fun fromId(id: String): TaskType? = entries.find { it.name == id }
    }
}