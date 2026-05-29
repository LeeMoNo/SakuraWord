package com.tusizi.sakuraword.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tusizi.sakuraword.data.db.AppDatabase
import com.tusizi.sakuraword.data.db.WrongQuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val db = AppDatabase.getDatabase(application)
    private val wrongQuestionDao = db.wrongQuestionDao()

    fun handleIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.LoadQuiz -> loadQuestions(intent.title, intent.fileName, intent.isExamMode)
            is QuizIntent.SelectOption -> selectOption(intent.index)
            is QuizIntent.SubmitAnswer -> submitAnswer()
            is QuizIntent.NextQuestion -> nextQuestion()
            is QuizIntent.Restart -> restartQuiz()
            is QuizIntent.Tick -> {
                _state.value = _state.value.copy(timeSeconds = _state.value.timeSeconds + 1)
            }
            is QuizIntent.ToggleAnswerSheet -> {
                _state.value = _state.value.copy(showAnswerSheet = !_state.value.showAnswerSheet)
            }
            is QuizIntent.GoToQuestion -> {
                _state.value = _state.value.copy(
                    currentIndex = intent.index,
                    selectedOption = _state.value.userAnswers[intent.index],
                    isAnswered = if (_state.value.isExamMode) false else _state.value.isAnswered,
                    showAnswerSheet = false
                )
            }
            is QuizIntent.ToggleTips -> {
                _state.value = _state.value.copy(showTips = !_state.value.showTips)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                handleIntent(QuizIntent.Tick)
            }
        }
    }

    private fun loadQuestions(title: String, fileName: String, isExamMode: Boolean) {
        _state.value = _state.value.copy(title = title, isLoading = true, isExamMode = isExamMode)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 加载 tips.md
                val tipsContent = getApplication<Application>().assets.open("tips.md")
                    .bufferedReader()
                    .readText()

                val questions = mutableListOf<Question>()
                val inputStream = getApplication<Application>().assets.open(fileName)
                val reader = inputStream.bufferedReader(Charset.forName("Shift_JIS"))
                val lines = reader.readLines()
                
                var i = 0
                while (i < lines.size) {
                    val line1 = lines[i].split(",")
                    if (line1.isNotEmpty() && line1[0].isNotBlank()) {
                        val id = line1[0].trim()
                        val text = line1[1].trim()
                        
                        if (i + 1 < lines.size) {
                            val line2 = lines[i + 1].split(",")
                            if (line2.size >= 6) {
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
                        i += 3 
                    } else {
                        i++
                    }
                }

                // --- 逻辑优化：随机化与题目数量控制 ---
                val shuffled = questions.shuffled()
                val finalQuestions = if (isExamMode) {
                    // N1 考试标准：语法部分通常在 20-30 题左右。这里设定为 30 题
                    shuffled.take(30)
                } else {
                    // 练习模式：全量随机显示
                    shuffled
                }
                
                _state.value = _state.value.copy(
                    questions = finalQuestions,
                    isLoading = false,
                    tipsContent = tipsContent,
                    error = if (finalQuestions.isEmpty()) "未找到有效题目" else null
                )
                startTimer()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "加载题目失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun selectOption(index: Int) {
        val currentState = _state.value
        if (!currentState.isAnswered) {
            val newUserAnswers = currentState.userAnswers.toMutableMap()
            newUserAnswers[currentState.currentIndex] = index
            _state.value = currentState.copy(
                selectedOption = index,
                userAnswers = newUserAnswers
            )
        }
    }

    private fun submitAnswer() {
        val currentState = _state.value
        if (currentState.selectedOption == null) return

        if (currentState.isExamMode) {
            nextQuestion()
        } else {
            val currentQuestion = currentState.questions.getOrNull(currentState.currentIndex) ?: return
            val isCorrect = currentState.selectedOption == currentQuestion.correctAnswer
            
            val newWrongIndices = currentState.wrongQuestionIndices.toMutableSet()
            if (!isCorrect) {
                newWrongIndices.add(currentState.currentIndex)
                saveToWrongDb(currentQuestion)
            }

            _state.value = currentState.copy(
                isAnswered = true,
                score = if (isCorrect) currentState.score + 1 else currentState.score,
                wrongQuestionIndices = newWrongIndices
            )
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        if (currentState.currentIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentIndex + 1
            _state.value = currentState.copy(
                currentIndex = nextIndex,
                selectedOption = currentState.userAnswers[nextIndex],
                isAnswered = false
            )
        } else {
            if (currentState.isExamMode) {
                calculateFinalScore()
            } else {
                _state.value = currentState.copy(isFinished = true)
            }
            timerJob?.cancel()
        }
    }

    private fun calculateFinalScore() {
        val currentState = _state.value
        var finalScore = 0
        val newWrongIndices = mutableSetOf<Int>()
        
        currentState.questions.forEachIndexed { index, question ->
            if (currentState.userAnswers[index] == question.correctAnswer) {
                finalScore++
            } else {
                newWrongIndices.add(index)
                saveToWrongDb(question)
            }
        }
        _state.value = currentState.copy(
            score = finalScore, 
            isFinished = true,
            wrongQuestionIndices = newWrongIndices
        )
    }

    private fun saveToWrongDb(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            wrongQuestionDao.insert(
                WrongQuestionEntity(
                    questionText = question.text,
                    options = question.options.joinToString("|"),
                    correctAnswer = question.correctAnswer,
                    explanation = null,
                    sourceTitle = _state.value.title
                )
            )
        }
    }

    private fun restartQuiz() {
        // 重新加载以确保重新触发随机抽取逻辑
        loadQuestions(_state.value.title, "mondai1kyu1_462.csv", _state.value.isExamMode)
        _state.value = _state.value.copy(
            currentIndex = 0,
            selectedOption = null,
            isAnswered = false,
            score = 0,
            isFinished = false,
            userAnswers = emptyMap(),
            timeSeconds = 0,
            wrongQuestionIndices = emptySet()
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
