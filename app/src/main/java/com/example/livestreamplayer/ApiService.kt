// 文件路径: app/src/main/java/com/example/livestreamplayer/ApiService.kt

package com.example.livestreamplayer

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    // 将返回值从 List<Platform> 修改为 PlatformResponse
    @GET("mf/json.txt")
    suspend fun getPlatforms(): Response<PlatformResponse>

    // 获取频道列表的这部分保持不变
    @GET
    suspend fun getChannels(@Url url: String): Response<ChannelList>
}