package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityFavoriteChannelsBinding

class FavoriteChannelsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteChannelsBinding
    private lateinit var favoriteChannelAdapter: FavoriteChannelAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "收藏主播列表"
        preferenceManager = PreferenceManager(this)
        
        setupFavoriteChannelsRecyclerView()
        updateFavoriteChannels()
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
        
        binding.recyclerViewFavoriteChannels.apply {
            adapter = favoriteChannelAdapter
            layoutManager = LinearLayoutManager(this@FavoriteChannelsActivity)
        }
    }
    
    private fun updateFavoriteChannels() {
        try {
            val favoriteChannels = preferenceManager.getFavoriteChannels()
            binding.emptyView.visibility = if (favoriteChannels.isEmpty()) View.VISIBLE else View.GONE
            favoriteChannelAdapter.updateData(favoriteChannels)
        } catch (e: Exception) {
            Toast.makeText(this, "加载收藏主播失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}