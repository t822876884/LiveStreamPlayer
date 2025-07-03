package com.example.livestreamplayer

import com.google.gson.annotations.SerializedName

data class ChannelList(@SerializedName("zhubo") val channels: List<Channel>)

data class Channel(val title: String, val address: String) {
    /**
     * 判断是否为直播的扩展属性。
     * 新逻辑：
     * - 如果地址包含 .m3u8 或 .flv，则判定为直播 (isLive = true)。
     * - 其他所有情况，包括 rtmp:// 开头的，都判定为录播 (isLive = false)。
     *
     * http://dsj.jshfgy.cn:8880/mgtv/624878396.m3u8
     * http://mtfvihalkhofnzhftui.xinzhitushu.xyz/live/cx_363373.flv
     * rtmp://xueli130.vihyvz.top/live/4516_1751345
     */
    val isLive: Boolean
        get() = address.contains(".m3u8", ignoreCase = true) || address.contains(
            ".flv",
            ignoreCase = true
        )
}