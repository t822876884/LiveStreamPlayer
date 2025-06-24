package com.example.livestreamplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.livestreamplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var preferenceManager: PreferenceManager
    private var streamUrl: String? = null
    private var streamTitle: String? = null
    private var isRecording = false
    private var currentDownloadTaskId: String? = null

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
        Log.d("PlayerActivity", "onCreate: 初始化底部导航栏")
        
        // 确保底部导航栏可见并设置Z轴层级
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.bottomNavigation.elevation = 10f
        binding.bottomNavigation.bringToFront() // 强制置于顶层
        
        // 添加日志输出底部导航栏状态
        Log.d("PlayerActivity", "底部导航栏可见性: ${binding.bottomNavigation.visibility == View.VISIBLE}")
        
        preferenceManager = PreferenceManager(this)
        
        // 在 onCreate 方法中添加以下代码，位于 preferenceManager 初始化之后
        
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
        
        // 确保底部导航栏可见
        binding.bottomNavigation.visibility = View.VISIBLE
        
        // 添加底部导航按钮
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                R.id.nav_download -> {
                    val intent = Intent(this, DownloadTasksActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, DownloadSettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                else -> false
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

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            // 根据URL类型创建适当的MediaItem
            val mediaItem = when {
                url.startsWith("rtmp://") -> {
                    // RTMP流 - 使用正确的MIME类型或不设置MIME类型
                    MediaItem.Builder()
                        .setUri(url)
                        // RTMP不需要特殊的MIME类型设置
                        // 或者使用通用的应用程序类型
                        // .setMimeType(MimeTypes.APPLICATION_OCTET_STREAM)
                        .build()
                }
                else -> {
                    // 其他类型的流
                    MediaItem.fromUri(url)
                }
            }
            
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
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
            it.release()
            player = null
            binding.playerView.player = null
        }
    }
}