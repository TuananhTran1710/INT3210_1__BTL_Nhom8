package com.example.wink.ui.features.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalyzeDialog(
    isLoading: Boolean,
    score: Int?,
    onDismiss: () -> Unit,
    onStartAnalysis: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!isLoading && score != null) {
                Button(onClick = onStartAnalysis) {
                    Text("Bắt đầu phân tích")
                }
            }
        },
        dismissButton = {
            if (!isLoading && score != null) {
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        },
        title = {
            val titleText = if (isLoading) "Đang phân tích..." else if (score != null) "Kết quả phân tích" else ""
            if (titleText.isNotEmpty()) {
                Text(text = titleText)
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                } else if (score != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mức độ thấu hiểu của bạn là:", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$score/100",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}
