package com.example.wink.data.repository

import android.annotation.SuppressLint
import com.example.wink.data.model.Quiz
import com.example.wink.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log // THÊM IMPORT NÀY
import com.example.wink.data.model.Answer
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.remote.OpenRouterApiService
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val openRouterApiService: OpenRouterApiService,
    private val json: Json
) : QuizRepository {
    private val quizCollection = firestore.collection("quizzes")
    private val usersCollection = firestore.collection("users")

    override suspend fun getAllQuizzes(): List<Quiz> {
        return try {
            val snapshot = quizCollection.get().await()
            Log.d("QuizRepo", "Fetched ${snapshot.size()} quiz documents.")

            snapshot.documents.mapNotNull { document ->
                try {
                    val quiz = document.toObject(Quiz::class.java)

                    quiz?.copy(id = document.id, questions = emptyList())
                } catch (e: Exception) {
                    Log.e("QuizRepo", "Mapping error for Quiz ID: ${document.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuizRepo", "Error fetching all quizzes from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getQuizById(id: String): Quiz? {
        return try {
            val quizDocument = quizCollection.document(id).get().await()
            val quiz = quizDocument.toObject(Quiz::class.java)

            if (quiz != null) {
                val quizWithId = quiz.copy(id = quizDocument.id)

                val questionsSnapshot = quizCollection.document(id)
                    .collection("questions")
                    .get()
                    .await()

                val questions = questionsSnapshot.documents.mapNotNull { qDoc ->
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

    @SuppressLint("DefaultLocale")
    override suspend fun generateQuizByAi(topic: String, userId: String, cost: Int): Result<String> {
        return try {
            val prompt = """
            Đóng vai là một người ra đề trắc nghiệm, tạo một bài kiểm tra trắc nghiệm thú vị về chủ đề "$topic" liên quan đến tình yêu, hẹn hò và tán tỉnh. Viết bằng ngôn ngữ thân mật, trẻ trung.
            Yêu cầu:
            - Chính xác 5 câu hỏi.
            - Mỗi câu hỏi có 4 answers.
            - One correct answer index (0-3).
            - Output ONLY raw JSON (no markdown, no code blocks) with this specific structure:
            {
              "title": "Tên tiêu đề liên quan đến $topic",
              "description": "Mô tả ngắn gọn",
              "questions": [
                {
                  "text": "Câu hỏi?",
                  "answers": ["Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"],
                  "correctIndex": 0 // Hoặc 1, hoặc 2, hoặc 3, (là chỉ số của đáp án đúng) (0-3)
                }
              ]
            }
        """.trimIndent()

            val request = ChatGptRequest(
                model = "mistralai/devstral-2512:free",
                messages = listOf(ChatGptMessage("user", prompt)),
                maxTokens = 1000
            )

            val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"
            val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)

            val content = response.choices.firstOrNull()?.message?.content ?: throw Exception("AI trả về rỗng")
            val jsonString = content.replace("```json", "").replace("```", "").trim()

            val generatedQuizData = json.parseToJsonElement(jsonString)
            val jsonObj = generatedQuizData.jsonObject

            val title = jsonObj["title"]?.jsonPrimitive?.content ?: "Quiz về $topic"
            val desc = jsonObj["description"]?.jsonPrimitive?.content ?: "Được tạo bởi AI"

            val questionsArray = jsonObj["questions"]?.jsonArray ?: throw Exception("Cấu trúc JSON không hợp lệ")
            val questionsToSave = questionsArray.map { qElement -> // ĐỔI TÊN BIẾN THÀNH questionsToSave
                val qObj = qElement.jsonObject
                val qText = qObj["text"]?.jsonPrimitive?.content ?: ""
                val answersArray = qObj["answers"]?.jsonArray
                val correctIndex = qObj["correctIndex"]?.jsonPrimitive?.int ?: 0

                val answerList = answersArray?.map { Answer(it.jsonPrimitive.content) } ?: emptyList()

                Question(
                    id = UUID.randomUUID().toString(),
                    text = qText,
                    answers = answerList,
                    correctIndex = correctIndex
                )
            }

            val snapshot = quizCollection
                .whereGreaterThanOrEqualTo(com.google.firebase.firestore.FieldPath.documentId(), "rizz_")
                .whereLessThan(com.google.firebase.firestore.FieldPath.documentId(), "rizz_\uf8ff")
                .orderBy(com.google.firebase.firestore.FieldPath.documentId(), com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            var nextNum = 1

            if (!snapshot.isEmpty) {
                val lastId = snapshot.documents[0].id
                val numberPart = lastId.replace("rizz_", "").toIntOrNull()
                if (numberPart != null) {
                    nextNum = numberPart + 1
                }
            }

            val newQuizId = String.format("rizz_%03d", nextNum)

            val quizHeader = Quiz(
                id = newQuizId,
                title = title,
                description = desc,
                rizzUnlockCost = 150,
                questions = emptyList(),
                questionCount = questionsToSave.size
            )

            Log.d("check6767", questionsArray.toString())

            firestore.runTransaction { transaction ->
                val userRef = usersCollection.document(userId)
                val userSnapshot = transaction.get(userRef)
                val currentRizz = userSnapshot.getLong("rizzPoints") ?: 0L

                if (currentRizz < cost) {
                    throw Exception("Bạn không đủ $cost Rizz Points để tạo Quiz!")
                }

                val quizRef = quizCollection.document(newQuizId)
                transaction.set(quizRef, quizHeader)

                val questionCollectionRef = quizRef.collection("questions")
                questionsToSave.forEach { question ->
                    val questionRef = questionCollectionRef.document(question.id)
                    transaction.set(questionRef, question)
                }

                transaction.update(userRef, "rizzPoints", currentRizz - cost)
                transaction.update(userRef, "quizzesUnlocked", FieldValue.arrayUnion(newQuizId))

            }.await()

            Result.success(newQuizId)

        } catch (e: Exception) {
            Log.e("QuizRepo", "Generate Quiz Error", e)
            Result.failure(e)
        }
    }
}