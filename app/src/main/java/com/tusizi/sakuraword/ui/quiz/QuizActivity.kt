package com.tusizi.sakuraword.ui.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
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
import java.util.Locale

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
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        // 根据标题动态判断模式：包含“测验”则开启考试模式（30题），否则为练习模式（随机全量）
        val isExam = initialTitle.contains("测验") || initialTitle.contains("模拟")
        viewModel.handleIntent(QuizIntent.LoadQuiz(initialTitle, initialFileName, isExamMode = isExam))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatTime(state.timeSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (state.questions.isNotEmpty() && !state.isFinished) {
                        IconButton(onClick = { viewModel.handleIntent(QuizIntent.ToggleTips) }) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "解题技巧", tint = Color(0xFFFFB300))
                        }
                        IconButton(onClick = { viewModel.handleIntent(QuizIntent.ToggleAnswerSheet) }) {
                            Icon(Icons.Default.GridView, contentDescription = "答题卡")
                        }
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
                        timeSeconds = state.timeSeconds,
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
                        isExamMode = state.isExamMode,
                        onOptionSelect = { viewModel.handleIntent(QuizIntent.SelectOption(it)) },
                        onSubmit = { viewModel.handleIntent(QuizIntent.SubmitAnswer) },
                        onNext = { viewModel.handleIntent(QuizIntent.NextQuestion) }
                    )
                }
            }
        }

        // 提示对话框
        if (state.showTips && state.tipsContent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.handleIntent(QuizIntent.ToggleTips) },
                title = { Text("日语解题技巧") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(state.tipsContent!!)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.handleIntent(QuizIntent.ToggleTips) }) {
                        Text("知道了")
                    }
                }
            )
        }

        if (state.showAnswerSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.handleIntent(QuizIntent.ToggleAnswerSheet) },
                sheetState = sheetState
            ) {
                AnswerSheetContent(
                    totalCount = state.questions.size,
                    currentIndex = state.currentIndex,
                    userAnswers = state.userAnswers,
                    onNavigate = { viewModel.handleIntent(QuizIntent.GoToQuestion(it)) }
                )
            }
        }
    }
}

@Composable
fun AnswerSheetContent(
    totalCount: Int,
    currentIndex: Int,
    userAnswers: Map<Int, Int>,
    onNavigate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "答题卡",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(totalCount) { index ->
                val isSelected = index == currentIndex
                val isAnswered = userAnswers.containsKey(index)
                
                Surface(
                    onClick = { onNavigate(index) },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isAnswered -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = if (!isAnswered && !isSelected) BorderStroke(1.dp, Color.LightGray) else null,
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

@Composable
fun QuizQuestionContent(
    question: Question,
    currentIndex: Int,
    totalCount: Int,
    selectedOption: Int?,
    isAnswered: Boolean,
    isExamMode: Boolean,
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
            if (isExamMode) {
                Text(text = "考试模式", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
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
                !isExamMode && isAnswered && isCorrect -> Color(0xFFC8E6C9) // 练习模式：正确绿
                !isExamMode && isAnswered && isSelected && !isCorrect -> Color(0xFFFFCDD2) // 练习模式：选错红
                isSelected -> MaterialTheme.colorScheme.primaryContainer // 选中态
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

        // 按钮逻辑：考试模式下，如果是最后一题，显示“提交试卷”
        val isLastQuestion = currentIndex == totalCount - 1
        val buttonText = when {
            isExamMode && isLastQuestion -> "提交试卷"
            isExamMode -> "下一题"
            isAnswered -> "下一题"
            else -> "检查答案"
        }

        Button(
            onClick = { 
                if (isExamMode) {
                    onSubmit() 
                } else {
                    if (isAnswered) onNext() else onSubmit()
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuizResultView(score: Int, total: Int, timeSeconds: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉 测验完成", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "耗时: ${formatTime(timeSeconds)}", style = MaterialTheme.typography.bodyMedium)
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
