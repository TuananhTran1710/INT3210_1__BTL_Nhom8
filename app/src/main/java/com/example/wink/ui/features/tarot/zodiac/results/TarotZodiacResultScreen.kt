package com.example.wink.ui.features.tarot.zodiac.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

// --- CONTAINER ---
@Composable
fun TarotZodiacResultScreen(
    navController: NavController,
    viewModel: TarotZodiacResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TarotZodiacResultEvent.BackToHub -> {
                    navController.popBackStack(Screen.TarotHub.route, inclusive = false)
                }
                is TarotZodiacResultEvent.RetryPaid -> {
                    navController.navigate(Screen.TarotZodiac.route) {
                        popUpTo(Screen.TarotHub.route) { inclusive = false }
                    }
                }
            }
        }
    }

    TarotZodiacResultScreenContent(
        state = state,
        onBackClick = { viewModel.onBackClicked() },
        onRetryClick = { viewModel.onRetryClicked() },
        onConfirmUseRizz = { viewModel.onConfirmUseRizz() },
        onDismissDialogs = { viewModel.onDismissDialogs() },
        onNotEnoughOk = { viewModel.onNotEnoughOk() }
    )
}

// --- CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotZodiacResultScreenContent(
    state: TarotZodiacResultState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onConfirmUseRizz: () -> Unit,
    onDismissDialogs: () -> Unit,
    onNotEnoughOk: () -> Unit
) {
    // Dialogs
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            title = { Text("Thử lại lần nữa?") },
            text = { Text("Dùng 5 Rizz để bói lại cho cặp đôi khác nhé?") },
            confirmButton = {
                Button(onClick = onConfirmUseRizz) { Text("Chốt đơn") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialogs) { Text("Thôi") }
            },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
        )
    }

    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm Rizz để chơi tiếp. Hẹn bạn lần sau nhé!") },
            confirmButton = {
                Button(onClick = onNotEnoughOk) { Text("Quay về hub") }
            }
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Kết Quả Cung Hoàng Đạo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)) {
                                    append(state.yourSignName)
                                }
                                append("  &  ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)) {
                                    append(state.crushSignName)
                                }
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(160.dp)
                            ) {}

                            Text(
                                text = "${state.score}%",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 64.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(1500)) + slideInVertically(tween(1500), initialOffsetY = { 50 })
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lời Tiên Tri Tình Yêu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .width(60.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 2.dp
                            )

                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Quay về trang chủ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}