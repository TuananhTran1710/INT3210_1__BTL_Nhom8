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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
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

// --- PHẦN 1: STATEFUL COMPOSABLE (Logic kết nối ViewModel) ---
@Composable
fun TarotCardScreen(
    navController: NavController,
    viewModel: TarotCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Gọi hàm Content và truyền các sự kiện (Lambda) xuống
    TarotCardScreenContent(
        state = state,
        onBackClick = { navController.popBackStack() },
        onReturnHomeClick = {
            navController.popBackStack(Screen.TarotHub.route, inclusive = false)
        },
        onDrawClick = { viewModel.onDrawButtonClicked() },
        onConfirmUseRizz = { viewModel.onConfirmUseRizz() },
        onDismissDialogs = { viewModel.onDismissDialogs() },
        onNotEnoughDialogHandled = {
            viewModel.onNotEnoughDialogHandled()
            navController.popBackStack(Screen.TarotHub.route, inclusive = false)
        }
    )
}

// --- PHẦN 2: STATELESS COMPOSABLE (Chỉ hiển thị UI - Dùng để Test) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardScreenContent(
    state: TarotCardState,
    onBackClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    onDrawClick: () -> Unit,
    onConfirmUseRizz: () -> Unit,
    onDismissDialogs: () -> Unit,
    onNotEnoughDialogHandled: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Dialogs Logic
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            icon = { Icon(Icons.Rounded.AutoAwesome, null) },
            title = { Text("Rút bài lại?") },
            text = { Text("Vũ trụ nói rằng bạn cần 5 RIZZ để thấu hiểu thêm một thông điệp nữa.") },
            confirmButton = {
                Button(onClick = onConfirmUseRizz) { Text("Dùng 5 Rizz") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialogs) { Text("Thôi") }
            }
        )
    }

    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = {}, // Chặn tắt ngang xương nếu muốn
            title = { Text("Thiếu năng lượng") },
            text = { Text("Bạn không đủ RIZZ để thực hiện kết nối này.") },
            confirmButton = {
                Button(onClick = onNotEnoughDialogHandled) { Text("Quay về") }
            }
        )
    }

    // Background Gradient Logic
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
                    IconButton(onClick = onBackClick) {
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
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (state.currentCard == null) Arrangement.Center else Arrangement.Top
            ) {

                if (state.currentCard != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TarotCardView(
                    state = state,
                    onDrawClick = onDrawClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = state.currentCard != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                ) {
                    state.currentCard?.let { card ->
                        TarotMeaningCard(card = card)
                    }
                }

                if (state.currentCard == null) {
                    Text(
                        "Chạm vào lá bài",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.currentCard != null) {
                    FilledTonalButton(
                        onClick = onReturnHomeClick,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay về",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Quay về trang chủ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// --- CÁC COMPOSABLE CON GIỮ NGUYÊN ---

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
            .fillMaxWidth(0.75f)
            .aspectRatio(CARD_ASPECT_RATIO)
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

@Composable
fun CardFrontDesign(card: TarotCardInfo?) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG_COLOR),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
                    .padding(1.dp)
            ) {
                if (card != null) {
                    Image(
                        painter = painterResource(id = card.imageRes),
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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