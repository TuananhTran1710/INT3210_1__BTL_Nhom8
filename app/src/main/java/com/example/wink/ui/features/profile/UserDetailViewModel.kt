package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.FriendRequestRepository
import com.example.wink.data.repository.FriendRequestStatus
import com.example.wink.data.repository.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserDetailState(
    val user: User? = null,
    val userPosts: List<SocialPost> = emptyList(),
    val isLoading: Boolean = true,
    val friendRequestStatus: FriendRequestStatus = FriendRequestStatus.NOT_SENT,
    val isSendingRequest: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val socialRepository: SocialRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(UserDetailState())
    val uiState = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadCurrentUser()
        loadUserProfile()
        loadUserPosts()
        checkFriendRequestStatus()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUser = user
            }
        }
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

    private var postsJob: kotlinx.coroutines.Job? = null

    private fun loadUserPosts() {
        // Cancel previous job to avoid multiple listeners
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            try {
                socialRepository.getUserPosts(userId).collectLatest { posts ->
                    _uiState.update { it.copy(userPosts = posts) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkFriendRequestStatus() {
        viewModelScope.launch {
            try {
                val status = friendRequestRepository.checkRequestStatus(userId)
                _uiState.update { it.copy(friendRequestStatus = status) }
                Log.d("UserDetailViewModel", "Friend request status: $status")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error checking friend request status", e)
            }
        }
    }

    fun sendFriendRequest() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSendingRequest = true,
                    errorMessage = null
                )
            }

            try {
                friendRequestRepository.sendFriendRequest(
                    toUserId = userId,
                    fromUsername = user.username,
                    fromAvatarUrl = user.avatarUrl
                ).getOrThrow()

                _uiState.update {
                    it.copy(
                        isSendingRequest = false,
                        friendRequestStatus = FriendRequestStatus.REQUEST_SENT,
                        successMessage = "Đã gửi lời mời kết bạn"
                    )
                }
                Log.d("UserDetailViewModel", "Friend request sent successfully")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error sending friend request", e)
                _uiState.update {
                    it.copy(
                        isSendingRequest = false,
                        errorMessage = e.message ?: "Không thể gửi lời mời kết bạn"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun sendMessage() {
        // TODO: Tạo chat room và navigate
    }
}