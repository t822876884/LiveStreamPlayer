package com.example.livestreamplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemDownloadTaskBinding
import java.text.SimpleDateFormat
import java.util.*

public class DownloadTaskAdapter(
    private var tasks: List<DownloadTask>,
    private val onStopClick: (DownloadTask) -> Unit,
    private val onDeleteClick: (DownloadTask) -> Unit  // 添加删除回调
) : RecyclerView.Adapter<DownloadTaskAdapter.TaskViewHolder>() {
    
    inner class TaskViewHolder(val binding: ItemDownloadTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemDownloadTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        holder.binding.tvTaskTitle.text = task.channelTitle
        holder.binding.tvStartTime.text = "开始时间: ${dateFormat.format(task.startTime)}"
        
        when (task.status) {
            DownloadStatus.DOWNLOADING -> {
                holder.binding.tvStatus.text = "下载中"
                holder.binding.btnStopDownload.visibility = View.VISIBLE
                holder.binding.tvEndTime.visibility = View.GONE
                holder.binding.tvError.visibility = View.GONE
            }
            DownloadStatus.COMPLETED -> {
                holder.binding.tvStatus.text = "已完成"
                holder.binding.btnStopDownload.visibility = View.GONE
                holder.binding.tvEndTime.visibility = View.VISIBLE
                holder.binding.tvEndTime.text = "结束时间: ${dateFormat.format(task.endTime)}"
                holder.binding.tvError.visibility = View.GONE
            }
            DownloadStatus.CANCELLED -> {
                holder.binding.tvStatus.text = "已取消"
                holder.binding.btnStopDownload.visibility = View.GONE
                holder.binding.tvEndTime.visibility = View.VISIBLE
                holder.binding.tvEndTime.text = "结束时间: ${dateFormat.format(task.endTime)}"
                holder.binding.tvError.visibility = View.GONE
            }
            DownloadStatus.ERROR -> {
                holder.binding.tvStatus.text = "下载失败"
                holder.binding.btnStopDownload.visibility = View.GONE
                holder.binding.tvEndTime.visibility = View.VISIBLE
                holder.binding.tvEndTime.text = "结束时间: ${dateFormat.format(task.endTime)}"
                holder.binding.tvError.visibility = View.VISIBLE
                holder.binding.tvError.text = "错误: ${task.errorMessage ?: "未知错误"}"
            }
        }
        
        holder.binding.btnStopDownload.setOnClickListener {
            onStopClick(task)
        }
        
        // 添加删除按钮的可见性和点击事件
        holder.binding.btnDeleteTask.visibility = if (task.status != DownloadStatus.DOWNLOADING) View.VISIBLE else View.GONE
        holder.binding.btnDeleteTask.setOnClickListener {
            onDeleteClick(task)
        }
    }
    
    override fun getItemCount() = tasks.size
    
    fun updateData(newTasks: List<DownloadTask>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}