package com.example.livestreamplayer

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    companion object {
        private const val KEY_FAVORITE_PLATFORMS = "favorite_platforms"
        private const val KEY_FAVORITE_CHANNELS = "favorite_channels"
        private const val KEY_BLOCKED_CHANNELS = "blocked_channels"
    }
}

// 收藏的主播需要记录其所属平台
data class FavoriteChannel(val channel: Channel, val platformUrl: String)