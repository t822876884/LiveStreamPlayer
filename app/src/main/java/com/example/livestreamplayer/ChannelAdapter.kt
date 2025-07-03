package com.example.livestreamplayer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemChannelBinding

class ChannelAdapter(
    private var channels: List<Channel>,
    private val preferenceManager: PreferenceManager,
    private val platformUrl: String = "", // 平台URL，用于收藏主播时关联平台
    private val onItemClick: (Channel) -> Unit,
    private val onFavoriteClick: (Channel, Boolean) -> Unit,
    private val onBlockClick: (Channel, Boolean) -> Unit,
    private val onDownloadClick: ((Channel) -> Unit)? = null
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
        
        // 设置直播/录播标识
        if (channel.isLive) {
            holder.binding.itemStreamType.text = "直播"
            holder.binding.itemStreamType.setBackgroundResource(android.R.color.holo_red_light)
            holder.binding.itemStreamType.visibility = ViewGroup.VISIBLE
            
            // 只有直播才显示下载按钮
            holder.binding.btnDownload.visibility = ViewGroup.VISIBLE
        } else {
            holder.binding.itemStreamType.text = "录播"
            holder.binding.itemStreamType.setBackgroundResource(android.R.color.holo_blue_light)
            holder.binding.itemStreamType.visibility = ViewGroup.VISIBLE
            
            // 录播隐藏下载按钮
            holder.binding.btnDownload.visibility = ViewGroup.GONE
        }
        
        // 点击频道进入播放器
        holder.binding.itemTitle.setOnClickListener { onItemClick(channel) }

        // --- 新增：为复制按钮设置点击事件 ---
        holder.binding.btnCopy.setOnClickListener {
            val context = holder.itemView.context
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Stream URL", channel.address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "地址已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

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
        
        // 点击下载按钮
        holder.binding.btnDownload.setOnClickListener {
            if (channel.isLive) {
                if (onDownloadClick != null) {
                    onDownloadClick.invoke(channel)
                } else {
                    // 检查是否配置了下载路径
                    val downloadPath = preferenceManager.getDownloadPath()
                    if (downloadPath == null) {
                        // 如果未配置下载路径，提示用户
                        Toast.makeText(holder.itemView.context, "请先在设置中配置下载路径", Toast.LENGTH_SHORT).show()
                        
                        // 跳转到下载设置页面
                        val intent = Intent(holder.itemView.context, DownloadSettingsActivity::class.java)
                        holder.itemView.context.startActivity(intent)
                    } else {
                        // 创建下载任务
                        val task = DownloadService.createDownloadTask(holder.itemView.context, channel, channel.title)
                        if (task != null) {
                            preferenceManager.saveDownloadTask(task)
                            
                            // 启动下载服务
                            val intent = Intent(holder.itemView.context, DownloadService::class.java).apply {
                                action = DownloadService.ACTION_START_DOWNLOAD
                                putExtra(DownloadService.EXTRA_TASK_ID, task.id)
                            }
                            holder.itemView.context.startService(intent)
                            
                            Toast.makeText(holder.itemView.context, "开始录制: ${channel.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
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