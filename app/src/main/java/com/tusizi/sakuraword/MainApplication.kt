package com.tusizi.sakuraword

import android.app.Application
//import com.facebook.react.PackageList
//import com.facebook.react.ReactApplication
//import com.facebook.react.ReactHost
//import com.facebook.react.ReactNativeHost
//import com.facebook.react.ReactPackage
//import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
//import com.facebook.react.defaults.DefaultReactNativeHost
//import com.facebook.soloader.SoLoader

/**
 * MainApplication 类实现 ReactApplication 接口
 * 用于初始化 React Native 环境
 */
class MainApplication : Application(){//, ReactApplication

//    override val reactNativeHost: ReactNativeHost = object : DefaultReactNativeHost(this) {
//        override fun getPackages(): List<ReactPackage> =
//            PackageList(this).packages.apply {
//                // 在这里可以添加自定义的 React Native 包
//                // Packages that cannot be autolinked yet can be added manually here
//            }
//
//        override fun getJSMainModuleName(): String = "index"
//
//        override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
//
//        override val isNewArchEnabled: Boolean = false // 新架构（Fabric）默认关闭
//
//        override val isHermesEnabled: Boolean = true // 启用 Hermes 引擎
//    }
//
//    override val reactHost: ReactHost
//        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
//        SoLoader.init(this, false)
//        if (BuildConfig.DEBUG) {
//            // 开发模式下可以启用 React Native 调试工具
//        }
    }
}

