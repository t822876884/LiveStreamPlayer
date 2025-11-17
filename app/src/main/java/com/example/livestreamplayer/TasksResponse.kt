package com.example.livestreamplayer

data class TasksResponse(
    val ok: Boolean,
    val items: List<DownloadTask>, // Changed to DownloadTask
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val message: String? = null // For error messages
)
