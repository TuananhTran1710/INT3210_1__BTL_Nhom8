package com.example.wink.data.model

import com.google.firebase.Timestamp

/**
 * Represents a notification in the app
 * @param id The unique identifier of the notification
 * @param type The type of notification
 * @param title The title of the notification
 * @param message The message content of the notification
 * @param fromUserId The UID of the user who triggered this notification (if applicable)
 * @param fromUsername The display name of the user who triggered this notification
 * @param fromAvatarUrl The avatar URL of the user who triggered this notification
 * @param relatedId The ID of the related object (e.g., friend request ID, post ID)
 * @param isRead Whether the notification has been read
 * @param createdAt The timestamp when the notification was created
 */
data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val title: String = "",
    val message: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromAvatarUrl: String = "",
    val relatedId: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Types of notifications supported in the app
 */
enum class NotificationType {
    FRIEND_REQUEST,           // Someone sent you a friend request
    FRIEND_REQUEST_ACCEPTED,  // Someone accepted your friend request
    LIKE_POST,                // Someone liked your post
    COMMENT_POST,             // Someone commented on your post
    NEW_MESSAGE,              // New message received
    DAILY_REMINDER,           // Daily check-in reminder
    REWARD_EARNED,            // User earned a reward
    GENERAL                   // General notification
}
