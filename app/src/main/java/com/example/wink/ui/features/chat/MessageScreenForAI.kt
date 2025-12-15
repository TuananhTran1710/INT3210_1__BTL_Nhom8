package com.example.wink.ui.features.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MessageScreenForAI(
    navController: NavController,
    viewModel: MessageViewModelForAI = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var showAnalyzeDialog by remember { mutableStateOf(false) }

    val analyzeResult by viewModel.analyzeResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    Scaffold(
        topBar = {
            MessageTopBar(
                title = "Wink AI",
                avatarUrl = null, // Có thể thêm URL ảnh robot vào đây
                onBackClick = { navController.popBackStack() },
                onAnalyzeClick = {
                    showAnalyzeDialog = true
                    viewModel.analyzeConversation()
                },
                showAnalyzeButton = true
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Photo, contentDescription = "Add image")
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Hỏi Wink AI...") },
                        shape = CircleShape,
                        enabled = !isSending
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank() && !isSending
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Tái sử dụng MessageContainer đã được update ở trên
            MessageContainer(
                messages = messages,
                currentUserId = currentUserId,
                modifier = Modifier.padding(paddingValues),
                isTyping = isSending
            )
        }
    }
    if (showAnalyzeDialog) {
        AnalyzeDialog(
            isLoading = isAnalyzing,
            result = analyzeResult,
            onDismiss = { showAnalyzeDialog = false }
        )
    }
}