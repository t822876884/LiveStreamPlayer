package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemDownloadedFileBinding
import java.text.SimpleDateFormat
import java.util.*

class DownloadedFileAdapter(
    private var files: List<DocumentFile>,
    private val onItemClick: (DocumentFile) -> Unit,
    private val onDeleteClick: (DocumentFile) -> Unit  // 添加删除回调
) : RecyclerView.Adapter<DownloadedFileAdapter.FileViewHolder>() {
    
    inner class FileViewHolder(val binding: ItemDownloadedFileBinding) :
        RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemDownloadedFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FileViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        holder.binding.tvFileName.text = file.name
        file.lastModified().let {
            if (it > 0) {
                holder.binding.tvFileDate.text = "修改时间: ${dateFormat.format(Date(it))}"
            } else {
                holder.binding.tvFileDate.text = "修改时间: 未知"
            }
        }
        
        holder.binding.tvFileSize.text = "大小: ${formatFileSize(file.length())}"
        
        holder.itemView.setOnClickListener {
            onItemClick(file)
        }
        
        // 添加删除按钮点击事件
        holder.binding.btnDeleteFile.setOnClickListener {
            onDeleteClick(file)
        }
    }
    
    override fun getItemCount() = files.size
    
    fun updateData(newFiles: List<DocumentFile>) {
        this.files = newFiles
        notifyDataSetChanged()
    }
    
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}