package com.example.wink.ui.features.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.data.model.SocialPost
import com.example.wink.data.repository.FriendRequestStatus
import com.example.wink.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user
    val context = LocalContext.current // Lấy context để show Toast
    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserDetailEffect.NavigateToChat -> {
                    // Điều hướng sang màn hình nhắn tin với chatId lấy được
                    navController.navigate("message/${effect.chatId}")
                }
                is UserDetailEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {}, // Để trống để hiện ảnh bìa
                navigationIcon = {
                    // Nút Back nổi trên nền (có background mờ để dễ nhìn)
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy người dùng")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
            ) {
                // 1. HEADER (Ảnh bìa + Avatar)
                item {
                    Box(modifier = Modifier.height(180.dp)) {
                        Spacer(modifier = Modifier.fillMaxWidth())
                        // Avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(140.dp)
                                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(user.username.take(1), style = MaterialTheme.typography.displayMedium)
                            }
                        }
                    }
                }

                // 2. INFO & ACTIONS
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text(user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("RIZZ Master • Level ${user.rizzPoints / 100}", color = MaterialTheme.colorScheme.secondary)

                        Spacer(Modifier.height(24.dp))

                        // Snackbar for messages
                        state.successMessage?.let { message ->
                            LaunchedEffect(message) {
                                kotlinx.coroutines.delay(2000)
                                viewModel.clearMessages()
                            }
                        }
                        state.errorMessage?.let { message ->
                            LaunchedEffect(message) {
                                kotlinx.coroutines.delay(3000)
                                viewModel.clearMessages()
                            }
                        }

                        // Success/Error message display
                        state.successMessage?.let { message ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        state.errorMessage?.let { message ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        // Action Buttons - Ẩn nút kết bạn nếu xem profile của chính mình
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Nút Kết Bạn - chỉ hiển thị khi không phải profile của chính mình
                            if (!state.isOwnProfile) when (state.friendRequestStatus) {
                                FriendRequestStatus.NOT_SENT -> {
                                    Button(
                                        onClick = { viewModel.sendFriendRequest() },
                                        modifier = Modifier.weight(1f),
                                        enabled = !state.isSendingRequest
                                    ) {
                                        if (state.isSendingRequest) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Đang gửi...")
                                        } else {
                                            Icon(Icons.Default.PersonAdd, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Kết bạn")
                                        }
                                    }
                                }
                                FriendRequestStatus.REQUEST_SENT -> {
                                    OutlinedButton(
                                        onClick = { },
                                        modifier = Modifier.weight(1f),
                                        enabled = false
                                    ) {
                                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.outline)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Đã gửi lời mời", color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                FriendRequestStatus.REQUEST_RECEIVED -> {
                                    OutlinedButton(
                                        onClick = { },
                                        modifier = Modifier.weight(1f),
                                        enabled = false
                                    ) {
                                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Đã nhận lời mời", color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                FriendRequestStatus.ALREADY_FRIENDS -> {
                                    var showUnfriendMenu by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        FilledTonalButton(
                                            onClick = { showUnfriendMenu = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        ) {
                                            Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Bạn bè", color = MaterialTheme.colorScheme.primary)
                                        }
                                        DropdownMenu(
                                            expanded = showUnfriendMenu,
                                            onDismissRequest = { showUnfriendMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Hủy kết bạn", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showUnfriendMenu = false
                                                    viewModel.unfriend()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.PersonRemove,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Nút Nhắn Tin - chỉ hiển thị khi là bạn bè và không phải profile của chính mình
                            if (!state.isOwnProfile && state.friendRequestStatus == FriendRequestStatus.ALREADY_FRIENDS) {
                                FilledTonalButton(
                                    onClick = { viewModel.sendMessage() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Message, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Nhắn tin")
                                }
                            }
                        }
                    }
                }

                // 3. STATS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "${user.rizzPoints}", label = "RIZZ")
                        VerticalDivider()
                        StatItem(value = "${state.userPosts.size}", label = "Bài đăng")
                        VerticalDivider()
                        StatItem(value = "10", label = "Bạn bè")
                    }
                }

                // 4. USER POSTS
                if (state.userPosts.isNotEmpty()) {
                    item {
                        Text(
                            "Bài viết",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(state.userPosts.size) { index ->
                        val post = state.userPosts[index]
                        PostCard(post = post, uid=user.username)
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Log.d("UserDetailScreen", "uid = ${user.username}")
                    }
                } else {
                    item {
                        Text(
                            "Chưa có bài viết nào",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: SocialPost, uid: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        // Retweet Badge - hiển thị khi đây là bài đăng lại
        if (post.isRepost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Retweet",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@${post.username} đã đăng lại",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Hiển thị thông báo nếu bài gốc đã bị xóa
        if (post.isRepost && post.isOriginalDeleted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bài gốc đã bị xóa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Xác định avatar và username để hiển thị (dùng bài gốc nếu là repost)
        val displayAvatarUrl = if (post.isRepost) post.originalAvatarUrl else post.avatarUrl
        val displayUsername = if (post.isRepost) post.originalUsername ?: post.username else post.username

        // Header với Avatar và Username của bài gốc
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                if (!displayAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(displayUsername.take(1).uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(displayUsername, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(TimeUtils.getRelativeTime(post.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        if (post.content.isNotBlank()) {
            Text(post.content, style = MaterialTheme.typography.bodyMedium)
        }

        // Images (nếu có)
        if (post.imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                post.imageUrls.take(3).forEach { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likes}", style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.comments}", style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (post.isRetweetedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.retweetCount}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
