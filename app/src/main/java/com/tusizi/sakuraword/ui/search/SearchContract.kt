package com.tusizi.sakuraword.ui.search

import com.tusizi.sakuraword.data.DictEntry

class SearchContract {
    data class State(
        val query: String = "",
        val results: List<DictEntry> = emptyList(),
        val history: List<DictEntry> = emptyList(), // 新增：搜索历史
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        data class UpdateQuery(val query: String) : Intent()
        object Search : Intent()
        data class AddToHistory(val entry: DictEntry) : Intent()
        object ClearHistory : Intent()
    }
}
