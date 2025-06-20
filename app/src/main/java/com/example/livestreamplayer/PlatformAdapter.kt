// 粘贴上一个回答中 PlatformAdapter.kt 的完整代码
package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemListEntryBinding

class PlatformAdapter(
    private var platforms: List<Platform>,
    private val onItemClick: (Platform) -> Unit
) : RecyclerView.Adapter<PlatformAdapter.PlatformViewHolder>() {
    inner class PlatformViewHolder(val binding: ItemListEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformViewHolder {
        val binding =
            ItemListEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatformViewHolder, position: Int) {
        val platform = platforms[position]
        holder.binding.itemTitle.text = platform.title
        holder.itemView.setOnClickListener { onItemClick(platform) }
    }

    override fun getItemCount() = platforms.size
    fun updateData(newPlatforms: List<Platform>) {
        this.platforms = newPlatforms
        notifyDataSetChanged()
    }
}