package com.tusizi.sakuraword.ui.article

import com.tusizi.sakuraword.data.Article
import com.tusizi.sakuraword.data.ReactionResponse

class ArticleDetailContract {
    data class State(
        val article: Article? = null,
        val reaction: ReactionResponse? = null,
        val isLoading: Boolean = false,
        val isReacting: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        data class LoadDetail(val id: String) : Intent()
        data class React(val id: String, val reaction: String) : Intent()
    }
}
