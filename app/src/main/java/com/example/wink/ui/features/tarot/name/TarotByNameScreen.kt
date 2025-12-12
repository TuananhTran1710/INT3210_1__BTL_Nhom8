package com.example.wink.ui.features.tarot.name

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotNameScreen(
    navController: NavController,
    viewModel: TarotNameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Lắng nghe yêu cầu điều hướng sang màn kết quả
    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is TarotNameNav.ShowResult -> {
                    navController.navigate(
                        Screen.TarotNameResult.buildRoute(
                            event.yourName,
                            event.crushName
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bói Theo Tên", fontWeight = FontWeight.Bold) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.yourName,
                onValueChange = viewModel::onYourNameChange,
                label = { Text("Tên của bạn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.crushName,
                onValueChange = viewModel::onCrushNameChange,
                label = { Text("Tên crush") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { viewModel.onAnalyze() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading // Disable khi đang load
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Xem kết quả")
                }
            }
        }
    }
}
