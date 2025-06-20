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

    companion object {
        const val EXTRA_PLATFORM_URL = "extra_platform_url"
        const val EXTRA_PLATFORM_TITLE = "extra_platform_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val platformUrl = intent.getStringExtra(EXTRA_PLATFORM_URL)
        val platformTitle = intent.getStringExtra(EXTRA_PLATFORM_TITLE)

        title = platformTitle ?: "Channels"

        if (platformUrl == null) {
            Toast.makeText(this, "Error: Platform URL not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        // 调用正确的获取频道函数
        fetchChannels(platformUrl)
    }

    private fun setupRecyclerView() {
        channelAdapter = ChannelAdapter(emptyList()) { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.address)
                putExtra(PlayerActivity.EXTRA_STREAM_TITLE, channel.title)
            }
            startActivity(intent)
        }
        binding.recyclerViewChannels.apply {
            adapter = channelAdapter
            layoutManager = LinearLayoutManager(this@ChannelListActivity)
        }
    }

    // 这是 ChannelListActivity 应该有的获取数据的函数
    private fun fetchChannels(url: String) {
        binding.progressBarChannels.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // 注意这里拼接了完整的URL
                val fullUrl = "http://api.hclyz.com:81/mf/$url"
                // 调用的是 getChannels，返回的是 ChannelList
                val response = RetrofitInstance.api.getChannels(fullUrl)
                binding.progressBarChannels.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    // 从 ChannelList 对象中取出 channels 列表
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
}