package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class ChannelAdapter(
    private var channels: List<Channel>,
    private val preferenceManager: PreferenceManager,
    private val platformUrl: String = "", // 平台URL，用于收藏主播时关联平台
    private val onItemClick: (Channel) -> Unit,
    private val onFavoriteClick: (Channel, Boolean) -> Unit,
    private val onBlockClick: (Channel, Boolean) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {
    
    inner class ChannelViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding =
            ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.binding.itemTitle.text = channel.title
        
        // 设置收藏和屏蔽状态
        val isFavorite = preferenceManager.isChannelFavorite(channel)
        val isBlocked = preferenceManager.isChannelBlocked(channel)
        
        updateFavoriteButton(holder.binding, isFavorite)
        updateBlockButton(holder.binding, isBlocked)
        
        // 点击频道进入播放器
        holder.binding.itemTitle.setOnClickListener { onItemClick(channel) }
        
        // 点击收藏按钮
        holder.binding.btnFavorite.setOnClickListener {
            val newState = !preferenceManager.isChannelFavorite(channel)
            onFavoriteClick(channel, newState)
            updateFavoriteButton(holder.binding, newState)
        }
        
        // 点击屏蔽按钮
        holder.binding.btnBlock.setOnClickListener {
            val newState = !preferenceManager.isChannelBlocked(channel)
            onBlockClick(channel, newState)
            updateBlockButton(holder.binding, newState)
        }
    }
    
    private fun updateFavoriteButton(binding: ItemChannelBinding, isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on 
            else android.R.drawable.btn_star_big_off
        )
    }
    
    private fun updateBlockButton(binding: ItemChannelBinding, isBlocked: Boolean) {
        binding.btnBlock.setImageResource(
            if (isBlocked) android.R.drawable.ic_delete 
            else android.R.drawable.ic_menu_close_clear_cancel
        )
    }

    override fun getItemCount() = channels.size
    
    // 添加获取当前列表的方法
    fun getCurrentList(): List<Channel> {
        return channels
    }
    
    fun updateData(newChannels: List<Channel>) {
        // 过滤掉被屏蔽的频道
        val filteredChannels = newChannels.filter { channel ->
            !preferenceManager.isChannelBlocked(channel)
        }
        this.channels = filteredChannels
        notifyDataSetChanged()
    }
}