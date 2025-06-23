package com.example.livestreamplayer

import com.google.gson.annotations.SerializedName

data class ChannelList(@SerializedName("zhubo") val channels: List<Channel>)
data class Channel(val title: String, val address: String) {
    // 添加扩展属性，判断是否为直播
    val isLive: Boolean
        get() = address.startsWith("http") && (address.contains(".m3u8") || address.contains(".flv")) ||
                address.startsWith("rtmp://")
}