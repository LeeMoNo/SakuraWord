package com.tusizi.sakuraword.ui.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

class VerbQuizActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_NAME = "quiz_file"
        const val EXTRA_TITLE = "quiz_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "n4.csv"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "动词活用练习"

        setContent {
            SakuraWordTheme {
                VerbQuizScreen(
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
fun VerbQuizScreen(
    initialTitle: String,
    initialFileName: String,
    onBack: () -> Unit,
    viewModel: VerbQuizViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(VerbQuizIntent.LoadVerbs(initialTitle, initialFileName))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 16.dp).height(24.dp)
                    ) {
                        Text("Combo: ${state.combo}", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 模式选择 Tab
            ScrollableTabRow(
                selectedTabIndex = state.quizType.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                VerbQuizType.entries.forEach { type ->
                    Tab(
                        selected = state.quizType == type,
                        onClick = { viewModel.handleIntent(VerbQuizIntent.ChangeType(type)) },
                        text = { Text(type.label) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.currentVerb != null) {
                    val verb = state.currentVerb!!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
                    ) {
                        Text(
                            text = state.quizType.questionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        // 题目文本
                        Text(
                            text = if (state.quizType == VerbQuizType.MEANING_TO_DIC) verb.chinese else verb.dictionaryForm,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // 提示发音 (仅在中文转辞书模式显示)
                        if (state.quizType == VerbQuizType.MEANING_TO_DIC) {
                            Text(
                                text = "(${verb.reading})",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                text = verb.reading,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(Modifier.height(48.dp))

                        // 输入框
                        OutlinedTextField(
                            value = state.userInput,
                            onValueChange = { viewModel.handleIntent(VerbQuizIntent.UpdateInput(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("输入活用形...") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            singleLine = true,
                            isError = state.isCorrect == false,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.handleIntent(VerbQuizIntent.CheckAnswer) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (state.isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                errorBorderColor = Color(0xFFF44336)
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        // 反馈信息
                        if (state.isCorrect != null) {
                            val expectedAnswer = when (state.quizType) {
                                VerbQuizType.DIC_TO_MASU -> verb.masuForm
                                VerbQuizType.DIC_TO_TE -> verb.teForm
                                VerbQuizType.DIC_TO_NAI -> verb.naiForm
                                VerbQuizType.MEANING_TO_DIC -> verb.dictionaryForm
                            }

                            if (state.isCorrect == true) {
                                Text("✨ 太棒了！正确", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(1000)
                                    viewModel.handleIntent(VerbQuizIntent.NextQuestion)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("❌ 错误了", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                                    Text("正确答案是: $expectedAnswer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))

                        Button(
                            onClick = { 
                                if (state.isCorrect == false) {
                                    viewModel.handleIntent(VerbQuizIntent.NextQuestion)
                                } else {
                                    viewModel.handleIntent(VerbQuizIntent.CheckAnswer)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (state.isCorrect == false) "跳过此题" else "检查答案", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
