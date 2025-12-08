import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wink.ui.features.quiz.QuizUiState
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    state: QuizUiState.QuizDetail,
    onSelect: (String, Int?) -> Unit,
    onSubmit: () -> Unit,
    onMovePrev: () -> Unit,
    onMoveNext: () -> Unit,
    onJumpTo: (Int) -> Unit,
    onBack: () -> Unit
) {
    val quiz = state.quiz
    val currentIndex = state.currentQuestionIndex
    val currentQuestion = quiz.questions[currentIndex]

    val progress by animateFloatAsState(
        targetValue = (currentIndex + 1).toFloat() / quiz.questions.size,
        label = "progress"
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Câu ${currentIndex + 1}/${quiz.questions.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!state.isSubmitted) {
                            TextButton(onClick = onSubmit) {
                                Text("Nộp bài", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onMovePrev, enabled = currentIndex > 0, modifier = Modifier.weight(1f)) {
                        Text("Trước")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { if (currentIndex < quiz.questions.size - 1) onMoveNext() else onSubmit() },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSubmitted || currentIndex < quiz.questions.size - 1
                    ) {
                        Text(if (currentIndex < quiz.questions.size - 1) "Tiếp theo" else "Hoàn thành")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text(
                text = currentQuestion.text,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            currentQuestion.answers.forEachIndexed { index, answer ->
                AnswerCard(
                    text = answer.text,
                    isSelected = state.selectedAnswers[currentQuestion.id] == index,
                    isSubmitted = state.isSubmitted,
                    isCorrect = index == currentQuestion.correctIndex,
                    isUserSelected = state.selectedAnswers[currentQuestion.id] == index,
                    onClick = { if (!state.isSubmitted) onSelect(currentQuestion.id, index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (state.isSubmitted) {
                Spacer(modifier = Modifier.height(24.dp))
                val isCorrect = state.selectedAnswers[currentQuestion.id] == currentQuestion.correctIndex
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCorrect) "Chính xác! Bạn rất tinh tế." else "Sai rồi! Đáp án đúng là đáp án màu xanh.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AnswerCard(text: String, isSelected: Boolean, isSubmitted: Boolean, isCorrect: Boolean, isUserSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> Color(0xFFE8F5E9)
            isSubmitted && isUserSelected && !isCorrect -> Color(0xFFFFEBEE)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }, label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> Color(0xFF2E7D32)
            isSubmitted && isUserSelected && !isCorrect -> Color(0xFFC62828)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        }, label = "border"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected || isSubmitted) 2.dp else 1.dp, borderColor),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(20.dp).border(width = 2.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape = CircleShape).padding(4.dp)
            ) {
                if (isSelected) Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary, CircleShape))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}

// --- 3. PHÁO HOA KHI KẾT QUẢ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    state: QuizUiState.QuizResult,
    onBackToList: () -> Unit,
    onTryAgain: (String) -> Unit
) {
    // Cấu hình pháo hoa
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("Kết quả") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val title = if (state.isPerfectScore) "Xuất Sắc! \uD83C\uDF89" else "Hoàn Thành"
                val color = if (state.isPerfectScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )

                Spacer(Modifier.height(16.dp))

                // Điểm số Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Điểm số của bạn", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${state.score}/${state.maxScore}",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (state.rizzPointsEarned > 0) {
                    Text(
                        text = "+${state.rizzPointsEarned} RIZZ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFFD700), // Gold
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(32.dp))
                }

                Button(
                    onClick = onBackToList,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quay về danh sách")
                }

                if (!state.isPerfectScore) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onTryAgain(state.quiz.id) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Làm lại")
                    }
                }
            }
        }

        // Hiệu ứng pháo hoa đè lên trên cùng (chỉ hiện khi hoàn thành)
        if (state.isPerfectScore || state.score > 0) { // Điều kiện nổ pháo
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party),
            )
        }
    }
}