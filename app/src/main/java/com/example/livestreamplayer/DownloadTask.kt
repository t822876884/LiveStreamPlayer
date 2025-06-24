package com.example.livestreamplayer

import java.util.Date

enum class DownloadStatus {
    DOWNLOADING, COMPLETED, CANCELLED, ERROR
}

data class DownloadTask(
    val id: String,                // 任务ID
    val channelTitle: String,      // 主播名称
    val streamUrl: String,         // 直播流地址
    val outputPath: String,        // 输出文件路径
    val startTime: Date = Date(),  // 开始时间
    var endTime: Date? = null,     // 结束时间
    var status: DownloadStatus = DownloadStatus.DOWNLOADING,  // 状态
    var errorMessage: String? = null  // 错误信息
)