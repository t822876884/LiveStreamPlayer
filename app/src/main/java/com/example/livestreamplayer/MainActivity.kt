package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var liveChannelAdapter: LiveChannelAdapter
    private lateinit var preferenceManager: PreferenceManager
    private val handler = Handler(Looper.getMainLooper())
    private val checkLiveRunnable = object : Runnable {
        override fun run() {
            checkLiveChannels()
            // 每5分钟检查一次
            handler.postDelayed(this, 5 * 60 * 1000)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        setupLiveChannelsRecyclerView()
        
        // 设置底部导航栏
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 点击首页时刷新直播状态
                    checkLiveChannels()
                    Toast.makeText(this, "正在刷新直播信息...", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_platforms -> {
                    val intent = Intent(this, PlatformListActivity::class.java)
                    startActivity(intent)
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
        
        // 设置当前选中的导航项
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        
        // 启动定时检查直播状态
        startLiveChannelsCheck()
    }
    
    override fun onResume() {
        super.onResume()
        // 检查直播状态
        checkLiveChannels()
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停定时检查
        handler.removeCallbacks(checkLiveRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 停止定时检查
        handler.removeCallbacks(checkLiveRunnable)
    }

    // 启动定时检查直播状态
    private fun startLiveChannelsCheck() {
        handler.post(checkLiveRunnable)
    }
    
    private fun setupLiveChannelsRecyclerView() {
        liveChannelAdapter = LiveChannelAdapter(
            emptyList(),
            preferenceManager,
            onItemClick = { liveChannel ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, liveChannel.channel.address)
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, liveChannel.channel.title)
                }
                startActivity(intent)
            }
        )
        binding.recyclerViewLiveChannels.apply {
            adapter = liveChannelAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
    
    // 检查收藏的主播中是否有直播中的主播
    private fun checkLiveChannels() {
        val favoriteChannels = preferenceManager.getFavoriteChannels()
        if (favoriteChannels.isEmpty()) {
            // 如果没有收藏的主播，显示提示文本
            binding.tvNoLiveChannels.visibility = View.VISIBLE
            binding.recyclerViewLiveChannels.visibility = View.GONE
            return
        }
        
        // 创建一个列表来存储已确认直播中的主播
        val liveChannels = mutableListOf<FavoriteChannel>()
        
        // 遍历收藏的主播，检查是否有直播中的
        lifecycleScope.launch {
            // 首先过滤出可能是直播的频道（根据地址判断）
            val potentialLiveChannels = favoriteChannels.filter { it.channel.isLive }
            
            if (potentialLiveChannels.isNotEmpty()) {
                // 有可能直播的频道，添加到直播列表
                liveChannels.addAll(potentialLiveChannels)
                
                // 更新UI
                runOnUiThread {
                    binding.tvNoLiveChannels.visibility = View.GONE
                    binding.recyclerViewLiveChannels.visibility = View.VISIBLE
                    liveChannelAdapter.updateData(liveChannels)
                }
            } else {
                // 没有直播中的主播，显示提示文本
                runOnUiThread {
                    binding.tvNoLiveChannels.visibility = View.VISIBLE
                    binding.recyclerViewLiveChannels.visibility = View.GONE
                }
            }
        }
    }
}