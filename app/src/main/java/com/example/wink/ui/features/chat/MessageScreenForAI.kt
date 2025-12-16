package com.example.wink.ui.features.chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.wink.R
import kotlinx.coroutines.delay


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
    val context = LocalContext.current
    val avatarUri = "android.resource://${context.packageName}/${R.drawable.ai_crush}"
    var currentStepIndex by remember { mutableStateOf(-1) } // Chỉ số message đang highlight
    val analysisSteps by viewModel.analysisSteps.collectAsState() // Kết quả phân tích
    val listState = rememberLazyListState()


    Scaffold(
        topBar = {
            MessageTopBar(
                title = "Lan Anh",
                avatarUrl = avatarUri, // Có thể thêm URL ảnh robot vào đây
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
            val currentHighlightMessageId = analysisSteps.getOrNull(currentStepIndex)?.messageId
            val currentInsight = analysisSteps.getOrNull(currentStepIndex)?.insight

            MessageContainer(
                messages = messages,
                currentUserId = currentUserId,
                listState = listState,
                modifier = Modifier.padding(paddingValues),
                isTyping = isSending,
                highlightMessageId = currentHighlightMessageId,
                insightMessage = currentInsight
            )
        }
    }

    LaunchedEffect(isAnalyzing) {
        // Chỉ kích hoạt khi việc phân tích VỪA MỚI KẾT THÚC
        Log.d("ANALYSIS_DEBUG", "[UI] Effect(isAnalyzing) Triggered | isAnalyzing = $isAnalyzing | analysisSteps.size = ${analysisSteps.size}")
        if (!isAnalyzing && analysisSteps.isNotEmpty()) {
            // Bắt đầu chuỗi highlight bằng cách đặt index về 0
            Log.d("ANALYSIS_DEBUG", "[UI] --> Condition MET. Starting sequence by setting currentStepIndex = 0.")
            currentStepIndex = 0
        }
    }

    LaunchedEffect(currentStepIndex) {
        // 2. Nếu đang ở một bước highlight hợp lệ (không phải -1)
        Log.d("ANALYSIS_DEBUG", "[UI] Effect(currentStepIndex) Triggered | currentStepIndex = $currentStepIndex")
        if (currentStepIndex >= 0 && currentStepIndex < analysisSteps.size) {
            // Scroll tới item
//            val msgId = analysisSteps[currentStepIndex].messageId
//            Log.d("ANALYSIS_DEBUG", "[UI] ---> Step $currentStepIndex: Attempting to find message with ID: $msgId")
//            val indexInList = messages.indexOfFirst { it.messageId == msgId }
//            Log.d("ANALYSIS_DEBUG", "[UI] ---> Step $currentStepIndex: Search Result -> indexInList = $indexInList")
//            if (indexInList != -1) {
//                listState.animateScrollToItem(indexInList)
//            }

            // 3. Đợi 1.5 giây
            delay(5500)
            Log.d("ANALYSIS_DEBUG", "[UI] ---> Step $currentStepIndex: Delay finished. Incrementing index to ${currentStepIndex + 1}")

            // 4. Tự động chuyển sang bước tiếp theo
            currentStepIndex++
        }
        // 5. Nếu currentStepIndex vượt quá giới hạn, reset lại
        else if (currentStepIndex >= analysisSteps.size && analysisSteps.isNotEmpty()) {
            delay(500) // Đợi chút trước khi tắt highlight cuối cùng
            currentStepIndex = -1 // Kết thúc chuỗi, xóa mọi highlight
        }
    }

    if (showAnalyzeDialog) {
        AnalyzeDialog(
            isLoading = isAnalyzing,
//            result = analyzeResult,
            onDismiss = { showAnalyzeDialog = false }
        )
    }
}