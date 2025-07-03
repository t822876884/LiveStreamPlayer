package com.example.livestreamplayer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.livestreamplayer.databinding.ActivityDownloadSettingsBinding

class DownloadSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadSettingsBinding
    private lateinit var preferenceManager: PreferenceManager
    
    private val directoryPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // 持久化权限
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                preferenceManager.saveDownloadPath(uri.toString())
                updatePathDisplay()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "设置"
        preferenceManager = PreferenceManager(this)
        
        updatePathDisplay()
        updateStorageInfo()
        
        binding.btnSelectDirectory.setOnClickListener {
            openDirectoryPicker()
        }
        
        binding.btnSave.setOnClickListener {
            if (preferenceManager.getDownloadPath() != null) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "请先选择下载目录", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnViewDownloads.setOnClickListener {
            val intent = Intent(this, DownloadedFilesActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnViewTasks.setOnClickListener {
            val intent = Intent(this, DownloadTasksActivity::class.java)
            startActivity(intent)
        }
        
        // 设置底部导航栏
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish() // 结束当前Activity
                    true
                }
                R.id.nav_platforms -> {
                    val intent = Intent(this, PlatformListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    // 已经在设置页面，不需要操作
                    true
                }
                else -> false
            }
        }
        
        // 设置当前选中的导航项
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
    }
    
    private fun updatePathDisplay() {
        val path = preferenceManager.getDownloadPath()
        if (path != null) {
            binding.tvSelectedPath.text = "已选择: $path"
            updateStorageInfo()
        } else {
            binding.tvSelectedPath.text = "未选择下载目录"
            binding.tvStorageInfo.text = "存储信息：未选择目录"
        }
    }
    
    private fun updateStorageInfo() {
        val path = preferenceManager.getDownloadPath() ?: return
        val uri = Uri.parse(path)
        val directory = DocumentFile.fromTreeUri(this, uri) ?: return
        
        // 获取已下载文件总大小
        val files = directory.listFiles().filter { it.isFile && it.name?.endsWith(".mp4") == true }
        var totalSize = 0L
        files.forEach { file ->
            totalSize += file.length()
        }
        
        // 格式化大小显示
        val formattedSize = formatFileSize(totalSize)
        binding.tvStorageInfo.text = "存储信息：已下载 ${files.size} 个文件，总大小 $formattedSize"
    }
    
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
    
    private fun openDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        directoryPicker.launch(intent)
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