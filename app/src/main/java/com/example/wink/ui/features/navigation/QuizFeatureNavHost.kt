package com.example.wink.ui.features.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wink.ui.common.ErrorScreen
import com.example.wink.ui.common.LoadingScreen
import com.example.wink.ui.features.quiz.*

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
            QuizListScreen(
                quizzes = (state as QuizUiState.QuizList).quizzes,
                onOpen = { id ->
                    viewModel.onEvent(QuizEvent.OpenQuiz(id))
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
    }
}
