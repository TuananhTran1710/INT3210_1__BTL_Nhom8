package com.example.wink.ui.features.tarot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Star // 1. Import icon Ngôi sao
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is TarotHubNav.OpenFeature -> {
                    when (event.type) {
                        LoveFortuneType.BY_NAME ->
                            navController.navigate(Screen.TarotName.route)

                        LoveFortuneType.ZODIAC ->
                            navController.navigate(Screen.TarotZodiac.route)

                        LoveFortuneType.TAROT_CARD ->
                            navController.navigate(Screen.TarotCard.route)
                    }
                }
            }
        }
    }

    // Dialog: không đủ điểm Rizz
    if (state.showNotEnoughDialogFor != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm RIZZ để dùng chức năng này.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Dialog: xác nhận dùng Rizz
    state.confirmingFor?.let { type ->
        val feature = state.features.first { it.type == type }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Hết lượt miễn phí!") },
            text = {
                Text(
                    "Bạn đã dùng hết lượt miễn phí hôm nay.\n" +
                            "Dùng ${feature.price} Rizz để chơi tiếp nhé?"
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmSpendRizz() }) {
                    Text("Chốt đơn")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Thôi")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bói Tình Yêu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                // 2. THÊM PHẦN HIỂN THỊ ĐIỂM RIZZ TẠI ĐÂY
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700), // Màu vàng gold
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.rizzPoints} RIZZ", // Lấy điểm từ State
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.features.forEach { feature ->
                    LoveFortuneItem(
                        feature = feature,
                        onClick = { viewModel.onFeatureClick(feature.type) }
                    )
                }
            }
        }
    }
}

// ... (Phần LoveFortuneItem giữ nguyên như cũ)
@Composable
private fun LoveFortuneItem(
    feature: TarotSubFeatureUi,
    onClick: () -> Unit
) {
    // Gradient đổi theo theme nhưng vẫn khác nhau cho từng loại
    val colorScheme = MaterialTheme.colorScheme
    val gradient = when (feature.type) {
        LoveFortuneType.BY_NAME -> Brush.horizontalGradient(
            listOf(colorScheme.primary, colorScheme.secondary)
        )

        LoveFortuneType.ZODIAC -> Brush.horizontalGradient(
            listOf(colorScheme.tertiary, colorScheme.primaryContainer)
        )

        LoveFortuneType.TAROT_CARD -> Brush.horizontalGradient(
            listOf(colorScheme.secondary, colorScheme.tertiaryContainer)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon tròn bên trái
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❤", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = feature.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.description,
                        fontSize = 13.sp,
                        color = colorScheme.onPrimary.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge: "Miễn phí" hoặc "Rizz"
                val badgeColor =
                    if (feature.usedFreeToday) colorScheme.secondaryContainer
                    else colorScheme.primaryContainer
                val badgeTextColor =
                    if (feature.usedFreeToday) colorScheme.onSecondaryContainer
                    else colorScheme.onPrimaryContainer

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = if (feature.usedFreeToday) "${feature.price} Rizz" else "Miễn phí",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }
        }
    }
}