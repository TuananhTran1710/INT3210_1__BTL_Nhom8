package com.example.wink.ui.features.quiz

import com.example.wink.data.model.Quiz

sealed interface QuizUiState {
    object Loading : QuizUiState
    data class QuizList(val quizzes: List<Quiz>) : QuizUiState
    data class QuizDetail(
        val quiz: Quiz,
        val selectedAnswers: Map<String, Int>, // questionId -> selectedIndex
        val isSubmitted: Boolean,
        val score: Int?
    ) : QuizUiState
    data class Error(val message: String) : QuizUiState
}