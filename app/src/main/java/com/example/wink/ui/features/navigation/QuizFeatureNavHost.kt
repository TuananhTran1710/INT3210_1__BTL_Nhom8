package com.example.wink.ui.features.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wink.ui.common.ErrorScreen
import com.example.wink.ui.common.LoadingScreen
// Import đầy đủ các màn hình và State từ package quiz
import com.example.wink.ui.features.quiz.QuizDetailScreen
import com.example.wink.ui.features.quiz.QuizEvent
import com.example.wink.ui.features.quiz.QuizListContent // <--- Quan trọng: Dùng Content thay vì Screen
import com.example.wink.ui.features.quiz.QuizResultScreen
import com.example.wink.ui.features.quiz.QuizUiState
import com.example.wink.ui.features.quiz.QuizViewModel

object QuizRoutes {
    const val LIST = "quiz_list"
    const val DETAIL = "quiz_detail/{quizId}"
}

@Composable
fun QuizFeatureNavHost(
    viewModel: QuizViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        QuizUiState.Loading -> LoadingScreen()

        is QuizUiState.Error ->
            ErrorScreen((state as QuizUiState.Error).message)

        is QuizUiState.QuizList ->
            // SỬA LỖI Ở ĐÂY: Dùng QuizListContent thay vì QuizListScreen
            QuizListContent(
                state = state as QuizUiState.QuizList,
                onOpen = { id ->
                    viewModel.onEvent(QuizEvent.OpenQuiz(id))
                },
                onUnlock = { quizId, cost ->
                    viewModel.onEvent(QuizEvent.UnlockQuiz(quizId, cost))
                },
                onBack = onBack,
                onGenerateQuiz = { topic ->
                    viewModel.onEvent(QuizEvent.GenerateQuiz(topic))
                },
                onShowGenerateDialog = {
                    viewModel.onEvent(QuizEvent.ShowGenerateDialog)
                },
                onDismissGenerateDialog = {
                    viewModel.onEvent(QuizEvent.DismissGenerateDialog)
                }
            )

        is QuizUiState.QuizDetail ->
            QuizDetailScreen(
                state = state as QuizUiState.QuizDetail,
                onSelect = { qId, idx ->
                    viewModel.onEvent(QuizEvent.SelectAnswer(qId, idx))
                },
                onSubmit = {
                    viewModel.onEvent(QuizEvent.Submit)
                },
                onMovePrev = {
                    viewModel.onEvent(QuizEvent.MovePrev)
                },
                onMoveNext = {
                    viewModel.onEvent(QuizEvent.MoveNext)
                },
                onJumpTo = { index ->
                    viewModel.onEvent(QuizEvent.JumpTo(index))
                },
                onBack = {
                    viewModel.onEvent(QuizEvent.LoadList)
                }
            )

        is QuizUiState.QuizResult ->
            QuizResultScreen(
                state = state as QuizUiState.QuizResult,
                onBackToList = {
                    viewModel.onEvent(QuizEvent.LoadList)
                },
                onTryAgain = { quizId ->
                    viewModel.onEvent(QuizEvent.TryAgain(quizId))
                }
            )
    }
}