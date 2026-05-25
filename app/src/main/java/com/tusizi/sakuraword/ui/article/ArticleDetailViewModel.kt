package com.tusizi.sakuraword.ui.article

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tusizi.sakuraword.data.ArticleService
import com.tusizi.sakuraword.util.DeviceUtil
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArticleDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _viewState = MutableStateFlow(ArticleDetailContract.State())
    val viewState: StateFlow<ArticleDetailContract.State> = _viewState.asStateFlow()

    private val deviceId = DeviceUtil.getDeviceId(application)

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

    fun handleIntent(intent: ArticleDetailContract.Intent) {
        when (intent) {
            is ArticleDetailContract.Intent.LoadDetail -> loadDetail(intent.id)
            is ArticleDetailContract.Intent.React -> react(intent.id, intent.reaction)
        }
    }

    private fun loadDetail(id: String) {
        _viewState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val article = articleService.getArticle(id)
                _viewState.update { it.copy(article = article, isLoading = false) }
                
                // 并行记录阅读和获取互动状态
                launch { articleService.recordView(id) }
                val reaction = articleService.getReaction(id, deviceId)
                _viewState.update { it.copy(reaction = reaction) }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun react(id: String, reaction: String) {
        if (_viewState.value.isReacting) return
        _viewState.update { it.copy(isReacting = true) }
        viewModelScope.launch {
            try {
                val newReaction = articleService.react(id, mapOf("device_id" to deviceId, "reaction" to reaction))
                _viewState.update { it.copy(reaction = newReaction, isReacting = false) }
            } catch (e: Exception) {
                _viewState.update { it.copy(isReacting = false) }
            }
        }
    }
}
