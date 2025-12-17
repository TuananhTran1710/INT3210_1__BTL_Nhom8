package com.example.wink.ui.features.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.R
import com.example.wink.data.model.Message
import com.example.wink.ui.common.DateUtils
import com.example.wink.ui.features.chat.AnalyzeDialog
import com.example.wink.ui.navigation.Screen

@Composable
fun MessageScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // 1. STATE MỚI: Lưu URL của ảnh đang được bấm vào để xem
    var clickedImageUrl by remember { mutableStateOf<String?>(null) }
    val messages by viewModel.messages.collectAsState()
    val chatTitle by viewModel.chatTitle.collectAsState()
    val chatAvatarUrl by viewModel.chatAvatarUrl.collectAsState()
    var messageText by remember { mutableStateOf("") }
// 1. STATE MỚI: Danh sách ảnh đang chọn
    val selectedImageUris = remember { mutableStateListOf<android.net.Uri>() }

    // 2. LAUNCHER: Chọn NHIỀU ảnh
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.addAll(uris)
        }
    }
    Scaffold(
        topBar = {
            MessageTopBar(
                title = chatTitle,
                avatarUrl = chatAvatarUrl,
                onBackClick = { navController.popBackStack() },
                onAnalyzeClick = { /* TODO: Handle analyze click */ },
                onSettingClick = { },
                showSettingButton = false,
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // 3. VÙNG PREVIEW ẢNH (Copy từ Chat AI sang, đã làm đẹp nút X)
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    // Ảnh Thumbnail
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    )

                                    // Nút Xóa đẹp
                                    Surface(
                                        onClick = { selectedImageUris.remove(uri) },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        tonalElevation = 4.dp,
                                        shadowElevation = 4.dp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. INPUT ROW
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút chọn ảnh
                        IconButton(onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                            Icon(
                                Icons.Default.Photo,
                                contentDescription = "Add image"
                            )
                        }

                        // Input Text
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập tin nhắn...") },
                            shape = CircleShape,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Nút Gửi
                        val canSend = messageText.isNotBlank() || selectedImageUris.isNotEmpty()
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    // GỌI HÀM GỬI MỚI (Text + List Ảnh)
                                    viewModel.sendMessage(messageText, selectedImageUris.toList())

                                    // Reset UI ngay lập tức
                                    messageText = ""
                                    selectedImageUris.clear()
                                }
                            },
                            enabled = canSend
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        MessageContainer(
            messages = messages,
            currentUserId = viewModel.currentUserId,
            avatarUrl = chatAvatarUrl,
            modifier = Modifier.padding(paddingValues),
            // 2. TRUYỀN CALLBACK XỬ LÝ CLICK ẢNH
            onImageClick = { url ->
                clickedImageUrl = url
            }
        )
    }
    // 3. HIỂN THỊ TRÌNH XEM ẢNH NẾU CÓ URL
    if (clickedImageUrl != null) {
        FullScreenImageViewer(
            imageUrl = clickedImageUrl!!,
            onDismiss = { clickedImageUrl = null }
        )
    }

//    if (showAnalyzeDialog) {
//        AnalyzeDialog(
//            isLoading = isAnalyzing,
//            result = analyzeResult,
//            onDismiss = { showAnalyzeDialog = false }
//        )
//    }
}

// --- PHẦN QUAN TRỌNG CẦN SỬA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTopBar(
    title: String,
    avatarUrl: String?,
    onBackClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    showAnalyzeButton: Boolean = false,
    onSettingClick: () -> Unit,
    showSettingButton: Boolean = false,
) {
    TopAppBar(
        // 1. Nút Back nằm ở navigationIcon (bên trái cùng)
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        // 2. Avatar và Tên nằm ở title (ngay sau nút Back)
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_launcher_background), // Ảnh mặc định khi đang load
                    error = painterResource(R.drawable.ic_launcher_background),       // Ảnh mặc định khi lỗi/null
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)          // Kích thước chuẩn avatar topbar
                        .clip(CircleShape)    // Bo tròn
                )

                Spacer(modifier = Modifier.width(12.dp)) // Khoảng cách giữa ảnh và tên

                // Tên người dùng
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Nếu tên dài quá sẽ hiện dấu ...
                )
            }
        },
        actions = {
            if (showAnalyzeButton) {   // ✅ condition
                IconButton(onClick = onAnalyzeClick) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analyze"
                    )
                }
            }
            if (showSettingButton) {   // ✅ condition
                IconButton(onClick = onSettingClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Setting"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        windowInsets = WindowInsets(0.dp)
    )
}
// ui/features/chat/MessageScreen.kt

@Composable
fun MessageContainer(
    messages: List<Message>,
    currentUserId: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    isTyping: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    highlightMessageId: String? = null,
    insightMessage: String? = null,
    onImageClick: (String) -> Unit
) {
    LaunchedEffect(highlightMessageId) {
        highlightMessageId?.let { id ->
            val index = messages.indexOfFirst { it.messageId == id }
            if (index != -1) listState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
        reverseLayout = true
    ) {
        if (isTyping) {
            item { TypingIndicator() }
        }

        itemsIndexed(messages) { index, message ->
            // 1. Lấy tin nhắn CŨ HƠN (Visually Above - index + 1)
            val olderMessage = messages.getOrNull(index + 1)

            // 2. Lấy tin nhắn MỚI HƠN (Visually Below - index - 1)
            val newerMessage = messages.getOrNull(index - 1)

            // --- LOGIC XÁC ĐỊNH VỊ TRÍ TRONG NHÓM ---

            // Là tin ĐẦU TIÊN của nhóm (Về mặt hiển thị là trên cùng)
            // Điều kiện: Tin cũ hơn là của người khác HOẶC cách xa thời gian
            val isGroupTop = if (olderMessage != null) {
                olderMessage.senderId != message.senderId ||
                        DateUtils.shouldShowTimeSeparator(message.timestamp, olderMessage.timestamp)
            } else {
                true // Không có tin cũ hơn -> Nó là Top
            }

            // Là tin CUỐI CÙNG của nhóm (Về mặt hiển thị là dưới cùng - Có Avatar)
            // Điều kiện: Tin mới hơn là của người khác HOẶC cách xa thời gian
            val isGroupBottom = if (newerMessage != null) {
                newerMessage.senderId != message.senderId ||
                        DateUtils.shouldShowTimeSeparator(newerMessage.timestamp, message.timestamp)
            } else {
                true // Không có tin mới hơn -> Nó là Bottom
            }

            // --- RENDER ---

            // Time Separator (Dựa trên tin cũ hơn)
            if (olderMessage != null && DateUtils.shouldShowTimeSeparator(message.timestamp, olderMessage.timestamp)) {
                TimeSeparator(timestamp = message.timestamp)
            } else if (index == messages.lastIndex) {
                // Luôn hiện giờ cho tin nhắn đầu tiên của cả cuộc hội thoại
                TimeSeparator(timestamp = message.timestamp)
            }

            val highlight = message.messageId == highlightMessageId

            MessageItem(
                message = message,
                isMyMessage = message.senderId == currentUserId,
                avatarUrl = avatarUrl,
                highlight = highlight,
                insight = if (highlight) insightMessage else null,
                isGroupTop = isGroupTop,       // <--- TRUYỀN PARAM MỚI
                isGroupBottom = isGroupBottom, // <--- TRUYỀN PARAM MỚI
                onImageClick = onImageClick
            )
        }
    }
}

// Component hiển thị giờ ở giữa màn hình (nhỏ, màu xám)
@Composable
fun TimeSeparator(timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DateUtils.formatTimeSeparator(timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// Giữ lại TypingIndicator vì MessageItem.kt không có cái này
@Composable
fun TypingIndicator() {
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            tonalElevation = 2.dp
        ) {
            Text(
                text = "...",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}