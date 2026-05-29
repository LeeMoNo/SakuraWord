package com.tusizi.sakuraword.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_questions")
data class WrongQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionText: String,
    val options: String, // 以 | 分隔的选项
    val correctAnswer: Int,
    val explanation: String?,
    val sourceTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)
