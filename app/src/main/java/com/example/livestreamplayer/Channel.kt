package com.example.livestreamplayer

import com.google.gson.annotations.SerializedName

data class ChannelList(@SerializedName("zhubo") val channels: List<Channel>)
data class Channel(val title: String, val address: String)