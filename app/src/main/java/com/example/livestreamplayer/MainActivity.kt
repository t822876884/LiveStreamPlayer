package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var favoriteChannelAdapter: FavoriteChannelAdapter
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        setupPlatformRecyclerView()
        setupFavoriteChannelsRecyclerView()
        setupBlockedChannelsRecyclerView()
        
        // 设置底部导航栏
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 已经在首页，不需要操作
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
        
        // 设置当前选中的导航项
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到主页时只刷新收藏的主播列表，屏蔽列表已移到 BlockedChannelsActivity
        updateFavoriteChannels()
        // 移除 updateBlockedChannels() 调用
    }

    // 替换setupPlatformRecyclerView方法
    private fun setupPlatformRecyclerView() {
        // 设置平台卡片点击事件
        binding.cardViewPlatforms.setOnClickListener {
            // 跳转到平台列表页面
            val intent = Intent(this, PlatformListActivity::class.java)
            startActivity(intent)
        }
        
        // 设置主播屏蔽列表卡片点击事件
        binding.cardViewBlockedChannels.setOnClickListener {
            // 跳转到主播屏蔽列表页面
            val intent = Intent(this, BlockedChannelsActivity::class.java)
            startActivity(intent)
        }
        
        // 设置平台屏蔽列表卡片点击事件
        binding.cardViewBlockedPlatforms.setOnClickListener {
            // 跳转到平台屏蔽列表页面
            val intent = Intent(this, BlockedPlatformsActivity::class.java)
            startActivity(intent)
        }
        
        // 设置收藏主播列表卡片点击事件
        binding.cardViewFavoriteChannels.setOnClickListener {
            // 跳转到收藏主播列表页面
            val intent = Intent(this, FavoriteChannelsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupFavoriteChannelsRecyclerView() {
        favoriteChannelAdapter = FavoriteChannelAdapter(
            emptyList(),
            preferenceManager,
            onItemClick = { favoriteChannel ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, favoriteChannel.channel.address)
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, favoriteChannel.channel.title)
                }
                startActivity(intent)
            },
            onFavoriteClick = { channel, _ ->
                // 取消收藏
                preferenceManager.removeFavoriteChannel(channel)
                Toast.makeText(this, "已取消收藏主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                // 更新列表
                updateFavoriteChannels()
            }
        )
        binding.recyclerViewLiveChannels.apply {
            adapter = favoriteChannelAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        
        // 初始加载收藏的主播
        updateFavoriteChannels()
    }
    
    // 新增屏蔽主播列表设置
    // 修改屏蔽主播列表设置
    private fun setupBlockedChannelsRecyclerView() {
        // 只需要设置点击事件
        binding.cardViewBlockedChannels.setOnClickListener {
            val intent = Intent(this, BlockedChannelsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun updateFavoriteChannels() {
        val favoriteChannels = preferenceManager.getFavoriteChannels()
        favoriteChannelAdapter.updateData(favoriteChannels)
    }
}