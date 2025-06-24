package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemPlatformBinding

class PlatformAdapter(
    private var platforms: List<Platform>,
    private val preferenceManager: PreferenceManager,
    private val onItemClick: (Platform) -> Unit,
    private val onFavoriteClick: (Platform, Boolean) -> Unit,
    private val onBlockClick: (Platform, Boolean) -> Unit = { _, _ -> } // 默认空实现
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
        
        // 设置屏蔽状态
        val isBlocked = preferenceManager.isPlatformBlocked(platform)
        updateBlockButton(holder.binding, isBlocked)
        
        // 点击平台进入频道列表
        holder.binding.itemTitle.setOnClickListener { onItemClick(platform) }
        
        // 点击收藏按钮
        holder.binding.btnFavorite.setOnClickListener {
            val newState = !preferenceManager.isPlatformFavorite(platform)
            onFavoriteClick(platform, newState)
            updateFavoriteButton(holder.binding, newState)
        }
        
        // 点击屏蔽按钮
        holder.binding.btnBlock.setOnClickListener {
            val newState = !preferenceManager.isPlatformBlocked(platform)
            onBlockClick(platform, newState)
            updateBlockButton(holder.binding, newState)
        }
    }
    
    private fun updateFavoriteButton(binding: ItemPlatformBinding, isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on 
            else android.R.drawable.btn_star_big_off
        )
    }
    
    private fun updateBlockButton(binding: ItemPlatformBinding, isBlocked: Boolean) {
        binding.btnBlock.setImageResource(
            if (isBlocked) android.R.drawable.ic_delete 
            else android.R.drawable.ic_menu_close_clear_cancel
        )
    }

    override fun getItemCount() = platforms.size
    
    fun updateData(newPlatforms: List<Platform>) {
        // 过滤掉被屏蔽的平台
        val filteredPlatforms = newPlatforms.filter { platform ->
            !preferenceManager.isPlatformBlocked(platform)
        }
        this.platforms = filteredPlatforms
        notifyDataSetChanged()
    }
    
    // 添加一个不过滤的更新方法，用于屏蔽列表页面
    fun updateDataWithoutFilter(newPlatforms: List<Platform>) {
        this.platforms = newPlatforms
        notifyDataSetChanged()
    }
}