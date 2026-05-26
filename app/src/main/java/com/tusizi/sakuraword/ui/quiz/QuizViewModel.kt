package com.tusizi.sakuraword.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    fun handleIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.LoadQuiz -> loadQuestions(intent.title, intent.fileName)
            is QuizIntent.SelectOption -> {
                if (!_state.value.isAnswered) {
                    _state.value = _state.value.copy(selectedOption = intent.index)
                }
            }
            is QuizIntent.SubmitAnswer -> submitAnswer()
            is QuizIntent.NextQuestion -> nextQuestion()
            is QuizIntent.Restart -> restartQuiz()
        }
    }

    private fun loadQuestions(title: String, fileName: String) {
        _state.value = _state.value.copy(title = title, isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val questions = mutableListOf<Question>()
                val inputStream = getApplication<Application>().assets.open(fileName)
                // 使用 Shift_JIS 编码读取日文 CSV
                val reader = inputStream.bufferedReader(Charset.forName("Shift_JIS"))
                val lines = reader.readLines()
                
                var i = 0
                while (i < lines.size) {
                    val line1 = lines[i].split(",")
                    // 检查是否是题目行 (第一列有 ID)
                    if (line1.isNotEmpty() && line1[0].isNotBlank()) {
                        val id = line1[0].trim()
                        val text = line1[1].trim()
                        
                        // 下一行应该是选项行
                        if (i + 1 < lines.size) {
                            val line2 = lines[i + 1].split(",")
                            if (line2.size >= 6) {
                                // 提取选项并去掉 "1)" "2)" 等前缀
                                val options = listOf(
                                    line2[1].trim().replace(Regex("^[1-4]\\)"), ""),
                                    line2[2].trim().replace(Regex("^[1-4]\\)"), ""),
                                    line2[3].trim().replace(Regex("^[1-4]\\)"), ""),
                                    line2[4].trim().replace(Regex("^[1-4]\\)"), "")
                                )
                                val correct = line2[5].trim().toIntOrNull() ?: 1
                                questions.add(Question(id, text, options, correct))
                            }
                        }
                        // 题目格式通常是 题目、选项、空行，所以加 3
                        i += 3 
                    } else {
                        i++
                    }
                }
                
                _state.value = _state.value.copy(
                    questions = questions,
                    isLoading = false,
                    error = if (questions.isEmpty()) "未找到有效题目" else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "加载题目失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun submitAnswer() {
        val currentState = _state.value
        if (currentState.selectedOption != null && !currentState.isAnswered) {
            val currentQuestion = currentState.questions.getOrNull(currentState.currentIndex) ?: return
            val isCorrect = currentState.selectedOption == currentQuestion.correctAnswer
            
            _state.value = currentState.copy(
                isAnswered = true,
                score = if (isCorrect) currentState.score + 1 else currentState.score
            )
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        if (currentState.currentIndex < currentState.questions.size - 1) {
            _state.value = currentState.copy(
                currentIndex = currentState.currentIndex + 1,
                selectedOption = null,
                isAnswered = false
            )
        } else {
            _state.value = currentState.copy(isFinished = true)
        }
    }

    private fun restartQuiz() {
        _state.value = _state.value.copy(
            currentIndex = 0,
            selectedOption = null,
            isAnswered = false,
            score = 0,
            isFinished = false
        )
    }
}
