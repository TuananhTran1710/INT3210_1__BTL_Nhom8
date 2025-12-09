package com.example.wink.data.repository

import com.example.wink.data.model.Quiz
import com.example.wink.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log // THÊM IMPORT NÀY

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuizRepository {
    private val quizCollection = firestore.collection("quizzes")

    override suspend fun getAllQuizzes(): List<Quiz> {
        return try {
            val snapshot = quizCollection.get().await()
            Log.d("QuizRepo", "Fetched ${snapshot.size()} quiz documents.")

            snapshot.documents.mapNotNull { document ->
                try {
                    // 1. Ánh xạ các trường từ Firestore sang Quiz
                    val quiz = document.toObject(Quiz::class.java)

                    // 2. Gán document ID cho trường 'id' và đảm bảo 'questions' là rỗng (cho list view)
                    quiz?.copy(id = document.id, questions = emptyList())
                } catch (e: Exception) {
                    // 3. Log lỗi mapping cụ thể
                    Log.e("QuizRepo", "Mapping error for Quiz ID: ${document.id}", e)
                    null // Trả về null để mapNotNull bỏ qua document bị lỗi này
                }
            }
        } catch (e: Exception) {
            // Log lỗi khi gọi Firestore (ví dụ: lỗi mạng, lỗi quyền)
            Log.e("QuizRepo", "Error fetching all quizzes from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getQuizById(id: String): Quiz? {
        // ... (getQuizById giữ nguyên) ...
        return try {
            val quizDocument = quizCollection.document(id).get().await()
            val quiz = quizDocument.toObject(Quiz::class.java)

            if (quiz != null) {
                // Ensure ID is set here too, as it's crucial for subsequent calls
                val quizWithId = quiz.copy(id = quizDocument.id)

                val questionsSnapshot = quizCollection.document(id)
                    .collection("questions")
                    .get()
                    .await()

                val questions = questionsSnapshot.documents.mapNotNull { qDoc ->
                    // Cần gán ID cho Question nếu nó có trường ID
                    qDoc.toObject(Question::class.java)?.copy(id = qDoc.id)
                }

                return quizWithId.copy(questions = questions)
            }
            null
        } catch (e: Exception) {
            Log.e("QuizRepo", "Error fetching quiz by ID: $id", e)
            null
        }
    }
}