package com.example.wink.ui.features.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState

    init {
        loadList()
    }

    fun onEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.LoadList -> loadList()
            is QuizEvent.OpenQuiz -> openQuiz(event.quizId)
            is QuizEvent.SelectAnswer -> selectAnswer(event.questionId, event.selectedIndex)
            is QuizEvent.Submit -> submitCurrent()
            is QuizEvent.MoveNext -> moveNext()
            is QuizEvent.MovePrev -> movePrev()
            is QuizEvent.JumpTo -> jumpTo(event.index)
        }
    }

    private fun loadList() {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getAllQuizzes()
                _uiState.value = QuizUiState.QuizList(list)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.localizedMessage ?: "Unknown")
            }
        }
    }

    private fun openQuiz(quizId: String) {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                val quiz = repository.getQuizById(quizId)
                if (quiz == null) {
                    _uiState.value = QuizUiState.Error("Quiz not found")
                } else {
                    _uiState.value = QuizUiState.QuizDetail(
                        quiz = quiz,
                        selectedAnswers = emptyMap(),
                        isSubmitted = false,
                        score = null,
                        currentQuestionIndex = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.localizedMessage ?: "Unknown")
            }
        }
    }

    private fun selectAnswer(questionId: String, selectedIndex: Int?) {
        val current = _uiState.value
        if (current is QuizUiState.QuizDetail && !current.isSubmitted) {
            val newSelected = if (selectedIndex == null) {
                current.selectedAnswers - questionId
            } else {
                current.selectedAnswers + (questionId to selectedIndex)
            }
            _uiState.value = current.copy(selectedAnswers = newSelected)
        }
    }

    private fun submitCurrent() {
        val current = _uiState.value
        if (current is QuizUiState.QuizDetail && !current.isSubmitted) {
            var score = 0
            current.quiz.questions.forEach { q ->
                val selected = current.selectedAnswers[q.id]
                if (selected == q.correctIndex) score++
            }
            _uiState.value = current.copy(
                isSubmitted = true,
                score = score
            )
        }
    }

    private fun moveNext() {
        val current = _uiState.value
        if (current is QuizUiState.QuizDetail) {
            if (current.currentQuestionIndex < current.quiz.questions.size - 1) {
                _uiState.value = current.copy(
                    currentQuestionIndex = current.currentQuestionIndex + 1
                )
            }
        }
    }

    private fun movePrev() {
        val current = _uiState.value
        if (current is QuizUiState.QuizDetail) {
            if (current.currentQuestionIndex > 0) {
                _uiState.value = current.copy(
                    currentQuestionIndex = current.currentQuestionIndex - 1
                )
            }
        }
    }

    private fun jumpTo(index: Int) {
        val current = _uiState.value
        if (current is QuizUiState.QuizDetail) {
            if (index in current.quiz.questions.indices) {
                _uiState.value = current.copy(currentQuestionIndex = index)
            }
        }
    }
}
