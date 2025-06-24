package com.example.livestreamplayer

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemPlatformBinding

class BlockedPlatformAdapter(
    private var blockedPlatforms: List<Platform>,
    private val preferenceManager: PreferenceManager,
    private val onUnblockClick: (Platform) -> Unit
) : RecyclerView.Adapter<BlockedPlatformAdapter.BlockedPlatformViewHolder>() {
    
    inner class BlockedPlatformViewHolder(val binding: ItemPlatformBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedPlatformViewHolder {
        val binding =
            ItemPlatformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockedPlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockedPlatformViewHolder, position: Int) {
        val platform = blockedPlatforms[position]
        
        holder.binding.itemTitle.text = platform.title
        
        // 隐藏收藏按钮，因为这里只显示屏蔽的平台
        holder.binding.btnFavorite.visibility = ViewGroup.GONE
        
        // 屏蔽按钮显示为已屏蔽状态
        holder.binding.btnBlock.setImageResource(android.R.drawable.ic_delete)
        
        // 点击屏蔽按钮取消屏蔽
        holder.binding.btnBlock.setOnClickListener {
            // 使用try-catch包裹可能导致崩溃的代码
            try {
                onUnblockClick(platform)
            } catch (e: Exception) {
                Log.e("BlockedPlatformAdapter", "Error when unblocking platform", e)
            }
        }
    }

    override fun getItemCount() = blockedPlatforms.size
    
    fun updateData(newBlockedPlatforms: List<Platform>) {
        this.blockedPlatforms = newBlockedPlatforms
        notifyDataSetChanged()
    }
}