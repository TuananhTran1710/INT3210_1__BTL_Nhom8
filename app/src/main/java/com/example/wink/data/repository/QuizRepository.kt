package com.example.wink.data.repository

import com.example.wink.data.model.Quiz

interface QuizRepository {
    suspend fun getAllQuizzes(): List<Quiz>
    suspend fun getQuizById(id: String): Quiz?
    suspend fun generateQuizByAi(topic: String, userId: String, cost: Int): Result<String>
}