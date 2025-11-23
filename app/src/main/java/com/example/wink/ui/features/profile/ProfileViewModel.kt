package com.example.wink.ui.features.profile

import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.util.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import kotlin.String



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<ProfileState, ProfileEvent>() {

    override val uiState: StateFlow<ProfileState>
        get() = _uiState

    override fun getInitialState(): ProfileState = ProfileState(
        // state ban đầu

    )

    init{
        userInit()
        loadFriends()

        }
        // load dữ liệu ban đầu


    override fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Refresh -> {
                refresh()
            }
            ProfileEvent.LogoutClick -> {
                logout()
            }
            is ProfileEvent.AddFriendClick -> {
                addFriend(event.friendId)
            }
            is ProfileEvent.MessageClick -> {
                openChat(event.friendId)
            }
        }
    }
    private fun refresh () {

    }
    private fun logout () {

    }
    private fun addFriend (friendId: String) {

    }

    private fun openChat (friendId: String) {

    }
    private fun userInit () {
        // get the latest value from authRepository
        // run in a coroutine scope
        // _uiState is can be updated
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    user?.username?:"",
                    user?.avatarUrl?:"",
                    user?.rizzPoints?:0
                )
            }
        }
    }
    private fun loadFriends()  {
        viewModelScope.launch {
            val fakeFriends = listOf(
            FriendUi("1", "Ngọc", null, true),
            FriendUi("2", "Minh", null, true),
            FriendUi("3", "Hà", null, false),)
            _uiState.value = _uiState.value.copy(friends = fakeFriends, friendCount = fakeFriends.size)
        }
    }
}
