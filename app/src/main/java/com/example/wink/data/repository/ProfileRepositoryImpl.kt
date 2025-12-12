package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.User
import com.example.wink.ui.features.profile.FriendUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProfileRepository {
    
    // In-memory cache
    private var cachedProfile: User? = null
    private var cachedFriends: List<FriendUi> = emptyList()
    private var profileLastUpdated = 0L
    private var friendsLastUpdated = 0L
    
    // Cache TTL (5 phút)
    private val cacheTimeoutMs = 5 * 60 * 1000L
    
    // StateFlow để UI observe
    private val _profileData = MutableStateFlow<User?>(null)
    override val profileData: Flow<User?> = _profileData.asStateFlow()
    
    private val _friendsData = MutableStateFlow<List<FriendUi>>(emptyList())
    override val friendsData: Flow<List<FriendUi>> = _friendsData.asStateFlow()
    
    override suspend fun getCachedProfile(): User? {
        val now = System.currentTimeMillis()
        return if (cachedProfile != null && (now - profileLastUpdated) < cacheTimeoutMs) {
            Log.d("ProfileRepo", "Returning cached profile for ${cachedProfile?.username}")
            cachedProfile
        } else {
            Log.d("ProfileRepo", "Cache expired or empty, returning null")
            null
        }
    }
    
    override suspend fun fetchProfileFromNetwork(): User? {
        return try {
            val currentUser = firebaseAuth.currentUser ?: return null
            Log.d("ProfileRepo", "Fetching profile from Firestore for uid: ${currentUser.uid}")
            
            val doc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
                
            if (doc.exists()) {
                val user = User(
                    uid = doc.getString("uid") ?: currentUser.uid,
                    email = doc.getString("email"),
                    username = doc.getString("username") ?: "No Name",
                    gender = doc.getString("gender") ?: "",
                    preference = doc.getString("preference") ?: "",
                    rizzPoints = (doc.getLong("rizzPoints") ?: 0L).toInt(),
                    loginStreak = (doc.getLong("loginStreak") ?: 0L).toInt(),
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    friendsList = (doc.get("friendsList") as? List<String>) ?: emptyList(),
                    quizzesFinished = (doc.get("quizzesFinished") as? List<String>) ?: emptyList()
                )
                
                // Update cache
                cachedProfile = user
                profileLastUpdated = System.currentTimeMillis()
                
                // Update StateFlow
                _profileData.value = user
                
                Log.d("ProfileRepo", "Profile fetched and cached: ${user.username}")
                user
            } else {
                Log.w("ProfileRepo", "Profile document does not exist")
                null
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error fetching profile", e)
            null
        }
    }
    
    override suspend fun preloadProfile() {
        Log.d("ProfileRepo", "Preloading profile...")
        
        // Kiểm tra cache trước
        val cached = getCachedProfile()
        if (cached != null) {
            _profileData.value = cached
            Log.d("ProfileRepo", "Profile preloaded from cache")
            return
        }
        
        // Fetch từ network
        fetchProfileFromNetwork()
    }
    
    override suspend fun getCachedFriends(): List<FriendUi> {
        val now = System.currentTimeMillis()
        return if (cachedFriends.isNotEmpty() && (now - friendsLastUpdated) < cacheTimeoutMs) {
            Log.d("ProfileRepo", "Returning ${cachedFriends.size} cached friends")
            cachedFriends
        } else {
            Log.d("ProfileRepo", "Friends cache expired or empty")
            emptyList()
        }
    }
    
    override suspend fun fetchFriendsFromNetwork(): List<FriendUi> {
        return try {
            Log.d("ProfileRepo", "Fetching friends from network...")
            
            // Simulate network delay
            delay(800)
            
            // For now, return mock data (có thể thay bằng Firestore query thật)
            val mockFriends = listOf(
                FriendUi("friend1", "Alice Nguyễn", "https://picsum.photos/100/100?1", true),
                FriendUi("friend2", "Bob Trần", "https://picsum.photos/100/100?2", true),
                FriendUi("friend3", "Carol Lê", "https://picsum.photos/100/100?3", false),
                FriendUi("friend4", "David Phạm", "https://picsum.photos/100/100?4", true),
                FriendUi("friend5", "Eva Hoàng", "https://picsum.photos/100/100?5", false)
            )
            
            // Update cache
            cachedFriends = mockFriends
            friendsLastUpdated = System.currentTimeMillis()
            
            // Update StateFlow
            _friendsData.value = mockFriends
            
            Log.d("ProfileRepo", "Friends fetched and cached: ${mockFriends.size} items")
            mockFriends
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error fetching friends", e)
            emptyList()
        }
    }
    
    override suspend fun preloadFriends() {
        Log.d("ProfileRepo", "Preloading friends...")
        
        // Kiểm tra cache trước
        val cached = getCachedFriends()
        if (cached.isNotEmpty()) {
            _friendsData.value = cached
            Log.d("ProfileRepo", "Friends preloaded from cache")
            return
        }
        
        // Fetch từ network
        fetchFriendsFromNetwork()
    }
}