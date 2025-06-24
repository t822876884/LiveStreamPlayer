plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 添加 Kotlin 序列化插件
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.livestreamplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.livestreamplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // UI
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Networking (Retrofit & Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines for background tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Media Player (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // 移除冲突的ExoPlayer版本，只保留一个版本
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    
    // 确保RTMP扩展与ExoPlayer版本兼容
    implementation("com.google.android.exoplayer:extension-rtmp:2.18.7")  // 更新到兼容版本
    
    // 修改 FFmpeg-kit 依赖版本
    implementation("com.arthenica:ffmpeg-kit-full:5.1.LTS")
    // 或者尝试这个版本
    // implementation("com.arthenica:ffmpeg-kit-full:5.1.0")
    
    // 文件选择器
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // WorkManager用于后台任务
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // 添加 Kotlin 序列化依赖
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}