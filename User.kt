package com.brainify.quizapp.models

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val totalQuizzes: Int = 0,
    val averageScore: Float = 0f,
    val rank: Int = 0
)