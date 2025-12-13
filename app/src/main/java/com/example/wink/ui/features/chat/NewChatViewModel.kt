package com.example.wink.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sự kiện điều hướng (Side Effect)
sealed class NewChatEffect {
    data class NavigateToChat(val chatId: String) : NewChatEffect()
    data class ShowError(val message: String) : NewChatEffect()
}

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state = _state.asStateFlow()

    private val _effect = Channel<NewChatEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // 1. Lấy user hiện tại để lấy danh sách ID bạn bè
                val currentUser = authRepository.currentUser.firstOrNull()
                if (currentUser == null) {
                    _state.update { it.copy(isLoading = false, error = "Chưa đăng nhập") }
                    return@launch
                }

                val friendIds = currentUser.friendsList
                if (friendIds.isEmpty()) {
                    _state.update { it.copy(isLoading = false, allFriends = emptyList(), filteredFriends = emptyList()) }
                    return@launch
                }

                // 2. Lấy thông tin chi tiết các bạn bè từ ID
                val users = authRepository.getUsersByIds(friendIds)

                // 3. Map sang UI Model
                val friendUis = users.map { user ->
                    NewChatFriendUi(
                        uid = user.uid,
                        username = user.username,
                        avatarUrl = user.avatarUrl,
                        email = user.email
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        allFriends = friendUis,
                        filteredFriends = friendUis
                    )
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.allFriends
            } else {
                currentState.allFriends.filter { friend ->
                    friend.username.contains(query, ignoreCase = true) ||
                            (friend.email?.contains(query, ignoreCase = true) == true)
                }
            }
            currentState.copy(searchQuery = query, filteredFriends = filtered)
        }
    }

    fun onFriendSelected(friendId: String) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.firstOrNull() ?: return@launch

            _state.update { it.copy(isLoading = true) }

            // Logic giống hệt Profile: Tìm hoặc tạo chat 1-1
            val result = chatRepository.findOrCreatePrivateChat(currentUser.uid, friendId)

            result.onSuccess { chatId ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(NewChatEffect.NavigateToChat(chatId))
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(NewChatEffect.ShowError(e.message ?: "Lỗi tạo chat"))
            }
        }
    }
}