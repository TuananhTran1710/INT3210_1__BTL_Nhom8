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

    @SuppressLint("DefaultLocale")
    override suspend fun generateQuizByAi(topic: String, userId: String, cost: Int): Result<String> {
        return try {
            val prompt = """
            Create a multiple-choice quiz about "$topic" in Vietnamese.
            Requirements:
            - Exactly 5 questions.
            - Each question must have exactly 4 answers.
            - One correct answer index (0-3).
            - Output ONLY raw JSON (no markdown, no code blocks) with this specific structure:
            {
              "title": "Tên tiêu đề liên quan đến $topic",
              "description": "Mô tả ngắn gọn",
              "questions": [
                {
                  "text": "Câu hỏi?",
                  "answers": ["Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"],
                  "correctIndex": 0
                }
              ]
            }
        """.trimIndent()

            val request = ChatGptRequest(
                model = "mistralai/devstral-2512:free",
                messages = listOf(ChatGptMessage("user", prompt)),
                maxTokens = 1000
            )

            // Lấy API Key
            val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"
            val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)

            val content = response.choices.firstOrNull()?.message?.content ?: throw Exception("AI trả về rỗng")
            val jsonString = content.replace("```json", "").replace("```", "").trim()

            // Phân tích JSON
            val generatedQuizData = json.parseToJsonElement(jsonString)
            val jsonObj = generatedQuizData.jsonObject

            val title = jsonObj["title"]?.jsonPrimitive?.content ?: "Quiz về $topic"
            val desc = jsonObj["description"]?.jsonPrimitive?.content ?: "Được tạo bởi AI"

            // Phân tích Questions và Answers
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

            // --- BƯỚC 2: TÌM ID LỚN NHẤT & TẠO ID MỚI (rizz_XXX) (GIỮ NGUYÊN) ---
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

            // Tạo đối tượng Quiz HEADER (Chỉ lưu thông tin chính, list questions là EMPTY)
            val quizHeader = Quiz(
                id = newQuizId,
                title = title,
                description = desc,
                rizzUnlockCost = 250,
                questions = emptyList(), // KHÔNG LƯU QUESTIONS VÀO DOCUMENT CHÍNH
                questionCount = questionsToSave.size
            )

            Log.d("check6767", questionsArray.toString())

            // --- BƯỚC 3: GHI VÀO FIRESTORE DƯỚI DẠNG TRANSACTION (ĐÃ CHỈNH SỬA) ---
            firestore.runTransaction { transaction ->
                val userRef = usersCollection.document(userId)
                val userSnapshot = transaction.get(userRef)
                // Lấy RizzPoints, dùng 0L nếu null
                val currentRizz = userSnapshot.getLong("rizzPoints") ?: 0L

                // Kiểm tra đủ tiền không
                if (currentRizz < cost) {
                    throw Exception("Bạn không đủ $cost Rizz Points để tạo Quiz!")
                }

                // A. LƯU QUIZ HEADER mới vào collection "quizzes"
                val quizRef = quizCollection.document(newQuizId)
                transaction.set(quizRef, quizHeader) // Lưu document chính

                // B. LƯU TỪNG QUESTION VÀO SUB-COLLECTION "questions"
                val questionCollectionRef = quizRef.collection("questions")
                questionsToSave.forEach { question ->
                    // Mỗi câu hỏi là một document riêng trong sub-collection
                    val questionRef = questionCollectionRef.document(question.id)
                    transaction.set(questionRef, question)
                }

                // C. Trừ tiền user và Unlock quiz
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