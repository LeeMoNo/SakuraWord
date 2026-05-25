package com.tusizi.sakuraword.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tusizi.sakuraword.data.ArticleService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArticleListViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(ArticleListContract.State())
    val viewState: StateFlow<ArticleListContract.State> = _viewState.asStateFlow()

    private val articleService: ArticleService by lazy {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        Retrofit.Builder()
            .baseUrl(ArticleService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ArticleService::class.java)
    }

    init {
        loadArticles()
    }

    fun handleIntent(intent: ArticleListContract.Intent) {
        when (intent) {
            is ArticleListContract.Intent.LoadMore -> loadArticles()
            is ArticleListContract.Intent.Refresh -> refresh()
        }
    }

    private fun loadArticles() {
        val state = _viewState.value
        if (state.isLoading || !state.hasMore) return

        _viewState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val newArticles = articleService.getArticles(page = state.page)
                _viewState.update {
                    it.copy(
                        articles = it.articles + newArticles,
                        page = it.page + 1,
                        hasMore = newArticles.size >= 20,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun refresh() {
        _viewState.update { it.copy(articles = emptyList(), page = 1, hasMore = true, isRefreshing = true) }
        viewModelScope.launch {
            try {
                val newArticles = articleService.getArticles(page = 1)
                _viewState.update {
                    it.copy(
                        articles = newArticles,
                        page = 2,
                        hasMore = newArticles.size >= 20,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(isRefreshing = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}
