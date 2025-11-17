package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

        setSupportActionBar(binding.toolbar)
        setupLiveChannelsRecyclerView()
        setupBottomNavigation()
        setupSwipeToRefresh()

        // Initial data load
        syncChannels(isInitialLoad = true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                syncChannels(isInitialLoad = false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            syncChannels(isInitialLoad = false)
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
                syncChannels(isInitialLoad = false)
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

    private fun syncChannels(isInitialLoad: Boolean) {
        val favoritePlatforms = preferenceManager.getFavoritePlatforms()
        if (favoritePlatforms.isNotEmpty()) {
            checkLiveChannels(isInitialLoad, favoritePlatforms.map { it.address })
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitInstance.api.getPlatforms()
                    if (response.isSuccessful) {
                        val platformResponse = response.body()
                        if (platformResponse != null) {
                            val allPlatforms = platformResponse.platforms
                            val blockedPlatforms = preferenceManager.getBlockedPlatforms()
                            val nonBlockedPlatforms = allPlatforms.filter { platform ->
                                blockedPlatforms.none { blockedPlatform -> blockedPlatform.address == platform.address }
                            }
                            val platformsToFetch = nonBlockedPlatforms.take(5)
                            withContext(Dispatchers.Main) {
                                checkLiveChannels(isInitialLoad, platformsToFetch.map { it.address })
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "获取平台列表失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "获取平台列表失败: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        when (e) {
                            is java.net.UnknownHostException -> {
                                Toast.makeText(this@MainActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
                            }
                            is retrofit2.HttpException -> {
                                Toast.makeText(this@MainActivity, "获取平台列表失败: ${e.code()}", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@MainActivity, "获取平台列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkLiveChannels(isInitialLoad: Boolean, platformUrls: List<String>) {
        if (isInitialLoad) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.swipeRefreshLayout.isRefreshing = true
        }
        binding.tvNoLiveChannels.visibility = View.GONE
        binding.recyclerViewLiveChannels.visibility = View.GONE

        val allFavoriteChannels = preferenceManager.getFavoriteChannels().filter { platformUrls.contains(it.platformUrl) }

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

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    favoritesByPlatform.map { (platformUrl, favoriteChannelsOnPlatform) ->
                        launch {
                            try {
                                val fullUrl = "http://api.hclyz.com:81/mf/$platformUrl"
                                val response = RetrofitInstance.api.getChannels(fullUrl)
                                if (response.isSuccessful) {
                                    val currentPlatformChannels = response.body()?.channels
                                    if (currentPlatformChannels != null) {
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
                                    }
                                } else {
                                    Log.w(
                                        "MainActivity",
                                        "Failed to fetch channels for $platformUrl: ${response.code()}"
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    when (e) {
                                        is java.net.UnknownHostException -> {
                                            Toast.makeText(this@MainActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
                                        }
                                        is retrofit2.HttpException -> {
                                            Toast.makeText(this@MainActivity, "获取频道列表失败: ${e.code()}", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            Log.e("MainActivity", "Exception fetching channels for $platformUrl", e)
                                        }
                                    }
                                }
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
}
