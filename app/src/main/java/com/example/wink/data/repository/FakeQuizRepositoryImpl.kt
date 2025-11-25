package com.example.wink.data.repository

import com.example.wink.data.model.Answer
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz
import javax.inject.Inject

class FakeQuizRepositoryImpl @Inject constructor(): QuizRepository {
    private val sampleQuizzes = listOf(
        Quiz(
            id = "rizz_101",
            title = "Rizz 101",
            description = "Basic flirty quiz",
            questions = listOf(
                Question(
                    id = "q1",
                    text = "What do you say on the first message?",
                    answers = listOf(
                        Answer("Hey"),
                        Answer("Nice profile — tell me more"),
                        Answer("Is your dog single?"),
                        Answer("What's your favorite movie?")
                    ),
                    correctIndex = 3
                ),
                Question(
                    id = "q2",
                    text = "How do you keep convo going?",
                    answers = listOf(
                        Answer("Use open questions"),
                        Answer("Spam emojis"),
                        Answer("Send 10 memes in a row"),
                        Answer("Only say 'k'")
                    ),
                    correctIndex = 0
                )
            )
        ),
        Quiz(
            id = "rizz_advanced",
            title = "Rizz Advanced",
            description = "Advanced timing & build-up",
            questions = listOf(
                Question(
                    id = "q3",
                    text = "When is a good time to escalate to a date?",
                    answers = listOf(
                        Answer("Right away"),
                        Answer("After a couple meaningful convos"),
                        Answer("Wait 3 months"),
                        Answer("Never")
                    ),
                    correctIndex = 1
                ),
                Question(
                    id = "q4",
                    text = "What's a strong closing line?",
                    answers = listOf(
                        Answer("We should get coffee sometime"),
                        Answer("irl?"),
                        Answer("u up?"),
                        Answer("I have to go")
                    ),
                    correctIndex = 0
                )
            )
        )
    )
    override suspend fun getAllQuizzes(): List<Quiz> = sampleQuizzes
    override suspend fun getQuizById(id: String): Quiz? = sampleQuizzes.firstOrNull { it.id == id }
}