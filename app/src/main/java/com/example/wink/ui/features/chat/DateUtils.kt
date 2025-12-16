// ui/common/DateUtils.kt (hoặc để chung file ChatUtils)
package com.example.wink.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    // Format cho timestamp ẩn hiện khi bấm vào tin nhắn (VD: 14:30)
    fun formatMessageTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    // Format cho Header phân cách giữa các đoạn chat xa nhau (VD: "Hôm nay 10:30", "T2 14:00")
    fun formatTimeSeparator(timestamp: Long): String {
        val now = Calendar.getInstance()
        val time = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameDay = now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)

        return if (isSameDay) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        } else {
            // Nếu khác ngày, hiện đầy đủ hơn (VD: 12 Dec, 14:30)
            SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    // Kiểm tra xem 2 tin nhắn có cần hiển thị time separator ở giữa không
    // (Logic: cách nhau quá 1 tiếng = true)
    fun shouldShowTimeSeparator(currentMsgTime: Long, previousMsgTime: Long): Boolean {
        val diff = currentMsgTime - previousMsgTime
        return diff > 60 * 60 * 1000 // 1 tiếng
    }
}