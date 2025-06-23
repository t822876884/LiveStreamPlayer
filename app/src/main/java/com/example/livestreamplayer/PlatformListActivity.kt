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
        
        title = "平台列表"
        preferenceManager = PreferenceManager(this)
        
        setupPlatformRecyclerView()
        fetchPlatforms()
    }
    
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
                } else {
                    preferenceManager.removeFavoritePlatform(platform)
                }
                platformAdapter.notifyDataSetChanged()
            }
        )
        
        binding.recyclerViewPlatforms.apply {
            adapter = platformAdapter
            layoutManager = LinearLayoutManager(this@PlatformListActivity)
        }
    }
    
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