package com.example.wink.ui.features.tarot.name.results

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
fun TarotNameResultScreen(
    navController: NavController,
    yourName: String,
    crushName: String,
    viewModel: TarotNameResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Khởi tạo dữ liệu lần đầu
    LaunchedEffect(yourName, crushName) {
        viewModel.init(yourName, crushName)
    }

    // Lắng nghe nav events (quay về hub / quay lại màn nhập)
    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                TarotNameResultNav.GoBackToHub -> {
                    navController.popBackStack(Screen.TarotHub.route, false)
                }
                TarotNameResultNav.GoBackToInput -> {
                    // pop 1 màn -> quay lại TarotNameScreen
                    navController.popBackStack()
                }
            }
        }
    }

    // Dialog xác nhận trừ 50 Rizz
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Dùng 50 Rizz để chơi tiếp?") },
            text = { Text("Bạn muốn thử lại một lần nữa với 50 Rizz chứ?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRetry() },
                    enabled = !state.isProcessing
                ) {
                    Text("Chốt đơn")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDialogs() },
                    enabled = !state.isProcessing
                ) {
                    Text("Thôi")
                }
            }
        )
    }

    // Dialog không đủ điểm Rizz
    if (state.showNotEnoughDialog) {
        AlertDialog(
            onDismissRequest = { /* không cho dismiss ngoài */ },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm RIZZ để thử lại. Hẹn bạn lần sau nhé!") },
            confirmButton = {
                Button(onClick = { viewModel.backToHubFromNotEnough() }) {
                    Text("Quay về")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bói Theo Tên", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // back trên thanh app bar -> về hub luôn
                            navController.popBackStack(Screen.TarotHub.route, false)
                        }
                    ) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${state.score}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${state.yourName} & ${state.crushName}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onRetryClick() },
                enabled = !state.isProcessing
            ) {
                Text("Thử lại")
            }
        }
    }
}
