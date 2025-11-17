package com.example.livestreamplayer

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DownloadsViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DownloadingFragment()
            1 -> CompletedFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
