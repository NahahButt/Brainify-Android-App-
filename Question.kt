package com.brainify.quizapp.models

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String = "",
    val level: String = "Normal",
    var selectedAnswer: Int = -1
)
