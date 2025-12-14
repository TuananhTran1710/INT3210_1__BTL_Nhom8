package com.example.wink.ui.features.tarot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotHubScreen(
    navController: NavController,
    viewModel: TarotHubViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // --- LOGIC (Giữ nguyên) ---
    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is TarotHubNav.OpenFeature -> {
                    when (event.type) {
                        LoveFortuneType.BY_NAME -> navController.navigate(Screen.TarotName.route)
                        LoveFortuneType.ZODIAC -> navController.navigate(Screen.TarotZodiac.route)
                        LoveFortuneType.TAROT_CARD -> navController.navigate(Screen.TarotCard.route)
                    }
                }
            }
        }
    }

    if (state.showNotEnoughDialogFor != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn cần thêm điểm RIZZ để mở khóa tính năng này.") },
            confirmButton = { TextButton(onClick = { viewModel.dismissDialogs() }) { Text("Đóng") } },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )
    }

    state.confirmingFor?.let { type ->
        val feature = state.features.first { it.type == type }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Mở khóa tính năng") },
            text = { Text("Bạn đã hết lượt miễn phí. Dùng ${feature.price} Rizz để tiếp tục nhé?") },
            confirmButton = { Button(onClick = { viewModel.confirmSpendRizz() }) { Text("Chốt đơn") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissDialogs() }) { Text("Thôi") } },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
        )
    }

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bói Tình Yêu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    RizzChip(amount = state.rizzPoints)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Khám phá vận mệnh",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )

            // Render danh sách các thẻ
            state.features.forEach { feature ->
                LoveFortuneItemVibrant(
                    feature = feature,
                    onClick = { viewModel.onFeatureClick(feature.type) }
                )
            }
        }
    }
}

@Composable
fun RizzChip(amount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = CircleShape,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$amount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun LoveFortuneItemVibrant(
    feature: TarotSubFeatureUi,
    onClick: () -> Unit
) {
    // 1. Định nghĩa màu Gradient ĐẬM và NỔI BẬT cho từng loại
    val gradientBrush = when (feature.type) {
        LoveFortuneType.BY_NAME -> Brush.linearGradient(
            listOf(Color(0xFFD81B60), Color(0xFF8E24AA)) // Hồng đậm -> Tím
        )
        LoveFortuneType.ZODIAC -> Brush.linearGradient(
            listOf(Color(0xFF1E88E5), Color(0xFF004D40)) // Xanh dương -> Xanh rêu đậm
        )
        LoveFortuneType.TAROT_CARD -> Brush.linearGradient(
            listOf(Color(0xFFFF6F00), Color(0xFFBF360C)) // Cam hổ phách -> Cam cháy
        )
    }

    val iconVector = when (feature.type) {
        LoveFortuneType.BY_NAME -> Icons.Filled.Person
        LoveFortuneType.ZODIAC -> Icons.Filled.Star
        LoveFortuneType.TAROT_CARD -> Icons.Filled.AutoAwesome
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = MaterialTheme.shapes.large, // Bo góc lớn hơn chút (Large thay vì Medium)
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp), // Tăng độ nổi
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent // Để gradient hiển thị
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Container - Nền trắng mờ để nổi trên background đậm
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White, // Icon màu trắng
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Content - Màu trắng để tương phản
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, // Chữ đậm hơn
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f), // Màu trắng hơi mờ
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Nút trạng thái (Free/Locked) - Góc dưới phải
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                StatusBadgeVibrant(isLocked = feature.usedFreeToday, price = feature.price)
            }

            // Lớp phủ khi bị khóa (Overlay tối)
            if (feature.usedFreeToday) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)) // Phủ tối hơn chút để rõ trạng thái khóa
                )
            }
        }
    }
}

@Composable
fun StatusBadgeVibrant(isLocked: Boolean, price: Int) {
    // Màu badge tương phản với nền gradient
    val containerColor = if (isLocked)
        Color.Black.copy(alpha = 0.6f) // Nền đen mờ cho trạng thái khóa
    else
        Color.White // Nền trắng cho trạng thái Free

    val contentColor = if (isLocked)
        Color(0xFFFF5252) // Chữ đỏ cam
    else
        Color(0xFF2E7D32) // Chữ xanh lá đậm (Hoặc dùng màu chính của app)

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$price Rizz", // Rút gọn text
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
            } else {
                Text(
                    text = "MIỄN PHÍ", // Chữ in hoa
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor // Hoặc dùng màu Brand của bạn
                )
            }
        }
    }
}