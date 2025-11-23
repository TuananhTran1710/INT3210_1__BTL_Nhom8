package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.User
import com.example.wink.ui.features.signup.SignupScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Được Hilt tiêm vào từ AppModule
    // private val firestore: FirebaseFirestore // Sẽ cần cái này để lưu thông tin user chi tiết
) : AuthRepository {

    override val currentUser: Flow<User?> = flow {
        // Lắng nghe thay đổi trạng thái đăng nhập từ Firebase
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Convert Firebase User -> App User Model
                // Tạm thời chỉ lấy email, sau này sẽ fetch thêm từ Firestore
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    username = firebaseUser.displayName ?: "No Name",
                    gender = "",
                    preference = ""
                )
                // emit(user) - Lưu ý: Flow trong listener cần xử lý callbackFlow,
                // nhưng để đơn giản bạn có thể dùng firebaseAuth.currentUser trực tiếp trong các hàm khác
            } else {
                // emit(null)
            }
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
            // 1. Tạo tài khoản trên Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, pass).await()

            // 2. Cập nhật displayName = username (tuỳ chọn, nhưng giống logic cũ hơn)
            val user = firebaseAuth.currentUser
            user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )?.await()

            // 3. Trả về thành công
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
    }

    override suspend fun hasLoggedInUser(): Boolean {
        val user = firebaseAuth.currentUser
        Log.d("TestAuth", "currentUser = $user, uid = ${user?.uid}, email = ${user?.email}")
        return user != null
    }
}