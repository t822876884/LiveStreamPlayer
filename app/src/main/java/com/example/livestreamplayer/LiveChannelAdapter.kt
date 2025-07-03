package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class LiveChannelAdapter(
    private var liveChannels: List<FavoriteChannel>,
    private val preferenceManager: PreferenceManager,
    private val onItemClick: (FavoriteChannel) -> Unit
) : RecyclerView.Adapter<LiveChannelAdapter.LiveChannelViewHolder>() {
    
    inner class LiveChannelViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveChannelViewHolder {
        val binding =
            ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LiveChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LiveChannelViewHolder, position: Int) {
        val liveChannel = liveChannels[position]
        val channel = liveChannel.channel
        
        holder.binding.itemTitle.text = channel.title
        
        // 显示为已收藏状态
        updateFavoriteButton(holder.binding, true)
        
        // 隐藏屏蔽按钮
        holder.binding.btnBlock.visibility = ViewGroup.GONE
        
        // 点击频道进入播放器
        holder.binding.itemTitle.setOnClickListener { onItemClick(liveChannel) }
        holder.binding.btnFavorite.setOnClickListener { /* 不做任何操作 */ }
    }
    
    private fun updateFavoriteButton(binding: ItemChannelBinding, isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on 
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = liveChannels.size
    
    fun updateData(newLiveChannels: List<FavoriteChannel>) {
        this.liveChannels = newLiveChannels
        notifyDataSetChanged()
    }
}