package com.example.wink.ui.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wink.data.model.Message

@Composable
fun MessageItem(
    message: Message,
    isMyMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    val backgroundColor =
        if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val cornerRadius = 12.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = cornerRadius,
                            bottomStart = if (isMyMessage) cornerRadius else 0.dp,
                            bottomEnd = if (isMyMessage) 0.dp else cornerRadius
                        )
                    )
                    .background(backgroundColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = message.content)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageItemPreview() {
    Column {
        MessageItem(
            message = Message(content = "Hello!", timestamp = System.currentTimeMillis()),
            isMyMessage = true
        )
        MessageItem(
            message = Message(content = "Hi there!", timestamp = System.currentTimeMillis()),
            isMyMessage = false
        )
    }
}
