package com.example.wink.ui.features.quiz

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.data.model.Answer
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz
import org.junit.Rule
import org.junit.Test

class QuizFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockQuestions = listOf(
        Question(
            id = "q1",
            text = "Câu hỏi test 1",
            correctIndex = 0,
            answers = listOf(
                Answer(text = "Đáp án A (Đúng)"),
                Answer(text = "Đáp án B"),
                Answer(text = "Đáp án C"),
                Answer(text = "Đáp án D")
            )
        ),
        Question(
            id = "q2",
            text = "Câu hỏi test 2",
            correctIndex = 1,
            answers = listOf(
                Answer(text = "Đáp án A"),
                Answer(text = "Đáp án B (Đúng)")
            )
        )
    )

    private val mockQuizzes = listOf(
        Quiz(
            id = "quiz_free",
            title = "Quiz Miễn Phí",
            description = "Mô tả quiz miễn phí",
            rizzUnlockCost = 0,
            questions = mockQuestions,
            questionCount = mockQuestions.size
        ),
        Quiz(
            id = "quiz_locked",
            title = "Quiz Bị Khóa",
            description = "Cần 100 Rizz",
            rizzUnlockCost = 100,
            questions = mockQuestions,
            questionCount = mockQuestions.size
        ),
        Quiz(
            id = "quiz_done",
            title = "Quiz Đã Làm",
            description = "Đã hoàn thành",
            rizzUnlockCost = 0,
            questions = mockQuestions,
            questionCount = mockQuestions.size
        )
    )

    @Test
    fun testQuizList_TabsAndVisibility() {
        val state = QuizUiState.QuizList(
            quizzes = mockQuizzes,
            finishedQuizIds = setOf("quiz_done"),
            quizzesUnlocked = setOf("quiz_free"),
            currentRizzPoints = 500
        )

        composeTestRule.setContent {
            QuizListContent(
                state = state,
                onOpen = {},
                onUnlock = { _, _ -> },
                onBack = {},
                onGenerateQuiz = {},
                onShowGenerateDialog = {},
                onDismissGenerateDialog = {}
            )
        }

        composeTestRule.onNodeWithText("Chưa hoàn thành").assertIsSelected()
        composeTestRule.onNodeWithText("Quiz Miễn Phí").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz Bị Khóa").assertIsDisplayed()

        composeTestRule.onNodeWithText("Quiz Đã Làm").assertDoesNotExist()
        composeTestRule.onNodeWithText("Đã hoàn thành").performClick()

        composeTestRule.onNodeWithText("Quiz Đã Làm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz Miễn Phí").assertDoesNotExist()
    }

    @Test
    fun testQuizList_Search() {
        val state = QuizUiState.QuizList(
            quizzes = mockQuizzes,
            finishedQuizIds = emptySet(),
            quizzesUnlocked = setOf("quiz_free"),
            currentRizzPoints = 500
        )

        composeTestRule.setContent {
            QuizListContent(
                state = state,
                onOpen = {},
                onUnlock = { _, _ -> },
                onBack = {},
                onGenerateQuiz = {},
                onShowGenerateDialog = {},
                onDismissGenerateDialog = {}
            )
        }
        composeTestRule.onNodeWithText("Tìm kiếm chủ đề...").performTextInput("Khóa")
        composeTestRule.onNodeWithText("Quiz Bị Khóa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz Miễn Phí").assertDoesNotExist()
    }

    @Test
    fun testQuizList_UnlockDialog() {
        val state = QuizUiState.QuizList(
            quizzes = mockQuizzes,
            finishedQuizIds = emptySet(),
            quizzesUnlocked = setOf("quiz_free"),
            currentRizzPoints = 500
        )

        var unlockedId: String? = null
        var unlockedCost: Int? = null

        composeTestRule.setContent {
            QuizListContent(
                state = state,
                onOpen = {},
                onUnlock = { id, cost ->
                    unlockedId = id
                    unlockedCost = cost
                },
                onBack = {},
                onGenerateQuiz = {},
                onShowGenerateDialog = {},
                onDismissGenerateDialog = {}
            )
        }

        composeTestRule.onNodeWithText("Quiz Bị Khóa").performClick()

        composeTestRule.onNodeWithText("Mở khóa thử thách?").assertIsDisplayed()

        composeTestRule.onNodeWithText("Mở khóa ngay").performClick()

        assert(unlockedId == "quiz_locked")
        assert(unlockedCost == 100)
    }

    @Test
    fun testQuizList_GenerateDialog() {
        val state = QuizUiState.QuizList(
            quizzes = emptyList(),
            quizzesUnlocked = emptySet(),
            currentRizzPoints = 300,
            showGenerateDialog = true
        )

        var generatedTopic: String? = null

        composeTestRule.setContent {
            QuizListContent(
                state = state,
                onOpen = {},
                onUnlock = { _, _ -> },
                onBack = {},
                onGenerateQuiz = { topic -> generatedTopic = topic },
                onShowGenerateDialog = {},
                onDismissGenerateDialog = {}
            )
        }

        composeTestRule.onNodeWithText("Tạo Quiz cho riêng bạn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chủ đề").performTextInput("Test Topic")
        composeTestRule.onNodeWithText("Tạo ngay (-250 Rizz)").performClick()

        assert(generatedTopic == "Test Topic")
    }

    @Test
    fun testQuizDetail_SelectionAndSubmit() {
        val quiz = mockQuizzes[0]
        val state = QuizUiState.QuizDetail(
            quiz = quiz,
            selectedAnswers = emptyMap(),
            isSubmitted = false,
            score = null,
            currentQuestionIndex = 0
        )

        var selectedQId: String? = null
        var selectedAIndex: Int? = null

        composeTestRule.setContent {
            QuizDetailScreen(
                state = state,
                onSelect = { qId, idx ->
                    selectedQId = qId
                    selectedAIndex = idx
                },
                onSubmit = {},
                onMovePrev = {},
                onMoveNext = {},
                onJumpTo = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Câu hỏi test 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đáp án A (Đúng)").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đáp án A (Đúng)").performClick()

        assert(selectedQId == "q1")
        assert(selectedAIndex == 0)
    }

    @Test
    fun testQuizDetail_ResultFeedback() {
        val quiz = mockQuizzes[0]
        val state = QuizUiState.QuizDetail(
            quiz = quiz,
            selectedAnswers = mapOf("q1" to 0),
            isSubmitted = true,
            score = 1,
            currentQuestionIndex = 0
        )

        composeTestRule.setContent {
            QuizDetailScreen(
                state = state,
                onSelect = { _, _ -> },
                onSubmit = {},
                onMovePrev = {},
                onMoveNext = {},
                onJumpTo = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Chính xác! Bạn rất tinh tế.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nộp bài").assertDoesNotExist()
    }
}