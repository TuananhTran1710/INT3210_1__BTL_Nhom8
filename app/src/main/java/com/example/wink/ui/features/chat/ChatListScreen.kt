package com.example.wink.ui.features.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

// ... (Phần ChatListScreen giữ nguyên, chỉ sửa logic filter và gọi ChatContainer) ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val friends by viewModel.friends.collectAsState() // Lấy list bạn bè
    val isLoading by viewModel.isLoading.collectAsState()

    // State cho SearchBar
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // State cho Dialog xóa
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
                }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    // Lắng nghe sự kiện điều hướng (khi chọn bạn bè trong search)
    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatListEffect.NavigateToChat -> {
                    active = false // Đóng search bar
                    searchQuery = "" // Xóa text tìm kiếm
                    navController.navigate("message/${effect.chatId}")
                }
                is ChatListEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chats") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.NewChat.route) }) {
                Icon(Icons.Default.Message, contentDescription = "New Message")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // --- SEARCH BAR ---
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* Handle enter click if needed */ },
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) searchQuery = "" // Reset khi đóng
                },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            ) {
                // === PHẦN NỘI DUNG KHI ẤN VÀO SEARCH BAR ===

                // 1. Lọc danh sách bạn bè dựa trên từ khóa nhập vào
                val filteredFriends = if (searchQuery.isEmpty()) {
                    friends
                } else {
                    friends.filter { it.username.contains(searchQuery, ignoreCase = true) }
                }

                // 2. Hiển thị danh sách
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (filteredFriends.isEmpty()) {
                        item {
                            Text(
                                text = "Không tìm thấy người dùng",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        items(filteredFriends) { friend ->
                            SearchFriendItem(
                                friend = friend,
                                onClick = {
                                    // Khi ấn vào thì gọi ViewModel tạo chat
                                    viewModel.onSearchFriendSelected(friend.uid)
                                }
                            )
                        }
                    }
                }
            }

            // --- NỘI DUNG CHÍNH (LIST CHAT) ---
            // Chỉ hiển thị khi SearchBar không active (hoặc nằm dưới SearchBar chưa expand)
            if (!active) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Trong ChatContainer:
                    ChatContainer(
                        chats = chats, // hoặc chats thường
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        onPinClick = { chatId, isPinned -> viewModel.togglePinChat(chatId, isPinned) }, // Callback Pin
                        onDeleteClick = { chatId ->
                            chatToDeleteId = chatId
                            showDeleteDialog = true
                        } // Callback Delete
                    )
                }
            }
        }
    }
}
// Item hiển thị bạn bè trong SearchBar (Gọn nhẹ hơn)
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
        // Avatar
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

        // Tên
        Text(
            text = friend.username,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun ChatContainer(chats: List<UiChat>,
                  navController: NavController,
                  modifier: Modifier = Modifier,
                  onPinClick: (String, Boolean) -> Unit,
                  onDeleteClick: (String) -> Unit) {
    LazyColumn(modifier = modifier) {
        item {
            ChatAIItem(navController = navController)
        }
        if (chats.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chats found")
                }
            }
        } else {
            items(chats, key = { it.chat.chatId }) { uiChat -> // Thêm key để list update mượt
                ChatItem(
                    uiChat = uiChat,
                    navController = navController,
                    onPinClick = onPinClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}
// --- CẬP NHẬT GIAO DIỆN ITEM ---
@Composable
fun ChatItem(
    uiChat: UiChat,
    navController: NavController,
    onPinClick: (String, Boolean) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // State cho menu 3 chấm

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiChat.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false) // Để text co giãn
                )
                // HIỂN THỊ ICON PIN NẾU ĐƯỢC PIN
                if (uiChat.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = uiChat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiChat.displayAvatarUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Thời gian
                Text(
                    text = formatTimestamp(uiChat.chat.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // DẤU 3 CHẤM (MENU)
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // MENU OPTIONS
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Option Pin/Unpin
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
                                onPinClick(uiChat.chat.chatId, uiChat.isPinned)
                            }
                        )

                        // Option Delete
                        DropdownMenuItem(
                            text = { Text("Xóa tin nhắn", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                onDeleteClick(uiChat.chat.chatId)
                            }
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (uiChat.isPinned) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
            // Có thể làm màu nền khác nhẹ cho tin nhắn được pin nếu muốn
        ),
        modifier = Modifier
            .clickable { navController.navigate("message/${uiChat.chat.chatId}") }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}