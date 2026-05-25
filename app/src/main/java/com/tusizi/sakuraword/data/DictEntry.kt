package com.tusizi.sakuraword.data

import com.google.gson.annotations.SerializedName

data class DictEntry(
    val id: Int,
    val word: String,
    val reading: String,
    val tone: String? = null,
    @SerializedName("zh_cn") val zhCn: String,
    val pos: String? = null,
    val jlpt: String? = null,
    val frequency: Int? = null,
    @SerializedName("example_jp") val exampleJp: String? = null,
    @SerializedName("example_zh") val exampleZh: String? = null,
    val verified: Int? = 0,
    @SerializedName("ai_generated") val aiGenerated: Int? = 1
)

data class SearchResponse(
    val results: List<DictEntry>
)
