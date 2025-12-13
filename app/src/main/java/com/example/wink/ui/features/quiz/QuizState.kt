package com.example.wink.ui.features.quiz

import com.example.wink.data.model.Quiz

sealed interface QuizUiState {
    object Loading : QuizUiState
    data class QuizList(
        val quizzes: List<Quiz>,
        val finishedQuizIds: Set<String> = emptySet(),
        val quizzesUnlocked: Set<String>,
        val currentRizzPoints: Int,
        val isGenerating: Boolean = false,
        val showGenerateDialog: Boolean = false,
        val generateError: String? = null
    ) : QuizUiState
    data class QuizDetail(
        val quiz: Quiz,
        val selectedAnswers: Map<String, Int>,   // questionId -> selectedIndex
        val isSubmitted: Boolean,
        val score: Int?,
        val currentQuestionIndex: Int
    ) : QuizUiState
    data class Error(
        val message: String
    ) : QuizUiState
    data class QuizResult(
        val quiz: Quiz,
        val score: Int,
        val maxScore: Int,
        val isPerfectScore: Boolean,
        val rizzPointsEarned: Int = 0,
        val quizzesUnlocked: Set<String>,
        val currentRizzPoints: Int
    ) : QuizUiState
}
