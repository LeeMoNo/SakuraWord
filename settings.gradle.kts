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
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // 关键：指向 node_modules 中的 React Native Maven 仓库（如果存在）
        val reactNativeDir = file("${rootDir}/../node_modules/react-native")
        if (reactNativeDir.exists()) {
            maven {
                url = uri("${rootDir}/../node_modules/react-native/android")
            }
            // 关键：指向 node_modules 中的 JSC 或 Hermes 引擎仓库
            val jscDir = file("${rootDir}/../node_modules/jsc-android")
            if (jscDir.exists()) {
                maven {
                    url = uri("${rootDir}/../node_modules/jsc-android/dist")
                }
            }
        }
    }
}

rootProject.name = "SakuraWord"
include(":app")

// 如果 React Native 已安装，应用其 Gradle 配置
val reactNativeDir = file("${rootDir}/../node_modules/react-native")
val reactNativeAutolinkingFile = file("${reactNativeDir}/scripts/autolinking.gradle.kts")
if (reactNativeAutolinkingFile.exists()) {
    apply(from = reactNativeAutolinkingFile)
}
