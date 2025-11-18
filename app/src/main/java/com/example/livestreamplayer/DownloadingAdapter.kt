package com.example.livestreamplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemDownloadingTaskBinding

class DownloadingAdapter(
    private var tasks: MutableList<DownloadTask>,
    private val onCancelClick: (Context, DownloadTask) -> Unit,
    private val onPlayClick: (Context, DownloadTask) -> Unit
) : RecyclerView.Adapter<DownloadingAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemDownloadingTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<DownloadTask>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(private val binding: ItemDownloadingTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: DownloadTask) {
            binding.tvTaskName.text = task.channelTitle
            binding.btnCancel.setOnClickListener { onCancelClick(itemView.context, task) }
            binding.btnPlay.isEnabled = task.streamUrl.isNotEmpty()
            binding.btnPlay.setOnClickListener { onPlayClick(itemView.context, task) }
        }
    }
}
