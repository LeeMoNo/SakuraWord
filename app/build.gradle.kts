plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// React Native 配置（如果已安装 npm 包）
// 注意：需要先运行 npm install 安装 React Native 后才能启用
val reactNativeDir = file("${rootDir}/../node_modules/react-native")
if (reactNativeDir.exists()) {
    // 应用 React Native 的自动链接脚本
    val autolinkingFile = file("${reactNativeDir}/scripts/autolinking.gradle.kts")
    if (autolinkingFile.exists()) {
        apply(from = autolinkingFile)
    }
}

android {
    namespace = "com.tusizi.sakuraword"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tusizi.sakuraword"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 确保 ndk 架构过滤，RN 必须指定
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    
    // 开启原生向量指令支持
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //XML传统依赖
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // React Native 依赖（需要先安装 npm 包：npm install react-native）
    // 这些依赖从 node_modules/react-native/android Maven 仓库获取
    val reactNativeDir = file("${rootDir}/../node_modules/react-native")
    if (reactNativeDir.exists()) {
        implementation("com.facebook.react:react-android")
        // 引入 Hermes 引擎
        implementation("com.facebook.react:hermes-android")
    }

}