// 文件路径: app/src/main/java/com/example/livestreamplayer/ChannelListActivity.kt

package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityChannelListBinding
import kotlinx.coroutines.launch

class ChannelListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelListBinding
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var platformUrl: String? = null

    companion object {
        const val EXTRA_PLATFORM_URL = "extra_platform_url"
        const val EXTRA_PLATFORM_TITLE = "extra_platform_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)

        platformUrl = intent.getStringExtra(EXTRA_PLATFORM_URL)
        val platformTitle = intent.getStringExtra(EXTRA_PLATFORM_TITLE)

        title = platformTitle ?: "Channels"

        if (platformUrl == null) {
            Toast.makeText(this, "Error: Platform URL not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        fetchChannels(platformUrl!!)
    }

    private fun setupRecyclerView() {
        channelAdapter = ChannelAdapter(
            emptyList(),
            preferenceManager,
            platformUrl ?: "",
            onItemClick = { channel ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.address)
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, channel.title)
                }
                startActivity(intent)
            },
            onFavoriteClick = { channel, isFavorite ->
                if (isFavorite && platformUrl != null) {
                    preferenceManager.saveFavoriteChannel(channel, platformUrl!!)
                    Toast.makeText(this, "已收藏主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                } else {
                    preferenceManager.removeFavoriteChannel(channel)
                    Toast.makeText(this, "已取消收藏主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                }
            },
            onBlockClick = { channel, isBlocked ->
                if (isBlocked) {
                    preferenceManager.addBlockedChannel(channel)
                    Toast.makeText(this, "已屏蔽主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                    // 不再使用有问题的currentList，而是重新获取频道列表
                    if (platformUrl != null) {
                        fetchChannels(platformUrl!!)
                    }
                } else {
                    preferenceManager.removeBlockedChannel(channel)
                    Toast.makeText(this, "已取消屏蔽主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                    // 重新获取频道列表
                    if (platformUrl != null) {
                        fetchChannels(platformUrl!!)
                    }
                }
            }
        )
        binding.recyclerViewChannels.apply {
            adapter = channelAdapter
            layoutManager = LinearLayoutManager(this@ChannelListActivity)
        }
    }

    private fun fetchChannels(url: String) {
        binding.progressBarChannels.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val fullUrl = "http://api.hclyz.com:81/mf/$url"
                val response = RetrofitInstance.api.getChannels(fullUrl)
                binding.progressBarChannels.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    // 更新适配器数据，适配器内部会过滤掉被屏蔽的主播
                    channelAdapter.updateData(response.body()!!.channels)
                } else {
                    Toast.makeText(
                        this@ChannelListActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBarChannels.visibility = View.GONE
                Log.e("ChannelListActivity", "获取频道失败", e)
                Toast.makeText(
                    this@ChannelListActivity,
                    "加载失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    // 添加一个扩展属性，方便获取当前列表
    private val ChannelAdapter.currentList: List<Channel>
        get() = (0 until itemCount).map { position ->
            getItem(position)
        }
    
    private fun ChannelAdapter.getItem(position: Int): Channel {
        // 这里需要访问适配器内部的channels列表
        // 由于我们无法直接访问，所以这是一个模拟实现
        // 实际使用时，应该在ChannelAdapter中添加一个方法来获取指定位置的Channel
        return Channel("", "") // 这里只是一个占位符
    }
}