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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(quizzes: List<Quiz>, onOpen: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Quizzes") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(quizzes) { quiz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onOpen(quiz.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(quiz.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(quiz.description, style = MaterialTheme.typography.bodyMedium)
                    }
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