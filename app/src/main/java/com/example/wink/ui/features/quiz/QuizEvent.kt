package com.example.wink.ui.features.quiz

sealed interface QuizEvent {
    data class OpenQuiz(val quizId: String) : QuizEvent
    data class SelectAnswer(val questionId: String, val selectedIndex: Int) : QuizEvent
    object Submit : QuizEvent
    object LoadList : QuizEvent
}