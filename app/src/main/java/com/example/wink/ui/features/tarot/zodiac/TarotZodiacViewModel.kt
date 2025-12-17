package com.example.wink.ui.features.tarot.zodiac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.LoveAnalysisResponse
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.remote.OpenRouterApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class TarotZodiacEvent {
    object NavigateToResult : TarotZodiacEvent()
}

@HiltViewModel
class TarotZodiacViewModel @Inject constructor(
    private val openRouterApiService: OpenRouterApiService,
    private val json: Json
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotZodiacState())
    val uiState: StateFlow<TarotZodiacState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TarotZodiacEvent>()
    val events = _events.asSharedFlow()

    fun onYourSignSelected(sign: ZodiacSign) {
        _uiState.update { it.copy(yourSign = sign) }
    }

    fun onCrushSignSelected(sign: ZodiacSign) {
        _uiState.update { it.copy(crushSign = sign) }
    }

    fun onAnalyze() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Gọi AI
                val analysis = callAiAnalyzeZodiac(s.yourSign, s.crushSign)

                // 2. Lưu Cache
                ZodiacResultCache.lastResult = ZodiacCompatResult(
                    yourSign = s.yourSign,
                    crushSign = s.crushSign,
                    score = analysis.score,
                    message = analysis.comment
                )

                _uiState.update { it.copy(isLoading = false) }
                _events.emit(TarotZodiacEvent.NavigateToResult)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
                // Có thể thêm state error để hiển thị lên UI
            }
        }
    }

    private suspend fun callAiAnalyzeZodiac(sign1: ZodiacSign, sign2: ZodiacSign): LoveAnalysisResponse {
        val prompt = """
            Phân tích độ hợp nhau giữa cung ${sign1.displayName} và ${sign2.displayName}.
            Phong cách: Chiêm tinh học nhưng hài hước, thực tế, ngôn ngữ giới trẻ.
            
            Output duy nhất JSON (không markdown):
            {
              "score": (số nguyên 0-99),
              "comment": (lời phán khoảng 2-3 câu)
            }
            
            Lưu ý: score tỉ lệ thuận với độ tương hợp, nên những comment phải phù hợp với score nhé!
        """.trimIndent()

        val request = ChatGptRequest(
            model = "google/gemini-2.0-flash-exp:free",
            messages = listOf(ChatGptMessage("user", prompt)),
            maxTokens = 250
        )

        val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"
        val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)

        val content = response.choices.firstOrNull()?.message?.content ?: "{}"
        val jsonString = content.replace("```json", "").replace("```", "").trim()

        return json.decodeFromString(jsonString)
    }
}
