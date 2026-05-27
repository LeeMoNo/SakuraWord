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

class VerbQuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(VerbQuizState())
    val state: StateFlow<VerbQuizState> = _state.asStateFlow()

    fun handleIntent(intent: VerbQuizIntent) {
        when (intent) {
            is VerbQuizIntent.LoadVerbs -> loadVerbs(intent.title, intent.fileName)
            is VerbQuizIntent.UpdateInput -> _state.value = _state.value.copy(userInput = intent.input)
            is VerbQuizIntent.ChangeType -> {
                _state.value = _state.value.copy(quizType = intent.type)
                nextQuestion()
            }
            is VerbQuizIntent.CheckAnswer -> checkAnswer()
            is VerbQuizIntent.NextQuestion -> nextQuestion()
        }
    }

    private fun loadVerbs(title: String, fileName: String) {
        _state.value = _state.value.copy(title = title)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val verbs = mutableListOf<Verb>()
                val inputStream = getApplication<Application>().assets.open(fileName)
                // 指定使用 UTF-8 (因为 n4.csv 看起来是 UTF-8 编码)
                val reader = inputStream.bufferedReader(Charset.forName("UTF-8"))
                val lines = reader.readLines()
                
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 10 && parts[0] != "動詞") {
                        verbs.add(Verb(
                            dictionaryForm = parts[0].trim(),
                            masuForm = parts[1].trim(),
                            naiForm = parts[2].trim(),
                            teForm = parts[3].trim(),
                            chinese = parts[4].trim(),
                            reading = parts[9].trim()
                        ))
                    }
                }
                
                _state.value = _state.value.copy(verbs = verbs, isLoading = false)
                nextQuestion()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "加载失败: ${e.message}", isLoading = false)
            }
        }
    }

    private fun nextQuestion() {
        val verbs = _state.value.verbs
        if (verbs.isNotEmpty()) {
            _state.value = _state.value.copy(
                currentVerb = verbs.random(),
                userInput = "",
                isCorrect = null
            )
        }
    }

    private fun checkAnswer() {
        val state = _state.value
        val currentVerb = state.currentVerb ?: return
        val expected = when (state.quizType) {
            VerbQuizType.DIC_TO_MASU -> currentVerb.masuForm
            VerbQuizType.DIC_TO_TE -> currentVerb.teForm
            VerbQuizType.DIC_TO_NAI -> currentVerb.naiForm
            VerbQuizType.MEANING_TO_DIC -> currentVerb.dictionaryForm
        }

        val isCorrect = state.userInput.trim() == expected
        _state.value = state.copy(
            isCorrect = isCorrect,
            combo = if (isCorrect) state.combo + 1 else 0
        )
    }
}
