package com.example.livestreamplayer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityBlockedPlatformsBinding

class BlockedPlatformsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlockedPlatformsBinding
    private lateinit var blockedPlatformAdapter: BlockedPlatformAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedPlatformsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "平台屏蔽列表"
        preferenceManager = PreferenceManager(this)
        
        setupBlockedPlatformsRecyclerView()
        updateBlockedPlatforms()
    }
    
    private fun setupBlockedPlatformsRecyclerView() {
        blockedPlatformAdapter = BlockedPlatformAdapter(
            emptyList(),
            preferenceManager,
            onUnblockClick = { platform ->
                // 取消屏蔽
                preferenceManager.removeBlockedPlatform(platform)
                Toast.makeText(this, "已取消屏蔽平台: ${platform.title}", Toast.LENGTH_SHORT).show()
                // 更新列表
                updateBlockedPlatforms()
            }
        )
        
        binding.recyclerViewBlockedPlatforms.apply {
            adapter = blockedPlatformAdapter
            layoutManager = LinearLayoutManager(this@BlockedPlatformsActivity)
        }
    }
    
    private fun updateBlockedPlatforms() {
        try {
            val blockedPlatforms = preferenceManager.getBlockedPlatforms()
            binding.emptyView.visibility = if (blockedPlatforms.isEmpty()) View.VISIBLE else View.GONE
            blockedPlatformAdapter.updateData(blockedPlatforms)
        } catch (e: Exception) {
            Toast.makeText(this, "加载屏蔽平台列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}