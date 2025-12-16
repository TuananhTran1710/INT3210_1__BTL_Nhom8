package com.example.wink.ui.features.chat

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.R
import com.example.wink.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var chatToDeleteId by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog && chatToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa cuộc trò chuyện?") },
            text = { Text("Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChat(chatToDeleteId!!)
                    showDeleteDialog = false
                    chatToDeleteId = null
                }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatListEffect.NavigateToChat -> {
                    active = false
                    searchQuery = ""
                    navController.navigate("message/${effect.chatId}")
                }
                is ChatListEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val pinned = chats.filter { it.isPinned }
    val recent = chats.filter { !it.isPinned }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatListTopBar(
                title = "Tin nhắn",
                onNewChat = { navController.navigate(Screen.NewChat.route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            ChatSearchBar(
                query = searchQuery,
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) searchQuery = ""
                },
                onQueryChange = { searchQuery = it }
            ) {
                // Nội dung khi đang search
                val filteredFriends = if (searchQuery.isBlank()) friends
                else friends.filter { it.username.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (filteredFriends.isEmpty()) {
                        item {
                            Text(
                                text = "Không tìm thấy người dùng",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredFriends) { friend ->
                            SearchFriendItem(
                                friend = friend,
                                onClick = { viewModel.onSearchFriendSelected(friend.uid) }
                            )
                        }
                    }
                }
            }

            if (!active) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Section: ĐẶC BIỆT
                        item { SectionHeader(text = "ĐẶC BIỆT") }

                        item {
                            SpecialAIItem(
                                onClick = { navController.navigate("message/ai_chat") }
                            )
                        }

                        if (pinned.isNotEmpty()) {
                            items(pinned, key = { it.chat.chatId }) { uiChat ->
                                ChatRowItem(
                                    uiChat = uiChat,
                                    onClick = { navController.navigate("message/${uiChat.chat.chatId}") },
                                    onPinClick = { viewModel.togglePinChat(uiChat.chat.chatId, uiChat.isPinned) },
                                    onDeleteClick = {
                                        chatToDeleteId = uiChat.chat.chatId
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }

                        // Section: GẦN ĐÂY
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeader(text = "GẦN ĐÂY")
                        }

                        if (recent.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Chưa có tin nhắn nào",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            items(recent, key = { it.chat.chatId }) { uiChat ->
                                ChatRowItem(
                                    uiChat = uiChat,
                                    onClick = { navController.navigate("message/${uiChat.chat.chatId}") },
                                    onPinClick = { viewModel.togglePinChat(uiChat.chat.chatId, uiChat.isPinned) },
                                    onDeleteClick = {
                                        chatToDeleteId = uiChat.chat.chatId
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CÁC COMPONENT CON ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListTopBar(
    title: String,
    onNewChat: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            FilledIconButton(
                onClick = onNewChat,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "New message",
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        windowInsets = WindowInsets(0.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSearchBar(
    query: String,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { },
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Tìm kiếm cuộc trò chuyện...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        shape = RoundedCornerShape(24.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        windowInsets = WindowInsets(0.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )
}

// === NEW COMPONENT: Xử lý Avatar (Ảnh hoặc Chữ cái) ===
@Composable
fun UserAvatar(
    imageUrl: String?,
    userName: String,
    modifier: Modifier = Modifier,
    // Màu nền mặc định cho avatar chữ cái (ví dụ: primaryContainer)
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    // Màu chữ mặc định cho avatar chữ cái
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    if (imageUrl.isNullOrBlank()) {
        // Hiển thị chữ cái đầu nếu không có ảnh
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    } else {
        // Hiển thị ảnh nếu có URL
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_launcher_background), // Ảnh giữ chỗ an toàn
            error = painterResource(R.drawable.ic_launcher_background), // Ảnh lỗi an toàn
            contentDescription = "Avatar of $userName",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    }
}


@Composable
private fun SpecialAIItem(onClick: () -> Unit) {
    // Màu sắc dựa trên image_2.png (Nền tím tối, viền hồng rực)
    val cardBackgroundColor = Color(0xFF311133) // Màu nền tối từ ảnh mẫu
    val avatarBorderColor = Color(0xFFFF4081) // Màu hồng rực cho viền

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, avatarBorderColor.copy(alpha = 0.3f)),
        color = cardBackgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar AI với viền đậm
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.ai_crush) // Sử dụng đúng resource ảnh AI
                    .crossfade(true)
                    .build(),
                contentDescription = "AI Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(3.dp, avatarBorderColor, CircleShape) // Thêm viền dày màu hồng
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lan Anh",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White // Chữ màu trắng cho nổi trên nền tối
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đừng làm tớ ngại chứ \uD83D\uDE33",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
@Composable
private fun ChatRowItem(
    uiChat: UiChat,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // --- LOGIC CHECK UNREAD ---
    val isUnread = uiChat.isUnread && !uiChat.isAiChat // AI Chat thì ko cần check unread kiểu này (tuỳ logic)

    // 1. Cấu hình Font chữ: Đậm (Chưa đọc) vs Thường (Đã đọc)
    val nameWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.SemiBold
    val messageWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal

    // 2. Cấu hình Màu sắc: Đen rõ (Chưa đọc) vs Xám (Đã đọc)
    val msgColor = if (isUnread)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    // 3. Cấu hình Nền: Sáng hơn (Chưa đọc) vs Tối hơn (Đã đọc) - Tạo độ tương phản
    val containerColor = if (isUnread) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- AVATAR & UNREAD DOT ---
            Box {
                UserAvatar(
                    imageUrl = uiChat.displayAvatarUrl,
                    userName = uiChat.displayName,
                    modifier = Modifier.size(52.dp)
                )

                // Dấu chấm xanh báo tin chưa đọc
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(containerColor, CircleShape) // Viền giả trùng màu nền để cắt hình
                            .padding(3.dp) // Độ dày viền
                            .background(MaterialTheme.colorScheme.primary, CircleShape) // Chấm xanh chủ đạo
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- TEXT CONTENT ---
            Column(modifier = Modifier.weight(1f)) {
                // Hàng 1: Tên + Icon Ghim
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiChat.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = nameWeight, // Font đậm
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiChat.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier
                                .size(14.dp)
                                .padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hàng 2: Nội dung tin nhắn + Thời gian
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiChat.lastMessage.ifBlank { "Hình ảnh" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = messageWeight, // Font đậm
                        color = msgColor,           // Màu đậm
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "• ${formatChatRowTime(uiChat.chat.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal, // Giờ cũng in đậm
                        color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // --- MENU OPTIONS (3 DOTS) ---
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = (-10).dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (uiChat.isPinned) "Bỏ ghim" else "Ghim lên đầu") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiChat.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            expanded = false
                            onPinClick()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Xóa tin nhắn", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchFriendItem(
    friend: SearchFriendUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sử dụng lại UserAvatar cho đồng bộ
        UserAvatar(
            imageUrl = friend.avatarUrl,
            userName = friend.username,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = friend.username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}