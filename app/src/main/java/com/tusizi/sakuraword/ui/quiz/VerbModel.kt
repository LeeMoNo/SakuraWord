package com.tusizi.sakuraword.ui.quiz

/**
 * 动词活用数据模型 (对应 n4.csv / n5.csv)
 */
data class Verb(
    val dictionaryForm: String, // 辞書形
    val masuForm: String,       // ます形
    val naiForm: String,        // ない形
    val teForm: String,         // て形
    val chinese: String,        // 中文
    val reading: String         // 发音
)

data class VerbQuizState(
    val title: String = "",
    val verbs: List<Verb> = emptyList(),
    val currentVerb: Verb? = null,
    val quizType: VerbQuizType = VerbQuizType.DIC_TO_MASU,
    val userInput: String = "",
    val isCorrect: Boolean? = null, // null: 未检查, true: 正确, false: 错误
    val combo: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class VerbQuizType(val label: String, val questionLabel: String) {
    DIC_TO_MASU("ます形", "辞书形 → ます形"),
    DIC_TO_TE("て形", "辞书形 → て形"),
    DIC_TO_NAI("ない形", "辞书形 → ない形"),
    MEANING_TO_DIC("辞书形", "中文意思 → 辞书形")
}

sealed class VerbQuizIntent {
    data class LoadVerbs(val title: String, val fileName: String) : VerbQuizIntent()
    data class UpdateInput(val input: String) : VerbQuizIntent()
    data class ChangeType(val type: VerbQuizType) : VerbQuizIntent()
    object CheckAnswer : VerbQuizIntent()
    object NextQuestion : VerbQuizIntent()
}
