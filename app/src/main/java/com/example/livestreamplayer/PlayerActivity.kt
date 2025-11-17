package com.example.livestreamplayer

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.rtmp.RtmpDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.livestreamplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var preferenceManager: PreferenceManager
    private var streamUrl: String? = null
    private var streamTitle: String? = null
    private var isRecording = false
    private var currentDownloadTaskId: String? = null
    private val playbackListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateKeepScreenOn(isPlaying)
        }
    }

    private val downloadSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startDownload()
        }
    }

    companion object {
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_STREAM_TITLE = "extra_stream_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    
        // 添加日志输出
        Log.d("PlayerActivity", "onCreate: 初始化播放器")
    
        // --- 请将下面这段代码添加到您的 onCreate 方法中 ---
        binding.btnCopyUrl.setOnClickListener {
            if (!streamUrl.isNullOrEmpty()) {
                // 获取系统剪贴板服务
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                // 创建一个 ClipData 对象
                val clip = ClipData.newPlainText("Stream URL", streamUrl)
                // 将 ClipData 设置到剪贴板
                clipboard.setPrimaryClip(clip)
    
                // 弹出提示，告知用户复制成功
                Toast.makeText(this, "直播地址已复制到剪贴板", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "没有可复制的地址", Toast.LENGTH_SHORT).show()
            }
        }

        preferenceManager = PreferenceManager(this)

        // 检查是否从其他页面传入了流URL和标题
        if (intent.hasExtra(EXTRA_STREAM_URL) && intent.hasExtra(EXTRA_STREAM_TITLE)) {
            streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
            streamTitle = intent.getStringExtra(EXTRA_STREAM_TITLE)
            title = streamTitle

            if (streamUrl != null) {
                initializePlayer(streamUrl!!)
            }
        } else {
            // 默认加载内容（可以是上次播放的内容或推荐内容）
            val lastPlayedChannel = preferenceManager.getLastPlayedChannel()
            if (lastPlayedChannel != null) {
                streamUrl = lastPlayedChannel.address
                streamTitle = lastPlayedChannel.title
                title = streamTitle

                if (streamUrl != null) {
                    initializePlayer(streamUrl!!)
                }
            } else {
                // 如果没有上次播放记录，显示主页
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_player, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val downloadItem = menu.findItem(R.id.action_download)
        if (isRecording) {
            downloadItem.setTitle("停止录制")
            downloadItem.setIcon(android.R.drawable.ic_media_pause)
        } else {
            downloadItem.setTitle("开始录制")
            downloadItem.setIcon(android.R.drawable.ic_media_play)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_download -> {
                if (isRecording) {
                    stopDownload()
                } else {
                    checkDownloadSettings()
                }
                true
            }

            R.id.action_download_tasks -> {  // 修改这里，从 action_view_downloads 改为 action_download_tasks
                val intent = Intent(this, DownloadTasksActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String) {
        try {
            // 1. 根据链接的协议类型，选择正确的数据源工厂 (DataSource.Factory)
            val dataSourceFactory: DataSource.Factory = if (url.startsWith("rtmp")) {
                // 如果是rtmp链接，使用RtmpDataSource.Factory
                RtmpDataSourceFactory()
            } else {
                // 对于其他链接 (http, https等)，使用默认的DefaultDataSource.Factory
                DefaultDataSource.Factory(this)
            }

            // 2. 使用我们选择的数据源工厂来创建一个媒体源工厂 (MediaSource.Factory)
            val mediaSourceFactory = DefaultMediaSourceFactory(this)
                .setDataSourceFactory(dataSourceFactory)

            // 3. 创建ExoPlayer实例时，注入我们自定义的媒体源工厂
            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory) // <-- 这是关键的修改！
                .build()
                .also { exoPlayer ->
                    exoPlayer.addListener(playbackListener)
                    binding.playerView.player = exoPlayer

                    // MediaItem的创建现在可以简化，因为工厂会自动处理
                    val mediaItem = MediaItem.fromUri(url)

                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.playWhenReady = true
                    exoPlayer.prepare()
                }
        } catch (e: Exception) {
            // 增加异常捕获，防止因为URL或协议问题直接导致App崩溃
            Log.e("PlayerActivity", "初始化播放器失败", e)
            Toast.makeText(this, "播放失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkDownloadSettings() {
        val downloadPath = preferenceManager.getDownloadPath()
        if (downloadPath == null) {
            // 如果未配置下载路径，跳转到设置页面
            val intent = Intent(this, DownloadSettingsActivity::class.java)
            downloadSettingsLauncher.launch(intent)
        } else {
            startDownload()
        }
    }

    private fun startDownload() {
        if (streamUrl == null || streamTitle == null) return

        val channel = Channel(streamTitle!!, streamUrl!!)
        val task = DownloadService.createDownloadTask(this, channel, streamTitle!!)

        if (task != null) {
            preferenceManager.saveDownloadTask(task)
            currentDownloadTaskId = task.id

            val intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.ACTION_START_DOWNLOAD
                putExtra(DownloadService.EXTRA_TASK_ID, task.id)
            }
            startService(intent)

            isRecording = true
            invalidateOptionsMenu()
            Toast.makeText(this, "开始录制: $streamTitle", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "无法创建下载任务，请检查下载设置", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopDownload() {
        currentDownloadTaskId?.let { taskId ->
            val intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.ACTION_STOP_DOWNLOAD
                putExtra(DownloadService.EXTRA_TASK_ID, taskId)
            }
            startService(intent)

            isRecording = false
            invalidateOptionsMenu()
            Toast.makeText(this, "已停止录制", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onStart() {
        super.onStart()
        if (player == null) {
            streamUrl?.let { initializePlayer(it) }
        }
    }

    // 添加一个方法在onResume中调用，确保底部导航栏始终可见
    override fun onResume() {
        super.onResume()
        if (player == null) {
            streamUrl?.let { initializePlayer(it) }
        }
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let {
            updateKeepScreenOn(false)
            it.removeListener(playbackListener)
            it.release()
            player = null
            binding.playerView.player = null
        }
    }

    private fun updateKeepScreenOn(keep: Boolean) {
        if (keep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}