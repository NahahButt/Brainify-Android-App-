package com.brainify.quizapp

import java.util.*

data class Note(
    var id: Long = System.currentTimeMillis(),
    var title: String = "",
    var content: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var backupTime: Long = 0, // For backup/deletion tracking
    var isDeleted: Boolean = false
)