package com.tusizi.sakuraword.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tusizi.sakuraword.data.DictEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictDetailScreen(
    entry: DictEntry,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词条详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = entry.word,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.reading,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                if (!entry.tone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[${entry.tone}]",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.jlpt?.let { jlpt ->
                    Badge(text = jlpt, containerColor = MaterialTheme.colorScheme.secondaryContainer)
                }
                if (entry.verified == 1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), Color(0xFF4CAF50))
                        Text(" 已认证", fontSize = 12.sp, color = Color(0xFF4CAF50))
                    }
                }
                if (entry.aiGenerated == 1) {
                    Badge(text = "AI", containerColor = Color(0xFFF0F0F0))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

            // Meaning
            Text("释义", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (!entry.pos.isNullOrBlank()) {
                Text(entry.pos, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = entry.zhCn,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp
            )

            if (entry.exampleJp != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("例句", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.exampleJp, fontSize = 15.sp, lineHeight = 22.sp)
                        if (entry.exampleZh != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(entry.exampleZh, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
            
            if (entry.frequency != null && entry.frequency > 0) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("使用频次: ${entry.frequency}", fontSize = 12.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun Badge(text: String, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
