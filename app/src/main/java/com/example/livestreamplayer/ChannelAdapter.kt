// 粘贴上一个回答中 ChannelAdapter.kt 的完整代码
package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemListEntryBinding

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onItemClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {
    inner class ChannelViewHolder(val binding: ItemListEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding =
            ItemListEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.binding.itemTitle.text = channel.title
        holder.itemView.setOnClickListener { onItemClick(channel) }
    }

    override fun getItemCount() = channels.size
    fun updateData(newChannels: List<Channel>) {
        this.channels = newChannels
        notifyDataSetChanged()
    }
}