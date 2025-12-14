package com.example.wink.ui.features.dashboard

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wink.R
import com.example.wink.data.model.FriendRequest
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.*
import androidx.compose.ui.res.painterResource



data class DailyTask(
    val id: Int,
    val title: String,
    val reward: Int,
    val isCompleted: Boolean = false
)

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Hiển thị dialog lời mời kết bạn
    if (uiState.showFriendRequestsDialog) {
        FriendRequestsDialog(
            requests = uiState.pendingFriendRequests,
            onAccept = { requestId -> viewModel.onEvent(DashboardEvent.OnAcceptFriendRequest(requestId)) },
            onReject = { requestId -> viewModel.onEvent(DashboardEvent.OnRejectFriendRequest(requestId)) },
            onDismiss = { viewModel.onEvent(DashboardEvent.OnCloseFriendRequests) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                pendingRequestsCount = uiState.pendingFriendRequests.size,
                onNotificationClick = { viewModel.onEvent(DashboardEvent.OnOpenFriendRequests) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // RIZZ Points Card
            item {
                AnimatedDashboardItem(delay = 0) {
                    RizzPointsCard(
                        points = uiState.rizzPoints,
                        streakDays = uiState.dailyStreak,
                        attended = uiState.hasDailyCheckIn,
                        onStreakClick = {
                            viewModel.onEvent(DashboardEvent.OnDailyCheckIn)
                        }
                    )
                }
            }

            // AI Chat Feature
            item {
                AnimatedDashboardItem(delay = 100) {
                    AIFeatureCard(
                        onClick = {

                            viewModel.onEvent(DashboardEvent.OnStartAIChat) }
                    )
                }
            }

            // Daily Tasks Section
            item {
                AnimatedDashboardItem(delay = 200) {
                    DailyTasksSection(
                        onTaskClick = { viewModel.onEvent(DashboardEvent.OnCompleteTask) }
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    pendingRequestsCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    // Animation cho chuông khi có request mới
    val infiniteTransition = rememberInfiniteTransition(label = "bell_animation")
    val bellRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_rotation"
    )

    val bellScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_scale"
    )

    TopAppBar(
        title = {
            Text(
                text = "TRANG CHỦ",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            // Nút chuông thông báo với badge
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = if (pendingRequestsCount > 0) {
                        Modifier
                            .scale(bellScale)
                            .graphicsLayer { rotationZ = bellRotation }
                    } else {
                        Modifier
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Thông báo kết bạn",
                        tint = if (pendingRequestsCount > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                // Badge hiển thị số lượng lời mời
                if (pendingRequestsCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (pendingRequestsCount > 99) "99+" else pendingRequestsCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
    )
}

@Composable
private fun AnimatedDashboardItem(
    delay: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        content()
    }
}

@Composable
private fun RizzPointsCard(
    points: Int,
    streakDays: Int,
    attended: Boolean,
    onStreakClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val isDarkMode = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(300),
        label = "rizz_card_scale"
    )

    // Adaptive gradient colors for light and dark mode
    val gradientColors = if (isDarkMode) {
        listOf(
            Color(0xFFB8478A),  // Darker pink for dark mode
            Color(0xFF5A4BA3)   // Darker purple for dark mode
        )
    } else {
        listOf(
            Color(0xFFDA47B5),  // Original pink
            Color(0xFF7B5DFF)   // Original purple
        )
    }

    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onStreakClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = brush)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Tổng điểm RIZZ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp
                )

                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Streak Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (attended) R.drawable.fire1 else R.drawable.fire2
                                    ),
                                    contentDescription = "Streak",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "$streakDays ngày",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Streak đăng nhập",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = onStreakClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = if (isDarkMode) Color(0xFF5A4BA3) else Color(0xFF7B5DFF)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (attended) "Đã điểm danh" else "Điểm danh",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun AIFeatureCard(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val isDarkMode = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(300),
        label = "ai_card_scale"
    )

    // Adaptive gradient colors for light and dark mode
    val gradientColors = if (isDarkMode) {
        listOf(
            Color(0xFFD88940),  // Darker orange for dark mode
            Color(0xFF6D4BA8)   // Darker purple for dark mode
        )
    } else {
        listOf(
            Color(0xFFF9A546),  // Original orange
            Color(0xFF8F5FF3)   // Original purple
        )
    }

    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = brush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AI Crush",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Lan Anh",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = if (isDarkMode) Color(0xFFD88940) else Color(0xFFF9A546)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "Vào hâm nóng",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Avatar placeholder
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "AI Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTasksSection(
    onTaskClick: () -> Unit
) {
    val tasks = listOf(
        DailyTask(1, "Nhắn tin với AI Crush 6 lần", 150, false),
        DailyTask(2, "Bình luận vào 3 bài viết khác nhau", 100, false),
        DailyTask(3, "Đăng một bài viết lên Bảng tin", 67, false)
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Tasks",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Nhiệm vụ hôm nay",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.forEach { task ->
                DailyTaskItem(
                    task = task,
                    onClick = onTaskClick
                )
            }
        }
    }
}

@Composable
private fun DailyTaskItem(
    task: DailyTask,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(300),
        label = "task_item_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                color = if (task.isCompleted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "+${task.reward} RIZZ",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF9A546),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// Preview functions
@Preview(showBackground = true)
@Composable
private fun DashboardTopBarPreview() {
    DashboardTopBar()
}

@Preview(showBackground = true)
@Composable
private fun RizzPointsCardPreview() {
    RizzPointsCard(
        points = 1250,
        streakDays = 2,
        attended = false,
        onStreakClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun AIFeatureCardPreview() {
    AIFeatureCard(
        onClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun DailyTasksSectionPreview() {
    DailyTasksSection(
        onTaskClick = { }
    )
}
/**
 * Dialog hiển thị danh sách lời mời kết bạn
 */
@Composable
fun FriendRequestsDialog(
    requests: List<FriendRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lời mời kết bạn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có lời mời kết bạn nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requests) { request ->
                        FriendRequestItem(
                            request = request,
                            onAccept = { onAccept(request.id) },
                            onReject = { onReject(request.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

/**
 * Item hiển thị một lời mời kết bạn
 */
@Composable
private fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = request.fromUsername.ifBlank { "Người dùng" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Muốn kết bạn với bạn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons - Accept & Decline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decline button
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Từ chối")
                }

                // Accept button
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chấp nhận")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendRequestsDialogPreview() {
    val mockRequests = listOf(
        FriendRequest(
            id = "1",
            fromUserId = "user1",
            toUserId = "currentUser",
            fromUsername = "Nguyễn Văn A",
            fromAvatarUrl = ""
        ),
        FriendRequest(
            id = "2",
            fromUserId = "user2",
            toUserId = "currentUser",
            fromUsername = "Trần Thị B",
            fromAvatarUrl = ""
        )
    )
    FriendRequestsDialog(
        requests = mockRequests,
        onAccept = {},
        onReject = {},
        onDismiss = {}
    )
}