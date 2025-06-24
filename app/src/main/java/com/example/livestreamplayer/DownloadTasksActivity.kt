package com.example.livestreamplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityDownloadTasksBinding
import android.widget.Toast

class DownloadTasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadTasksBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var downloadTaskAdapter: DownloadTaskAdapter
    private var downloadService: DownloadService? = null
    private var bound = false
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DownloadService.LocalBinder
            downloadService = binder.getService()
            bound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            bound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "下载任务"
        preferenceManager = PreferenceManager(this)
        
        setupRecyclerView()
        bindService()
    }
    
    override fun onStart() {
        super.onStart()
        bindService()
    }
    
    override fun onResume() {
        super.onResume()
        updateTaskList()
    }
    
    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_download_tasks, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_files -> {
                val intent = Intent(this, DownloadedFilesActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, DownloadSettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupRecyclerView() {
        downloadTaskAdapter = DownloadTaskAdapter(
            emptyList(),
            onStopClick = { task -> stopDownload(task.id) },
            onDeleteClick = { task -> deleteTask(task.id) }  // 添加删除回调
        )
        
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@DownloadTasksActivity)
            adapter = downloadTaskAdapter
        }
    }
    
    private fun bindService() {
        Intent(this, DownloadService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    
    private fun updateTaskList() {
        val tasks = preferenceManager.getDownloadTasks()
        downloadTaskAdapter.updateData(tasks)
        
        if (tasks.isEmpty()) {
            binding.tvNoTasks.visibility = android.view.View.VISIBLE
            binding.recyclerViewTasks.visibility = android.view.View.GONE
        } else {
            binding.tvNoTasks.visibility = android.view.View.GONE
            binding.recyclerViewTasks.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun stopDownload(taskId: String) {
        val intent = Intent(this, DownloadService::class.java).apply {
            action = DownloadService.ACTION_STOP_DOWNLOAD
            putExtra(DownloadService.EXTRA_TASK_ID, taskId)
        }
        startService(intent)
        
        // 更新UI
        updateTaskList()
    }
    
    private fun deleteTask(taskId: String) {
        // 确保任务不在下载中
        val task = preferenceManager.getDownloadTasks().find { it.id == taskId }
        if (task?.status == DownloadStatus.DOWNLOADING) {
            Toast.makeText(this, "请先停止下载任务", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 从存储中删除任务
        preferenceManager.removeDownloadTask(taskId)
        
        // 更新UI
        updateTaskList()
        
        Toast.makeText(this, "任务已删除", Toast.LENGTH_SHORT).show()
    }
    
    // 添加此方法处理返回键行为
    override fun onBackPressed() {
        // 返回到首页而不是退出应用
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}