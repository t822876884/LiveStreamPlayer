package com.example.livestreamplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.ActivityDownloadedFilesBinding
import android.widget.Toast  // 如果还没有导入
import androidx.appcompat.app.AlertDialog  // 添加这一行

class DownloadedFilesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadedFilesBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var fileAdapter: DownloadedFileAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadedFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "已下载文件"
        preferenceManager = PreferenceManager(this)
        
        setupRecyclerView()
        loadFiles()
    }
    
    private fun setupRecyclerView() {
        fileAdapter = DownloadedFileAdapter(
            emptyList(),
            onItemClick = { file -> openFile(file) },
            onDeleteClick = { file -> deleteFile(file) }  // 添加删除回调
        )
        
        binding.recyclerViewFiles.apply {
            layoutManager = LinearLayoutManager(this@DownloadedFilesActivity)
            adapter = fileAdapter
        }
    }
    
    private fun loadFiles() {
        val downloadPathUri = preferenceManager.getDownloadPath() ?: return
        val uri = Uri.parse(downloadPathUri)
        val directory = DocumentFile.fromTreeUri(this, uri) ?: return
        
        val files = directory.listFiles().filter { it.isFile && it.name?.endsWith(".mp4") == true }
        
        if (files.isEmpty()) {
            binding.tvNoFiles.visibility = View.VISIBLE
            binding.recyclerViewFiles.visibility = View.GONE
        } else {
            binding.tvNoFiles.visibility = View.GONE
            binding.recyclerViewFiles.visibility = View.VISIBLE
            fileAdapter.updateData(files.toList())
        }
    }
    
    private fun openFile(file: DocumentFile) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
    
    private fun deleteFile(file: DocumentFile) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("确认删除")
            .setMessage("确定要删除文件 ${file.name} 吗？")
            .setPositiveButton("删除") { _, _ ->
                if (file.delete()) {
                    Toast.makeText(this, "文件已删除", Toast.LENGTH_SHORT).show()
                    loadFiles()  // 重新加载文件列表
                } else {
                    Toast.makeText(this, "删除失败，请检查权限", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}