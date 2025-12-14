package com.example.wink.ui.features.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
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
            text = { Text("Bạn có chắc chắn muốn xóa cuộc trò chuyện này không? Hành động này không thể hoàn tác.") },
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

    // === Chia section giống ảnh ===
    val pinned = chats.filter { it.isPinned }
    val recent = chats.filter { !it.isPinned }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
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
                .padding(padding)

        ) {
            // Search giống ảnh (ô bo tròn)
            ChatSearchBar(
                query = searchQuery,
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) searchQuery = ""
                },
                onQueryChange = { searchQuery = it }
            ) {
                // Nội dung khi search active: list bạn bè
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
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // ===== ĐẶC BIỆT =====
                        item {
                            SectionHeader(text = "ĐẶC BIỆT")
                        }

                        // AI card (luôn đứng đầu giống ảnh)
                        item {
                            SpecialAIItem(
                                onClick = { navController.navigate("message/ai_chat") }
                            )
                        }

                        // Nếu bạn muốn: pinned chats cũng xem là “đặc biệt”
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

                        // ===== GẦN ĐÂY =====
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(text = "GẦN ĐÂY")
                        }

                        if (recent.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Chưa có cuộc trò chuyện nào",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListTopBar(
    title: String,
    onNewChat: () -> Unit
) {
    TopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        actions = {
            IconButton(onClick = onNewChat) {
                Icon(Icons.Default.Message, contentDescription = "New message")
            }
        }
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
            // Chỉ chỉnh padding ngang và dưới, top = 0 để sát lên trên
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 0.dp),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { /* no-op */ },
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Tìm kiếm cuộc trò chuyện...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,

        // --- QUAN TRỌNG: THÊM DÒNG NÀY ---
        // Xóa bỏ khoảng trống mặc định dành cho Status Bar
        windowInsets = WindowInsets(0.dp),
        // ---------------------------------

        content = content
    )
}
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SpecialAIItem(onClick: () -> Unit) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
        )
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(gradientBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.ic_launcher_background)
                .crossfade(true)
                .build(),
            contentDescription = "AI Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Wink AI 💖 ✨",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Đừng làm tớ ngại chứ 😳",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }

//        Text(
//            text = "1m",
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
//        )
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

    val titleWeight = if (uiChat.isUnread && !uiChat.isAiChat) FontWeight.Bold else FontWeight.SemiBold
    val subtitleColor =
        if (uiChat.isUnread && !uiChat.isAiChat) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant

    // Row container giống ảnh: item nền “card” nhẹ, bo tròn
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiChat.displayAvatarUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_launcher_background),
            error = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name + last msg
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiChat.displayName,
                    fontWeight = titleWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (uiChat.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiChat.isUnread && !uiChat.isAiChat) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = uiChat.lastMessage.ifBlank { " " },
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Time + menu
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatChatRowTime(uiChat.chat.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
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

/** Item bạn bè trong search (giữ như bạn đã có, chỉ để lại ở đây cho đủ file) */
@Composable
fun SearchFriendItem(
    friend: SearchFriendUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (friend.avatarUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.username.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(friend.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = friend.username,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
