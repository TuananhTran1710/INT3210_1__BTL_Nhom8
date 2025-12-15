package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.DailyTask
import com.example.wink.data.model.TaskType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskRepository {

    private val _taskCompletionEvent = MutableSharedFlow<String>()
    override val taskCompletionEvent = _taskCompletionEvent.asSharedFlow()

    private val currentUserId get() = auth.currentUser?.uid ?: ""

    // Lấy ngày hiện tại dạng chuỗi "yyyy-MM-dd" để làm ID cho collection ngày
    private fun getTodayId(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun getDailyTasks(): Flow<List<DailyTask>> = callbackFlow {
        if (currentUserId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val today = getTodayId()
        val tasksRef = firestore.collection("users")
            .document(currentUserId)
            .collection("daily_tasks")
            .document(today) // Mỗi ngày là 1 document chứa 1 list hoặc subcollection.
            // Để đơn giản và tiết kiệm read, ta dùng Subcollection "tasks" bên trong document ngày
            .collection("tasks")

        val listener = tasksRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(DailyTask::class.java)
            }?.sortedBy { it.isCompleted } ?: emptyList() // Chưa xong lên đầu

            trySend(tasks)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun checkAndGenerateDailyTasks() {
        if (currentUserId.isEmpty()) return
        val today = getTodayId()
        val dayRef = firestore.collection("users").document(currentUserId)
            .collection("daily_tasks").document(today)

        try {
            val doc = dayRef.get().await()
            if (!doc.exists()) {
                generateRandomTasks(today)
                // Đánh dấu đã tạo
                dayRef.set(mapOf("created_at" to FieldValue.serverTimestamp())).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun generateRandomTasks(dayId: String) {
        val allTypes = TaskType.entries.toList()

        // Random 5 nhiệm vụ bất kỳ
        val selectedTypes = allTypes.shuffled().take(5)

        val batch = firestore.batch()
        val tasksCollection = firestore.collection("users").document(currentUserId)
            .collection("daily_tasks").document(dayId).collection("tasks")

        selectedTypes.forEach { type ->
            val task = DailyTask(
                id = type.name,
                type = type.name,
                title = type.title,
                description = "Mục tiêu: ${type.target}",
                currentProgress = 0,
                target = type.target,
                reward = type.reward,
                isCompleted = false
            )
            val docRef = tasksCollection.document(type.name)
            batch.set(docRef, task)
        }
        batch.commit().await()
    }

    override suspend fun updateTaskProgress(typeStr: String): Boolean {
        if (currentUserId.isEmpty()) return false
        val today = getTodayId()
        val taskRef = firestore.collection("users").document(currentUserId)
            .collection("daily_tasks").document(today)
            .collection("tasks").document(typeStr)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(taskRef)
                if (!snapshot.exists()) return@runTransaction false

                val task = snapshot.toObject(DailyTask::class.java) ?: return@runTransaction false

                if (task.isCompleted) return@runTransaction false

                val newProgress = task.currentProgress + 1
                val isNowCompleted = newProgress >= task.target

                Log.d("TaskRepo", "Task progress $typeStr: $newProgress" )

                transaction.update(taskRef, "currentProgress", newProgress)

                if (isNowCompleted) {
                    Log.d("TaskRepo", "Task completed: $typeStr")
                    transaction.update(taskRef, "isCompleted", true)
                    val userRef = firestore.collection("users").document(currentUserId)
                    transaction.update(userRef, "rizzPoints", FieldValue.increment(task.reward.toLong()))

                    return@runTransaction true // Bắn tín hiệu về để hiện thông báo
                }

                return@runTransaction false
            }.await().also { isSuccess ->
                if (isSuccess) {
                    val taskInfo = TaskType.entries.find { it.name == typeStr }
                    val msg = "🎉 Đã xong: ${taskInfo?.title} (+${taskInfo?.reward} RIZZ)"
                    _taskCompletionEvent.emit(msg)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error updating task", e)
            false
        }
    }
}