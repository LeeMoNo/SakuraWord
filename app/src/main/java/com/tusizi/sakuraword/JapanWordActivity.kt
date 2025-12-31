package com.tusizi.sakuraword

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * WebView Activity 用于加载日文学习 HTML 页面
 */
class JapanWordActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_HTML_FILE = "html_file"
        const val EXTRA_TITLE = "title"
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建 WebView
        webView = WebView(this)
        setContentView(webView)

        // 获取传入的 HTML 文件名和标题
        val htmlFile = intent.getStringExtra(EXTRA_HTML_FILE) ?: "n5.html"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "日文学习"
        
        // 设置标题
//        setTitle(title)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 配置 WebView
        webView.settings.apply {
            javaScriptEnabled = true  // 启用 JavaScript
            domStorageEnabled = true  // 启用 DOM 存储
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        // 设置 WebViewClient
        webView.webViewClient = WebViewClient()

        // 加载 assets 中的 HTML 文件
        webView.loadUrl("file:///android_asset/$htmlFile")
        
        // 处理返回按钮
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

