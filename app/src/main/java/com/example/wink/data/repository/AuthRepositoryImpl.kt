package com.example.wink.data.repository

import android.net.Uri
import android.util.Log
import com.example.wink.data.model.User
import com.example.wink.ui.features.signup.SignupScreen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Được Hilt tiêm vào từ AppModule
    private val firestore: FirebaseFirestore, // Sẽ cần cái này để lưu thông tin user chi tiết
    private val storage: FirebaseStorage
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        // 1. Định nghĩa Listener
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Lấy thông tin user từ Firestore
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            Log.e("AuthRepository", "Error listening to user document", error)
                            // Fallback to basic user info từ Firebase Auth
                            val basicUser = User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                username = firebaseUser.displayName ?: "No Name",
                                gender = "",
                                preference = "",
                                avatarUrl = firebaseUser.photoUrl?.toString() ?: ""
                            )
                            trySend(basicUser)
                            return@addSnapshotListener
                        }

                        if (document != null && document.exists()) {
                            try {
                                val data = document.data!!
                                val user = User(
                                    uid = data["uid"] as String,
                                    email = data["email"] as? String,
                                    username = data["username"] as String,
                                    gender = data["gender"] as? String ?: "",
                                    preference = data["preference"] as? String ?: "",
                                    rizzPoints = (data["rizzPoints"] as? Long)?.toInt() ?: 0,
                                    loginStreak = (data["loginStreak"] as? Long)?.toInt() ?: 0,
                                    avatarUrl = data["avatarUrl"] as? String ?: "",
                                    lastCheckInDate = data["lastCheckInDate"] as? Timestamp,
                                    friendsList = data["friendsList"] as? List<String> ?: emptyList(),
                                    quizzesFinished = data["quizzesFinished"] as? List<String> ?: emptyList()
                                )
                                trySend(user)
                                Log.d("AuthRepository", "Emitted User from Firestore: ${user.username}")
                            } catch (e: Exception) {
                                Log.e("AuthRepository", "Error parsing user data", e)
                                // Fallback to basic user info
                                val basicUser = User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email,
                                    username = firebaseUser.displayName ?: "No Name",
                                    gender = "",
                                    preference = "",
                                    avatarUrl = firebaseUser.photoUrl?.toString() ?: ""
                                )
                                trySend(basicUser)
                            }
                        } else {
                            // Document doesn't exist, tạo basic user
                            val basicUser = User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                username = firebaseUser.displayName ?: "No Name",
                                gender = "",
                                preference = "",
                                avatarUrl = firebaseUser.photoUrl?.toString() ?: ""
                            )
                            trySend(basicUser)
                        }
                    }
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
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(2000, Calendar.JANUARY, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val veryOldDate = Timestamp(cal.time)
                val userDoc = hashMapOf(
                    "uid" to fbUser.uid,
                    "email" to (fbUser.email ?: email),
                    "username" to username,
                    "rizzPoints" to 0L,
                    "friendsList" to emptyList<String>(),
                    "quizzesFinished" to emptyList<String>(),
                    "gender" to "",
                    "preference" to "",
                    "avatarUrl" to (fbUser.photoUrl?.toString() ?: ""),
                    "streak" to 0,
                    "longestStreak" to 0,
                    "lastCheckInDate" to veryOldDate,        // Firestore sẽ lưu null
                    "createdAt" to Timestamp.now()
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
//
//    // Methods để update database
//    override
//    suspend fun updateRizzPoints(newPoints: Int): AuthResult {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//                ?: return Result.failure(Exception("User not authenticated"))
//
//            // Use set with merge to handle missing documents
//            firestore.collection("users")
//                .document(currentUser.uid)
//                .set(
//                    mapOf("rizzPoints" to newPoints.toLong()),
//                    com.google.firebase.firestore.SetOptions.merge()
//                )
//                .await()
//
//            Log.d("AuthRepository", "RIZZ points updated to: $newPoints")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Update RIZZ points error", e)
//            Result.failure(e)
//        }
//    }
//
//    override
//    suspend fun updateUsername(newUsername: String): AuthResult {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//                ?: return Result.failure(Exception("User not authenticated"))
//
//            // Use set with merge to handle missing documents
//            firestore.collection("users")
//                .document(currentUser.uid)
//                .set(
//                    mapOf("username" to newUsername),
//                    com.google.firebase.firestore.SetOptions.merge()
//                )
//                .await()
//
//            // Update Firebase Auth display name
//            currentUser.updateProfile(
//                com.google.firebase.auth.UserProfileChangeRequest.Builder()
//                    .setDisplayName(newUsername)
//                    .build()
//            )?.await()
//
//            Log.d("AuthRepository", "Username updated to: $newUsername")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Update username error", e)
//            Result.failure(e)
//        }
//    }
//
//    override
//    suspend fun addFriend(friendUid: String): AuthResult {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//                ?: return Result.failure(Exception("User not authenticated"))
//
//            // Use set with merge to handle missing documents
//            firestore.collection("users")
//                .document(currentUser.uid)
//                .set(
//                    mapOf("friendsList" to com.google.firebase.firestore.FieldValue.arrayUnion(friendUid)),
//                    com.google.firebase.firestore.SetOptions.merge()
//                )
//                .await()
//
//            Log.d("AuthRepository", "Friend added: $friendUid")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Add friend error", e)
//            Result.failure(e)
//        }
//    }
//
override suspend fun performDailyCheckIn(): AuthResult {
    return try {
        val currentUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("User not authenticated"))

        val userDoc = firestore.collection("users").document(currentUser.uid)

        firestore.runTransaction { tx ->
            val snap = tx.get(userDoc)

            val now = com.google.firebase.Timestamp.now()

            val offsetSeconds = TimeZone.getDefault().rawOffset.toLong() / 1000L

            fun dayNumber(ts: com.google.firebase.Timestamp?): Long? {
                return ts?.seconds?.let { (it + offsetSeconds) / 86400L }
            }

            val todayDay = (now.seconds + offsetSeconds) / 86400L

            val lastAny = snap.get("lastCheckInDate")
            val lastTs = lastAny as? com.google.firebase.Timestamp
            val lastDay = dayNumber(lastTs)

            val oldStreak = (snap.getLong("loginStreak") ?: 0L).toInt()
            val oldLongest = (snap.getLong("longestStreak") ?: 0L).toInt()
            val oldRizz = (snap.getLong("rizzPoints") ?: 0L).toInt()

            // Debug nếu cần
            Log.d("AuthRepository", "todayDay=$todayDay lastDay=$lastDay now=$now lastTs=$lastTs")

            // 🔹 Nếu cùng "ngày local" -> coi như đã check-in hôm nay
            if (lastDay == todayDay) {
                return@runTransaction null
            }

            val newStreak = when (lastDay) {
                todayDay - 1 -> oldStreak + 1   // check-in liên tiếp
                else -> 1                       // bị đứt quãng -> reset
            }

            val newLongest = kotlin.math.max(newStreak, oldLongest)
            val newRizz = oldRizz + 10

            tx.set(
                userDoc,
                mapOf(
                    "loginStreak" to newStreak.toLong(),
                    "longestStreak" to newLongest.toLong(),
                    "lastCheckInDate" to now,
                    "rizzPoints" to newRizz.toLong()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            null
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Daily check-in error", e)
        Result.failure(e)
    }
}

    override suspend fun uploadAvatar(uri: Uri): Result<String> {
        return try {
            // Đặt tên file theo UID để mỗi user chỉ có 1 ảnh avatar (tiết kiệm dung lượng)
            // Hoặc dùng UUID nếu muốn lưu lịch sử
            val uid = firebaseAuth.currentUser?.uid ?: UUID.randomUUID().toString()
            val ref = storage.reference.child("avatars/$uid.jpg")

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. UPDATE PROFILE (Không còn Bio)
    override suspend fun updateUserProfile(
        uid: String,
        username: String,
        avatarUrl: String
    ): Result<Unit> {
        return try {
            // 1. Chuẩn bị dữ liệu update cho User Collection
            val userUpdates = mapOf(
                "username" to username,
                "avatarUrl" to avatarUrl
            )

            // Khởi tạo Batch (Ghi hàng loạt)
            val batch = firestore.batch()

            // A. Thêm lệnh update User vào batch
            val userRef = firestore.collection("users").document(uid)
            batch.update(userRef, userUpdates)

            // B. Tìm tất cả bài viết của User này để update theo
            // (Lưu ý: Nếu user có quá nhiều bài > 500, cần chia nhỏ batch,
            // nhưng với bài tập lớn thì làm thế này là ok)
            val postsSnapshot = firestore.collection("posts")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            for (document in postsSnapshot.documents) {
                // Thêm lệnh update từng bài viết vào batch
                batch.update(
                    document.reference,
                    mapOf(
                        "username" to username,
                        "avatarUrl" to avatarUrl
                    )
                )
            }

            // 2. Thực thi tất cả lệnh cùng lúc
            batch.commit().await()

            // 3. Cập nhật Firebase Auth (để đồng bộ hiển thị cục bộ nếu cần)
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(username)

            if (avatarUrl.isNotBlank()) {
                profileUpdates.setPhotoUri(android.net.Uri.parse(avatarUrl))
            }

            firebaseAuth.currentUser?.updateProfile(profileUpdates.build())?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 3. UPDATE EMAIL
    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            // Lưu ý: User phải đăng nhập gần đây mới đổi được email
            firebaseAuth.currentUser?.updateEmail(newEmail)?.await()

            firebaseAuth.currentUser?.uid?.let { uid ->
                firestore.collection("users").document(uid).update("email", newEmail).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//
//    override
//    suspend fun ensureUserDocumentExists(): AuthResult {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//                ?: return Result.failure(Exception("User not authenticated"))
//
//            // Create user document with only the fields that should exist
//            val defaultUserData = mapOf(
//                "uid" to currentUser.uid,
//                "email" to (currentUser.email ?: ""),
//                "username" to (currentUser.displayName ?: "Unknown"),
//                "rizzPoints" to 0L,
//                "friendsList" to emptyList<String>(),
//                "quizzesFinished" to emptyList<String>(),
//                "gender" to "",
//                "preference" to "",
//                "avatarUrl" to (currentUser.photoUrl?.toString() ?: "")
//            )
//
//            firestore.collection("users")
//                .document(currentUser.uid)
//                .set(defaultUserData, com.google.firebase.firestore.SetOptions.merge())
//                .await()
//
//            Log.d("AuthRepository", "User document ensured for: ${currentUser.uid}")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Ensure user document error", e)
//            Result.failure(e)
//        }
//    }
}