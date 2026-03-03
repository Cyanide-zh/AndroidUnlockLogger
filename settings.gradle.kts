// settings.gradle.kts (项目根目录)

pluginManagement {
    repositories {
        // 声明插件的查找仓库
        gradlePluginPortal()
        google()
        mavenCentral()
        //maven { setUrl("https://jitpack.io") }
        //maven("https://maven.aliyun.com/repository/google")
        //maven("https://maven.aliyun.com/repository/central")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // 声明依赖库的查找仓库
        google()
        mavenCentral()
        //maven { setUrl("https://jitpack.io") }
        //maven("https://maven.aliyun.com/repository/google")
        //maven("https://maven.aliyun.com/repository/central")

    }
}

rootProject.name = "MyUnlockLoggerApp" // 替换为您项目的名称
include(":app")