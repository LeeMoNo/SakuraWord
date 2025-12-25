package com.tusizi.sakuraword.data

import androidx.compose.ui.graphics.vector.ImageVector

// --- 1. 数据模型 ---
data class Level(val level: String, val title: String)
data class Vocabulary(val id: Int, val kanji: String, val reading: String, val translation: String, val icon: ImageVector)

// --- 2. 模拟数据源 ---
val levels = listOf(
    Level("N5", "入门"),
    Level("N4", "基础"),
    Level("N3", "中级"),
    Level("N2", "上级"),
    Level("N1", "超上级")
)


// 数据类
data class Words(
    val id: String,
    val kanji: String,
    val kana: String,
    val meaning: String
)