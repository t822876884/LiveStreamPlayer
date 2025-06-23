package com.example.livestreamplayer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityBlockedChannelsBinding

class BlockedChannelsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlockedChannelsBinding
    private lateinit var blockedChannelAdapter: BlockedChannelAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "屏蔽列表"
        preferenceManager = PreferenceManager(this)
        
        setupBlockedChannelsRecyclerView()
        updateBlockedChannels()
    }
    
    private fun setupBlockedChannelsRecyclerView() {
        blockedChannelAdapter = BlockedChannelAdapter(
            emptyList(),
            preferenceManager,
            onUnblockClick = { channel ->
                // 取消屏蔽
                preferenceManager.removeBlockedChannel(channel)
                Toast.makeText(this, "已取消屏蔽主播: ${channel.title}", Toast.LENGTH_SHORT).show()
                // 更新列表
                updateBlockedChannels()
            }
        )
        
        binding.recyclerViewBlockedChannels.apply {
            adapter = blockedChannelAdapter
            layoutManager = LinearLayoutManager(this@BlockedChannelsActivity)
        }
    }
    
    private fun updateBlockedChannels() {
        try {
            val blockedChannels = preferenceManager.getBlockedChannels()
            binding.emptyView.visibility = if (blockedChannels.isEmpty()) View.VISIBLE else View.GONE
            blockedChannelAdapter.updateData(blockedChannels)
        } catch (e: Exception) {
            Toast.makeText(this, "加载屏蔽列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}