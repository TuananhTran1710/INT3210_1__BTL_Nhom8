package com.example.wink.ui.features.tarot.zodiac.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen
import com.example.wink.ui.theme.WinkTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotZodiacResultScreen(
    navController: NavController,
    viewModel: TarotZodiacResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

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

    // Dialogs logic remains the same
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            title = { Text("Thử lại lần nữa?") },
            text = { Text("Dùng 50 Rizz để bói lại cho cặp đôi khác nhé?") },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmUseRizz() }) { Text("Chốt đơn") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDialogs() }) { Text("Thôi") }
            },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
        )
    }

    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = { /* */ },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm Rizz để chơi tiếp. Hẹn bạn lần sau nhé!") },
            confirmButton = {
                Button(onClick = { viewModel.onNotEnoughOk() }) { Text("Quay về hub") }
            }
        )
    }

    // Gradient Background (Consistent with Zodiac Theme)
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
                    IconButton(onClick = { viewModel.onBackClicked() }) {
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Animated Score Section
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // Styled Name Text
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

                        // Score Display
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
                                color = MaterialTheme.colorScheme.secondary // Consistent with secondary theme color
                            )
                        }
                    }
                }

                // 2. Advice Card
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

                // 3. Action Buttons
                Button(
                    onClick = { viewModel.onRetryClicked() },
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
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Thử lại",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thử cặp đôi khác",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// #region PREVIEW IMPLEMENTATION

private data class PreviewTarotZodiacResultState(
    val score: Int = 0,
    val yourSignName: String = "Bạch Dương",
    val crushSignName: String = "Thiên Yết",
    val message: String = "Kết quả bói cho thấy đây là một mối quan hệ đầy đam mê và thử thách. Cả hai đều có cá tính mạnh mẽ, cần học cách thỏa hiệp để tránh xung đột.",
    val showConfirmDialog: Boolean = false,
    val showNotEnoughDialog: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewTarotZodiacResultContent(
    state: PreviewTarotZodiacResultState,
) {
    // Replicating the main composable structure for preview
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
                title = { Text("Kết quả Cung Hoàng Đạo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "${state.yourSignName} & ${state.crushSignName}",
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
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TarotZodiacResultScreenPreview() {
    WinkTheme {
        PreviewTarotZodiacResultContent(
            state = PreviewTarotZodiacResultState(score = 75)
        )
    }
}
// #endregion PREVIEW IMPLEMENTATION