package com.example.wink.data.repository

import com.example.wink.data.model.User
import com.example.wink.ui.features.profile.FriendUi
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    // Lấy profile của user hiện tại (đã cache)
    suspend fun getCachedProfile(): User?
    
    // Fetch profile từ network và cache
    suspend fun fetchProfileFromNetwork(): User?
    
    // Preload dữ liệu Profile (gọi khi app khởi tạo)
    suspend fun preloadProfile()
    
    // Lấy danh sách bạn bè (cache + network)
    suspend fun getCachedFriends(): List<FriendUi>
    suspend fun fetchFriendsFromNetwork(): List<FriendUi>
    suspend fun preloadFriends()
    
    // Observable để UI theo dõi
    val profileData: Flow<User?>
    val friendsData: Flow<List<FriendUi>>
}