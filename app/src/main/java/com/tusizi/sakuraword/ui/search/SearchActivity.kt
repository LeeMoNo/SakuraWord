package com.tusizi.sakuraword.ui.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tusizi.sakuraword.ui.theme.SakuraWordTheme

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SakuraWordTheme {
                SearchScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}
