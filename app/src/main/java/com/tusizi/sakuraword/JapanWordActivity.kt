package com.tusizi.sakuraword

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MVI 状态定义
 */
data class JapanWordState(
    val title: String = "",
    val url: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * MVI 意图定义
 */
sealed class JapanWordIntent {
    data class LoadPage(val title: String, val fileName: String) : JapanWordIntent()
}

/**
 * JapanWord ViewModel
 */
class JapanWordViewModel : ViewModel() {
    private val _state = MutableStateFlow(JapanWordState())
    val state: StateFlow<JapanWordState> = _state.asStateFlow()

    fun handleIntent(intent: JapanWordIntent) {
        when (intent) {
            is JapanWordIntent.LoadPage -> {
                if (intent.fileName.isBlank()) {
                    _state.value = _state.value.copy(
                        title = intent.title,
                        isLoading = false,
                        error = "暂无该章节 HTML 内容"
                    )
                } else {
                    _state.value = _state.value.copy(
                        title = intent.title,
                        url = "file:///android_asset/${intent.fileName}",
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }
}

/**
 * WebView Activity 用于加载日文学习 HTML 页面 (已重构为 Compose + MVI)
 */
class JapanWordActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HTML_FILE = "html_file"
        const val EXTRA_TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 启用沉浸式状态栏，解决显示区域偏移问题
        enableEdgeToEdge()
        
        val htmlFile = intent.getStringExtra(EXTRA_HTML_FILE) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "日文学习"

        setContent {
            SakuraWordTheme {
                JapanWordScreen(
                    initialTitle = title,
                    initialFileName = htmlFile,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JapanWordScreen(
    initialTitle: String,
    initialFileName: String,
    onBack: () -> Unit,
    viewModel: JapanWordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var webViewRef: WebView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(JapanWordIntent.LoadPage(initialTitle, initialFileName))
    }

    // 处理物理返回键
    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // 正确应用 Scaffold 的 padding
        ) {
            if (state.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (state.url.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                // 2. 增加自适应屏幕的设置
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }
                            loadUrl(state.url)
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        // 如果 URL 变化，在此处更新，虽然这里 URL 是初始化的，但这样写更健壮
                        if (view.url != state.url) {
                            view.loadUrl(state.url)
                        }
                    }
                )
            }
            
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
