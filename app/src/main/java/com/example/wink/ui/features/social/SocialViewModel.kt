package com.example.wink.ui.features.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SocialState())
    val uiState = _uiState.asStateFlow()

    // Mock User hiện tại (Sau này lấy từ AuthRepository)
    private val currentUser = User(
        uid = "me",
        email = "me@wink.com",
        username = "Tôi",
        gender = "male",
        preference = "female"
    )

    init {
        loadData()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // --- LOGIC BÀI ĐĂNG MỚI ---

    fun onFabClick() {
        _uiState.update { it.copy(isCreatingPost = true) }
    }

    fun onDismissPostDialog() {
        _uiState.update { it.copy(isCreatingPost = false, newPostContent = "") }
    }

    fun onPostContentChange(text: String) {
        _uiState.update { it.copy(newPostContent = text) }
    }

    fun onSendPost() {
        val content = _uiState.value.newPostContent
        if (content.isBlank()) return

        // Tạo bài post mới (Fake)
        val newPost = SocialPost(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            username = currentUser.username,
            avatarUrl = null,
            content = content,
            timestamp = System.currentTimeMillis(),
            likes = 0,
            comments = 0
        )

        // Cập nhật UI: Thêm bài mới vào đầu danh sách & Đóng dialog
        _uiState.update {
            it.copy(
                feedList = listOf(newPost) + it.feedList,
                isCreatingPost = false,
                newPostContent = ""
            )
        }
    }

    private fun loadData() {
        // ... (Code cũ giữ nguyên)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)
            val mockFeed = listOf(
                SocialPost("1", "u1", "Thánh Tán Gái", null, "Vừa đạt 1000 điểm RIZZ! Ai solo không? \uD83D\uDE0E", System.currentTimeMillis(), 120, 10),
                SocialPost("2", "u2", "Crush Hunter", null, "Đã hoàn thành bí kíp 'Đọc vị ngôn ngữ cơ thể'. App xịn quá!", System.currentTimeMillis(), 85, 5),
            )
            val mockLeaderboard = listOf(
                User("u1", "a@a.com", "Thánh Tán Gái", "", "", 1250, 10),
                User("u2", "c@c.com", "Crush Hunter", "", "", 980, 5),
            )
            _uiState.update {
                it.copy(isLoading = false, feedList = mockFeed, leaderboardList = mockLeaderboard)
            }
        }
    }
}