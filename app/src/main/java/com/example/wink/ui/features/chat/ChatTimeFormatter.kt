package com.example.wink.ui.features.chat

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Rule:
 * - Nếu cùng ngày: "HH:mm"
 * - Nếu ngày trước nhưng cùng tuần: "Mon, Tue, Wed..."
 * - Nếu khác tuần: "9 Dec", "8 Dec"...
 */
fun formatChatRowTime(timestamp: Long): String {
    val nowCal = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    // 1) same day
    if (isSameDay(nowCal, msgCal)) {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    // 2) same week (ISO-like by Calendar settings)
    val nowWeek = nowCal.get(Calendar.WEEK_OF_YEAR)
    val msgWeek = msgCal.get(Calendar.WEEK_OF_YEAR)
    val nowYear = nowCal.get(Calendar.YEAR)
    val msgYear = msgCal.get(Calendar.YEAR)

    val isSameWeek = (nowYear == msgYear && nowWeek == msgWeek)
    if (isSameWeek) {
        // Mon, Tue, Wed...
        return SimpleDateFormat("EEE", Locale.ENGLISH).format(Date(timestamp))
    }

    // 3) different week
    return SimpleDateFormat("d MMM", Locale.ENGLISH).format(Date(timestamp))
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.ERA) == b.get(Calendar.ERA) &&
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
