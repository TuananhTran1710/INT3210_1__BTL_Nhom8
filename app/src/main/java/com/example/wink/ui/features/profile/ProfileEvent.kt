package com.example.wink.ui.features.profile

sealed class ProfileEvent {
    object Refresh : ProfileEvent()
    object LogoutClick : ProfileEvent()
    data class AddFriendClick(val friendId: String) : ProfileEvent()
    data class MessageClick(val friendId: String) : ProfileEvent()
}
