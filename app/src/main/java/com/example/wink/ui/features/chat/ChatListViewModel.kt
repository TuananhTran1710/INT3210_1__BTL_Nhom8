package com.example.wink.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Chat
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.compose.material.icons.filled.MoreVert // Icon 3 chấm
import androidx.compose.material.icons.filled.PushPin // Icon ghim (filled)
import androidx.compose.material.icons.outlined.PushPin

// Cập nhật UiChat để UI biết trạng thái pin
data class UiChat(
    val chat: Chat,
    val lastMessage: String,
    val displayName: String,
    val displayAvatarUrl: String?,
    val isPinned: Boolean, // MỚI
    val isAiChat: Boolean = false // MỚI: để phân biệt Wink AI
)
// Sự kiện điều hướng (Side Effect)
sealed class ChatListEffect {
    data class NavigateToChat(val chatId: String) : ChatListEffect()
    data class ShowError(val message: String) : ChatListEffect()
}

// Model rút gọn để hiển thị trong SearchBar
data class SearchFriendUi(
    val uid: String,
    val username: String,
    val avatarUrl: String?
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore // Inject thêm Firestore để lấy info user
) : ViewModel() {

    private val _chats = MutableStateFlow<List<UiChat>>(emptyList())
    val chats: StateFlow<List<UiChat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // --- State MỚI cho danh sách bạn bè (Search) ---
    private val _friends = MutableStateFlow<List<SearchFriendUi>>(emptyList())
    val friends: StateFlow<List<SearchFriendUi>> = _friends.asStateFlow()

    // --- Channel điều hướng ---
    private val _effect = Channel<ChatListEffect>()
    val effect = _effect.receiveAsFlow()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        loadChats()
        loadFriends()
    }
    // --- Logic MỚI: Tải danh sách bạn bè ---
    private fun loadFriends() {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            try {
                // Giả sử AuthRepository có hàm lấy User hiện tại
                // Nếu chưa có hàm getUserById, bạn cần thêm vào Interface AuthRepository
                val currentUser = authRepository.getUserById(myId)
                val friendIds = currentUser?.friendsList ?: emptyList()

                if (friendIds.isNotEmpty()) {
                    val users = authRepository.getUsersByIds(friendIds)
                    val friendUis = users.map {
                        SearchFriendUi(it.uid, it.username, it.avatarUrl)
                    }
                    _friends.value = friendUis
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Logic MỚI: Khi chọn bạn từ Search Bar -> Tạo chat ---
    fun onSearchFriendSelected(friendId: String) {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            // Tái sử dụng hàm findOrCreatePrivateChat mà bạn đã viết trong ChatRepository
            val result = chatRepository.findOrCreatePrivateChat(myId, friendId)

            result.onSuccess { chatId ->
                _effect.send(ChatListEffect.NavigateToChat(chatId))
            }.onFailure {
                _effect.send(ChatListEffect.ShowError("Lỗi tạo chat: ${it.message}"))
            }
        }
    }

    private fun loadChats() {
        val myId = currentUserId ?: return

        viewModelScope.launch {
            _isLoading.value = true

            // Lắng nghe thay đổi từ danh sách chat
            chatRepository.listenChats(myId).collect { rawChats ->
                // Xử lý song song từng đoạn chat để lấy thông tin người dùng
                val processedChats = rawChats.map { chat ->
                    async {
                        // 1. Lấy Last Message
                        val lastMessage = chatRepository.getLastMessage(chat.chatId)?.content ?: ""

                        // 2. Xử lý Tên và Avatar
                        var displayName = chat.name
                        var displayAvatar = chat.avatarUrl

                        // Nếu tên chat rỗng (chat 1-1 chưa đặt tên nhóm), tìm người kia
                        if (chat.name.isBlank()) {
                            val otherUserId = chat.participants.find { it != myId }
                            if (otherUserId != null) {
                                try {
                                    val userSnapshot = firestore.collection("users")
                                        .document(otherUserId)
                                        .get()
                                        .await()

                                    displayName = userSnapshot.getString("username") ?: "Người dùng"
                                    displayAvatar = userSnapshot.getString("avatarUrl")
                                } catch (e: Exception) {
                                    displayName = "Người dùng"
                                }
                            } else {
                                displayName = "Chat riêng"
                            }
                        }
                        // Kiểm tra xem user hiện tại có pin chat này không
                        val isPinned = chat.pinnedBy.containsKey(myId)
                        val pinnedAt = chat.pinnedBy[myId] ?: 0L
                        // Trả về object UiChat hoàn chỉnh
                        UiChat(
                            chat = chat,
                            lastMessage = lastMessage,
                            displayName = displayName,
                            displayAvatarUrl = displayAvatar,
                            isPinned = isPinned,
                            isAiChat = false // Chat thường
                        )
                    }
                }.awaitAll() // Chờ tất cả các coroutine con chạy xong
                // LOGIC SẮP XẾP QUAN TRỌNG
                val sortedChats = processedChats.sortedWith(
                    compareBy<UiChat> { !it.isPinned } // 1. Pinned lên đầu (false < true nên dùng ! để đảo ngược)
                        .thenBy { it.chat.pinnedBy[myId] } // 2. Nếu cùng Pinned -> Pin trước nằm trên (Ascending)
                        .thenByDescending { it.chat.updatedAt } // 3. Nếu không Pinned -> Mới nhất nằm trên
                )
                _chats.value = processedChats
                _isLoading.value = false
            }
        }
    }
    // Các hàm xử lý sự kiện từ UI
    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
        }
    }

    fun togglePinChat(chatId: String, isCurrentlyPinned: Boolean) {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            if (isCurrentlyPinned) {
                chatRepository.unpinChat(chatId, myId)
            } else {
                chatRepository.pinChat(chatId, myId)
            }
        }
    }
}

