package com.example.wink.ui.features.games.humanai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.wink.data.model.Message
import com.example.wink.ui.features.chat.MessageContainer
import com.example.wink.ui.features.chat.MessageTopBar
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun HumanAiGameScreen(
    navController: NavController,
    viewModel: HumanAiGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.stage) {
                GameStage.LOBBY -> LobbyView(
                    rizz = state.currentRizz,
                    online = state.onlineUsers,
                    onStart = viewModel::onStartMatchmaking,
                    onBack = { navController.popBackStack() }
                )
                GameStage.SEARCHING -> SearchingView(
                    time = state.searchTimeSeconds
                )
                GameStage.CHATTING -> ChattingView(
                    state = state,
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
}

// --- 1. LOBBY VIEW ---
@Composable
fun LobbyView(rizz: Int, online: Int, onStart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Human or AI?",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text("Thử tài phán đoán của bạn", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            // Card Rizz
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                    Spacer(Modifier.width(8.dp))
                    Text("$rizz RIZZ hiện có", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("🟢 $online người đang online", color = Color.Green, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Bắt đầu chơi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 2. SEARCHING VIEW ---
@Composable
fun SearchingView(time: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            Icon(
                Icons.Default.Search, null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(32.dp))
        Text("Đang tìm đối thủ...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Thời gian: ${time}s", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text("Đang kết nối với người lạ...", color = Color.Gray)
    }
}

// --- 3. CHATTING VIEW ---
@Composable
fun ChattingView(state: HumanAiGameState, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    val progress = state.timeLeft / 60f
    val progressColor = if (state.timeLeft < 10) Color.Red else MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Box(
            Modifier.fillMaxWidth().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Còn lại: ${state.timeLeft}s",
                fontWeight = FontWeight.Bold,
                color = if (state.timeLeft < 10) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }

        // SỬA: Dùng GameMessageList thay vì MessageContainer mặc định
        GameMessageList(
            messages = state.messages,
            modifier = Modifier.weight(1f),
            isTyping = state.isOpponentTyping
        )

        // Input Area
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                // Hiển thị placeholder tùy theo lượt
                placeholder = {
                    Text(if (state.isMyTurn) "Đến lượt bạn..." else "Đợi đối phương...")
                },
                shape = RoundedCornerShape(24.dp),
                // KHÓA NHẬP LIỆU NẾU KHÔNG PHẢI LƯỢT
                enabled = state.isMyTurn
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                // Chỉ cho bấm nút gửi khi có text VÀ đúng lượt
                enabled = text.isNotBlank() && state.isMyTurn
            ) {
                Icon(
                    Icons.Default.Send,
                    null,
                    tint = if (state.isMyTurn) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun GameMessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier,
    isTyping: Boolean = false
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Auto scroll
    LaunchedEffect(messages.size, isTyping) {
        listState.animateScrollToItem(0)
    }

    androidx.compose.foundation.lazy.LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        reverseLayout = true
    ) {
        if (isTyping) {
            item {
                com.example.wink.ui.features.chat.TypingIndicator()
            }
        }

        items(messages) { message ->
            if (message.senderId == "system") {
                // Hiển thị tin nhắn hệ thống
                SystemMessageItem(content = message.content)
            } else {
                // Hiển thị tin nhắn chat bình thường
                com.example.wink.ui.features.chat.MessageItem(
                    message = message,
                    isSentByCurrentUser = message.senderId == "me"
                )
            }
        }
    }
}

// UI cho tin nhắn hệ thống (Căn giữa, chữ nhỏ, màu xám)
@Composable
fun SystemMessageItem(content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// --- 4. GUESSING VIEW ---
@Composable
fun GuessingView(onGuess: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hết giờ!", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Bạn vừa trò chuyện với ai?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onGuess(true) }, // True = AI
                modifier = Modifier.weight(1f).height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", fontSize = 40.sp)
                    Text("AI (Bot)", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Button(
                onClick = { onGuess(false) }, // False = Human
                modifier = Modifier.weight(1f).height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧑", fontSize = 40.sp)
                    Text("Người thật", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

// --- 5. RESULT VIEW ---
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

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val title = if (state.didWin) "CHÍNH XÁC!" else "SAI RỒI!"
            val color = if (state.didWin) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            val opponentName = if (state.isOpponentActuallyAi) "AI (Bot) 🤖" else "Người thật 🧑"

            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = color
            )

            Spacer(Modifier.height(16.dp))

            Text("Đối thủ của bạn là:", style = MaterialTheme.typography.bodyLarge)
            Text(opponentName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            // Score Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.didWin) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.didWin) "+" else "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "${state.earnedRizz} RIZZ",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Chơi lại")
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onExit) {
                Text("Thoát ra")
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