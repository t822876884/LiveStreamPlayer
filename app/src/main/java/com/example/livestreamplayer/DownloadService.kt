package com.example.livestreamplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DownloadService : Service() {
    private val TAG = "DownloadService"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "download_channel"
    
    private val binder = LocalBinder()
    private val activeDownloads = mutableMapOf<String, Job>()
    private lateinit var preferenceManager: PreferenceManager
    
    inner class LocalBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
    
    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                val task = preferenceManager.getDownloadTasks().find { it.id == taskId } ?: return START_NOT_STICKY
                startDownload(task)
            }
            ACTION_STOP_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                stopDownload(taskId)
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "下载通知"
            val descriptionText = "显示直播下载状态"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForeground() {
        val notification = createNotification("正在下载直播内容", "下载中...")
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(title: String, content: String): Notification {
        val notificationIntent = Intent(this, DownloadTasksActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    fun startDownload(task: DownloadTask) {
        if (activeDownloads.isEmpty()) {
            startForeground()
        }
        
        if (activeDownloads.containsKey(task.id)) {
            Log.d(TAG, "Download already in progress for task: ${task.id}")
            return
        }
        
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 确保输出目录存在
                val outputUri = Uri.parse(task.outputPath)
                val ffmpegOutputPath = FFmpegKitConfig.getSafParameterForWrite(applicationContext, outputUri)
                
                // 构建FFmpeg命令
                val command = when {
                    task.streamUrl.contains(".m3u8") -> {
                        // HLS流
                        "-i ${task.streamUrl} -c copy ${ffmpegOutputPath}"
                    }
                    task.streamUrl.startsWith("rtmp://") -> {
                        // RTMP流
                        "-i ${task.streamUrl} -c copy ${ffmpegOutputPath}"
                    }
                    task.streamUrl.contains(".flv") -> {
                        // FLV流
                        "-i ${task.streamUrl} -c copy ${ffmpegOutputPath}"
                    }
                    else -> {
                        // 其他流
                        "-i ${task.streamUrl} -c copy ${ffmpegOutputPath}"
                    }
                }
                
                Log.d(TAG, "Starting download with command: $command")
                
                // 执行FFmpeg命令
                val session = FFmpegKit.execute(command)
                val rc = session.returnCode
                
                withContext(Dispatchers.Main) {
                    if (ReturnCode.isSuccess(rc)) {
                        preferenceManager.updateDownloadTaskStatus(task.id, DownloadStatus.COMPLETED)
                        Log.d(TAG, "Download completed for task: ${task.id}")
                    } else if (ReturnCode.isCancel(rc)) {
                        preferenceManager.updateDownloadTaskStatus(task.id, DownloadStatus.CANCELLED)
                        Log.d(TAG, "Download cancelled for task: ${task.id}")
                    } else {
                        preferenceManager.updateDownloadTaskStatus(
                            task.id, 
                            DownloadStatus.ERROR, 
                            "FFmpeg process exited with rc=$rc"
                        )
                        Log.e(TAG, "Download failed for task: ${task.id}, rc=$rc")
                    }
                    
                    activeDownloads.remove(task.id)
                    if (activeDownloads.isEmpty()) {
                        stopForeground(true)
                        stopSelf()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    preferenceManager.updateDownloadTaskStatus(
                        task.id, 
                        DownloadStatus.ERROR, 
                        e.message
                    )
                    Log.e(TAG, "Exception during download for task: ${task.id}", e)
                    
                    activeDownloads.remove(task.id)
                    if (activeDownloads.isEmpty()) {
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        }
        
        activeDownloads[task.id] = job
    }
    
    fun stopDownload(taskId: String) {
        val job = activeDownloads[taskId]
        if (job != null) {
            // 取消FFmpeg进程 - 更新为新的API
            FFmpegKitConfig.clearSessions()
            FFmpegKit.cancel()
            
            // 取消协程
            job.cancel()
            activeDownloads.remove(taskId)
            
            // 更新任务状态
            preferenceManager.updateDownloadTaskStatus(taskId, DownloadStatus.CANCELLED)
            
            if (activeDownloads.isEmpty()) {
                stopForeground(true)
                stopSelf()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消所有下载
        activeDownloads.keys.toList().forEach { stopDownload(it) }
    }
    
    companion object {
        const val ACTION_START_DOWNLOAD = "com.example.livestreamplayer.action.START_DOWNLOAD"
        const val ACTION_STOP_DOWNLOAD = "com.example.livestreamplayer.action.STOP_DOWNLOAD"
        const val EXTRA_TASK_ID = "com.example.livestreamplayer.extra.TASK_ID"
        
        fun createDownloadTask(context: Context, channel: Channel, streamTitle: String): DownloadTask? {
            val preferenceManager = PreferenceManager(context)
            val downloadPathUri = preferenceManager.getDownloadPath() ?: return null
            
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "${streamTitle}_${timestamp}.mp4"
            
            // 使用DocumentFile处理content URI
            val uri = Uri.parse(downloadPathUri)
            val directory = DocumentFile.fromTreeUri(context, uri) ?: return null
            
            // 创建新文件
            val newFile = directory.createFile("video/mp4", fileName) ?: return null
            
            return DownloadTask(
                id = UUID.randomUUID().toString(),
                channelTitle = streamTitle,
                streamUrl = channel.address,
                outputPath = newFile.uri.toString()
            )
        }
    }
}