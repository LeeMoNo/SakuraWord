package com.tusizi.sakuraword.ui.quiz

/**
 * 生活单词数据模型
 */
data class LifeWord(
    val jp: String,
    val reading: String,
    val meaning: String
)

data class LifeWordState(
    val title: String = "",
    val words: List<LifeWord> = emptyList(),
    val currentWord: LifeWord? = null,
    val quizType: LifeWordQuizType = LifeWordQuizType.CN_TO_JP,
    val userInput: String = "",
    val isCorrect: Boolean? = null,
    val combo: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class LifeWordQuizType(val label: String, val questionLabel: String) {
    CN_TO_JP("中 → 日", "请输出对应的日文汉字或假名"),
    JP_TO_READING("日 → 读音", "请输入该单词的读音（平假名）")
}

sealed class LifeWordIntent {
    data class LoadWords(val title: String, val fileName: String) : LifeWordIntent()
    data class UpdateInput(val input: String) : LifeWordIntent()
    data class ChangeType(val type: LifeWordQuizType) : LifeWordIntent()
    object CheckAnswer : LifeWordIntent()
    object NextQuestion : LifeWordIntent()
}
