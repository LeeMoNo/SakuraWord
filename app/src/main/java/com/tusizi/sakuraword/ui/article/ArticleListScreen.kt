package com.tusizi.sakuraword.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tusizi.sakuraword.data.Article
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel = viewModel(),
    onArticleClick: (String) -> Unit = {}
) {
    val state by viewModel.viewState.collectAsState()
    val listState = rememberLazyListState()

    // 检测滑动到底部加载更多
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMore && !state.isLoading) {
            viewModel.handleIntent(ArticleListContract.Intent.LoadMore)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("最新内容", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF111827)
                )
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (state.articles.isEmpty() && state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.articles) { _, article ->
                        ArticleCard(article = article, onClick = { onArticleClick(article.id) })
                    }

                    if (state.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
                            }
                        }
                    } else if (!state.hasMore && state.articles.isNotEmpty()) {
                        item {
                            Text(
                                text = "没有更多了",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                if (state.articles.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = error, color = Color.Red)
                        TextButton(onClick = { viewModel.handleIntent(ArticleListContract.Intent.Refresh) }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    val isVideo = article.type == "video"
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val timeStr = article.publishedAt?.let { dateFormat.format(it) } ?: dateFormat.format(article.createdAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column {
            // Cover Image
            if (article.coverUrl != null) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    AsyncImage(
                        model = article.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isVideo) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "文章",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = timeStr, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_view), // 暂时用自带的
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${article.viewCount}", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.width(8.dp))
                    // 点赞图标省略或用文字代替
                    Text(text = "👍 ${article.likeCount}", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}
