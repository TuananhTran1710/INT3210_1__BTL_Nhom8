package com.example.wink.data.repository

import com.example.wink.data.model.DailyTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Lấy danh sách nhiệm vụ hôm nay (Realtime)
    fun getDailyTasks(): Flow<List<DailyTask>>

    // Kiểm tra và khởi tạo nhiệm vụ ngày mới
    suspend fun checkAndGenerateDailyTasks()

    // Cập nhật tiến độ cho một loại nhiệm vụ
    // Trả về true nếu nhiệm vụ vừa được hoàn thành (để hiện thông báo)
    suspend fun updateTaskProgress(type: String): Boolean
    val taskCompletionEvent: Flow<String>
}