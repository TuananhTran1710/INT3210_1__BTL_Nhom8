package com.example.wink.ui.features.tarot.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardScreen(
    navController: NavController,
    viewModel: TarotCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog xác nhận dùng 50 Rizz
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            title = { Text("Hết lượt miễn phí!") },
            text = { Text("Dùng 50 Rizz để rút lại một lá bài khác nhé?") },
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

    // Dialog không đủ Rizz -> quay về hub
    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = { /* không cho bấm ra ngoài */ },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm Rizz để rút tiếp. Hẹn bạn lần sau nhé!") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onNotEnoughDialogHandled()
                    navController.popBackStack(Screen.TarotHub.route, inclusive = false)
                }) {
                    Text("Quay về hub")
                }
            },
            dismissButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bói bài tây", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ====== CARD TAROT ======
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable(enabled = !state.isLoading) {
                        // Chạm vào lá bài cũng coi như bấm nút rút
                        viewModel.onDrawButtonClicked()
                    },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    when {
                        state.isLoading -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Vũ trụ đang shuffle bài cho bạn...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        state.error != null -> {
                            Text(
                                text = state.error ?: "Đã có lỗi xảy ra",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        state.currentCard == null -> {
                            Text(
                                text = "Nhấn \"Rút bài\" hoặc chạm vào lá bài\nđể xem thông điệp hôm nay 💫",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            val card = state.currentCard!!
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = card.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = card.shortMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = card.detail,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====== NÚT ACTION (một nút duy nhất, căn giữa) ======
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    enabled = !state.isLoading,
                    onClick = { viewModel.onDrawButtonClicked() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)   // ~60% chiều rộng, nhìn gọn
                        .height(48.dp)
                ) {
                    Text(
                        text = if (state.currentCard == null) "Rút bài" else "Rút lại"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
