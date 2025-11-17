package com.example.livestreamplayer

data class ApiResponse(
    val ok: Boolean,
    val message: String,
    val taskId: String? = null, // For /cancel success
    val id: String? = null // For /delete success
)
