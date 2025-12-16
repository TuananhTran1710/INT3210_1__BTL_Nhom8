package com.example.wink.ui.features.social

import androidx.compose.runtime.Stable

@Stable
data class SocialActions(
    val onUserClick: (String) -> Unit,
    val onLikeClick: (String) -> Unit,
    val onCommentClick: (String) -> Unit,
    val onImageClick: (String) -> Unit,
    val onRetweetClick: (String) -> Unit,
    val onDeletePost: (String) -> Unit,
    val onEditPost: (String, String, List<String>) -> Unit,
    val onCreatePostClick: () -> Unit // Fab or Input bar click
)