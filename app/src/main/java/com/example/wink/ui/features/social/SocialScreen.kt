package com.example.wink.ui.features.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
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
                onSend = { viewModel.onSendComment() }
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
                    )
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
    onLikeClick: (String) -> Unit,      // Mới
    onCommentClick: (String) -> Unit,    // Mới
    onImageClick: (String) -> Unit
) {
    LazyColumn {
        // ITEM 1: Thanh nhập liệu luôn nằm trên cùng
        item {
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
            contentType = { "post_item" }
        ) { post ->
            FeedItem(post, onUserClick, onLikeClick, onCommentClick, onImageClick = onImageClick)
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
    onImageClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        // 1. Header (Avatar + Tên)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp).clickable { onUserClick(post.userId) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                if (post.avatarUrl.isNullOrBlank()) {
                    // Trường hợp 1: Không có avatar -> Hiện Icon mặc định
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.username.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    // Trường hợp 2: Có avatar -> Load ảnh từ URL
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.avatarUrl)
                            .crossfade(true)
                            .size(Size.ORIGINAL)
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
            Column {
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TimeUtils.getRelativeTime(post.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                            .size(Size.ORIGINAL)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop, // Cắt ảnh thành hình vuông
                        modifier = Modifier
                            .size(250.dp) // Kích thước cố định hình vuông nhỏ
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onImageClick(imageUrl) } // Bấm vào để xem full
                    )
                }
            }
        }

        // 4. Action Bar (Like/Comment)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Nút Like
            Row(
                modifier = Modifier.clickable { onLikeClick(post.id) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if(post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if(post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
        }

        Spacer(modifier = Modifier.height(8.dp))
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
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = TimeUtils.getRelativeTime(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
        }
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
    minimizedMaxLines: Int = 3, // Số dòng tối đa khi thu gọn
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var isExpanded by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(text) }

    // Tính toán xem có cần cắt bớt chữ không
    LaunchedEffect(textLayoutResult) {
        textLayoutResult?.let {
            if (it.hasVisualOverflow) {
                isClickable = true
            }
        }
    }

    Column(modifier = modifier) {
        Text(
            text = finalText,
            style = style,
            color = color,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult = it }
        )

        // Nếu văn bản dài quá số dòng quy định -> Hiện nút Xem thêm
        if (isClickable || (textLayoutResult?.lineCount ?: 0) > minimizedMaxLines) {
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