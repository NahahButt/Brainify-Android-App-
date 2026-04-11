package com.brainify.quizapp.models

data class Subject(
    val name: String,
    val iconResId: Int,
    val color: String,
    val progress: Int = 0 // ✅ Ye percentage ke liye zaroori hai
)