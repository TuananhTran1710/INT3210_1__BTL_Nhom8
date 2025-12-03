package com.example.wink.data.repository

import com.example.wink.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun saveUserProfile(user: User) {
        val uid = user.uid
        firestore.collection("users")
            .document(uid)
            .set(user)
            .await()
    }

    override suspend fun getCurrentUid(): String? = auth.currentUser?.uid
    override suspend fun getCurrentUserEmail(): String? = auth.currentUser?.email
}