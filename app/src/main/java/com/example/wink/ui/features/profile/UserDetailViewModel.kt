package com.example.wink.ui.features.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserDetailState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isFriend: Boolean = false, // Giả lập trạng thái bạn bè
    val requestSent: Boolean = false
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(UserDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Lấy thông tin user từ Firestore
                val snapshot = firestore.collection("users").document(userId).get().await()
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(uid = snapshot.id)
                    _uiState.update { it.copy(user = user, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendFriendRequest() {
        // TODO: Gọi API gửi kết bạn (UC05)
        // Tạm thời update UI giả
        _uiState.update { it.copy(requestSent = true) }
    }

    fun sendMessage() {
        // TODO: Tạo chat room và navigate
    }
}