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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var liveChannelAdapter: LiveChannelAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupLiveChannelsRecyclerView()
        setupBottomNavigation()
        setupSwipeToRefresh()

        // Initial data load
        checkLiveChannels(isInitialLoad = true)
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            checkLiveChannels(isInitialLoad = false)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
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
                    finish() // End current Activity
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupLiveChannelsRecyclerView() {
        liveChannelAdapter = LiveChannelAdapter(
            emptyList(),
            preferenceManager,
            onItemClick = { favoriteChannel ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, favoriteChannel.channel.address)
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, favoriteChannel.channel.title)
                }
                startActivity(intent)
            },
            onFavoriteClick = { favoriteChannel ->
                // Unfavorite the channel
                preferenceManager.removeFavoriteChannel(favoriteChannel.channel)
                Toast.makeText(this, "已取消收藏主播: ${favoriteChannel.channel.title}", Toast.LENGTH_SHORT).show()
                // Refresh the list locally
                checkLiveChannels(isInitialLoad = false)
            },
            onDownloadClick = { channel ->
                // Handle download logic
                val downloadPath = preferenceManager.getDownloadPath()
                if (downloadPath == null) {
                    Toast.makeText(this, "请先在设置中配置下载路径", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DownloadSettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    val task = DownloadService.createDownloadTask(this, channel, channel.title)
                    if (task != null) {
                        preferenceManager.saveDownloadTask(task)
                        val intent = Intent(this, DownloadService::class.java).apply {
                            action = DownloadService.ACTION_START_DOWNLOAD
                            putExtra(DownloadService.EXTRA_TASK_ID, task.id)
                        }
                        startService(intent)
                        Toast.makeText(this, "开始录制: ${channel.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        binding.recyclerViewLiveChannels.apply {
            adapter = liveChannelAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    // 检查收藏的主播中是否有直播中的主播
    private fun checkLiveChannels(isInitialLoad: Boolean) {
        if (isInitialLoad) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.swipeRefreshLayout.isRefreshing = true
        }
        binding.tvNoLiveChannels.visibility = View.GONE
        binding.recyclerViewLiveChannels.visibility = View.GONE

        val allFavoriteChannels = preferenceManager.getFavoriteChannels()

        if (allFavoriteChannels.isEmpty()) {
            if (isInitialLoad) {
                binding.progressBar.visibility = View.GONE
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            binding.tvNoLiveChannels.visibility = View.VISIBLE
            liveChannelAdapter.updateData(emptyList())
            return
        }

        val favoritesByPlatform = allFavoriteChannels.groupBy { it.platformUrl }
        val onlineFavoriteChannels = mutableListOf<FavoriteChannel>()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                favoritesByPlatform.map { (platformUrl, favoriteChannelsOnPlatform) ->
                    launch {
                        try {
                            val fullUrl = "http://api.hclyz.com:81/mf/$platformUrl"
                            val response = RetrofitInstance.api.getChannels(fullUrl)
                            if (response.isSuccessful && response.body() != null) {
                                val currentPlatformChannels = response.body()!!.channels
                                val onlineFavs = favoriteChannelsOnPlatform
                                    .mapNotNull { favChannel ->
                                        currentPlatformChannels.find { it.address == favChannel.channel.address }
                                            ?.let { updatedChannel ->
                                                FavoriteChannel(
                                                    channel = updatedChannel,
                                                    platformUrl = platformUrl
                                                )
                                            }
                                    }
                                synchronized(onlineFavoriteChannels) {
                                    onlineFavoriteChannels.addAll(onlineFavs)
                                }
                            } else {
                                Log.w(
                                    "MainActivity",
                                    "Failed to fetch channels for $platformUrl: ${response.code()}"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Exception fetching channels for $platformUrl", e)
                        }
                    }
                }.forEach { it.join() }
            } finally {
                val channelsToShow = onlineFavoriteChannels.filter { it.channel.address.isNotBlank() }

                withContext(Dispatchers.Main) {
                    if (isInitialLoad) {
                        binding.progressBar.visibility = View.GONE
                    } else {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    if (channelsToShow.isEmpty()) {
                        binding.tvNoLiveChannels.visibility = View.VISIBLE
                        binding.recyclerViewLiveChannels.visibility = View.GONE
                    } else {
                        binding.tvNoLiveChannels.visibility = View.GONE
                        binding.recyclerViewLiveChannels.visibility = View.VISIBLE
                    }
                    liveChannelAdapter.updateData(channelsToShow)
                }
            }
        }
    }
}
