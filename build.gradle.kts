// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // 注意：React Native 插件不是通过这里声明的，而是通过 settings.gradle.kts 从 node_modules 加载
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}