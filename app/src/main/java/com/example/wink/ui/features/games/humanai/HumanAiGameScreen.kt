package com.example.wink.ui.features.games.humanai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.data.model.Message
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HumanAiGameScreen(
    navController: NavController,
    viewModel: HumanAiGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val myUserId = remember { viewModel.getMyUserId() }

    // Sử dụng AnimatedContent để chuyển đổi giữa các màn hình mượt mà hơn
    AnimatedContent(
        targetState = state.stage,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "GameStageTransition"
    ) { targetStage ->
        when (targetStage) {
            GameStage.LOBBY -> LobbyView(
                rizz = state.currentRizz,
                online = state.onlineUsers,
                onStart = viewModel::onStartMatchmaking,
                onBack = { navController.popBackStack() }
            )
            GameStage.SEARCHING -> SearchingView(
                time = state.searchTimeSeconds,
                online = state.onlineUsers,
                onCancel = viewModel::onCancelMatchmaking
            )
            GameStage.CHATTING -> ChattingScreenLayout( // Layout riêng cho Chat để xử lý bàn phím chuẩn
                state = state,
                currentUserId = myUserId,
                onSend = viewModel::sendMessage
            )
            GameStage.GUESSING -> GuessingView(
                onGuess = viewModel::onGuess
            )
            GameStage.RESULT -> ResultView(
                state = state,
                onPlayAgain = viewModel::onPlayAgain,
                onExit = { navController.popBackStack() }
            )
        }
    }
}

// ==========================================
// 1. LOBBY VIEW
// ==========================================
@Composable
fun LobbyView(rizz: Int, online: Int, onStart: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar thủ công
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onBack,modifier = Modifier.testTag("lobby_back_button")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = {},
                    label = { Text("$rizz RIZZ", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Rounded.Bolt, null, tint = Color(0xFFFFD700)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = null
                )
            }

            Spacer(Modifier.weight(1f))

            // Hero Image / Icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
                Icon(
                    imageVector = Icons.Rounded.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Human or AI?",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Nội dung hướng dẫn
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Trò chuyện với ai đó trong 2 phút.\nHãy thử đoán xem bạn vừa nhắn với một người thật hay một AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            // Online count pill
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(8.dp).background(Color.Green, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$online đang tìm trận",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Start Button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .testTag("start_game_button")
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("BẮT ĐẦU CHƠI", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.PlayArrow, null)
            }
        }
    }
}

// ==========================================
// 2. SEARCHING VIEW
// ==========================================
@Composable
fun SearchingView(time: Int, online: Int, onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    val formattedTime = remember(time) {
        String.format(Locale.getDefault(), "%02d:%02d", time / 60, time % 60)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Pulse Circles
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale * 0.9f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                )
                // Center Icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Text("Đang tìm đối thủ...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(formattedTime, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            // Online count pill
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(8.dp).background(Color.Green, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$online đang tìm trận",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Hủy tìm kiếm")
            }
        }
    }
}

// ==========================================
// 3. CHATTING SCREEN (FIXED KEYBOARD LAYOUT)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChattingScreenLayout(state: HumanAiGameState, currentUserId: String, onSend: (String) -> Unit) {
    // Scaffold tự động xử lý system bars và bàn phím (nếu config đúng trong theme/manifest)
    // Nhưng để chắc chắn, ta dùng BottomBar cho Input Field

    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf("") }

    val formattedTimer = remember(state.timeLeft) {
        String.format(Locale.getDefault(), "%02d:%02d", state.timeLeft / 60, state.timeLeft % 60)
    }

    val timerColor = if (state.timeLeft < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = timerColor,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = formattedTimer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // INPUT AREA: Đặt ở bottomBar để Scaffold xử lý vị trí khi bàn phím hiện
            ChatInputBar(
                text = text,
                onTextChange = { text = it },
                onSend = {
                    onSend(text)
                    text = ""
                    focusManager.clearFocus() // Ẩn phím sau khi gửi (tùy chọn)
                },
                isEnabled = state.isMyTurn
            )
        }
    ) { padding ->
        // CHAT CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Padding này bao gồm cả topBar và bottomBar (Input)
        ) {
            // Thanh Progress mảnh
            LinearProgressIndicator(
                progress = { state.timeLeft / 60f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = if (state.timeLeft < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )

            GameMessageList(
                messages = state.messages,
                currentUserId = currentUserId,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    Surface(
        tonalElevation = 2.dp,
        // Dùng windowInsets để tự động cộng padding của navigation bar và bàn phím
        // Đây là "bí thuật" để fix lỗi khoảng trống
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                // Quan trọng: Chỉ thêm padding cho navigation bars (thanh gạch ngang dưới đáy),
                // còn bàn phím (ime) thì Scaffold đã lo rồi.
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (isEnabled) "Nhập tin nhắn..." else "Đợi đối phương...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(24.dp),
                enabled = isEnabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (text.isNotBlank() && isEnabled) onSend() })
            )

            Spacer(Modifier.width(12.dp))

            val canSend = text.isNotBlank() && isEnabled
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_button")
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                enabled = canSend
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    null,
                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ... (GameMessageList, GameChatBubble, SystemMessageItem - GIỮ NGUYÊN hoặc copy từ code cũ) ...
@Composable
fun GameMessageList(
    messages: List<Message>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            if (message.senderId == "system" || message.senderId == "sys_init" || message.senderId == "sys_ai") {
                SystemMessageItem(content = message.content)
            } else {
                GameChatBubble(
                    message = message,
                    isMe = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
fun GameChatBubble(message: Message, isMe: Boolean) {
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    // Bo góc kiểu bong bóng chat
    val shape = if (isMe)
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    else
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun SystemMessageItem(content: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// ==========================================
// 4. GUESSING VIEW
// ==========================================
@Composable
fun GuessingView(onGuess: (Boolean) -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("HẾT GIỜ!", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text(
                "Theo trực giác của bạn,\nbạn vừa trò chuyện với ai?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(48.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GuessCard(
                    icon = "🤖",
                    label = "AI (Bot)",
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    onClick = { onGuess(true) },
                    modifier = Modifier.weight(1f)
                )

                GuessCard(
                    icon = "🧑",
                    label = "Người thật",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { onGuess(false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GuessCard(icon: String, label: String, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Shadow thấp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 5. RESULT VIEW
// ==========================================
@Composable
fun ResultView(state: HumanAiGameState, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val title = if (state.didWin) "CHÍNH XÁC!" else "SAI RỒI!"
                val color = if (state.didWin) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                val opponentIcon = if (state.isOpponentActuallyAi) "🤖" else "🧑"
                val opponentLabel = if (state.isOpponentActuallyAi) "AI (Bot)" else "Người thật"

                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = color,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // Reveal Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Đối thủ của bạn là", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Text(opponentIcon, fontSize = 64.sp)
                        Text(
                            opponentLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Points Badge
                Surface(
                    color = if (state.didWin) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.didWin) "+" else "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = "${state.earnedRizz} RIZZ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

//                Button(
//                    onClick = onPlayAgain,
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
//                ) {
//                    Text("Chơi lại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                }
//                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onExit) {
                    Text("Quay lại", fontSize = 16.sp)
                }
            }

            if (state.didWin) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(party),
                )
            }
        }
    }
}