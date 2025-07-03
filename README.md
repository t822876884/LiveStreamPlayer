# LiveStream Player (安卓直播播放器)
这是一个功能丰富的安卓直播应用，允许用户浏览来自不同平台的直播源，观看直播，并提供了收藏、屏蔽和录制直播等高级功能。

## ✨ 主要功能
动态平台加载: 从远程JSON地址动态获取和展示直播平台列表。

多级列表浏览: 支持“平台 -> 频道”的多级浏览模式。

## 个性化管理:

收藏: 用户可以收藏自己喜欢的平台和频道，并在专门的页面中查看。

屏蔽: 用户可以屏蔽不感兴趣的平台或频道，使其在列表中被隐藏。

独立的管理页面: 提供“收藏列表”和“屏蔽列表”的独立管理入口。

## 强大的视频播放器:

基于 ExoPlayer (Media3) 构建，性能优秀，支持格式广泛。

支持 HLS (.m3u8), FLV (.flv), 和 RTMP 等多种直播协议。

## 直播录制与下载:

集成了强大的 ffmpeg-kit 库，可将直播流录制并保存为本地视频文件。

下载任务在前台服务 (Foreground Service) 中运行，确保App退至后台时录制任务依然稳定可靠。

## 下载管理系统:

提供“下载设置”页面，允许用户使用安卓的存储访问框架 (Storage Access Framework) 自定义文件保存目录。

提供“下载任务”页面，用于查看正在进行和已完成的录制任务。

提供“已下载文件”页面，方便浏览和管理已录制好的视频文件。

## 现代化的UI设计:

主页采用仪表盘式布局，功能入口清晰明了。

使用底部导航栏 (BottomNavigationView) 在核心功能模块间切换。

列表项包含丰富的操作按钮，如复制地址、收藏、屏蔽和下载。

## 🛠️ 技术栈
语言: Kotlin

架构: 单Activity + 多Activity/Fragment混合模式

UI: XML布局 + ViewBinding

异步编程: Kotlin Coroutines

网络请求: Retrofit + Gson

媒体播放: ExoPlayer (Media3)

视频处理: FFmpeg-Kit

数据持久化: SharedPreferences

UI组件: Material Design Components

## 🚀 如何运行
克隆或下载本仓库到本地。

使用最新版的 Android Studio 打开项目。

等待 Gradle 自动同步并下载所有项目依赖。如果下载缓慢或失败，请检查 settings.gradle.kts 文件中的仓库配置，可考虑使用国内镜像。

直接点击 "Run 'app'" 按钮，即可在模拟器或连接的真机上运行。
