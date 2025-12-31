package com.tusizi.sakuraword

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tusizi.sakuraword.data.Words
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

/**
 * https://dribbble.com/shots/26392061-Sakura   bg
 * https://gemini.google.com/share/27d89fd9d7c6  聊天记录
 * 日本语能力测试（JLPT）
 * https://github.com/dominhhai/jlpt-test 日本語能力試験 JLPT問題集
 * https://github.com/AnchorI/jlpt-kanji-dictionary  按 JLPT 级别组织的结构化日语汉字和词汇 JSON 数据集
 * https://github.com/mmm3w/JLPT-Mitsuki/blob/master/update/words。单词
 * https://github.com/nguyenduylong/JLPT_questions。可参考
 * https://github.com/Myrtenast3r/Kanji-Practice-App。 练习日语能力考试N1级汉字题
 */
class MainActivity : ComponentActivity() {

    data class LearningOption(
        val title: String,
        val description: String,
        val htmlFile: String,
        val icon: String
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val learningOptions = listOf(
            LearningOption("N5 动词测验", "JLPT N5 级别动词练习", "n5.html", "📝"),
            LearningOption("N4 动词测验", "JLPT N4 级别动词练习", "n4.html", "📖"),
            LearningOption("50音打字", "日文50音打字练习", "50.html", "⌨️"),
            LearningOption("生活日文", "日常生活单词学习", "seikatsu.html", "🏠"),
            LearningOption("单词配对", "单词配对游戏","",""),
            LearningOption("落樱辨音", "听语音识别出单词游戏","","")
        )

        val sampleWords = listOf(
            Words("1", "桜", "さくら", "cherry blossom"),
            Words("2", "花", "はな", "flower"),
            Words("3", "春", "はる", "spring"),
            Words("4", "月", "つき", "moon"),
            Words("7", "勉強", "べんきょう", "Study"),
            Words("5", "風", "かぜ", "wind"),
            Words("6", "雪", "ゆき", "snow")
        )
        
        setContent {
            SakuraWordTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("樱花单词 - 日文学习") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        // 主标题
                        Text(
                            text = "选择学习内容",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // 学习选项列表
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(learningOptions) { option ->
                                // 学习选项卡片
//                                if (option.title =="单词配对"){
                                    // 使用组件
//                                    ListeningGameView(
//                                        words = sampleWords,
//                                        onBack = { /* 返回操作 */ }
//                                    )
//                                } else if (option.title =="落樱辨音"){
//                                    MatchingGameView(
//                                        words = sampleWords,
//                                        onBack = {}
//                                    )
//                                } else{
                                LearningOptionCard(option) {
                                    // 点击后跳转到 WebView 页面
                                    val intent = Intent(this@MainActivity, JapanWordActivity::class.java).apply {
                                        putExtra(JapanWordActivity.EXTRA_HTML_FILE, option.htmlFile)
                                        putExtra(JapanWordActivity.EXTRA_TITLE, option.title)
                                    }
                                    startActivity(intent)
                                }
//                                }
                            }
                        }
                        
                        // 底部其他功能按钮
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, JLPTActivity::class.java))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("JLPT 词汇列表")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LearningOptionCard(option: MainActivity.LearningOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Text(
                text = option.icon,
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // 标题和描述
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = option.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // 箭头
            Text(
                text = "▶",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    SakuraWordTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                LearningOptionCard(
                    MainActivity.LearningOption(
                        "N5 动词测验",
                        "JLPT N5 级别动词练习",
                        "n5.html",
                        "📝"
                    )
                ) {}
            }
        }
    }
}