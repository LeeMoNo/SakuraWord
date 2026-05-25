package com.tusizi.sakuraword.ui.article

import com.tusizi.sakuraword.data.Article

class ArticleListContract {
    data class State(
        val articles: List<Article> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val hasMore: Boolean = true,
        val page: Int = 1,
        val error: String? = null
    )

    sealed class Intent {
        object LoadMore : Intent()
        object Refresh : Intent()
    }
}
