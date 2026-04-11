package com.brainify.quizapp.models

import java.util.Date

data class QuizResult(
    val subject: String,
    val topic: String,
    val score: Int,
    val total: Int,
    val date: Date,
    val timeTaken: Int, // in seconds
    val cheatingDetected: Boolean = false
) {
    val percentage: Float
        get() = if (total == 0) 0f else (score.toFloat() / total) * 100
}
