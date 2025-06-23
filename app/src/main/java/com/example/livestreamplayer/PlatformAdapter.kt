package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemPlatformBinding

class PlatformAdapter(
    private var platforms: List<Platform>,
    private val preferenceManager: PreferenceManager,
    private val onItemClick: (Platform) -> Unit,
    private val onFavoriteClick: (Platform, Boolean) -> Unit
) : RecyclerView.Adapter<PlatformAdapter.PlatformViewHolder>() {
    
    inner class PlatformViewHolder(val binding: ItemPlatformBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformViewHolder {
        val binding =
            ItemPlatformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatformViewHolder, position: Int) {
        val platform = platforms[position]
        holder.binding.itemTitle.text = platform.title
        
        // 设置收藏状态
        val isFavorite = preferenceManager.isPlatformFavorite(platform)
        updateFavoriteButton(holder.binding, isFavorite)
        
        // 点击平台进入频道列表
        holder.binding.itemTitle.setOnClickListener { onItemClick(platform) }
        
        // 点击收藏按钮
        holder.binding.btnFavorite.setOnClickListener {
            val newState = !preferenceManager.isPlatformFavorite(platform)
            onFavoriteClick(platform, newState)
            updateFavoriteButton(holder.binding, newState)
        }
    }
    
    private fun updateFavoriteButton(binding: ItemPlatformBinding, isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on 
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = platforms.size
    
    fun updateData(newPlatforms: List<Platform>) {
        this.platforms = newPlatforms
        notifyDataSetChanged()
    }
}