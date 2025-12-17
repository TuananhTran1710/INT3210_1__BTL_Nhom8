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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    var clickedImageUrl by remember { mutableStateOf<String?>(null) }
    val selectedImageUris = remember { mutableStateListOf<android.net.Uri>() }

    // Launcher chọn NHIỀU ảnh
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10) // Cho phép chọn tối đa 10 ảnh
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.addAll(uris)
        }
    }

    val onNext: () -> Unit = {
        if (currentStepIndex < analysisSteps.size - 1) {
            currentStepIndex++
        }
    }
    // Hàm xử lý khi bấm nút "Previous" (lên)
    val onPrev: () -> Unit = {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    val onFinish: () -> Unit = {
        currentStepIndex = -1 // Reset chỉ số để ẩn highlight và các nút
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "android.resource://${context.packageName}/${R.drawable.love_background}", // << THAY TÊN ẢNH Ở ĐÂY
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    tonalElevation = 4.dp
                ) {
                    MessageTopBar(
                        title = "Lan Anh",
                        avatarUrl = avatarUri,
                        onBackClick = { navController.popBackStack() },
                        onAnalyzeClick = {
                            showAnalyzeDialog = true
                            viewModel.analyzeConversation()
                        },
                        showAnalyzeButton = true,
                        onSettingsClick = {
                            navController.navigate("ai_settings")
                        },
                        showSettingsButton = true,
                    )
                }
            },
            bottomBar = {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(
                                                        alpha = 0.5f
                                                    ),
                                                    RoundedCornerShape(16.dp)
                                                ) // Viền mờ nhẹ
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
                            val canSend =
                                (messageText.isNotBlank() || selectedImageUris.isNotEmpty()) && !isSending
                            IconButton(
                                onClick = {
                                    if (canSend) {
                                        // Gửi text và copy danh sách ảnh để gửi
                                        viewModel.sendMessage(
                                            messageText,
                                            selectedImageUris.toList()
                                        )

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
            },
            floatingActionButton = {
                val isLastStep = currentStepIndex != -1 && currentStepIndex == analysisSteps.size - 1
                AnalysisNavigation(
                    isVisible = currentStepIndex != -1,
                    onNext = onNext,
                    onPrev = onPrev,
                    onFinish = onFinish,
                    isLastStep = isLastStep
                )
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
                    // --- TRUYỀN CALLBACK VÀO ĐÂY ---
                    onImageClick = { url ->
                        clickedImageUrl = url
                    }
                )
            }
        }
    }

    if (showAnalyzeDialog) {
        AnalyzeDialog(
            isLoading = isAnalyzing,
            score = analyzeResult?.score,
            onDismiss = {
                showAnalyzeDialog = false
                onFinish() // onFinish resets the step index
            },
            onStartAnalysis = {
                showAnalyzeDialog = false
                // Start the analysis sequence
                if (analysisSteps.isNotEmpty()) {
                    currentStepIndex = 0
                }
            }
        )
    }

    // --- HIỂN THỊ ẢNH FULL MÀN HÌNH ---
    if (clickedImageUrl != null) {
        FullScreenImageViewer(
            imageUrl = clickedImageUrl!!,
            onDismiss = { clickedImageUrl = null }
        )
    }
}

@Composable
private fun AnalysisNavigation(
    isVisible: Boolean,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onFinish: () -> Unit
) {
    if (isVisible) {
        // Sử dụng Column để xếp hai nút dọc theo chiều từ trên xuống
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa 2 nút
        ) {
            // Nút Lên (Previous)
            FloatingActionButton(
                onClick = onPrev,
                modifier = Modifier.size(40.dp), // Kích thước nhỏ hơn, mỏng hơn
                shape = CircleShape // Đảm bảo nút luôn tròn
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Step")
            }

            // Nút Xuống (Next) hoặc Đóng (Finish)
            FloatingActionButton(
                // Nếu là bước cuối, gọi onFinish, ngược lại gọi onNext
                onClick = { if (isLastStep) onFinish() else onNext() },
                modifier = Modifier.size(40.dp), // Kích thước nhỏ hơn, mỏng hơn
                shape = CircleShape
            ) {
                // Nếu là bước cuối, hiển thị icon 'X', ngược lại hiển thị mũi tên xuống
                if (isLastStep) {
                    Icon(Icons.Default.Close, contentDescription = "Finish Analysis")
                } else {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Step")
                }
            }
        }
    }
}
// --- KẾT THÚC PHẦN THÊM MỚI ---