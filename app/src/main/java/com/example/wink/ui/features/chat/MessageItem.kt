package com.example.wink.ui.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.R
import com.example.wink.data.model.Message
import com.example.wink.ui.common.DateUtils
// ui/features/chat/MessageItem.kt

@Composable
fun MessageItem(
    message: Message,
    isMyMessage: Boolean,
    avatarUrl: String? = null,
    senderName: String = "",
    modifier: Modifier = Modifier,

    highlight: Boolean = false,
    insight: String? = null,
    isGroupTop: Boolean = true,    // Tham số mới: Là tin đầu nhóm?
    isGroupBottom: Boolean = true, // Tham số mới: Là tin cuối nhóm?
    onImageClick: (String) -> Unit,
    isChattingWithAi: Boolean = false
) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start

    val backgroundColor = if (isMyMessage)
        if (isChattingWithAi) Color(0xFFFCE4E3) else MaterialTheme.colorScheme.primaryContainer
    else
        if (isChattingWithAi) Color(0xFF671A27) else MaterialTheme.colorScheme.secondaryContainer

    val targetColor = if (highlight) Color.Yellow.copy(alpha = 0.7f) else backgroundColor
    val animatedBackgroundColor by animateColorAsState(targetColor, label = "color")

    var isTimestampVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val largeRadius = 18.dp
    val smallRadius = 3.dp // Góc nhọn (Messenger dùng khoảng 2-4dp)

    val bubbleShape = if (isMyMessage) {
        RoundedCornerShape(
            topStart = largeRadius, // Góc trên trái luôn tròn
            bottomStart = largeRadius, // Góc dưới trái luôn tròn
            topEnd = if (isGroupTop) largeRadius else smallRadius,
            bottomEnd = if (isGroupBottom) largeRadius else smallRadius
        )
    } else {
        RoundedCornerShape(
            topStart = if (isGroupTop) largeRadius else smallRadius,
            topEnd = largeRadius, // Góc trên phải luôn tròn
            bottomStart = if (isGroupBottom) largeRadius else smallRadius,
            bottomEnd = largeRadius // Góc dưới phải luôn tròn
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { isTimestampVisible = !isTimestampVisible },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.Bottom
        ) {
            // --- AVATAR (Chỉ hiện khi là tin cuối nhóm của người khác) ---
            if (!isMyMessage) {
                if (isGroupBottom) {
                    // --- THAY THẾ ĐOẠN ASYNCIMAGE BẰNG USERAVATAR ---
                    UserAvatar(
                        imageUrl = avatarUrl,
                        userName = senderName, // Dùng tên được truyền vào
                        modifier = Modifier.size(28.dp),
                        textSize = 12.sp // Chữ nhỏ hơn vì avatar nhỏ
                    )
                } else {
                    Spacer(modifier = Modifier.width(28.dp)) // Giữ khoảng trống bằng size avatar
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // --- BONG BÓNG CHAT ---
            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape) // Áp dụng Shape đã tính toán
                        .background(animatedBackgroundColor)
                        .padding(4.dp)
                ) {
                    Column {
                        // 1. Ảnh
                        if (!message.mediaUrl.isNullOrEmpty()) {
                            message.mediaUrl.forEach { imageUrl ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .heightIn(max = 300.dp)
                                        // Bo góc ảnh cũng phải ăn theo bubbleShape một chút (tùy chọn)
                                        .clip(RoundedCornerShape(14.dp))
                                        .padding(bottom = if (message.content.isNotBlank() && message.content != "Đã gửi một ảnh") 4.dp else 0.dp)
                                        .clickable { onImageClick(imageUrl) }
                                )
                            }
                        }

                        // 2. Text
                        if (message.content.isNotBlank() && message.content != "Đã gửi một ảnh") {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (highlight || (isMyMessage && isChattingWithAi)) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- TIMESTAMP ---
        AnimatedVisibility(
            visible = isTimestampVisible || highlight,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, start = if (!isMyMessage) 44.dp else 0.dp, end = if (isMyMessage) 8.dp else 0.dp),
                horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
            ) {
                if (highlight && !insight.isNullOrBlank()) {
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA000),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = DateUtils.formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Spacer lớn hơn để tách các nhóm chat khác nhau
        if (isGroupBottom) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}