package com.example.wink.ui.features.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wink.data.model.Quiz

@Composable
fun QuizListScreen(
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state is QuizUiState.QuizList) {
        val listState = state as QuizUiState.QuizList
        QuizListContent(
            state = listState,
            onOpen = onOpen,
            onUnlock = { id, cost -> viewModel.onEvent(QuizEvent.UnlockQuiz(id, cost)) },
            onBack = onBack,
            onGenerateQuiz = { topic -> viewModel.onEvent(QuizEvent.GenerateQuiz(topic)) },
            onShowGenerateDialog = { viewModel.onEvent(QuizEvent.ShowGenerateDialog) },
            onDismissGenerateDialog = { viewModel.onEvent(QuizEvent.DismissGenerateDialog) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListContent(
    state: QuizUiState.QuizList,
    onOpen: (String) -> Unit,
    onUnlock: (String, Int) -> Unit,
    onBack: () -> Unit,
    onGenerateQuiz: (String) -> Unit,
    onShowGenerateDialog: () -> Unit,
    onDismissGenerateDialog: () -> Unit
) {
    val quizzes = state.quizzes
    val finishedIds = state.finishedQuizIds
    val unlockedIds = state.quizzesUnlocked
    val currentRizz = state.currentRizzPoints

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("Chưa hoàn thành", "Đã hoàn thành")
    var searchQuery by remember { mutableStateOf("") }

    var quizToUnlock by remember { mutableStateOf<Quiz?>(null) }

    val filteredQuizzes = remember(quizzes, finishedIds, searchQuery, selectedCategoryIndex) {
        quizzes
            .filter { quiz ->
                val isFinished = finishedIds.contains(quiz.id)
                when (selectedCategoryIndex) {
                    0 -> !isFinished
                    1 -> isFinished
                    else -> true
                }
            }
            .filter { quiz ->
                if (searchQuery.isBlank()) true
                else quiz.title.contains(searchQuery.trim(), ignoreCase = true)
            }
    }

    if (quizToUnlock != null) {
        val quiz = quizToUnlock!!
        val canAfford = currentRizz >= quiz.rizzUnlockCost

        AlertDialog(
            onDismissRequest = { quizToUnlock = null },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Mở khóa thử thách?") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bạn có muốn dùng ${quiz.rizzUnlockCost} RIZZ để mở khóa bài \"${quiz.title}\" không?", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!canAfford) {
                        Text("Bạn không đủ điểm! (Hiện có: $currentRizz)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    } else {
                        Text("Số dư sau khi mua: ${currentRizz - quiz.rizzUnlockCost} RIZZ", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnlock(quiz.id, quiz.rizzUnlockCost)
                        quizToUnlock = null
                    },
                    enabled = canAfford
                ) {
                    Text("Mở khóa ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { quizToUnlock = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (state.showGenerateDialog) {
        var topicInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismissGenerateDialog,
            title = { Text("Tạo Quiz cho riêng bạn") },
            text = {
                Column {
                    Text("Nhập chủ đề bạn muốn.\nPhí tạo: 250 RIZZ.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Chủ đề") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onGenerateQuiz(topicInput) },
                    enabled = topicInput.isNotBlank() && state.currentRizzPoints >= 250
                ) {
                    Text("Tạo ngay (-250 Rizz)")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissGenerateDialog) {
                    Text("Hủy")
                }
            }
        )
    }

    if (state.isGenerating) {
        Dialog(onDismissRequest = {}) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Đang soạn câu hỏi...", fontWeight = FontWeight.Bold)
                    Text("Vui lòng đợi giây lát", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }

    LaunchedEffect(state.generateError) {
        state.generateError?.let {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thử thách EQ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$currentRizz",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            SearchBarCompact(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            ExtendedFloatingActionButton(
                onClick = onShowGenerateDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                text = { Text("Tạo Quiz (-250 RIZZ)") }
            )

            TabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredQuizzes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có bài quiz nào ở mục này", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                items(filteredQuizzes) { quiz ->
                    val isFinished = finishedIds.contains(quiz.id)
                    val isUnlocked = unlockedIds.contains(quiz.id) || quiz.rizzUnlockCost == 0

                    QuizCard(
                        quiz = quiz,
                        isFinished = isFinished,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked || isFinished) onOpen(quiz.id)
                            else quizToUnlock = quiz
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBarCompact(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(42.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Tìm kiếm chủ đề...", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    innerTextField()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuizCard(
    quiz: Quiz,
    isFinished: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val isLocked = !isFinished && !isUnlocked

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isLocked) 0.dp else 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isFinished) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = when {
                        isFinished -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.Quiz,
                            contentDescription = null,
                            tint = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quiz.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${quiz.questionCount} câu hỏi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Mở khóa ${quiz.rizzUnlockCost} RIZZ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}