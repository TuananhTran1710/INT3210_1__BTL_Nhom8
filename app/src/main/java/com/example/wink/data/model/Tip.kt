package com.example.wink.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tip(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val price: Int = 0,
    val imageUrl: String? = null,
    val order: Int = 0,

    @get:com.google.firebase.firestore.Exclude
    var isLocked: Boolean = false
) : Parcelable