package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class FavoriteChannelAdapter(
    private var favoriteChannels: List<FavoriteChannel>,
    private val preferenceManager: PreferenceManager,
    private val onItemClick: (FavoriteChannel) -> Unit,
    private val onFavoriteClick: (Channel, Boolean) -> Unit
) : RecyclerView.Adapter<FavoriteChannelAdapter.FavoriteChannelViewHolder>() {
    
    inner class FavoriteChannelViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteChannelViewHolder {
        val binding =
            ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteChannelViewHolder, position: Int) {
        val favoriteChannel = favoriteChannels[position]
        val channel = favoriteChannel.channel
        
        holder.binding.itemTitle.text = channel.title
        
        // 收藏的主播一定是收藏状态
        updateFavoriteButton(holder.binding, true)
        
        // 隐藏屏蔽按钮，因为这里只显示收藏的主播
        holder.binding.btnBlock.visibility = ViewGroup.GONE
        
        // 点击频道进入播放器
        holder.binding.itemTitle.setOnClickListener { onItemClick(favoriteChannel) }
        
        // 点击收藏按钮取消收藏
        holder.binding.btnFavorite.setOnClickListener {
            onFavoriteClick(channel, false)
            // 数据会在MainActivity中更新，这里不需要更新UI
        }
    }
    
    private fun updateFavoriteButton(binding: ItemChannelBinding, isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on 
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = favoriteChannels.size
    
    fun updateData(newFavoriteChannels: List<FavoriteChannel>) {
        this.favoriteChannels = newFavoriteChannels
        notifyDataSetChanged()
    }
}