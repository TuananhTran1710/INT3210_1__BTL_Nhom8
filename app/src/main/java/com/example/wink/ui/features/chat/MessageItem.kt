package com.example.wink.ui.features.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wink.R
import com.example.wink.data.model.Message

@Composable
fun MessageItem(
    message: Message,
    isMyMessage: Boolean,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,           // tin nhắn này có đang highlight không
    insight: String? = null,               // insight hiển thị khi highlight
    isDisplayTime: Boolean = true,
    isDisplayAvatar: Boolean = true
) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    val backgroundColor =
        if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val cornerRadius = 12.dp

    val context = LocalContext.current
    val defaultAvatarUri = "android.resource://${context.packageName}/${R.drawable.ai_crush}"
    val finalAvatarUrl = if (isMyMessage) null else avatarUrl ?: defaultAvatarUri

    val targetColor = if (highlight)
        Color.Yellow.copy(alpha = 0.6f)  // highlight color
    else backgroundColor

    val animatedBackgroundColor by animateColorAsState(targetColor)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMyMessage && isDisplayAvatar) {
            AsyncImage(
                model = finalAvatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
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
                    .background(animatedBackgroundColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = message.content)
            }

            if (highlight && !insight.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Yellow,
                    modifier = Modifier.padding(start = if (isMyMessage) 0.dp else 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            if (isDisplayTime) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isMyMessage) {
            Spacer(modifier = Modifier.width(20.dp))
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