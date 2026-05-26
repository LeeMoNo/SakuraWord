package com.tusizi.sakuraword.ui.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

class QuizActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_NAME = "quiz_file"
        const val EXTRA_TITLE = "quiz_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "mondai1kyu1_462.csv"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "N1 语法练习"

        setContent {
            SakuraWordTheme {
                QuizScreen(
                    initialTitle = title,
                    initialFileName = fileName,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    initialTitle: String,
    initialFileName: String,
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(QuizIntent.LoadQuiz(initialTitle, initialFileName))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                state.isFinished -> {
                    QuizResultView(
                        score = state.score,
                        total = state.questions.size,
                        onRestart = { viewModel.handleIntent(QuizIntent.Restart) },
                        onBack = onBack
                    )
                }
                state.questions.isNotEmpty() -> {
                    val currentQuestion = state.questions[state.currentIndex]
                    QuizQuestionContent(
                        question = currentQuestion,
                        currentIndex = state.currentIndex,
                        totalCount = state.questions.size,
                        selectedOption = state.selectedOption,
                        isAnswered = state.isAnswered,
                        onOptionSelect = { viewModel.handleIntent(QuizIntent.SelectOption(it)) },
                        onSubmit = { viewModel.handleIntent(QuizIntent.SubmitAnswer) },
                        onNext = { viewModel.handleIntent(QuizIntent.NextQuestion) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizQuestionContent(
    question: Question,
    currentIndex: Int,
    totalCount: Int,
    selectedOption: Int?,
    isAnswered: Boolean,
    onOptionSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 进度和得分
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "进度: ${currentIndex + 1} / $totalCount",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalCount },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(8.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 题干
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = question.text,
                modifier = Modifier.padding(20.dp),
                fontSize = 20.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 选项列表
        question.options.forEachIndexed { index, optionText ->
            val optionNum = index + 1
            val isSelected = selectedOption == optionNum
            val isCorrect = question.correctAnswer == optionNum
            
            val containerColor = when {
                isAnswered && isCorrect -> Color(0xFFC8E6C9) // 正确答案绿
                isAnswered && isSelected && !isCorrect -> Color(0xFFFFCDD2) // 选错红
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                else -> Color.LightGray
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable(enabled = !isAnswered) { onOptionSelect(optionNum) },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = optionNum.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = optionText, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 提交/下一题按钮
        Button(
            onClick = { if (isAnswered) onNext() else onSubmit() },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isAnswered) "下一题" else "检查答案",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuizResultView(score: Int, total: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉 测验完成", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "你的得分", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "$score / $total",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("重新开始")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回主页")
        }
    }
}
