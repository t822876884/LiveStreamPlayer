package com.example.livestreamplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.livestreamplayer.databinding.ActivityDownloadsBinding
import com.google.android.material.tabs.TabLayoutMediator

class DownloadsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = DownloadsViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "下载中"
                1 -> "已完成"
                else -> null
            }
        }.attach()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_platforms -> {
                    val intent = Intent(this, PlatformListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_downloads -> {
                    // Already on this screen
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, DownloadSettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_downloads
    }
}
