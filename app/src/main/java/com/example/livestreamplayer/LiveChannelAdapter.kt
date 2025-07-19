package com.example.livestreamplayer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class LiveChannelAdapter(
    private var liveChannels: List<FavoriteChannel>,
    private val preferenceManager: PreferenceManager,
    private val onItemClick: (FavoriteChannel) -> Unit,
    private val onFavoriteClick: (FavoriteChannel) -> Unit, // Takes the whole object
    private val onDownloadClick: (Channel) -> Unit
) : RecyclerView.Adapter<LiveChannelAdapter.LiveChannelViewHolder>() {

    inner class LiveChannelViewHolder(val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveChannelViewHolder {
        val binding =
            ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LiveChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LiveChannelViewHolder, position: Int) {
        val favoriteChannel = liveChannels[position]
        val channel = favoriteChannel.channel
        val context = holder.itemView.context

        holder.binding.itemTitle.text = channel.title

        // Set favorite status (always on) and handle unfavorite click
        holder.binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        holder.binding.btnFavorite.setOnClickListener {
            onFavoriteClick(favoriteChannel)
        }

        // Hide block button
        holder.binding.btnBlock.visibility = ViewGroup.GONE

        // Set live/recorded status tag
        if (channel.isLive) {
            holder.binding.itemStreamType.text = "直播"
            holder.binding.itemStreamType.setBackgroundResource(android.R.color.holo_red_light)
            holder.binding.itemStreamType.visibility = ViewGroup.VISIBLE

            // Show download button only for live streams
            holder.binding.btnDownload.visibility = ViewGroup.VISIBLE
        } else {
            holder.binding.itemStreamType.text = "录播"
            holder.binding.itemStreamType.setBackgroundResource(android.R.color.holo_blue_light)
            holder.binding.itemStreamType.visibility = ViewGroup.VISIBLE

            // Hide download button for recordings
            holder.binding.btnDownload.visibility = ViewGroup.GONE
        }

        // Click on title to play
        holder.binding.itemTitle.setOnClickListener { onItemClick(favoriteChannel) }

        // Click to copy stream URL
        holder.binding.btnCopy.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Stream URL", channel.address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "地址已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

        // Click to download
        holder.binding.btnDownload.setOnClickListener {
            if (channel.isLive) {
                onDownloadClick(channel)
            }
        }
    }

    override fun getItemCount() = liveChannels.size

    fun updateData(newLiveChannels: List<FavoriteChannel>) {
        this.liveChannels = newLiveChannels
        notifyDataSetChanged()
    }
}