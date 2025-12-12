package com.example.wink.ui.features.tarot.zodiac.results

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotZodiacResultScreen(
    navController: NavController,
    viewModel: TarotZodiacResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Lắng nghe event điều hướng
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                TarotZodiacResultEvent.BackToHub -> {
                    navController.popBackStack(Screen.TarotHub.route, inclusive = false)
                }
                TarotZodiacResultEvent.RetryPaid -> {
                    navController.navigate(Screen.TarotZodiac.route) {
                        popUpTo(Screen.TarotHub.route) { inclusive = false }
                    }
                }
            }
        }
    }

    // Dialog xác nhận dùng 50 Rizz
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            title = { Text("Hết lượt miễn phí!") },
            text = { Text("Dùng 50 Rizz để thử lại lần nữa nhé?") },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmUseRizz() }) {
                    Text("Chốt đơn")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDialogs() }) {
                    Text("Thôi")
                }
            }
        )
    }

    // Dialog không đủ Rizz
    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = { /* không cho dismiss ngoài */ },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm Rizz để chơi tiếp. Hẹn bạn lần sau nhé!") },
            confirmButton = {
                Button(onClick = { viewModel.onNotEnoughOk() }) {
                    Text("Quay về hub")
                }
            },
            dismissButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cung Hoàng Đạo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onBackClicked() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${state.score}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${state.yourSignName} - ${state.crushSignName}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onRetryClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Chọn lại")
            }
        }
    }
}
