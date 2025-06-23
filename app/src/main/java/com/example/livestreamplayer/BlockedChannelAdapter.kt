package com.example.livestreamplayer

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class BlockedChannelAdapter(
    private var blockedChannels: List<Channel>,
    private val preferenceManager: PreferenceManager,
    private val onUnblockClick: (Channel) -> Unit
) : RecyclerView.Adapter<BlockedChannelAdapter.BlockedChannelViewHolder>() {
    
    inner class BlockedChannelViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedChannelViewHolder {
        val binding =
            ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockedChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockedChannelViewHolder, position: Int) {
        val channel = blockedChannels[position]
        
        holder.binding.itemTitle.text = channel.title
        
        // 隐藏收藏按钮，因为这里只显示屏蔽的主播
        holder.binding.btnFavorite.visibility = ViewGroup.GONE
        
        // 屏蔽按钮显示为已屏蔽状态
        holder.binding.btnBlock.setImageResource(android.R.drawable.ic_delete)
        
        // 点击屏蔽按钮取消屏蔽
        holder.binding.btnBlock.setOnClickListener {
            // 使用try-catch包裹可能导致崩溃的代码
            try {
                onUnblockClick(channel)
            } catch (e: Exception) {
                Log.e("BlockedChannelAdapter", "Error when unblocking channel", e)
            }
        }
    }

    override fun getItemCount() = blockedChannels.size
    
    fun updateData(newBlockedChannels: List<Channel>) {
        this.blockedChannels = newBlockedChannels
        notifyDataSetChanged()
    }
}