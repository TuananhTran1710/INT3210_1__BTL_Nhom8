package com.example.wink.ui.features.chat

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.foundation.lazy.LazyRow // Import LazyRow
import androidx.compose.foundation.lazy.items // Import items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

// --- STATE MỚI: Lưu danh sách ảnh (List) ---
    // Sử dụng mutableStateListOf để Compose dễ dàng theo dõi thay đổi thêm/xóa
    val selectedImageUris = remember { mutableStateListOf<android.net.Uri>() }

    // Launcher chọn NHIỀU ảnh
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10) // Cho phép chọn tối đa 10 ảnh
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.addAll(uris)
        }
    }
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
                Column(modifier = Modifier.fillMaxWidth()) {

                    // --- PHẦN 1: PREVIEW DANH SÁCH ẢNH (LazyRow) ---
                    // --- PHẦN 1: PREVIEW DANH SÁCH ẢNH (LazyRow) ---
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp) // Tăng khoảng cách giữa các ảnh chút cho thoáng
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    // 1. Ảnh Thumbnail
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp)) // Bo góc 16dp cho mềm mại, hiện đại
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // Viền mờ nhẹ
                                    )

                                    // 2. Nút Xóa (Đã làm đẹp)
                                    Surface(
                                        onClick = { selectedImageUris.remove(uri) },
                                        shape = CircleShape,
                                        // Màu nền: Dùng màu Surface (thường là trắng hoặc xám đậm) pha chút trong suốt nhẹ
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        tonalElevation = 4.dp, // Đổ bóng để nút nổi lên
                                        shadowElevation = 4.dp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp) // Cách lề góc phải 6dp
                                            .size(28.dp)   // Kích thước nút to hơn chút cho dễ bấm
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            // Màu icon: Dùng màu tương phản với nền
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(6.dp) // Padding bên trong để chữ X nhỏ nhắn tinh tế
                                                .fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- PHẦN 2: THANH NHẬP LIỆU ---
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút chọn ảnh
                        IconButton(onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                            Icon(Icons.Default.Photo, contentDescription = "Add images")
                        }

                        // Input Text
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Hỏi Wink AI...") },
                            shape = CircleShape,
                            enabled = !isSending,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Nút Gửi
                        val canSend = (messageText.isNotBlank() || selectedImageUris.isNotEmpty()) && !isSending
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    // Gửi text và copy danh sách ảnh để gửi
                                    viewModel.sendMessage(messageText, selectedImageUris.toList())

                                    // Reset UI ngay lập tức
                                    messageText = ""
                                    selectedImageUris.clear()
                                }
                            },
                            enabled = canSend
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
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
                insightMessage = currentInsight,
                avatarUrl = avatarUri,
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