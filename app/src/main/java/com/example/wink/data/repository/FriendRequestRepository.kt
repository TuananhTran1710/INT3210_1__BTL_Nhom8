package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.FriendRequest
import com.example.wink.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val friendRequestsCollection = firestore.collection("friendRequests")
    private val usersCollection = firestore.collection("users")

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Send a friend request from current user to another user
     */
    suspend fun sendFriendRequest(
        toUserId: String,
        fromUsername: String,
        fromAvatarUrl: String
    ): Result<Unit> {
        val fromUserId = currentUserId ?: return Result.failure(Exception("User not authenticated"))
        
        // Prevent sending friend request to yourself
        if (fromUserId == toUserId) {
            return Result.failure(Exception("Không thể kết bạn với chính mình"))
        }
        
        return try {
            // Check if request already exists
            val existingRequest = friendRequestsCollection
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (!existingRequest.isEmpty) {
                return Result.failure(Exception("Friend request already sent"))
            }

            // Check if they are already friends
            val userDoc = usersCollection.document(fromUserId).get().await()
            val friendsList = userDoc.get("friendsList") as? List<String> ?: emptyList()
            if (friendsList.contains(toUserId)) {
                return Result.failure(Exception("Already friends"))
            }

            // Create new friend request
            val requestData = hashMapOf(
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "fromUsername" to fromUsername,
                "fromAvatarUrl" to fromAvatarUrl,
                "status" to "pending",
                "createdAt" to Timestamp.now()
            )

            friendRequestsCollection.add(requestData).await()
            Log.d("FriendRequestRepo", "Friend request sent from $fromUserId to $toUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendRequestRepo", "Error sending friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Listen to pending friend requests for the current user (realtime)
     * Note: Removed orderBy to avoid needing composite index.
     * Sorting is done locally after fetching.
     */
    fun listenPendingRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = friendRequestsCollection
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendRequestRepo", "Error listening to friend requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                Log.d("FriendRequestRepo", "Received ${snapshot?.size()} pending requests and ${userId} ")

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FriendRequest(
                            id = doc.id,
                            fromUserId = doc.getString("fromUserId") ?: "",
                            toUserId = doc.getString("toUserId") ?: "",
                            fromUsername = doc.getString("fromUsername") ?: "",
                            fromAvatarUrl = doc.getString("fromAvatarUrl") ?: "",
                            status = doc.getString("status") ?: "pending",
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        Log.e("FriendRequestRepo", "Error parsing friend request", e)
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(requests)
                Log.d("FriendRequestRepo", "Received ${requests.size} pending requests")
            }

        awaitClose { listener.remove() }
    }

    /**
     * Accept a friend request
     */
    suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        val currentUid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Get the request
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val fromUserId = requestDoc.getString("fromUserId") 
                ?: return Result.failure(Exception("Invalid request"))
            val toUserId = requestDoc.getString("toUserId")
                ?: return Result.failure(Exception("Invalid request"))

            // Verify current user is the recipient
            if (toUserId != currentUid) {
                return Result.failure(Exception("Not authorized"))
            }

            // Use batch write for atomicity
            firestore.runBatch { batch ->
                // Update request status
                batch.update(friendRequestsCollection.document(requestId), "status", "accepted")

                // Add each other to friends lists
                batch.update(
                    usersCollection.document(fromUserId),
                    "friendsList", FieldValue.arrayUnion(toUserId)
                )
                batch.update(
                    usersCollection.document(toUserId),
                    "friendsList", FieldValue.arrayUnion(fromUserId)
                )
            }.await()

            Log.d("FriendRequestRepo", "Friend request accepted: $requestId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendRequestRepo", "Error accepting friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Reject/Cancel a friend request
     */
    suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        val currentUid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Verify and update
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val toUserId = requestDoc.getString("toUserId")
            
            if (toUserId != currentUid) {
                return Result.failure(Exception("Not authorized"))
            }

            friendRequestsCollection.document(requestId)
                .update("status", "rejected")
                .await()

            Log.d("FriendRequestRepo", "Friend request rejected: $requestId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendRequestRepo", "Error rejecting friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a friend request already exists between two users
     */
    suspend fun checkRequestStatus(targetUserId: String): FriendRequestStatus {
        val currentUid = currentUserId ?: return FriendRequestStatus.NOT_SENT
        
        return try {
            // Check if already friends
            val userDoc = usersCollection.document(currentUid).get().await()
            val friendsList = userDoc.get("friendsList") as? List<String> ?: emptyList()
            if (friendsList.contains(targetUserId)) {
                return FriendRequestStatus.ALREADY_FRIENDS
            }

            // Check if current user sent a pending request
            val sentRequest = friendRequestsCollection
                .whereEqualTo("fromUserId", currentUid)
                .whereEqualTo("toUserId", targetUserId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (!sentRequest.isEmpty) {
                return FriendRequestStatus.REQUEST_SENT
            }

            // Check if current user received a pending request
            val receivedRequest = friendRequestsCollection
                .whereEqualTo("fromUserId", targetUserId)
                .whereEqualTo("toUserId", currentUid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (!receivedRequest.isEmpty) {
                return FriendRequestStatus.REQUEST_RECEIVED
            }

            FriendRequestStatus.NOT_SENT
        } catch (e: Exception) {
            Log.e("FriendRequestRepo", "Error checking request status", e)
            FriendRequestStatus.NOT_SENT
        }
    }

    /**
     * Get user info by ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                User(
                    uid = doc.getString("uid") ?: userId,
                    email = doc.getString("email"),
                    username = doc.getString("username") ?: "",
                    gender = doc.getString("gender") ?: "",
                    preference = doc.getString("preference") ?: "",
                    rizzPoints = (doc.getLong("rizzPoints") ?: 0).toInt(),
                    loginStreak = (doc.getLong("loginStreak") ?: 0).toInt(),
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    friendsList = doc.get("friendsList") as? List<String> ?: emptyList()
                )
            } else null
        } catch (e: Exception) {
            Log.e("FriendRequestRepo", "Error getting user by ID", e)
            null
        }
    }
}

enum class FriendRequestStatus {
    NOT_SENT,           // No request exists
    REQUEST_SENT,       // Current user sent a request
    REQUEST_RECEIVED,   // Current user received a request
    ALREADY_FRIENDS     // Already friends
}
