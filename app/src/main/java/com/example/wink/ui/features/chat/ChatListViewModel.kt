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
    val isAiChat: Boolean = false, // MỚI: để phân biệt Wink AI
    val isUnread: Boolean = false // MỚI: Trạng thái chưa đọc
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

            // Lắng nghe Realtime - Code chạy ĐỒNG BỘ, không có async/await chặn luồng
            chatRepository.listenChats(myId).collect { rawChats ->

                // Danh sách các chat cũ bị thiếu thông tin cần fix
                val chatsNeedsFixing = mutableListOf<Chat>()

                val processedChats = rawChats.map { chat ->
                    // 1. Logic Unread
                    val isUnread = if (chat.lastMessage.isNotEmpty()) {
                        !chat.lastReadBy.contains(myId)
                    } else false

                    // 2. Logic Tên & Avatar
                    var displayName = chat.name
                    var displayAvatar = chat.avatarUrl

                    if (chat.name.isBlank()) { // Chat 1-1
                        val otherUserId = chat.participants.find { it != myId }

                        if (otherUserId != null) {
                            // Lấy từ Cache
                            val cachedInfo = chat.participantInfo[otherUserId]

                            if (cachedInfo != null && !cachedInfo["name"].isNullOrBlank()) {
                                // CASE 1: Đã có cache (Chat mới hoặc đã fix) -> Hiển thị ngay
                                displayName = cachedInfo["name"] ?: "Người dùng"
                                displayAvatar = cachedInfo["avatar"]
                            } else {
                                // CASE 2: Dữ liệu cũ (Chưa có cache)
                                // -> Tạm thời hiển thị placeholder hoặc "Đang tải..."
                                // -> Đẩy vào danh sách cần fix
                                displayName = "..."
                                chatsNeedsFixing.add(chat)
                            }
                        }
                    }

                    // 3. Logic nội dung tin nhắn
                    val prefix = if (chat.lastSenderId == myId) "Bạn: " else ""
                    val finalContent = if (chat.lastMessage.isNotBlank()) "$prefix${chat.lastMessage}" else "Bắt đầu trò chuyện"

                    UiChat(
                        chat = chat,
                        lastMessage = finalContent,
                        displayName = displayName,
                        displayAvatarUrl = displayAvatar,
                        isPinned = chat.pinnedBy.containsKey(myId),
                        isAiChat = false,
                        isUnread = isUnread
                    )
                }

                // 4. Update UI ngay lập tức (Không chờ fix)
                _chats.value = processedChats.sortedWith(
                    compareBy<UiChat> { !it.isPinned }
                        .thenBy { it.chat.pinnedBy[myId] }
                        .thenByDescending { it.chat.updatedAt }
                )
                _isLoading.value = false

                // 5. Kích hoạt luồng chạy ngầm để sửa dữ liệu cũ (Background Auto-Fix)
                if (chatsNeedsFixing.isNotEmpty()) {
                    fixOldChatsInBackground(chatsNeedsFixing, myId)
                }
            }
        }
    }
    /**
     * Hàm chạy ngầm: Tự động điền thông tin còn thiếu vào Firestore
     * Khi hàm này chạy xong -> Firestore update -> Trigger lại listenChats -> UI tự cập nhật tên đúng
     */
    private fun fixOldChatsInBackground(chatsToFix: List<Chat>, myId: String) {
        viewModelScope.launch {
            chatsToFix.forEach { chat ->
                val otherUserId = chat.participants.find { it != myId } ?: return@forEach
                // Gọi hàm sửa lỗi trong Repository
                chatRepository.migrateChatInfo(chat.chatId, otherUserId)
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

