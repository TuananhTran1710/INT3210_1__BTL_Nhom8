package com.example.wink.ui.features.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search // Import icon Search
import androidx.compose.material.icons.filled.Star // Import icon Star (dùng làm placeholder)
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow // Thêm import cho TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(
    state: QuizUiState.QuizList,
    onOpen: (String) -> Unit,
    onUnlock: (String, Int) -> Unit
) {
    val quizzes = state.quizzes
    val finishedIds = state.finishedQuizIds
    val unlockedIds = state.quizzesUnlocked
    val currentRizz = state.currentRizzPoints

    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val categories = remember { listOf("Chưa làm", "Đã làm") }
    val selectedCategory = categories[selectedCategoryIndex]

    var searchQuery by remember { mutableStateOf("") }

    val filteredQuizzes = remember(quizzes, finishedIds, searchQuery, selectedCategory) {
        quizzes
            .filter { quiz ->
                val isFinished = finishedIds.contains(quiz.id)
                when (selectedCategory) {
                    "Đã làm" -> isFinished
                    "Chưa làm" -> !isFinished
                    else -> true
                }
            }
            .filter { quiz ->
                if (searchQuery.isBlank()) {
                    true
                } else {
                    quiz.title.contains(searchQuery.trim(), ignoreCase = true)
                }
            }
    }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Quizzes") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchBarComponent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(categories) { index, category ->
                        CategoryChip(
                            category = category,
                            isSelected = category == selectedCategory,
                            onClick = { selectedCategoryIndex = index } // Thêm onClick
                        )
                    }
                }
            }

            items(filteredQuizzes) { quiz ->
                val isFinished = finishedIds.contains(quiz.id)
                QuizCard(
                    quiz = quiz,
                    isFinished = isFinished,
                    isUnlocked = unlockedIds.contains(quiz.id),
                    currentRizzPoints = currentRizz,
                    onClick = { onOpen(quiz.id) },
                    onUnlock = onUnlock
                )
            }
        }
    }
}

@Composable
fun SearchBarComponent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Tìm kiếm quiz") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun CategoryChip(category: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QuizCard(
    quiz: Quiz,
    isFinished: Boolean,
    isUnlocked: Boolean,
    currentRizzPoints: Int,
    onClick: () -> Unit,
    onUnlock: (String, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val durationText = "3 phút"

                    Text(
                        text = "5 câu hỏi",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "|",
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val isLocked = !isFinished && !isUnlocked
                val canAffordUnlock = currentRizzPoints >= quiz.rizzUnlockCost

                Button(
                    onClick = {
                        when {
                            isFinished || isUnlocked -> onClick()
                            isLocked && canAffordUnlock -> onUnlock(quiz.id, quiz.rizzUnlockCost)
                        }
                    },
                    enabled = !isLocked || canAffordUnlock,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val buttonText = when {
                        isFinished -> "Ôn lại"
                        isUnlocked -> "Bắt đầu"
                        else -> "Unlock: ${quiz.rizzUnlockCost} Rizz"
                    }
                    Text(buttonText)
                }
            }
        }
    }
}

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
    var showQuestionPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQuestionPicker = true }) {
                        Text("List")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Question ${currentIndex + 1}/${quiz.questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                QuestionItem(
                    question = currentQuestion,
                    selectedIndex = state.selectedAnswers[currentQuestion.id],
                    isSubmitted = state.isSubmitted,
                    onSelect = { idx -> onSelect(currentQuestion.id, idx) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = currentIndex > 0,
                        onClick = onMovePrev,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("← Prev")
                    }

                    Button(
                        enabled = currentIndex < quiz.questions.size - 1,
                        onClick = onMoveNext,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next →")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitted
            ) {
                Text("Submit")
            }

            if (state.isSubmitted) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Result: ${state.score} / ${quiz.questions.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (showQuestionPicker) {
            QuestionPickerDialog(
                state = state,
                onSelectQuestion = { idx ->
                    onJumpTo(idx)
                    showQuestionPicker = false
                },
                onSubmit = {
                    onSubmit()
                    showQuestionPicker = false
                },
                onDismiss = { showQuestionPicker = false }
            )
        }
    }
}

@Composable
private fun QuestionItem(
    question: Question,
    selectedIndex: Int?,
    isSubmitted: Boolean,
    onSelect: (Int?) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = question.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            question.answers.forEachIndexed { index, answer ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitted) {
                        if (!isSubmitted) {
                            if (selectedIndex == index) onSelect(null) else onSelect(index)
                        }
                    }
                    .padding(vertical = 6.dp)
                ) {
                    RadioButton(
                        selected = selectedIndex == index,
                        onClick = {
                            if (!isSubmitted) {
                                if (selectedIndex == index) onSelect(null) else onSelect(index)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = answer.text)
                    if (isSubmitted) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (index == question.correctIndex) {
                            Text(text = "✓", fontWeight = FontWeight.Bold)
                        } else if (selectedIndex == index) {
                            Text(text = "✗", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionPickerDialog(
    state: QuizUiState.QuizDetail,
    onSelectQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Questions") },
        text = {
            FlowRow(
                maxItemsInEachRow = 6,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.quiz.questions.forEachIndexed { index, q ->
                    val answered = state.selectedAnswers[q.id] != null

                    val bg = if (answered)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { onSelectQuestion(index) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = !state.isSubmitted) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    state: QuizUiState.QuizResult,
    onBackToList: () -> Unit,
    onTryAgain: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kết quả") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val title = if (state.isPerfectScore) "Hoàn Thành Xuất Sắc! 🎉" else "Cần cố gắng hơn!"
            val message = if (state.isPerfectScore)
                "Bạn đã trả lời đúng tất cả ${state.maxScore} câu hỏi. Thật tuyệt vời!"
            else
                "Bạn đã đúng ${state.score} trên ${state.maxScore} câu. Đừng bỏ cuộc!"

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (state.isPerfectScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (state.rizzPointsEarned > 0) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "🎉 +${state.rizzPointsEarned} Rizz Points!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(40.dp))

            if (!state.isPerfectScore) {
                Button(onClick = { onTryAgain(state.quiz.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Làm lại")
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = onBackToList,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Về danh sách Quiz")
            }
        }
    }
}