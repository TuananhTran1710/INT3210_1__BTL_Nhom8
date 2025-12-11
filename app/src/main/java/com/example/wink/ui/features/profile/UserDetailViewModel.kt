package com.example.wink.ui.features.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.User
import com.example.wink.data.repository.UserRepository // Import Repository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// 1. Định nghĩa các sự kiện bắn ra UI (để hiện Toast)
sealed class UserDetailSideEffect {
    object FriendRequestSuccess : UserDetailSideEffect()
    data class ShowError(val message: String) : UserDetailSideEffect()
}

data class UserDetailState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isFriend: Boolean = false,
    val requestSent: Boolean = false // Trạng thái để đổi nút "Kết bạn" thành "Đã gửi"
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository // <--- THÊM DÒNG NÀY (Inject Repository)
) : ViewModel() {

    // Lấy ID người đang xem từ Navigation arguments
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(UserDetailState())
    val uiState = _uiState.asStateFlow()

    // Channel để bắn thông báo ra UI
    private val _effectChannel = Channel<UserDetailSideEffect>()
    val effect = _effectChannel.receiveAsFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // (Sau này bạn nên chuyển logic này vào Repository luôn nhé)
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

    // Hàm gửi kết bạn (Không cần truyền tham số vì đã có userId ở trên)
    fun sendFriendRequest() {
        viewModelScope.launch {
            // 1. Bật loading
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 2. Gọi Repository để ghi vào DB
                // userId ở đây chính là ID của người mà bạn đang xem profile
                userRepository.sendAddFriendRequest(userId)

                // 3. Cập nhật UI: Đã gửi thành công
                _uiState.update { it.copy(requestSent = true, isLoading = false) }

                // 4. Bắn tín hiệu để hiện Toast
                _effectChannel.send(UserDetailSideEffect.FriendRequestSuccess)

            } catch (e: Exception) {
                e.printStackTrace()
                // Tắt loading và báo lỗi
                _uiState.update { it.copy(isLoading = false) }
                _effectChannel.send(UserDetailSideEffect.ShowError(e.message ?: "Lỗi không xác định"))
            }
        }
    }

    fun sendMessage() {
        // TODO: Tạo chat room và navigate
    }
}