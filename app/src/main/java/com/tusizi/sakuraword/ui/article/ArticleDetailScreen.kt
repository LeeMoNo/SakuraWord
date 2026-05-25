package com.tusizi.sakuraword.ui.article

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    viewModel: ArticleDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.viewState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(articleId) {
        viewModel.handleIntent(ArticleDetailContract.Intent.LoadDetail(articleId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (scrollState.value > 100) Color.White else Color.Transparent,
                    navigationIconContentColor = Color(0xFF111827)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading && state.article == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.article?.let { article ->
                val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                val timeStr = article.publishedAt?.let { dateFormat.format(it) } ?: dateFormat.format(article.createdAt)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Cover Image
                    if (article.coverUrl != null) {
                        AsyncImage(
                            model = article.coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        // Type Badge
                        Surface(
                            color = Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (article.type == "video") "🎬 视频" else "📄 文章",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title
                        Text(
                            text = article.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827),
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Time
                        Text(
                            text = timeStr,
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(28.dp))

                        // HTML Content via WebView
                        HtmlText(html = article.content)

                        Spacer(modifier = Modifier.height(40.dp))

                        // Reaction Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.RemoveRedEye,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF9CA3AF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${state.reaction?.viewCount ?: article.viewCount} 次阅读",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            Row {
                                ReactionButton(
                                    icon = Icons.Outlined.ThumbUp,
                                    activeIcon = Icons.Filled.ThumbUp,
                                    label = "${state.reaction?.likeCount ?: article.likeCount}",
                                    isActive = state.reaction?.userReaction == "like",
                                    activeColor = Color(0xFF4F46E5),
                                    onTap = { viewModel.handleIntent(ArticleDetailContract.Intent.React(article.id, "like")) }
                                )
                                Spacer(modifier = Modifier.width(15.dp))
                                ReactionButton(
                                    icon = Icons.Outlined.ThumbDown,
                                    activeIcon = Icons.Filled.ThumbDown,
                                    label = "${state.reaction?.dislikeCount ?: article.dislikeCount}",
                                    isActive = state.reaction?.userReaction == "dislike",
                                    activeColor = Color(0xFFDC2626),
                                    onTap = { viewModel.handleIntent(ArticleDetailContract.Intent.React(article.id, "dislike")) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }

            state.error?.let { error ->
                Text(text = error, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlText(html: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                }
                webViewClient = WebViewClient()
                // 禁止 WebView 自身滚动，交给外层的 Column 控制
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        },
        update = { webView ->
            val styledHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        body { 
                            font-family: -apple-system, system-ui, sans-serif; 
                            font-size: 16px; 
                            line-height: 1.75; 
                            color: #1F2937;
                            margin: 0;
                            padding: 0;
                        }
                        img { 
                            max-width: 100% !important; 
                            height: auto !important; 
                            display: block; 
                            margin: 12px auto; 
                            border-radius: 8px;
                        }
                        p { margin-bottom: 1em; }
                        h1, h2, h3 { color: #111827; margin-top: 1.5em; }
                    </style>
                </head>
                <body>
                    $html
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ReactionButton(
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onTap: () -> Unit
) {
    val color by animateColorAsState(targetValue = if (isActive) activeColor else Color(0xFF6B7280))

    Row(
        modifier = Modifier
            .clickable { onTap() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
