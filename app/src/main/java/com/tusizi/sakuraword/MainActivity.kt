package com.tusizi.sakuraword

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tusizi.sakuraword.data.Words
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

/**
 * https://dribbble.com/shots/26392061-Sakura   bg
 * https://gemini.google.com/share/27d89fd9d7c6  聊天记录
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SakuraWordTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding).clickable {
                            // 这里写点击逻辑，就像 Flutter 的 onTap
                            println("Text clicked!")
                            //设置点击事件，跳转到下一个页面 WordListActivity
                            startActivity(Intent(this, JLPTActivity::class.java))
                        }
                    )
                    Text( text = "Listening Game" , modifier = Modifier.padding(innerPadding).clickable(
                        onClick = {}
                    ))

                    val sampleWords = listOf(
                        Words("1", "桜", "さくら", "cherry blossom"),
                        Words("2", "花", "はな", "flower"),
                        Words("3", "春", "はる", "spring"),
                        Words("4", "月", "つき", "moon"),
                        Words("7", "勉強", "べんきょう", "Study"),
                        Words("5", "風", "かぜ", "wind"),
                        Words("6", "雪", "ゆき", "snow")
                    )

                    // 使用组件
                    ListeningGameView(
                        words = sampleWords,
                        onBack = { /* 返回操作 */ }
                    )

                    MatchingGameView(
                        words = sampleWords,
                        onBack = {}
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SakuraWordTheme {
        Greeting("Android")
    }
}