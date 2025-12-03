package com.example.wink.ui.features.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    navController: NavController,
    viewModel: SocialViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Bảng tin", "Xếp hạng")

    // BottomSheetState
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- BOTTOM SHEET BÌNH LUẬN ---
    if (state.activePostId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissCommentSheet() },
            sheetState = sheetState
        ) {
            // Nội dung Bottom Sheet
            CommentSheetContent(
                comments = state.commentsForActivePost,
                newComment = state.newCommentContent,
                onValueChange = { viewModel.onCommentContentChange(it) },
                onSend = { viewModel.onSendComment() }
            )
        }
    }

    // Hộp thoại soạn bài đăng (Hiện lên khi bấm vào thanh nhập liệu)
    if (state.isCreatingPost) {
        CreatePostDialog(
            content = state.newPostContent,
            onContentChange = { viewModel.onPostContentChange(it) },
            onDismiss = { viewModel.onDismissPostDialog() },
            onPost = { viewModel.onSendPost() }
        )
    }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (state.selectedTab == 0) {
                    FeedList(
                        posts = state.feedList,
                        onUserClick = { /* TODO */ },
                        onCreatePostClick = { viewModel.onFabClick() },
                        onLikeClick = { postId -> viewModel.onLikeClick(postId) },
                        onCommentClick = { postId -> viewModel.onOpenCommentSheet(postId) }
                    )
                } else {
                    LeaderboardList(users = state.leaderboardList, onUserClick = { /* TODO */ })
                }
            }
        }
    }
}

// --- 1. Thanh nhập liệu "Bạn đang nghĩ gì?" (Giống Facebook) ---
@Composable
fun CreatePostInputBar(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar người dùng hiện tại
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Phần giả lập ô nhập liệu (Hình con nhộng/tròn)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    //.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Bạn đang nghĩ gì?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Icon hình ảnh (Chỉ để trang trí cho giống)
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Ảnh",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- UI Bảng tin (Feed) ---
@Composable
fun FeedList(
    posts: List<SocialPost>,
    onUserClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onLikeClick: (String) -> Unit,      // Mới
    onCommentClick: (String) -> Unit    // Mới
) {
    LazyColumn {
        // ITEM 1: Thanh nhập liệu luôn nằm trên cùng
        item {
            CreatePostInputBar(onClick = onCreatePostClick)
            // Đường kẻ phân cách đậm hơn chút để tách phần nhập liệu với Feed
            HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }

        // Các bài post bên dưới
        items(posts) { post ->
            FeedItem(post, onUserClick, onLikeClick, onCommentClick)
            // Đường kẻ mờ phân cách các bài viết
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun FeedItem(
    post: SocialPost,
    onUserClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCommentClick(post.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header: Avatar + Tên
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • 2 giờ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Content
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Actions (Like/Comment)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Nút Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick(post.id) } // Gọi hàm Like
            ) {
                // Logic đổi màu tim
                Icon(
                    imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    modifier = Modifier.size(20.dp),
                    tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${post.likes}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 2. Nút Comment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCommentClick(post.id) } // Gọi hàm Comment
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${post.comments}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- UI NỘI DUNG COMMENT SHEET ---
@Composable
fun CommentSheetContent(
    comments: List<Comment>,
    newComment: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f) // Chiều cao 70% màn hình
            .padding(16.dp)
    ) {
        Text("Bình luận", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Danh sách comment
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (comments.isEmpty()) {
                item { Text("Chưa có bình luận nào. Hãy là người đầu tiên!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
            }
            items(comments) { comment ->
                CommentItem(comment)
            }
        }

        // Ô nhập comment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newComment,
                onValueChange = onValueChange,
                placeholder = { Text("Viết bình luận...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, contentDescription = "Gửi", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(6.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("vừa xong", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// --- Dialog Soạn Bài (Giữ nguyên) ---
@Composable
fun CreatePostDialog(
    content: String,
    onContentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onPost: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tạo bài viết", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Chia sẻ thành tích RIZZ của bạn...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPost,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank()
                ) {
                    Text("Đăng ngay")
                }
            }
        }
    }
}

// --- Leaderboard (Giữ nguyên) ---
@Composable
fun LeaderboardList(users: List<User>, onUserClick: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        itemsIndexed(users) { index, user ->
            LeaderboardItem(index + 1, user, onUserClick)
            if (index < users.lastIndex) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: User, onUserClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(user.uid) }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.
            titleLarge,
            fontWeight = FontWeight.Bold,
            color = when(rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.width(40.dp)
        )
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(10.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "Streak: ${user.loginStreak} \uD83D\uDD25", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "${user.rizzPoints} RIZZ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}