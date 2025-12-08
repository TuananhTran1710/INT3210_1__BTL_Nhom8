package com.example.wink.ui.features.quiz

sealed interface QuizEvent {
    object LoadList : QuizEvent
    data class OpenQuiz(val quizId: String) : QuizEvent
    data class SelectAnswer(val questionId: String, val selectedIndex: Int?) : QuizEvent
    object Submit : QuizEvent
    object MoveNext : QuizEvent
    object MovePrev : QuizEvent
    data class JumpTo(val index: Int) : QuizEvent
    data class TryAgain(val quizId: String) : QuizEvent
    object BackToList : QuizEvent
    data class UnlockQuiz(val quizId: String, val cost: Int) : QuizEvent
}
