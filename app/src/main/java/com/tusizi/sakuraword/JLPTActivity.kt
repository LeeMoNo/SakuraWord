package com.tusizi.sakuraword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tusizi.sakuraword.data.Vocabulary
import com.tusizi.sakuraword.data.levels
import com.tusizi.sakuraword.ui.theme.LightPinkBg
import com.tusizi.sakuraword.ui.theme.PrimaryPink
import com.tusizi.sakuraword.ui.theme.UnselectedPink
import androidx.compose.foundation.shape.GenericShape


val vocabularyMap = mapOf(
    "N5" to listOf(Vocabulary(1, "私", "わたし", "I / Me", Icons.Default.Person)),
    "N4" to listOf(Vocabulary(1, "食べる", "たべる", "To Eat", Icons.Default.Person)),
    "N3" to listOf(
        Vocabulary(1, "花見", "はなみ", "Flower Viewing", Icons.Default.Person),
        Vocabulary(2, "春", "はる", "Spring", Icons.Default.Person),
        Vocabulary(3, "卒業", "そつぎょう", "Graduation", Icons.Default.Person)
    ),
    "N2" to listOf(Vocabulary(1, "議論", "ぎろん", "Discussion", Icons.Default.Place)),
    "N1" to listOf(Vocabulary(1, "卓越", "たくえつ", "Excellence", Icons.Default.Star))
)


class JLPTActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 设置 Material3 主题
            MaterialTheme {
                JLPTMainScreen()
            }
        }
    }

}

@Composable
fun JLPTMainScreen() {
    // 关键状态：当前选中的等级 (默认 N3)
    var selectedLevelCode by remember { mutableStateOf("N3") }

    // 根据状态获取数据
    val currentWords = vocabularyMap[selectedLevelCode] ?: emptyList()
    // 将 dp 转换为 px 供 Path 使用
    val density = androidx.compose.ui.platform.LocalDensity.current
    val arcHeightPx = with(density) { 40.dp.toPx() } // 这里控制弧度的“陡峭”程度

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBg)
    ) {
        // 顶部标题
        Text(
            text = "単語帳を選ぶ",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E342E)
        )

        // 等级选择器横向列表
        LevelSelectorRow(
            selectedLevel = selectedLevelCode,
            onLevelSelected = { selectedLevelCode = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 下半部分白色容器
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = getArcShape(arcHeightPx)
        ) {
            // 切换数据时的淡入淡出动画
            Crossfade(targetState = currentWords, label = "ListAnimation") { words ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
                ) {
                    items(words) { word ->
                        WordCardItem(word)
                    }
                }
            }
        }
    }
}

@Composable
fun LevelSelectorRow(selectedLevel: String, onLevelSelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(levels) { item ->
            val isSelected = item.level == selectedLevel

            // 选中的组件稍微变大一点点
            val scale by animateFloatAsState(if (isSelected) 1.05f else 0.95f, label = "scale")

            Column(
                modifier = Modifier
                    .width(75.dp)
                    .height(120.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryPink else UnselectedPink)
                    .clickable { onLevelSelected(item.level) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(item.level, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(item.title, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun WordCardItem(word: Vocabulary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号
            Text(
                text = word.id.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )

            // 模拟设计稿图标
            Icon(
                imageVector = word.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = PrimaryPink
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 单词与翻译
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.kanji, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "（${word.reading}）| ",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(word.translation, fontSize = 15.sp, color = Color.Gray)
            }

            // 语音图标
            IconButton(onClick = { /* TODO: 播放逻辑 */ }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PrimaryPink.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// 自定义一个弧形 Shape
// arcHeight 控制弧线隆起的高度，可以根据视觉效果微调
fun getArcShape(arcHeight: Float) = GenericShape { size, _ ->
    moveTo(0f, arcHeight) // 起点在左侧，往下偏移一个 arcHeight

    // 使用二次贝塞尔曲线：
    // x1, y1 是控制点（位于顶部中心 y=0 的位置）
    // x2, y2 是终点（位于右侧，往下偏移一个 arcHeight）
    quadraticBezierTo(
        x1 = size.width / 2f,
        y1 = -arcHeight,      // 控制点往上拉，形成隆起感
        x2 = size.width,
        y2 = arcHeight
    )

    // 闭合路径：连接到右下角、左下角，回到起点
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}