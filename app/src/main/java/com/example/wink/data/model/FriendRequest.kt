package com.example.wink.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

/**
 * Represents a friend request between two users
 * @param id The unique identifier of the friend request document
 * @param fromUserId The UID of the user who sent the request
 * @param toUserId The UID of the user who received the request
 * @param fromUsername The display name of the sender
 * @param fromAvatarUrl The avatar URL of the sender
 * @param status The current status of the request: "pending", "accepted", "rejected"
 * @param createdAt The timestamp when the request was created
 */
data class FriendRequest(
    @DocumentId
    @get:Exclude
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val fromUsername: String = "",
    val fromAvatarUrl: String = "",
    val status: String = "pending", // "pending", "accepted", "rejected"
    val createdAt: Timestamp = Timestamp.now()
)
