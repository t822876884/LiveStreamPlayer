pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 使用阿里云的镜像作为主仓库，速度会快很多
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }

        // 保留官方仓库作为备用
        google()
        mavenCentral()

        // 如果有其他库需要，可以保留
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "LiveStreamPlayer"
include(":app")
