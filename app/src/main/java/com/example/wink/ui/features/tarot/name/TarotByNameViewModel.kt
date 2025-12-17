package com.example.wink.ui.features.tarot.name

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.LoveAnalysisResponse
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.remote.OpenRouterApiService
import com.example.wink.ui.features.tarot.name.results.TarotNameResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Object Cache để truyền dữ liệu sang màn hình kết quả
object NameResultCache {
    var lastResult: TarotNameResultState? = null
}

sealed class TarotNameNav {
    data class ShowResult(val yourName: String, val crushName: String) : TarotNameNav()
}

@HiltViewModel
class TarotNameViewModel @Inject constructor(
    private val openRouterApiService: OpenRouterApiService,
    private val json: Json
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotNameState())
    val uiState: StateFlow<TarotNameState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<TarotNameNav>()
    val navEvents = _navEvents.asSharedFlow()

    fun onYourNameChange(value: String) {
        _uiState.update { it.copy(yourName = value, errorMessage = null) }
    }

    fun onCrushNameChange(value: String) {
        _uiState.update { it.copy(crushName = value, errorMessage = null) }
    }

    fun onAnalyze() {
        val s = _uiState.value
        if (s.yourName.isBlank() || s.crushName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ cả 2 tên.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. Gọi AI phân tích
                val analysis = callAiAnalyzeName(s.yourName, s.crushName)

                // 2. Lưu vào Cache
                NameResultCache.lastResult = TarotNameResultState(
                    yourName = s.yourName,
                    crushName = s.crushName,
                    score = analysis.score,
                    message = analysis.comment,
                    initialized = true
                )

                // 3. Chuyển màn hình
                _navEvents.emit(
                    TarotNameNav.ShowResult(
                        yourName = s.yourName.trim(),
                        crushName = s.crushName.trim()
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Lỗi kết nối vũ trụ: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun callAiAnalyzeName(name1: String, name2: String): LoveAnalysisResponse {
        val prompt = """
            Phân tích độ hợp nhau giữa hai cái tên: "$name1" và "$name2".
            Hãy đóng vai một thầy bói GenZ hài hước, phán cực gắt nhưng vui vẻ.
            
            Output duy nhất JSON (không markdown):
            {
              "score": (số nguyên 0-99
                (nếu một trong hai người tên Trình thì hãy cho số nguyên đó là một số bất kỳ cao hơn 75)
              ),
              "comment": (lời phán ngắn gọn khoảng 2-3 câu, hài hước, teencode nhẹ)
            }
            
            Lưu ý: score tỉ lệ thuận với độ tương hợp, nên những comment phải phù hợp với score nhé!
        """.trimIndent()

        val request = ChatGptRequest(
            model = "google/gemini-2.0-flash-exp:free",
            messages = listOf(ChatGptMessage("user", prompt)),
            maxTokens = 200
        )

        val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"
        val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)

        val content = response.choices.firstOrNull()?.message?.content ?: throw Exception("AI không trả lời")

        // Clean JSON string (phòng trường hợp AI trả về ```json ... ```)
        val jsonString = content.replace("```json", "").replace("```", "").trim()

        return json.decodeFromString(jsonString)
    }
}