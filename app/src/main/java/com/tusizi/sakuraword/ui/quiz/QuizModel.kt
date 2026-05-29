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
    val isAnswered: Boolean = false, // 是否已提交本题答案（仅在练习模式有效）
    val score: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val error: String? = null,
    
    // Phase 1 新增
    val isExamMode: Boolean = false,            // 是否为考试模式
    val userAnswers: Map<Int, Int> = emptyMap(), // 考试模式下记录所有答案 {题目索引: 选项ID}
    val timeSeconds: Int = 0,                  // 已用时间（秒）
    
    // Phase 2 新增
    val showAnswerSheet: Boolean = false,       // 是否显示答题卡
    
    // Phase 3 新增
    val wrongQuestionIndices: Set<Int> = emptySet(), // 记录答错的题目索引
    
    // Phase 4 新增
    val tipsContent: String? = null,             // tips.md 的内容
    val showTips: Boolean = false                // 是否显示提示对话框
)

/**
 * Quiz 意图
 */
sealed class QuizIntent {
    data class LoadQuiz(val title: String, val fileName: String, val isExamMode: Boolean = false) : QuizIntent()
    data class SelectOption(val index: Int) : QuizIntent()
    object SubmitAnswer : QuizIntent()
    object NextQuestion : QuizIntent()
    object Restart : QuizIntent()
    object Tick : QuizIntent() 
    
    // Phase 2 新增
    object ToggleAnswerSheet : QuizIntent()    // 切换答题卡显示/隐藏
    data class GoToQuestion(val index: Int) : QuizIntent() // 跳转到特定题目

    // Phase 4 新增
    object ToggleTips : QuizIntent()           // 切换提示显示/隐藏
}
