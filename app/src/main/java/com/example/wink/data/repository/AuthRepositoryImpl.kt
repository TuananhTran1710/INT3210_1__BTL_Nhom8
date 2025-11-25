package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.User
import com.example.wink.ui.features.signup.SignupScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Được Hilt tiêm vào từ AppModule
     private val firestore: FirebaseFirestore // Sẽ cần cái này để lưu thông tin user chi tiết
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        // 1. Định nghĩa Listener
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // User đã đăng nhập -> Convert sang Model của App
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    username = firebaseUser.displayName ?: "No Name",
                    gender = "",
                    preference = "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                // Gửi dữ liệu vào Flow
                trySend(user)
                Log.d("AuthRepository", "Emitted User: ${user.email}")
            } else {
                // User chưa đăng nhập hoặc đã đăng xuất
                trySend(null)
                Log.d("AuthRepository", "Emitted Null")
            }
        }

        // 2. Đăng ký Listener với Firebase
        firebaseAuth.addAuthStateListener(authStateListener)

        // 3. Quan trọng: awaitClose giữ cho Flow sống và dọn dẹp khi Flow bị hủy
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun login(email: String, pass: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(email: String, pass: String, username: String): AuthResult {
        return try {
            // 1. Tạo tài khoản trên Firebase Auth
            firebaseAuth.createUserWithEmailAndPassword(email, pass).await()

            val firebaseUser = firebaseAuth.currentUser

            // 2. Cập nhật displayName
            firebaseUser?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )?.await()

            // 3. Tạo document user trong Firestore (collection "users")
            firebaseUser?.let { fbUser ->
                val userDoc = hashMapOf(
                    "uid" to fbUser.uid,
                    "email" to (fbUser.email ?: email),
                    "username" to username,
                    "rizzPoints" to 0L,
                    "friendsList" to emptyList<String>(),
                    "quizzesFinished" to emptyList<String>(),
                    "gender" to "",
                    "preference" to "",
                    "avatarUrl" to (fbUser.photoUrl?.toString() ?: "")
                )

                firestore.collection("users")
                    .document(fbUser.uid)
                    .set(userDoc)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup error", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun hasLoggedInUser(): Boolean {
        val user = firebaseAuth.currentUser
        Log.d("TestAuth", "currentUser = $user, uid = ${user?.uid}, email = ${user?.email}")
        return user != null
    }
}