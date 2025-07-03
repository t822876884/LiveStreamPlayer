package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityFavoritePlatformsBinding

class FavoritePlatformsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritePlatformsBinding
    private lateinit var favoritePlatformAdapter: FavoritePlatformAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritePlatformsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "平台收藏列表"
        preferenceManager = PreferenceManager(this)
        
        setupFavoritePlatformsRecyclerView()
        updateFavoritePlatforms()
    }
    
    private fun setupFavoritePlatformsRecyclerView() {
        favoritePlatformAdapter = FavoritePlatformAdapter(
            emptyList(),
            preferenceManager,
            onRemoveFavoriteClick = { platform ->
                // 取消收藏
                preferenceManager.removeFavoritePlatform(platform)
                Toast.makeText(this, "已取消收藏平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                // 更新列表
                updateFavoritePlatforms()
            },
            onPlatformClick = { platform ->
                // 点击平台打开频道列表
                val intent = Intent(this, ChannelListActivity::class.java).apply {
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_URL, platform.address)
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_TITLE, platform.title)
                }
                startActivity(intent)
            }
        )
        
        binding.recyclerViewFavoritePlatforms.apply {
            adapter = favoritePlatformAdapter
            layoutManager = LinearLayoutManager(this@FavoritePlatformsActivity)
        }
    }
    
    private fun updateFavoritePlatforms() {
        try {
            val favoritePlatforms = preferenceManager.getFavoritePlatforms()
            binding.emptyView.visibility = if (favoritePlatforms.isEmpty()) View.VISIBLE else View.GONE
            favoritePlatformAdapter.updateData(favoritePlatforms)
        } catch (e: Exception) {
            Toast.makeText(this, "加载收藏平台列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}