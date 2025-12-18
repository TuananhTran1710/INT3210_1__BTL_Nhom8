package com.example.wink.ui.features.chat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.coroutines.flow.combine

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
    private val firestore: FirebaseFirestore, // Inject thêm Firestore để lấy info user
    private val application: Application,
) : ViewModel() {

    private val sharedPreferences = application.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "ai_name" -> _aiName.value = sharedPreferences.getString("ai_name", "Lan Anh") ?: "Lan Anh"
            "ai_avatar_uri" -> _aiAvatarUrl.value = sharedPreferences.getString("ai_avatar_uri", null)
        }
    }

    private val _chats = MutableStateFlow<List<UiChat>>(emptyList())
    val chats: StateFlow<List<UiChat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // --- State MỚI cho danh sách bạn bè (Search) ---
    private val _friends = MutableStateFlow<List<SearchFriendUi>>(emptyList())
    val friends: StateFlow<List<SearchFriendUi>> = _friends.asStateFlow()

    private val _aiName = MutableStateFlow("Lan Anh")
    val aiName = _aiName.asStateFlow()

    private val _aiAvatarUrl = MutableStateFlow<String?>(null)
    val aiAvatarUrl = _aiAvatarUrl.asStateFlow()

    private val _aiLastMessage = MutableStateFlow("...")
    val aiLastMessage = _aiLastMessage.asStateFlow()


    // --- Channel điều hướng ---
    private val _effect = Channel<ChatListEffect>()
    val effect = _effect.receiveAsFlow()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        loadAiSettings()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        loadChats()
        loadFriends()
        loadAiLastMessage()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun loadAiSettings() {
        _aiName.value = sharedPreferences.getString("ai_name", "Lan Anh") ?: "Lan Anh"
        _aiAvatarUrl.value = sharedPreferences.getString("ai_avatar_uri", null)
    }

    private fun loadAiLastMessage() {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            chatRepository.listenAiMessages(myId).collect { messages ->
                val lastMessage = messages.firstOrNull()
                val messageContent = lastMessage?.content ?: "Bắt đầu trò chuyện nào!"
                val prefix = if (lastMessage?.senderId == myId) "Bạn: " else ""
                _aiLastMessage.value = if (lastMessage?.mediaUrl?.isNotEmpty() == true) "Hình ảnh" else "$prefix$messageContent"
            }
        }
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

    // --- SỬA LẠI LOGIC HÀM NÀY ---
    private fun loadChats() {
        val myId = currentUserId ?: return

        viewModelScope.launch {
            _isLoading.value = true

            combine(
                chatRepository.listenChats(myId),
                _friends
            ) { rawChats, friendList ->

                val chatsNeedsFixing = mutableListOf<Chat>()

                val processedChats = rawChats.map { chat ->
                    // 1. Logic Unread
                    val isUnread = if (chat.lastMessage.isNotEmpty()) {
                        !chat.lastReadBy.contains(myId)
                    } else false

                    // 2. Logic Tên & Avatar (LÀM LẠI CHO CHẮC CHẮN)
                    var displayName = chat.name
                    var displayAvatar = chat.avatarUrl

                    // Xác định chat 1-1: Nếu tên rỗng HOẶC chỉ có 2 người tham gia
                    val otherUserId = chat.participants.find { it != myId }
                    val isPrivateChat = otherUserId != null && chat.participants.size == 2

                    // Chỉ xử lý override nếu là chat 1-1
                    if (isPrivateChat && otherUserId != null) {
                        // Lấy thông tin từ các nguồn
                        val cachedInfo = chat.participantInfo[otherUserId]
                        val friendInfo = friendList.find { it.uid == otherUserId }

                        // --- BƯỚC A: Xử lý Tên hiển thị ---
                        if (displayName.isBlank()) {
                            // Ưu tiên Cache -> Friend -> "Người dùng"
                            displayName = cachedInfo?.get("name")
                                ?: friendInfo?.username
                                        ?: "Người dùng"
                        }

                        // --- BƯỚC B: Xử lý Avatar (BUG CŨ NẰM Ở ĐÂY) ---
                        // Nếu avatar gốc rỗng -> Tìm trong Cache -> Tìm trong Friend
                        if (displayAvatar.isNullOrBlank()) {
                            displayAvatar = cachedInfo?.get("avatar") // Thử lấy từ cache

                            // Nếu cache vẫn null/rỗng -> Lấy từ Friend
                            if (displayAvatar.isNullOrBlank() && friendInfo != null) {
                                displayAvatar = friendInfo.avatarUrl
                            }
                        }

                        // --- BƯỚC C: Kiểm tra xem có cần fix data không ---
                        // Nếu Cache thiếu Tên HOẶC thiếu Avatar mà Friend lại có -> Cần update Cache
                        val isCacheMissing = cachedInfo == null || cachedInfo["name"].isNullOrBlank() || cachedInfo["avatar"].isNullOrBlank()

                        // Chỉ fix nếu cache thiếu, để lần sau load cho nhanh
                        if (isCacheMissing) {
                            chatsNeedsFixing.add(chat)
                        }
                    }

                    // 3. Logic nội dung
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

                Pair(processedChats, chatsNeedsFixing)

            }.collect { (processedChats, chatsToFix) ->
                _chats.value = processedChats.sortedWith(
                    compareBy<UiChat> { !it.isPinned }
                        .thenBy { it.chat.pinnedBy[myId] }
                        .thenByDescending { it.chat.updatedAt }
                )
                _isLoading.value = false

                if (chatsToFix.isNotEmpty()) {
                    fixOldChatsInBackground(chatsToFix, myId)
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

