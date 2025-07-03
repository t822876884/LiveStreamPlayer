package com.example.livestreamplayer

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemPlatformBinding

class FavoritePlatformAdapter(
    private var favoritePlatforms: List<Platform>,
    private val preferenceManager: PreferenceManager,
    private val onRemoveFavoriteClick: (Platform) -> Unit,
    private val onPlatformClick: ((Platform) -> Unit)? = null
) : RecyclerView.Adapter<FavoritePlatformAdapter.FavoritePlatformViewHolder>() {
    
    inner class FavoritePlatformViewHolder(val binding: ItemPlatformBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlatformViewHolder {
        val binding =
            ItemPlatformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritePlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritePlatformViewHolder, position: Int) {
        val platform = favoritePlatforms[position]
        
        holder.binding.itemTitle.text = platform.title
        
        // 设置收藏按钮为已收藏状态
        holder.binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        
        // 隐藏屏蔽按钮，因为这里只显示收藏的平台
        holder.binding.btnBlock.visibility = ViewGroup.GONE
        
        // 点击平台标题打开频道列表
        holder.binding.itemTitle.setOnClickListener {
            if (onPlatformClick != null) {
                onPlatformClick.invoke(platform)
            } else {
                // 默认行为：打开频道列表
                val intent = Intent(holder.itemView.context, ChannelListActivity::class.java).apply {
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_URL, platform.address)
                    putExtra(ChannelListActivity.EXTRA_PLATFORM_TITLE, platform.title)
                }
                holder.itemView.context.startActivity(intent)
            }
        }
        
        // 点击收藏按钮取消收藏
        holder.binding.btnFavorite.setOnClickListener {
            // 使用try-catch包裹可能导致崩溃的代码
            try {
                onRemoveFavoriteClick(platform)
            } catch (e: Exception) {
                Log.e("FavoritePlatformAdapter", "Error when removing favorite platform", e)
            }
        }
    }

    override fun getItemCount() = favoritePlatforms.size
    
    fun updateData(newFavoritePlatforms: List<Platform>) {
        this.favoritePlatforms = newFavoritePlatforms
        notifyDataSetChanged()
    }
}