package com.example.wink.ui.features.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalyzeDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    if (!isLoading) return  // Khi đã xong, dialog biến mất

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {}, // Không cần nút đóng
        title = {},
        text = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }
    )
}
