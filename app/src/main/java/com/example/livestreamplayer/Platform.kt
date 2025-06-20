// 文件路径: app/src/main/java/com/example/livestreamplayer/Platform.kt

package com.example.livestreamplayer

import com.google.gson.annotations.SerializedName

// 这个类是新增的，用来匹配JSON的最外层对象
data class PlatformResponse(
    // @SerializedName("pingtai") 告诉GSON库，
    // JSON中的 "pingtai" 键对应我们这里的 "platforms" 变量
    @SerializedName("pingtai")
    val platforms: List<Platform>
)

// 这个类保持不变，代表列表中的每一个平台
data class Platform(
    val title: String,
    val address: String,
    val xinimg: String, // 即使我们不用，也最好声明出来以匹配JSON
    val number: String
)