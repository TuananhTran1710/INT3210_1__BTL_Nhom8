package com.example.wink.ui.features.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.ui.components.ImagePreviewDialog
import com.example.wink.ui.navigation.Screen
import com.example.wink.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    navController: NavController,
    viewModel: SocialViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Bảng tin", "Xếp hạng")

    var viewingImageUrl by remember { mutableStateOf<String?>(null) }

    if (viewingImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = viewingImageUrl!!,
            onDismiss = { viewingImageUrl = null }
        )
    }

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
                onSend = { viewModel.onSendComment() },
                onCommentLikeClick = { commentId -> viewModel.onCommentLikeClick(commentId) },
                onEditComment = { commentId, newContent -> viewModel.onEditComment(commentId, newContent) }
            )
        }
    }

    // Hộp thoại soạn bài đăng (Hiện lên khi bấm vào thanh nhập liệu)
    if (state.isCreatingPost) {
        CreatePostDialog(
            content = state.newPostContent,
            selectedImages = state.selectedImageUris, // Truyền list ảnh
            onContentChange = { viewModel.onPostContentChange(it) },
            onImagesSelected = { viewModel.onImagesSelected(it) }, // Callback chọn ảnh
            onRemoveImage = { viewModel.onRemoveSelectedImage(it) }, // Callback xóa ảnh
            onDismiss = { viewModel.onDismissPostDialog() },
            onPost = { viewModel.onSendPost() },
            isPosting = state.isPosting,
            userAvatarUrl = state.currentUserAvatarUrl,
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
        Box(modifier = Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (state.selectedTab == 0) {
                    FeedList(
                        posts = state.feedList,
                        currentUserAvatarUrl = state.currentUserAvatarUrl,
                        onUserClick = { userId ->
                            // Chuyển sang màn hình hồ sơ người khác
                            navController.navigate(Screen.UserDetail.createRoute(userId))
                        },
                        onCreatePostClick = { viewModel.onFabClick() },
                        onLikeClick = { postId -> viewModel.onLikeClick(postId) },
                        onCommentClick = { postId -> viewModel.onOpenCommentSheet(postId) },
                        onImageClick = { url -> viewingImageUrl = url },
                        onRetweetClick = { postId -> viewModel.onRetweetClick(postId) },
                        onDeletePost = { postId -> viewModel.onDeletePost(postId) },
                        onEditPost = { postId, content, imageUrls ->
                            viewModel.onEditPost(postId, content, imageUrls)
                        }
                    )
                    AnimatedVisibility(
                        visible = state.hasNewPosts,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onRefreshFeed() },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Có bài viết mới")
                        }
                    }

                    // Loading khi refresh
                    if (state.isRefreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                    }
                } else {
                    LeaderboardList(users = state.leaderboardList, onUserClick = { userId ->
                        navController.navigate(Screen.UserDetail.createRoute(userId)) })
                }
            }
        }
    }
}

// --- 1. Thanh nhập liệu "Bạn đang nghĩ gì?" (Giống Facebook) ---
@Composable
fun CreatePostInputBar(currentUserAvatarUrl: String, onClick: () -> Unit) {
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
                if (currentUserAvatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentUserAvatarUrl)
                            .crossfade(true).build(),
                        contentDescription = "My Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Phần giả lập ô nhập liệu (Hình con nhộng/tròn)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Bạn đang nghĩ gì?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}

// --- UI Bảng tin (Feed) ---
@Composable
fun FeedList(
    posts: List<SocialPost>,
    currentUserAvatarUrl: String,
    onUserClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onRetweetClick: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onEditPost: (String, String, List<String>) -> Unit = { _, _, _ -> }
) {
    LazyColumn {
        // ITEM 1: Thanh nhập liệu luôn nằm trên cùng
        item(key = "create_post_input") {
            CreatePostInputBar(
                currentUserAvatarUrl = currentUserAvatarUrl,
                onClick = onCreatePostClick
            )
            // Đường kẻ phân cách đậm hơn chút để tách phần nhập liệu với Feed
            HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }

        // Các bài post bên dưới
        items(
            items = posts,
            key = { post -> post.id },
            contentType = { "social_post" }
        ) { post ->
            FeedItem(
                post,
                onUserClick,
                onLikeClick,
                onCommentClick,
                onImageClick = onImageClick,
                onRetweetClick = onRetweetClick,
                onDeletePost = onDeletePost,
                onEditPost = onEditPost
            )
            // Đường kẻ mờ phân cách các bài viết
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.67f)
            )
        }
    }
}

@Composable
fun FeedItem(
    post: SocialPost,
    onUserClick: (String) -> Unit,
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onRetweetClick: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onEditPost: (String, String, List<String>) -> Unit = { _, _, _ -> }
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editContent by remember(post.content) { mutableStateOf(post.content) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        // Retweet Badge (if applicable)
        if (post.isRepost && !post.originalUsername.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Retweet",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@${post.username} đã đăng lại",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Hiển thị thông báo nếu bài gốc đã bị xóa
        if (post.isRepost && post.isOriginalDeleted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

        if (!post.isOriginalDeleted) {
            // Xác định avatar và username để hiển thị (dùng bài gốc nếu là repost)
            val displayAvatarUrl = if (post.isRepost) post.originalAvatarUrl else post.avatarUrl
            val displayUsername =
                if (post.isRepost) post.originalUsername ?: post.username else post.username
            val displayUserId =
                if (post.isRepost) post.originalUserId ?: post.userId else post.userId

            // 1. Header (Avatar + Tên + More Button)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp).clickable { onUserClick(displayUserId) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    if (displayAvatarUrl.isNullOrBlank()) {
                        // Trường hợp 1: Không có avatar -> Hiện Icon mặc định
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayUsername.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        // Trường hợp 2: Có avatar -> Load ảnh từ URL
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(displayAvatarUrl)
                                .crossfade(true)
                                .size(100, 100)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayUsername,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = TimeUtils.getRelativeTime(post.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // More Menu (Edit/Delete)
                if (post.canDelete || post.canEdit) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (post.canEdit) {
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    onClick = {
                                        editContent = post.content
                                        showEditDialog = true
                                        showMenu = false
                                    }
                                )
                            }
                            if (post.canDelete) {
                                DropdownMenuItem(
                                    text = { Text("Xóa") },
                                    onClick = {
                                        onDeletePost(post.id)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Edit Post Dialog
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Chỉnh sửa bài viết") },
                    text = {
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nội dung bài viết") },
                            minLines = 3
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (editContent.isNotBlank()) {
                                    onEditPost(post.id, editContent, post.imageUrls)
                                    showEditDialog = false
                                }
                            }
                        ) {
                            Text("Lưu")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            // 2. Nội dung chữ
            if (post.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableText(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. hiển thị ảnh
            if (post.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp), // Cách lề 2 bên
                    horizontalArrangement = Arrangement.spacedBy(8.dp)  // Khoảng cách giữa các ảnh
                ) {
                    items(post.imageUrls) { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Post Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onImageClick(imageUrl) }
                        )
                    }
                }
            }

            // 4. Action Bar (Like/Comment/Retweet)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Nút Like
                Row(
                    modifier = Modifier.clickable { onLikeClick(post.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likes}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Nút Comment
                Row(
                    modifier = Modifier.clickable { onCommentClick(post.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Nút Retweet
                Row(
                    modifier = Modifier.clickable { onRetweetClick(post.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = if (post.isRetweetedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.retweetCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- UI NỘI DUNG COMMENT SHEET ---
@Composable
fun CommentSheetContent(
    comments: List<Comment>,
    newComment: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onCommentLikeClick: (String) -> Unit = {},
    onEditComment: (String, String) -> Unit = { _, _ -> }
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
                CommentItem(comment, onCommentLikeClick, onEditComment)
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
fun CommentItem(
    comment: Comment,
    onLikeClick: (String) -> Unit = {},
    onEditComment: (String, String) -> Unit = { _, _ -> }
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf(comment.content) }
    var showMenu by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            if (!comment.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comment.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Avatar mặc định (Chữ cái đầu)
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = comment.username.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = TimeUtils.getRelativeTime(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (comment.isEdited) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(đã sửa)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)

            // Like button + Edit button for comment
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onLikeClick(comment.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (comment.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(14.dp),
                        tint = if (comment.isLikedByMe) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comment.likeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Nút chỉnh sửa (chỉ hiện nếu user là chủ comment)
                if (comment.canEdit) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tùy chọn",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showMenu = true },
                            tint = Color.Gray
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Chỉnh sửa") },
                                onClick = {
                                    showMenu = false
                                    editContent = comment.content
                                    showEditDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog chỉnh sửa comment
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Chỉnh sửa bình luận") },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nội dung bình luận") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editContent.isNotBlank()) {
                            onEditComment(comment.id, editContent)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// --- Dialog Soạn Bài ---
@Composable
fun CreatePostDialog(
    content: String,
    selectedImages: List<Uri>, // Danh sách ảnh đã chọn
    onContentChange: (String) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit, // Callback khi chọn xong ảnh
    onRemoveImage: (Uri) -> Unit, // Callback xóa ảnh
    onDismiss: () -> Unit,
    onPost: () -> Unit,
    userAvatarUrl: String, // Avatar user
    isPosting: Boolean,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris -> onImagesSelected(uris) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = !isPosting, dismissOnClickOutside = !isPosting)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tạo bài viết", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    // Ẩn nút đóng khi đang đăng để tránh lỗi
                    if (!isPosting) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // User Info Mini (Avatar thật)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        if (userAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = userAvatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(6.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Có thể thêm tên user ở đây nếu muốn
                    Text("Công khai", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Text Input
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Bạn đang nghĩ gì?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    enabled = !isPosting // Khóa nhập khi đang đăng
                )

                // ... (Phần hiển thị ảnh đã chọn giữ nguyên) ...
                if (selectedImages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (!isPosting) { // Chỉ cho xóa khi chưa bấm đăng
                                    IconButton(
                                        onClick = { onRemoveImage(uri) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                // Tool bar (Thêm ảnh)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thêm vào bài viết:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }, enabled = !isPosting) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Thêm ảnh", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Post Button
                Button(
                    onClick = onPost,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (content.isNotBlank() || selectedImages.isNotEmpty()) && !isPosting
                ) {
                    if (isPosting) {
                        // Hiệu ứng xoay tròn
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang đăng...")
                    } else {
                        Text("Đăng ngay")
                    }
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
            if (user.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Avatar mặc định nếu user chưa đặt ảnh
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
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

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    minimizedMaxLines: Int = 3,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var isExpanded by remember(text) { mutableStateOf(false) }
    var showSeeMore by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            // Nếu đã mở rộng -> hiện hết, ngược lại -> giới hạn dòng
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                // Logic kiểm tra: Nếu chưa mở rộng VÀ có dòng bị ẩn (overflow) -> Hiện nút Xem thêm
                // Chỉ update state nếu nó chưa đúng để tránh recomposition vòng lặp
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    if (!showSeeMore) showSeeMore = true
                }
            }
        )

        if (showSeeMore) {
            Text(
                text = if (isExpanded) "Thu gọn" else "Xem thêm",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }
    }
}