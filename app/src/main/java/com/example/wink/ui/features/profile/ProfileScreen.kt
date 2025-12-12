package com.example.wink.ui.features.profile

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.data.model.SocialPost
import com.example.wink.ui.navigation.Screen
import com.example.wink.util.TimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Tab & Pager State
    val tabs = listOf("Bài viết", "Bạn bè")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Handle Logout
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // TopBar trong suốt đè lên ảnh bìa
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                // Nút Settings (Góc phải)
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Settings, "Settings")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()), // Trừ padding bottom bar
            contentPadding = PaddingValues(0.dp)
        ) {
            // 1. COVER & AVATAR SECTION
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Tổng chiều cao vùng header
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter) // Căn giữa dưới cùng
                            .offset(y = 0.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(4.dp, MaterialTheme.colorScheme.background), // Viền trùng màu nền
                            modifier = Modifier.size(140.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            if (uiState.avatarUrl.isNullOrBlank()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = uiState.username.take(1).uppercase(),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = uiState.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // 2. USER INFO & ACTIONS
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tên & Handle
                    Text(
                        text = uiState.username.ifBlank { "Chưa đặt tên" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "@${uiState.username.replace(" ", "").lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem("${uiState.rizzPoints}", "RIZZ")
                        ProfileVerticalDivider()
                        ProfileStatItem("12", "Streak")
                        ProfileVerticalDivider()
                        ProfileStatItem("${uiState.friendCount}", "Bạn bè")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. STICKY TAB ROW (Dính khi cuộn)
            stickyHeader {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex, // Dùng biến Int
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }, // Cập nhật biến Int
                                text = { Text(title, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }

            // 4. CONTENT BASED ON TAB

            if (selectedTabIndex == 0) {
                // --- TAB BÀI VIẾT ---
                val posts = uiState.posts // Lấy posts từ State thật

                if (posts.isEmpty()) {
                    item { EmptyStateView("Chưa có bài viết nào") }
                } else {
                    items(posts) { post ->
                        ProfilePostItem(post) // Truyền SocialPost vào
                    }
                }
            } else if (selectedTabIndex == 1) {
                // --- TAB BẠN BÈ ---
                if (uiState.loadedFriends.isEmpty()) {
                    Log.d("ProfileScreen", "No friends loaded")
                    item { EmptyStateView("Chưa có bạn bè") }
                } else {
                    Log.d("ProfileScreen", "Friends loaded: ${uiState.loadedFriends.size}")
                    items(uiState.loadedFriends) { friend ->
                        FriendListItem(friend,
                            onClick = { navController.navigate(Screen.UserDetail.createRoute(friend.id)) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

// --- Sub-Components ---

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ProfileVerticalDivider() {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun ProfilePostItem(post: SocialPost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
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
                    text = if (!post.originalUsername.isNullOrBlank()) 
                        "Đã đăng lại từ @${post.originalUsername}" 
                    else "Đã đăng lại",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                // Load Avatar thật
                if (post.avatarUrl != null) {
                    AsyncImage(model = post.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(post.username.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(post.username, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                // Dùng TimeUtils
                Text(TimeUtils.getRelativeTime(post.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(post.content, style = MaterialTheme.typography.bodyMedium)

        // Hiển thị ảnh (nếu có)
        if (post.imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            if (post.imageUrls.size == 1) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    post.imageUrls.take(3).forEach { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reaction Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, 
                null, 
                Modifier.size(20.dp), 
                tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(4.dp))
            Text("${post.likes}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.width(24.dp))

            Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(4.dp))
            Text("${post.comments}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.width(24.dp))

            Icon(
                Icons.Default.Repeat, 
                null, 
                Modifier.size(20.dp), 
                tint = if (post.isRetweetedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(4.dp))
            Text("${post.retweetCount}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
    HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLow)
}

@Composable
fun FriendListItem(friend: FriendUi,
                   onClick: () -> Unit = {},
                   onMessageClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, modifier = Modifier.size(50.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text(friend.name?:"", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.name?:"", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text("${friend.rizzPoints} RIZZ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        FilledTonalButton(onClick = { onMessageClick }) {
            Text("Nhắn tin")
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 82.dp))
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.outline)
    }
}