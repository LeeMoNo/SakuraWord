package com.tusizi.sakuraword.ui.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
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

class LifeWordActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_NAME = "quiz_file"
        const val EXTRA_TITLE = "quiz_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "seikatsu.csv"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "生活单词练习"

        setContent {
            SakuraWordTheme {
                LifeWordQuizScreen(
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
fun LifeWordQuizScreen(
    initialTitle: String,
    initialFileName: String,
    onBack: () -> Unit,
    viewModel: LifeWordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(LifeWordIntent.LoadWords(initialTitle, initialFileName))
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
            ScrollableTabRow(
                selectedTabIndex = state.quizType.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                LifeWordQuizType.values().forEach { type ->
                    Tab(
                        selected = state.quizType == type,
                        onClick = { viewModel.handleIntent(LifeWordIntent.ChangeType(type)) },
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
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                } else if (state.currentWord != null) {
                    val word = state.currentWord!!
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
                            text = if (state.quizType == LifeWordQuizType.CN_TO_JP) word.meaning else word.jp,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        // 发音按钮
                        if (state.quizType == LifeWordQuizType.JP_TO_READING) {
                            IconButton(onClick = { viewModel.speak(word.jp) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "朗读", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(Modifier.height(48.dp))

                        OutlinedTextField(
                            value = state.userInput,
                            onValueChange = { viewModel.handleIntent(LifeWordIntent.UpdateInput(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("请输入答案...") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            singleLine = true,
                            isError = state.isCorrect == false,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.handleIntent(LifeWordIntent.CheckAnswer) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (state.isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                errorBorderColor = Color(0xFFF44336)
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        if (state.isCorrect != null) {
                            if (state.isCorrect == true) {
                                Text("✨ 正确！", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(1000)
                                    viewModel.handleIntent(LifeWordIntent.NextQuestion)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("❌ 错误", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                                    Text("正确日文: ${word.jp}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("读音: ${word.reading}", fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))

                        Button(
                            onClick = { 
                                if (state.isCorrect == false) {
                                    viewModel.handleIntent(LifeWordIntent.NextQuestion)
                                } else {
                                    viewModel.handleIntent(LifeWordIntent.CheckAnswer)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (state.isCorrect == false) "下一题" else "提交确认", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
