// 粘贴上一个回答中 MainActivity.kt 的完整代码
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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var platformAdapter: PlatformAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        fetchPlatforms()
    }

    private fun setupRecyclerView() {
        platformAdapter = PlatformAdapter(emptyList()) { platform ->
            val intent = Intent(this, ChannelListActivity::class.java).apply {
                putExtra(ChannelListActivity.EXTRA_PLATFORM_URL, platform.address)
                putExtra(ChannelListActivity.EXTRA_PLATFORM_TITLE, platform.title)
            }
            startActivity(intent)
        }
        binding.recyclerViewPlatforms.apply {
            adapter = platformAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }


    private fun fetchPlatforms() {
        Log.d("MainActivity", "fetchPlatforms: 开始获取平台数据...")
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // 这里的 response.body() 现在是 PlatformResponse 类型
                val response = RetrofitInstance.api.getPlatforms()
                Log.d("MainActivity", "fetchPlatforms: 收到响应，是否成功: ${response.isSuccessful}")

                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    // 从响应体中取出平台列表
                    val platformList = response.body()!!.platforms

                    Log.d("MainActivity", "fetchPlatforms: 获取到 ${platformList.size} 个平台")
                    if (platformList.isEmpty()) {
                        Log.w("MainActivity", "fetchPlatforms: 平台列表为空，屏幕将显示空白")
                        Toast.makeText(this@MainActivity, "平台列表为空", Toast.LENGTH_SHORT).show()
                    }
                    // 将真正的列表交给Adapter
                    platformAdapter.updateData(platformList)
                } else {
                    Log.e("MainActivity", "fetchPlatforms: 请求失败或响应体为空，错误码: ${response.code()}, 消息: ${response.message()}")
                    Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("MainActivity", "fetchPlatforms: 捕获到异常", e)
                Toast.makeText(this@MainActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}