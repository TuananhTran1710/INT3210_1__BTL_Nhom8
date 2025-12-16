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

@Composable
fun MessageItem(
    message: Message,
    isMyMessage: Boolean,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    insight: String? = null,
    showTail: Boolean = true,
    onImageClick: (String) -> Unit
) {
    // 1. Cấu hình căn lề: Tin mình -> Phải (End), Tin bạn -> Trái (Start)
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start

    // Màu nền
    val backgroundColor = if (isMyMessage)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    // Animation Highlight
    val targetColor = if (highlight) Color.Yellow.copy(alpha = 0.6f) else backgroundColor
    val animatedBackgroundColor by animateColorAsState(targetColor, label = "color")

    // State Timestamp
    var isTimestampVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val cornerRadius = 18.dp
    val smallCorner = 4.dp

    // --- CẤU TRÚC CHÍNH ---
    Column(
        modifier = modifier
            .fillMaxWidth() // Cột ngoài cùng phải full width
            .padding(vertical = 1.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { isTimestampVisible = !isTimestampVisible },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // QUAN TRỌNG: Row phải full width thì Arrangement.End mới đẩy sang phải được
                .padding(horizontal = 8.dp),
            horizontalArrangement = horizontalArrangement, // Áp dụng căn trái/phải tại đây
            verticalAlignment = Alignment.Bottom
        ) {

            // --- LOGIC AVATAR (Chỉ cho người nhận - Bên trái) ---
            if (!isMyMessage) {
                if (showTail) {
                    // Tin cuối cùng trong chuỗi -> Hiện Avatar thật
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_launcher_background), // Ảnh chờ (có thể thay bằng hình xám nhẹ)
                        error = painterResource(R.drawable.ic_launcher_background),       // Ảnh lỗi
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Tin ở giữa chuỗi -> Hiện khoảng trống (Spacer) bằng kích thước Avatar để thẳng hàng
                    Spacer(modifier = Modifier.width(28.dp))
                }
                // Khoảng cách giữa Avatar và Tin nhắn
                Spacer(modifier = Modifier.width(8.dp))
            }

            // --- BONG BÓNG CHAT ---
            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = cornerRadius,
                                topEnd = cornerRadius,
                                // Logic bo góc: Nhọn ở phía có Avatar (hoặc phía mình)
                                bottomStart = if (!isMyMessage && showTail) smallCorner else cornerRadius,
                                bottomEnd = if (isMyMessage && showTail) smallCorner else cornerRadius
                            )
                        )
                        .background(animatedBackgroundColor)
                        .padding(4.dp)
                ) {
                    Column {
                        // A. HIỂN THỊ ẢNH
                        if (!message.mediaUrl.isNullOrEmpty()) {
                            message.mediaUrl.forEach { imageUrl ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image",
                                    // Dùng Fit để ảnh hiển thị trọn vẹn, không bị phóng to cắt mất
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .heightIn(max = 300.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .padding(bottom = if (message.content.isNotBlank() && message.content != "Đã gửi một ảnh") 4.dp else 0.dp)
                                        .clickable { onImageClick(imageUrl) }
                                )
                            }
                        }

                        // B. HIỂN THỊ TEXT
                        if (message.content.isNotBlank() && message.content != "Đã gửi một ảnh") {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- TIMESTAMP (Ẩn/Hiện bên dưới) ---
        AnimatedVisibility(
            visible = isTimestampVisible || highlight,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Căn chỉnh vị trí timestamp cho thẳng với bong bóng chat
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

        // Tách nhóm tin nhắn
        if (showTail) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}