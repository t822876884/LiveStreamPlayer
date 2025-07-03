package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityPlatformListBinding
import kotlinx.coroutines.launch

class PlatformListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlatformListBinding
    private lateinit var platformAdapter: PlatformAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatformListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "平台页面"
        preferenceManager = PreferenceManager(this)
        
        // 设置平台列表
        setupPlatformRecyclerView()
        
        // 获取平台数据
        fetchPlatforms()
        
        // 设置按钮点击事件
        setupButtonClickListeners()
        
        // 设置底部导航栏
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                R.id.nav_platforms -> {
                    // 已经在平台页面，不需要操作
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, DownloadSettingsActivity::class.java)
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                else -> false
            }
        }
        
        // 设置当前选中的导航项
        binding.bottomNavigation.selectedItemId = R.id.nav_platforms
    }
    
    // 设置按钮点击事件
    private fun setupButtonClickListeners() {
        // 收藏平台按钮
        binding.btnFavoritePlatforms.setOnClickListener {
            val intent = Intent(this, FavoritePlatformsActivity::class.java)
            startActivity(intent)
        }
        
        // 屏蔽平台按钮
        binding.btnBlockedPlatforms.setOnClickListener {
            val intent = Intent(this, BlockedPlatformsActivity::class.java)
            startActivity(intent)
        }
        
        // 收藏主播按钮
        binding.btnFavoriteChannels.setOnClickListener {
            val intent = Intent(this, FavoriteChannelsActivity::class.java)
            startActivity(intent)
        }
        
        // 屏蔽主播按钮
        binding.btnBlockedChannels.setOnClickListener {
            val intent = Intent(this, BlockedChannelsActivity::class.java)
            startActivity(intent)
        }
    }
    
    // 设置平台列表
    private fun setupPlatformRecyclerView() {
        platformAdapter = PlatformAdapter(
            emptyList(),
            preferenceManager,
            onItemClick = { platform ->
                val intent = Intent(this, ChannelListActivity::class.java).apply {
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_URL, platform.address)
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_TITLE, platform.title)
                }
                startActivity(intent)
            },
            onFavoriteClick = { platform, isFavorite ->
                if (isFavorite) {
                    preferenceManager.saveFavoritePlatform(platform)
                    Toast.makeText(this, "已收藏平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                } else {
                    preferenceManager.removeFavoritePlatform(platform)
                    Toast.makeText(this, "已取消收藏平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                }
            },
            onBlockClick = { platform, isBlocked ->
                if (isBlocked) {
                    preferenceManager.addBlockedPlatform(platform)
                    Toast.makeText(this, "已屏蔽平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                    // 刷新列表
                    fetchPlatforms()
                } else {
                    preferenceManager.removeBlockedPlatform(platform)
                    Toast.makeText(this, "已取消屏蔽平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                    fetchPlatforms()
                }
            }
        )
        
        binding.recyclerViewPlatforms.apply {
            adapter = platformAdapter
            layoutManager = LinearLayoutManager(this@PlatformListActivity)
        }
    }
    
    // 获取平台列表
    private fun fetchPlatforms() {
        Log.d("PlatformListActivity", "fetchPlatforms: 开始获取平台数据...")
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getPlatforms()
                Log.d("PlatformListActivity", "fetchPlatforms: 收到响应，是否成功: ${response.isSuccessful}")

                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val platformList = response.body()!!.platforms

                    Log.d("PlatformListActivity", "fetchPlatforms: 获取到 ${platformList.size} 个平台")
                    if (platformList.isEmpty()) {
                        Log.w("PlatformListActivity", "fetchPlatforms: 平台列表为空，屏幕将显示空白")
                        Toast.makeText(this@PlatformListActivity, "平台列表为空", Toast.LENGTH_SHORT).show()
                    }
                    platformAdapter.updateData(platformList)
                } else {
                    Log.e("PlatformListActivity", "fetchPlatforms: 请求失败或响应体为空，错误码: ${response.code()}, 消息: ${response.message()}")
                    Toast.makeText(this@PlatformListActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("PlatformListActivity", "fetchPlatforms: 发生异常", e)
                Toast.makeText(this@PlatformListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}