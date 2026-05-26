package com.tusizi.sakuraword.ui.quiz

/**
 * 题目数据模型
 */
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswer: Int // 1-4
)

/**
 * Quiz 界面状态
 */
data class QuizState(
    val title: String = "JLPT 测验",
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: Int? = null, // 用户当前选中的选项 (1-4)
    val isAnswered: Boolean = false, // 是否已提交本题答案
    val score: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val error: String? = null
)

/**
 * Quiz 意图
 */
sealed class QuizIntent {
    data class LoadQuiz(val title: String, val fileName: String) : QuizIntent()
    data class SelectOption(val index: Int) : QuizIntent()
    object SubmitAnswer : QuizIntent()
    object NextQuestion : QuizIntent()
    object Restart : QuizIntent()
}
