package com.tusizi.sakuraword.ui.quiz

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.Locale

class LifeWordViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val _state = MutableStateFlow(LifeWordState())
    val state: StateFlow<LifeWordState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.JAPANESE
        }
    }

    fun handleIntent(intent: LifeWordIntent) {
        when (intent) {
            is LifeWordIntent.LoadWords -> loadWords(intent.title, intent.fileName)
            is LifeWordIntent.UpdateInput -> _state.value = _state.value.copy(userInput = intent.input)
            is LifeWordIntent.ChangeType -> {
                _state.value = _state.value.copy(quizType = intent.type)
                nextQuestion()
            }
            is LifeWordIntent.CheckAnswer -> checkAnswer()
            is LifeWordIntent.NextQuestion -> nextQuestion()
        }
    }

    private fun loadWords(title: String, fileName: String) {
        _state.value = _state.value.copy(title = title, isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wordsList = mutableListOf<LifeWord>()
                val inputStream = getApplication<Application>().assets.open(fileName)
                val reader = inputStream.bufferedReader(Charset.forName("UTF-8"))
                
                reader.readLines().forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        wordsList.add(LifeWord(
                            jp = parts[0].trim(),
                            reading = parts[1].trim(),
                            meaning = parts[2].trim()
                        ))
                    }
                }
                
                _state.value = _state.value.copy(words = wordsList, isLoading = false)
                nextQuestion()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "加载失败: ${e.message}", isLoading = false)
            }
        }
    }

    private fun nextQuestion() {
        val words = _state.value.words
        if (words.isNotEmpty()) {
            val nextWord = words.random()
            _state.value = _state.value.copy(
                currentWord = nextWord,
                userInput = "",
                isCorrect = null
            )
            // 在日->读音模式下，自动朗读题目
            if (_state.value.quizType == LifeWordQuizType.JP_TO_READING) {
                speak(nextWord.jp)
            }
        }
    }

    private fun checkAnswer() {
        val state = _state.value
        val currentWord = state.currentWord ?: return
        
        // 验证规则：
        // 1. 中->日模式：输入 jp 或 reading 均算正确 (兼容原 HTML 逻辑)
        // 2. 日->读音模式：必须输入 reading
        val isCorrect = when (state.quizType) {
            LifeWordQuizType.CN_TO_JP -> {
                state.userInput.trim() == currentWord.jp || state.userInput.trim() == currentWord.reading
            }
            LifeWordQuizType.JP_TO_READING -> {
                state.userInput.trim() == currentWord.reading
            }
        }

        _state.value = state.copy(
            isCorrect = isCorrect,
            combo = if (isCorrect) state.combo + 1 else 0
        )
        
        // 如果答错，朗读正确答案
        if (!isCorrect) {
            speak(currentWord.jp)
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
