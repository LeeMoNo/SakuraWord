package com.tusizi.sakuraword.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tusizi.sakuraword.data.DictEntry
import com.tusizi.sakuraword.data.DictService
import com.tusizi.sakuraword.data.db.AppDatabase
import com.tusizi.sakuraword.data.db.HistoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _viewState = MutableStateFlow(SearchContract.State())
    val viewState: StateFlow<SearchContract.State> = _viewState.asStateFlow()

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()

    private val dictService: DictService by lazy {
        Retrofit.Builder()
            .baseUrl(DictService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictService::class.java)
    }

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyDao.getAllHistory().collect { historyEntities ->
                _viewState.update { it.copy(history = historyEntities.map { entity -> entity.toDictEntry() }) }
            }
        }
    }

    fun handleIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.UpdateQuery -> {
                _viewState.update { it.copy(query = intent.query) }
                if (intent.query.isBlank()) {
                    _viewState.update { it.copy(results = emptyList()) }
                }
            }
            is SearchContract.Intent.Search -> {
                performSearch()
            }
            is SearchContract.Intent.AddToHistory -> {
                addToHistory(intent.entry)
            }
            is SearchContract.Intent.ClearHistory -> {
                clearHistory()
            }
        }
    }

    private fun performSearch() {
        val currentQuery = _viewState.value.query
        if (currentQuery.isBlank()) return

        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = dictService.search(currentQuery)
                _viewState.update { it.copy(results = response.results, isLoading = false) }
            } catch (e: Exception) {
                _viewState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun addToHistory(entry: DictEntry) {
        viewModelScope.launch {
            historyDao.insert(HistoryEntity.fromDictEntry(entry))
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }
}
