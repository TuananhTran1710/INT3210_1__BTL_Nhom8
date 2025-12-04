package com.example.wink.ui.features.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
