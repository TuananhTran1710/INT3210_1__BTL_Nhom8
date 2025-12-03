package com.example.wink.data.repository

import com.example.wink.data.model.Quiz
import com.example.wink.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuizRepository {
    private val quizCollection = firestore.collection("quizzes")

    override suspend fun getAllQuizzes(): List<Quiz> {
        return try {
            val snapshot = quizCollection.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Quiz::class.java)?.copy(questions = emptyList())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getQuizById(id: String): Quiz? {
        return try {
            val quizDocument = quizCollection.document(id).get().await()
            val quiz = quizDocument.toObject(Quiz::class.java)

            if (quiz != null) {
                val questionsSnapshot = quizCollection.document(id)
                    .collection("questions")
                    .get()
                    .await()

                val questions = questionsSnapshot.documents.mapNotNull { qDoc ->
                    qDoc.toObject(Question::class.java)
                }
                
                return quiz.copy(questions = questions)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}