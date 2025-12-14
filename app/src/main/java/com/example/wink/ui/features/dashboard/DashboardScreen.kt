package com.example.wink.ui.features.dashboard

import android.content.res.Configuration
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
import com.example.wink.data.model.Notification
import com.example.wink.data.model.NotificationType
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

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
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị thông báo khi có người chấp nhận lời mời kết bạn
    LaunchedEffect(uiState.acceptedFriendNotification) {
        uiState.acceptedFriendNotification?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(DashboardEvent.OnClearAcceptedNotification)
        }
    }

    // Hiển thị dialog thông báo tổng hợp
    if (uiState.showNotificationsDialog) {
        NotificationsDialog(
            notifications = uiState.notifications,
            onAcceptFriendRequest = { requestId ->
                viewModel.onEvent(DashboardEvent.OnAcceptFriendRequest(requestId))
            },
            onRejectFriendRequest = { requestId ->
                viewModel.onEvent(DashboardEvent.OnRejectFriendRequest(requestId))
            },
            onMarkAsRead = { notificationId ->
                viewModel.onEvent(DashboardEvent.OnMarkNotificationRead(notificationId))
            },
            onClearAll = { viewModel.onEvent(DashboardEvent.OnClearAllNotifications) },
            onDismiss = { viewModel.onEvent(DashboardEvent.OnCloseNotifications) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DashboardTopBar(
                notificationsCount = uiState.notifications.count { !it.isRead },
                onNotificationClick = { viewModel.onEvent(DashboardEvent.OnOpenNotifications) },
                username = uiState.username,
                pendingRequestsCount = uiState.pendingFriendRequests.size,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() + 20.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // RIZZ Points Card
            item {
                AnimatedDashboardItem(delay = 0) {
                    RizzStatsRow(
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
                        onTaskClick = { viewModel.onEvent(DashboardEvent.OnCompleteTask) },
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    username: String,
    pendingRequestsCount: Int = 0,
    notificationsCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    // Chỉ tạo animation khi có notification để tránh chạy liên tục
    val hasNotifications = notificationsCount > 0

    // Animation values - chỉ animate khi có notifications
    val bellRotation by animateFloatAsState(
        targetValue = if (hasNotifications) 0f else 0f,
        animationSpec = if (hasNotifications) {
            spring(dampingRatio = 0.3f, stiffness = 300f)
        } else {
            snap()
        },
        label = "bell_rotation"
    )

    val bellScale by animateFloatAsState(
        targetValue = if (hasNotifications) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "bell_scale"
    )

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Xin chào,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = username.ifBlank { "Người lạ" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        actions = {
            // Nút chuông thông báo với badge
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .scale(bellScale)
                        .graphicsLayer { rotationZ = bellRotation }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = if (hasNotifications)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                // Badge hiển thị số lượng thông báo chưa đọc
                if (hasNotifications) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (notificationsCount > 99) "99+" else notificationsCount.toString(),
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
        delay(delay.toLong())
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
fun RizzStatsRow(
    points: Int,
    streakDays: Int,
    attended: Boolean,
    onStreakClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- CARD 1: ĐIỂM RIZZ ---
        val rizzGlowColor = MaterialTheme.colorScheme.primary

        StatCardItem(
            modifier = Modifier.weight(1f),
            title = "Điểm RIZZ",
            glowColor = rizzGlowColor,
            content = {
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        )

        // --- CARD 2: ĐIỂM DANH (STREAK) ---
        val fireGlowColor = Color(0xFFFF9800)

        // Animation "thở" (Pulse) nhẹ nếu chưa điểm danh để nhắc nhở
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by if (!attended) {
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_pulse"
            )
        } else {
            remember { mutableStateOf(1f) }
        }

        StatCardItem(
            modifier = Modifier
                .weight(1f)
                .scale(pulseScale),
            title = if (attended) "Đã điểm danh" else "Điểm danh ngay",
            onClick = onStreakClick,
            isClickable = true,
            glowColor = fireGlowColor,
            // Nếu chưa điểm danh: Thêm viền màu Primary để highlight
            border = if (!attended) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = if (attended) R.drawable.fire1 else R.drawable.fire2),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "$streakDays ngày",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Gợi ý hành động rõ ràng hơn
                        if (!attended) {
                            Text(
                                text = "Ấn để nhận",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun StatCardItem(
    modifier: Modifier = Modifier,
    title: String,
    glowColor: Color,
    content: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    isClickable: Boolean = false,
    border: BorderStroke? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "press")

    Card(
        modifier = modifier
            .height(110.dp)
            .scale(scale)
            .then(
                if (isClickable && onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isPressed = true
                            onClick()
                        }
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Thêm bóng nhẹ
    ) {
        // Reset click animation
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // --- KỸ THUẬT VẼ GRADIENT GÓC (SOFT SPARK) ---
                .drawBehind {
                    val glowRadius = size.maxDimension * 0.6f // Độ lan tỏa của ánh sáng
                    val offset = Offset(size.width, size.height) // Vị trí: Góc phải dưới

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.25f), // Màu tâm (trong suốt 15%)
                                Color.Transparent             // Màu ngoài cùng
                            ),
                            center = offset,
                            radius = glowRadius
                        )
                    )
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
                    content()
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
    val gradientColors =
        listOf(
            Color(0xFF4D25D3),  // purple
            Color(0xFFEE822F)   // orange
        )

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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = brush)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-50).dp, y = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI CRUSH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lan Anh",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFC24706)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Vào hâm nóng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    // Placeholder for Image
                    Image(
                        painter = painterResource(id = R.drawable.ai_crush),
                        contentDescription = "AI Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyTasksSection(
    onTaskClick: () -> Unit,
    modifier: Modifier
) {
    val tasks = listOf(
        DailyTask(1, "Nhắn tin với AI Crush 6 lần", 150, false),
        DailyTask(2, "Bình luận vào 3 bài viết khác nhau", 100, true),
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
                imageVector = Icons.Default.AssignmentLate,
                contentDescription = "Tasks",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Nhiệm vụ hôm nay",
                style = MaterialTheme.typography.titleLarge,
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) MaterialTheme.colorScheme.primary
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .border(
                        if (task.isCompleted) 0.dp else 2.dp,
                        if (task.isCompleted) Color.Transparent else Color.Gray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reward Badge
            Surface(
                color = Color(0xFFF9A546).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "+${task.reward} RIZZ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFF9A546),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


// Preview functions
@Preview(showBackground = true)
@Composable
private fun DashboardTopBarPreview() {
    DashboardTopBar(username = "duattrandang")
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
        onTaskClick = { },
        modifier = TODO()
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
 * Dialog hiển thị tất cả thông báo tổng hợp
 */
@Composable
fun NotificationsDialog(
    notifications: List<Notification>,
    onAcceptFriendRequest: (String) -> Unit,
    onRejectFriendRequest: (String) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thông báo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Xóa tất cả",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
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
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có thông báo nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onAcceptFriendRequest = onAcceptFriendRequest,
                            onRejectFriendRequest = onRejectFriendRequest,
                            onMarkAsRead = { onMarkAsRead(notification.id) }
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
 * Item hiển thị một thông báo
 */
@Composable
private fun NotificationItem(
    notification: Notification,
    onAcceptFriendRequest: (String) -> Unit,
    onRejectFriendRequest: (String) -> Unit,
    onMarkAsRead: () -> Unit
) {
    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }

    val icon = when (notification.type) {
        NotificationType.FRIEND_REQUEST -> Icons.Default.PersonAdd
        NotificationType.FRIEND_REQUEST_ACCEPTED -> Icons.Default.People
        NotificationType.LIKE_POST -> Icons.Default.Favorite
        NotificationType.COMMENT_POST -> Icons.Default.Comment
        NotificationType.NEW_MESSAGE -> Icons.Default.Message
        NotificationType.DAILY_REMINDER -> Icons.Default.Today
        NotificationType.REWARD_EARNED -> Icons.Default.EmojiEvents
        NotificationType.GENERAL -> Icons.Default.Notifications
    }

    val iconColor = when (notification.type) {
        NotificationType.FRIEND_REQUEST -> MaterialTheme.colorScheme.primary
        NotificationType.FRIEND_REQUEST_ACCEPTED -> MaterialTheme.colorScheme.tertiary
        NotificationType.LIKE_POST -> Color(0xFFE91E63)
        NotificationType.COMMENT_POST -> MaterialTheme.colorScheme.secondary
        NotificationType.NEW_MESSAGE -> MaterialTheme.colorScheme.primary
        NotificationType.DAILY_REMINDER -> MaterialTheme.colorScheme.tertiary
        NotificationType.REWARD_EARNED -> Color(0xFFFFD700)
        NotificationType.GENERAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkAsRead() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
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
                // Icon theo loại thông báo
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Thông tin thông báo
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Indicator chưa đọc
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Nút hành động cho friend request
            if (notification.type == NotificationType.FRIEND_REQUEST && !notification.isRead) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRejectFriendRequest(notification.relatedId) },
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
                    Button(
                        onClick = { onAcceptFriendRequest(notification.relatedId) },
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

@Preview(
    name = "1. Light Mode - chua diem danh",
    group = "Rizz Card",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5 // Giả lập màu nền màn hình sáng
)
@Composable
fun PreviewRizzStatsRow_Light_NotAttended() {
    // Thay 'MaterialTheme' bằng Theme của app bạn (ví dụ: RizzTheme) để màu chuẩn nhất
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RizzStatsRow(
                points = 2212,
                streakDays = 7,
                attended = false, // Trạng thái: Chưa điểm danh (sẽ có viền + gợi ý click)
                onStreakClick = {}
            )
        }
    }
}

@Preview(
    name = "2. Dark Mode - da diem danh",
    group = "Rizz Card",
    uiMode = Configuration.UI_MODE_NIGHT_YES, // Chế độ tối
    showBackground = true,
    backgroundColor = 0xFF000000 // Giả lập màu nền màn hình tối
)
@Composable
fun PreviewRizzStatsRow_Dark_Attended() {
    MaterialTheme(colorScheme = darkColorScheme()) { // Force dark theme
        Box(modifier = Modifier.padding(16.dp)) {
            RizzStatsRow(
                points = 3500,
                streakDays = 12,
                attended = true, // Trạng thái: Đã điểm danh
                onStreakClick = {}
            )
        }
    }
}

@Preview(
    name = "3. Light Mode - da diem danh",
    group = "Rizz Card",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun PreviewRizzStatsRow_Light_Attended() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RizzStatsRow(
                points = 2212,
                streakDays = 8,
                attended = true,
                onStreakClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsDialogPreview() {
    val mockNotifications = listOf(
        Notification(
            id = "1",
            type = NotificationType.FRIEND_REQUEST,
            title = "Lời mời kết bạn",
            message = "Nguyễn Văn A muốn kết bạn với bạn",
            fromUsername = "Nguyễn Văn A",
            relatedId = "request1",
            isRead = false
        ),
        Notification(
            id = "2",
            type = NotificationType.FRIEND_REQUEST_ACCEPTED,
            title = "Lời mời kết bạn được chấp nhận",
            message = "Trần Thị B đã chấp nhận lời mời kết bạn của bạn!",
            fromUsername = "Trần Thị B",
            isRead = false
        ),
        Notification(
            id = "3",
            type = NotificationType.LIKE_POST,
            title = "Bài viết được thích",
            message = "Lê Văn C đã thích bài viết của bạn",
            fromUsername = "Lê Văn C",
            isRead = true
        )
    )
    NotificationsDialog(
        notifications = mockNotifications,
        onAcceptFriendRequest = {},
        onRejectFriendRequest = {},
        onMarkAsRead = {},
        onClearAll = {},
        onDismiss = {}
    )
}