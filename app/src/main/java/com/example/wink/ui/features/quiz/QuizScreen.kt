package com.example.wink.ui.features.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wink.data.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(quizzes: List<com.example.wink.data.model.Quiz>, onOpen: (String) -> Unit) {
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
    onSelect: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val quiz = state.quiz

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(quiz.questions) { question ->
                    QuestionItem(
                        question = question,
                        selectedIndex = state.selectedAnswers[question.id],
                        isSubmitted = state.isSubmitted,
                        onSelect = { idx -> onSelect(question.id, idx) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (state.isSubmitted) {
                Text(
                    text = "Result: ${state.score} / ${quiz.questions.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isSubmitted) "Submitted" else "Submit")
            }
        }
    }
}

@Composable
private fun QuestionItem(
    question: Question,
    selectedIndex: Int?,
    isSubmitted: Boolean,
    onSelect: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = question.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            question.answers.forEachIndexed { index, answer ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitted) { onSelect(index) }
                    .padding(vertical = 6.dp)
                ) {
                    RadioButton(
                        selected = selectedIndex == index,
                        onClick = { if (!isSubmitted) onSelect(index) }
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