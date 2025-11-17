package com.example.livestreamplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestreamplayer.databinding.ItemCompletedTaskBinding

class CompletedAdapter(
    private var tasks: MutableList<DownloadTask>,
    private val onDeleteClick: (Context, DownloadTask) -> Unit,
    private val onPlayClick: (Context, DownloadTask) -> Unit
) : RecyclerView.Adapter<CompletedAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemCompletedTaskBinding.inflate(
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

    inner class TaskViewHolder(private val binding: ItemCompletedTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: DownloadTask) {
            binding.tvTaskName.text = task.channelTitle
            binding.btnDelete.setOnClickListener { onDeleteClick(itemView.context, task) }
            binding.btnPlay.setOnClickListener { onPlayClick(itemView.context, task) }
        }
    }
}
