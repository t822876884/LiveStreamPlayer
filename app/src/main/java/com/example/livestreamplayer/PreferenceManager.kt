package com.example.livestreamplayer

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "live_stream_preferences", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // 收藏的平台
    fun saveFavoritePlatform(platform: Platform) {
        val favorites = getFavoritePlatforms().toMutableList()
        if (!favorites.any { it.address == platform.address }) {
            favorites.add(platform)
            val json = gson.toJson(favorites)
            sharedPreferences.edit().putString(KEY_FAVORITE_PLATFORMS, json).apply()
        }
    }

    fun removeFavoritePlatform(platform: Platform) {
        val favorites = getFavoritePlatforms().toMutableList()
        favorites.removeAll { it.address == platform.address }
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString(KEY_FAVORITE_PLATFORMS, json).apply()
    }

    fun getFavoritePlatforms(): List<Platform> {
        val json = sharedPreferences.getString(KEY_FAVORITE_PLATFORMS, null) ?: return emptyList()
        val type = object : TypeToken<List<Platform>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isPlatformFavorite(platform: Platform): Boolean {
        return getFavoritePlatforms().any { it.address == platform.address }
    }

    // 收藏的主播
    fun saveFavoriteChannel(channel: Channel, platformUrl: String) {
        val favoriteChannel = FavoriteChannel(channel, platformUrl)
        val favorites = getFavoriteChannels().toMutableList()
        if (!favorites.any { it.channel.address == channel.address }) {
            favorites.add(favoriteChannel)
            val json = gson.toJson(favorites)
            sharedPreferences.edit().putString(KEY_FAVORITE_CHANNELS, json).apply()
        }
    }

    fun removeFavoriteChannel(channel: Channel) {
        val favorites = getFavoriteChannels().toMutableList()
        favorites.removeAll { it.channel.address == channel.address }
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString(KEY_FAVORITE_CHANNELS, json).apply()
    }

    fun getFavoriteChannels(): List<FavoriteChannel> {
        val json = sharedPreferences.getString(KEY_FAVORITE_CHANNELS, null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteChannel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isChannelFavorite(channel: Channel): Boolean {
        return getFavoriteChannels().any { it.channel.address == channel.address }
    }

    // 屏蔽的主播
    fun addBlockedChannel(channel: Channel) {
        val blocked = getBlockedChannels().toMutableList()
        if (!blocked.any { it.address == channel.address }) {
            blocked.add(channel)
            val json = gson.toJson(blocked)
            sharedPreferences.edit().putString(KEY_BLOCKED_CHANNELS, json).apply()
        }
    }

    fun removeBlockedChannel(channel: Channel) {
        val blocked = getBlockedChannels().toMutableList()
        blocked.removeAll { it.address == channel.address }
        val json = gson.toJson(blocked)
        sharedPreferences.edit().putString(KEY_BLOCKED_CHANNELS, json).apply()
    }

    fun getBlockedChannels(): List<Channel> {
        val json = sharedPreferences.getString(KEY_BLOCKED_CHANNELS, null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isChannelBlocked(channel: Channel): Boolean {
        return getBlockedChannels().any { it.address == channel.address }
    }

    // 下载路径配置
    fun saveDownloadPath(path: String) {
        sharedPreferences.edit().putString(KEY_DOWNLOAD_PATH, path).apply()
    }

    fun getDownloadPath(): String? {
        return sharedPreferences.getString(KEY_DOWNLOAD_PATH, null)
    }

    // 下载任务管理
    fun saveDownloadTask(task: DownloadTask) {
        val tasks = getDownloadTasks().toMutableList()
        // 移除相同ID的任务（如果存在）
        tasks.removeAll { it.id == task.id }
        tasks.add(task)
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_DOWNLOAD_TASKS, json).apply()
    }

    fun removeDownloadTask(taskId: String) {
        val tasks = getDownloadTasks().toMutableList()
        tasks.removeAll { it.id == taskId }
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_DOWNLOAD_TASKS, json).apply()
    }

    fun getDownloadTasks(): List<DownloadTask> {
        val json = sharedPreferences.getString(KEY_DOWNLOAD_TASKS, null) ?: return emptyList()
        val type = object : TypeToken<List<DownloadTask>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getActiveDownloadTasks(): List<DownloadTask> {
        return getDownloadTasks().filter { it.status == DownloadStatus.DOWNLOADING }
    }

    fun getCompletedDownloadTasks(): List<DownloadTask> {
        return getDownloadTasks().filter { it.status != DownloadStatus.DOWNLOADING }
    }

    fun updateDownloadTaskStatus(taskId: String, status: DownloadStatus, errorMessage: String? = null) {
        val tasks = getDownloadTasks().toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = tasks[taskIndex].copy(
                status = status,
                errorMessage = errorMessage,
                endTime = if (status != DownloadStatus.DOWNLOADING) Date() else null
            )
            tasks[taskIndex] = task
            val json = gson.toJson(tasks)
            sharedPreferences.edit().putString(KEY_DOWNLOAD_TASKS, json).apply()
        }
    }
    
    // 添加最后播放频道的保存和获取方法
    fun saveLastPlayedChannel(channel: Channel) {
        val json = gson.toJson(channel)
        sharedPreferences.edit().putString(KEY_LAST_PLAYED_CHANNEL, json).apply()
    }
    
    fun getLastPlayedChannel(): Channel? {
        val json = sharedPreferences.getString(KEY_LAST_PLAYED_CHANNEL, null) ?: return null
        return gson.fromJson(json, Channel::class.java)
    }

    // 屏蔽的平台
    fun addBlockedPlatform(platform: Platform) {
        val blocked = getBlockedPlatforms().toMutableList()
        if (!blocked.any { it.address == platform.address }) {
            blocked.add(platform)
            val json = gson.toJson(blocked)
            sharedPreferences.edit().putString(KEY_BLOCKED_PLATFORMS, json).apply()
        }
    }

    fun removeBlockedPlatform(platform: Platform) {
        val blocked = getBlockedPlatforms().toMutableList()
        blocked.removeAll { it.address == platform.address }
        val json = gson.toJson(blocked)
        sharedPreferences.edit().putString(KEY_BLOCKED_PLATFORMS, json).apply()
    }

    fun getBlockedPlatforms(): List<Platform> {
        val json = sharedPreferences.getString(KEY_BLOCKED_PLATFORMS, null) ?: return emptyList()
        val type = object : TypeToken<List<Platform>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isPlatformBlocked(platform: Platform): Boolean {
        return getBlockedPlatforms().any { it.address == platform.address }
    }

    companion object {
        private const val KEY_FAVORITE_PLATFORMS = "favorite_platforms"
        private const val KEY_FAVORITE_CHANNELS = "favorite_channels"
        private const val KEY_BLOCKED_CHANNELS = "blocked_channels"
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val KEY_DOWNLOAD_TASKS = "download_tasks"
        // 保留常量定义
        private const val KEY_LAST_PLAYED_CHANNEL = "last_played_channel"
        private const val KEY_BLOCKED_PLATFORMS = "blocked_platforms"
        private const val KEY_LAST_LIVE_REFRESH_TIME = "last_live_refresh_time"
    }
}