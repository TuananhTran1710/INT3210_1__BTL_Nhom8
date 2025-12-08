package com.example.wink.ui.features.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.data.model.Tip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    navController: NavController,
    viewModel: TipsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Lắng nghe sự kiện điều hướng (Khi mở bí kíp thành công)
    LaunchedEffect(true) {
        viewModel.navigationEvent.collect { tipId ->
            // Tìm object Tip tương ứng với ID (ViewModel chỉ gửi ID, ta cần tìm object đầy đủ)
            // Tuy nhiên, để nhanh gọn, ta sửa ViewModel gửi nguyên object Tip luôn (Xem Bước 2 phụ bên dưới)
            // HOẶC tìm trong list hiện tại:
            val selectedTip = state.tips.find { it.id == tipId }

            if (selectedTip != null) {
                // 1. Nhét dữ liệu vào túi
                navController.currentBackStackEntry?.savedStateHandle?.set("selectedTip", selectedTip)
                // 2. Đi tới màn chi tiết
                navController.navigate("tip_detail_screen")
            }
        }
    }

    // --- DIALOG XÁC NHẬN MỞ KHÓA ---
    if (state.selectedTipToUnlock != null) {
        UnlockDialog(
            tip = state.selectedTipToUnlock!!,
            userPoints = state.userRizzPoints,
            error = state.unlockError,
            onConfirm = { viewModel.confirmUnlock() },
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kho Bí Kíp", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    // Hiển thị điểm RIZZ hiện tại
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.userRizzPoints} RIZZ",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(state.tips) { tip ->
                TipCard(tip = tip, onClick = { viewModel.onTipClick(tip) })
            }
        }
    }
}

@Composable
fun TipCard(tip: Tip, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        // Nếu khóa thì nền xám, mở thì nền trắng/surface
        colors = CardDefaults.cardColors(
            containerColor = if (tip.isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Để chiều cao Row tự co giãn đều
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. THUMBNAIL AREA ---
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị ảnh nếu có URL
                if (!tip.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(tip.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Nếu KHÓA -> Phủ lớp đen mờ + Icon Khóa
                if (tip.isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)) // Mờ đen
                    )
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(32.dp)
                    )
                } else if (tip.imageUrl.isNullOrBlank()) {
                    // Nếu không có ảnh và đã mở khóa -> Hiện icon sách mặc định
                    Icon(
                        imageVector = Icons.Default.MenuBook, // Hoặc icon nào đó
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // --- 2. TEXT CONTENT AREA ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tip.isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: Giá tiền hoặc Mũi tên
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tip.isLocked) {
                        Text(
                            text = "${tip.price} RIZZ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Đọc ngay",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockDialog(
    tip: Tip,
    userPoints: Int,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        title = { Text("Mở khóa Bí kíp?") },
        text = {
            Column {
                Text("Bạn có muốn dùng ${tip.price} điểm RIZZ để mở khóa bài học: \"${tip.title}\" không?")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Điểm hiện tại: $userPoints RIZZ", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Mở khóa ngay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Để sau")
            }
        }
    )
}