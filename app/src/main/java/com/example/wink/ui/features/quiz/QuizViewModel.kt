package com.example.wink.ui.features.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    private val authRepository: AuthRepository
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
            is QuizEvent.TryAgain -> tryAgain(event.quizId)
            is QuizEvent.BackToList -> loadList()
            is QuizEvent.UnlockQuiz -> unlockQuiz(event.quizId, event.cost)
        }
    }

    private fun loadList() {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser.first()
                val list = repository.getAllQuizzes()

                val finishedIds = currentUser?.quizzesFinished?.toSet() ?: emptySet()

                val unlockedIds = currentUser?.quizzesUnlocked?.toSet()
                    ?: setOf("rizz_001", "rizz_002", "rizz_003", "rizz_004")
                val currentRizz = currentUser?.rizzPoints ?: 0

                _uiState.value = QuizUiState.QuizList(
                    quizzes = list,
                    finishedQuizIds = finishedIds,
                    quizzesUnlocked = unlockedIds,
                    currentRizzPoints = currentRizz
                )
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.localizedMessage ?: "Unknown error during load list")
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
            val maxScore = current.quiz.questions.size

            current.quiz.questions.forEach { q ->
                val selected = current.selectedAnswers[q.id]
                if (selected == q.correctIndex) score++
            }

            val isPerfectScore = score == maxScore

            viewModelScope.launch {
                var rizzPointsAwarded = 0

                try {
                    rizzPointsAwarded = authRepository.completeQuizAndAwardPoints(
                        quizId = current.quiz.id,
                        firstTimeAward = 50,
                        isPerfectScore = isPerfectScore
                    )
                } catch (e: Exception) {
                    rizzPointsAwarded = 0
                }

                val latestUser = authRepository.currentUser.first()
                val latestRizz = latestUser?.rizzPoints ?: 0
                val latestUnlocked = latestUser?.quizzesUnlocked?.toSet()
                    ?: setOf("rizz_001", "rizz_002", "rizz_003", "rizz_004")

                _uiState.value = QuizUiState.QuizResult(
                    quiz = current.quiz,
                    score = score,
                    maxScore = maxScore,
                    isPerfectScore = isPerfectScore,
                    rizzPointsEarned = rizzPointsAwarded,
                    quizzesUnlocked = latestUnlocked,
                    currentRizzPoints = latestRizz
                )
            }
        }
    }

    private fun tryAgain(quizId: String) {
        val current = _uiState.value
        if (current is QuizUiState.QuizResult) {
            _uiState.value = QuizUiState.QuizDetail(
                quiz = current.quiz,
                selectedAnswers = emptyMap(),
                isSubmitted = false,
                score = null,
                currentQuestionIndex = 0
            )
        } else {
            openQuiz(quizId)
        }
    }

    private fun unlockQuiz(quizId: String, cost: Int) {
        viewModelScope.launch {
            val success = authRepository.unlockQuiz(quizId, cost)

            if (success) {
                loadList()
            } else {
                val current = _uiState.value
                if (current is QuizUiState.QuizList && current.currentRizzPoints < cost) {
                    _uiState.value = QuizUiState.Error("Không đủ Rizz Points ($cost) để mở khóa Quiz này. Bạn đang có ${current.currentRizzPoints} điểm.")
                } else if (!success) {
                    _uiState.value = QuizUiState.Error("Có lỗi xảy ra khi mở khóa Quiz. Vui lòng thử lại.")
                }
            }
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
