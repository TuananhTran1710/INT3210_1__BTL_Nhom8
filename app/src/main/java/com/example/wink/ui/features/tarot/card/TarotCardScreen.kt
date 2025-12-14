package com.example.wink.ui.features.tarot.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen

// Tỉ lệ ảnh: 1664 / 2880 ≈ 0.577
private const val CARD_ASPECT_RATIO = 0.577f
// Màu nền background: fcf4db
private val CARD_BG_COLOR = Color(0xFFFCF4DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardScreen(
    navController: NavController,
    viewModel: TarotCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Dialog xác nhận (Giữ nguyên)
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            icon = { Icon(Icons.Rounded.AutoAwesome, null) },
            title = { Text("Rút bài lại?") },
            text = { Text("Vũ trụ nói rằng bạn cần 50 RIZZ để thấu hiểu thêm một thông điệp nữa.") },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmUseRizz() }) { Text("Dùng 50 Rizz") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDialogs() }) { Text("Thôi") }
            }
        )
    }

    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Thiếu năng lượng") },
            text = { Text("Bạn không đủ RIZZ để thực hiện kết nối này.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onNotEnoughDialogHandled()
                    navController.popBackStack(Screen.TarotHub.route, inclusive = false)
                }) { Text("Quay về") }
            }
        )
    }

    // Background Gradient tối
    val bgColors = if (state.currentCard == null) {
        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
    } else {
        listOf(Color(0xFF240046), Color(0xFF10002B))
    }
    val bgBrush = Brush.verticalGradient(bgColors)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Thông Điệp Vũ Trụ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Cho phép cuộn để không bị vỡ layout
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (state.currentCard == null) Arrangement.Center else Arrangement.Top
            ) {

                if (state.currentCard != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- KHUNG HIỂN THỊ LÁ BÀI ---
                TarotCardView(
                    state = state,
                    onDrawClick = { viewModel.onDrawButtonClicked() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- GIẢI NGHĨA ---
                AnimatedVisibility(
                    visible = state.currentCard != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                ) {
                    state.currentCard?.let { card ->
                        TarotMeaningCard(card = card)
                    }
                }

                // Hướng dẫn khi chưa rút
                if (state.currentCard == null) {
                    Text(
                        "Hãy tập trung vào câu hỏi của bạn\nvà chạm vào lá bài",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút Rút lại
                if (state.currentCard != null) {
                    FilledTonalButton(
                        onClick = { viewModel.onDrawButtonClicked() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Rounded.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rút lá khác (50 Rizz)")
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun TarotCardView(
    state: TarotCardState,
    onDrawClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (state.currentCard != null) 180f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "cardFlip"
    )

    val isBackVisible = rotation < 90f

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(2000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.75f) // Chiếm 75% chiều rộng màn hình
            .aspectRatio(CARD_ASPECT_RATIO) // Tỉ lệ 1664/2880
            .scale(if (state.currentCard == null) scale else 1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = state.currentCard == null) {
                onDrawClick()
            }
    ) {
        if (isBackVisible) {
            CardBackDesign()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                if (state.currentCard != null) {
                    CardFrontDesign(card = state.currentCard)
                }
            }
        }
    }
}

@Composable
fun CardBackDesign() {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFD4AF37)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "WINK\nTAROT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )
        }
    }
}

// --- THIẾT KẾ MẶT TRƯỚC (Sửa lại theo yêu cầu) ---
@Composable
fun CardFrontDesign(card: TarotCardInfo?) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp), // Bo góc vừa phải như ảnh mẫu
        colors = CardDefaults.cardColors(containerColor = CARD_BG_COLOR), // Màu fcf4db
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp), // Padding để tạo viền màu kem bao quanh
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Phần ảnh (Chiếm phần lớn không gian)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Có thể thêm border mảnh bao quanh ảnh nếu muốn giống ảnh mẫu "The Fool"
                    .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
                    .padding(1.dp) // Khoảng cách nhỏ giữa border và ảnh
            ) {
                if (card != null) {
                    Image(
                        painter = painterResource(id = card.imageRes),
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(),
                        // ContentScale.Fit giúp thấy toàn bộ ảnh mà không bị cắt
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Phần Tên lá bài (Nằm dưới ảnh, màu đen, font đậm)
            Text(
                text = card?.name?.uppercase() ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun TarotMeaningCard(card: TarotCardInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lời Tiên Tri",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = card.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "\"${card.shortMeaning}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color(0xFF7B1FA2),
                textAlign = TextAlign.Center
            )

            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp),
                color = Color(0xFFBA68C8),
                thickness = 1.dp
            )

            Text(
                text = card.detail,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = Color(0xFF212121),
                lineHeight = 22.sp
            )
        }
    }
}