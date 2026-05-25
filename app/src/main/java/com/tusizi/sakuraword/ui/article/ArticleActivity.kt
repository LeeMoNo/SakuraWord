package com.tusizi.sakuraword.ui.article

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

class ArticleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SakuraWordTheme {
                var currentArticleId by remember { mutableStateOf<String?>(null) }

                if (currentArticleId == null) {
                    ArticleListScreen(
                        onArticleClick = { id ->
                            currentArticleId = id
                        }
                    )
                } else {
                    ArticleDetailScreen(
                        articleId = currentArticleId!!,
                        onBackClick = {
                            currentArticleId = null
                        }
                    )
                }
            }
        }
    }
}
