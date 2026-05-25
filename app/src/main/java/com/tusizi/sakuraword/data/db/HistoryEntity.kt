package com.tusizi.sakuraword.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tusizi.sakuraword.data.DictEntry

@Entity(tableName = "search_history")
data class HistoryEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val reading: String,
    val tone: String? = null,
    val zhCn: String,
    val pos: String? = null,
    val jlpt: String? = null,
    val frequency: Int? = null,
    val exampleJp: String? = null,
    val exampleZh: String? = null,
    val verified: Int? = 0,
    val aiGenerated: Int? = 1,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDictEntry() = DictEntry(
        id = id,
        word = word,
        reading = reading,
        tone = tone,
        zhCn = zhCn,
        pos = pos,
        jlpt = jlpt,
        frequency = frequency,
        exampleJp = exampleJp,
        exampleZh = exampleZh,
        verified = verified,
        aiGenerated = aiGenerated
    )

    companion object {
        fun fromDictEntry(entry: DictEntry) = HistoryEntity(
            id = entry.id,
            word = entry.word,
            reading = entry.reading,
            tone = entry.tone,
            zhCn = entry.zhCn,
            pos = entry.pos,
            jlpt = entry.jlpt,
            frequency = entry.frequency,
            exampleJp = entry.exampleJp,
            exampleZh = entry.exampleZh,
            verified = entry.verified,
            aiGenerated = entry.aiGenerated
        )
    }
}
