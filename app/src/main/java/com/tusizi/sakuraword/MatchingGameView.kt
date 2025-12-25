package com.example.sakuragame.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tusizi.sakuraword.data.Words
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.random.Random

// 花瓣数据类
data class Petal(
    val id: String,
    val wordId: String,
    val content: String,
    val type: PetalType,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val matched: Boolean,
    val shaking: Boolean
)

enum class PetalType {
    QUESTION, ANSWER
}

// 自定义花瓣形状
class PetalShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height

            // 创建有机的花瓣形状（类似椭圆但不规则）
            moveTo(width * 0.5f, 0f)

            // 使用三次贝塞尔曲线创建花瓣形状
            cubicTo(
                width * 0.8f, height * 0.1f,
                width * 0.95f, height * 0.4f,
                width * 0.9f, height * 0.7f
            )

            cubicTo(
                width * 0.85f, height * 0.9f,
                width * 0.65f, height,
                width * 0.5f, height
            )

            cubicTo(
                width * 0.35f, height,
                width * 0.15f, height * 0.9f,
                width * 0.1f, height * 0.7f
            )

            cubicTo(
                width * 0.05f, height * 0.4f,
                width * 0.2f, height * 0.1f,
                width * 0.5f, 0f
            )

            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun MatchingGameView(
    words: List<Words>,
    onBack: () -> Unit
) {
    var petals by remember { mutableStateOf<List<Petal>>(emptyList()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }
    var moveCount by remember { mutableStateOf(0) }

    // 初始化游戏
    LaunchedEffect(Unit) {
        petals = createNewGame(words)
    }

    // 处理花瓣点击
    val handlePetalClick: (String) -> Unit = { id ->
        if (!completed) {
            val clickedPetal = petals.find { it.id == id }
            if (clickedPetal != null && !clickedPetal.matched) {
                when {
                    // 点击同一个花瓣 - 取消选择
                    selectedId == id -> {
                        selectedId = null
                    }
                    // 没有选中的花瓣 - 选择当前花瓣
                    selectedId == null -> {
                        selectedId = id
                    }
                    // 尝试配对
                    else -> {
                        val selectedPetal = petals.find { it.id == selectedId }
                        if (selectedPetal != null) {
                            moveCount++

                            if (selectedPetal.wordId == clickedPetal.wordId) {
                                // 配对成功！
                                petals = petals.map { petal ->
                                    if (petal.id == id || petal.id == selectedId) {
                                        petal.copy(matched = true)
                                    } else {
                                        petal
                                    }
                                }
                                selectedId = null

                                // 检查是否全部完成
                                val remaining = petals.count { !it.matched && it.id != id && it.id != selectedId }
                                if (remaining == 0) {
                                    kotlinx.coroutines.GlobalScope.launch {
                                        delay(1000)
                                        completed = true
                                    }
                                }
                            } else {
                                // 配对失败 - 抖动
                                petals = petals.map { petal ->
                                    if (petal.id == id || petal.id == selectedId) {
                                        petal.copy(shaking = true)
                                    } else {
                                        petal
                                    }
                                }

                                kotlinx.coroutines.GlobalScope.launch {
                                    delay(500)
                                    petals = petals.map { it.copy(shaking = false) }
                                    selectedId = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 重新开始游戏
    val startNewGame: () -> Unit = {
        petals = createNewGame(words)
        selectedId = null
        completed = false
        moveCount = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF1F2))
    ) {
        // 背景粒子效果
        BackgroundParticles()

        // 顶部栏
        TopBar(
            onBack = onBack,
            onRefresh = startNewGame
        )

        // 游戏区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            petals.forEach { petal ->
                PetalItem(
                    petal = petal,
                    isSelected = selectedId == petal.id,
                    onClick = { handlePetalClick(petal.id) }
                )
            }
        }

        // 胜利弹窗
        if (completed) {
            VictoryOverlay(
                moveCount = moveCount,
                onPlayAgain = startNewGame,
                onBack = onBack
            )
        }
    }
}

@Composable
fun BackgroundParticles() {
    // 创建固定的背景粒子
    val particles = remember {
        List(8) { index ->
            Triple(
                Random.nextInt(100).dp,
                Random.nextInt(120).dp * 10,
                (24 + Random.nextInt(24)).dp
            )
        }
    }

    particles.forEachIndexed { index, (x, y, size) ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle$index")

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (3000 + Random.nextInt(2000)),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier
                .offset(x = x, y = y)
                .size(size)
                .alpha(alpha),
            tint = Color(0xFFFDA4AF)
        )
    }
}

@Composable
fun TopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(2.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = Color(0xFF64748b)
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(2.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFFFB7185)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "単語合わせ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFB7185),
                letterSpacing = 2.sp
            )
            Text(
                text = "ペアを見つけよう",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748b)
            )
        }
    }
}

@Composable
fun PetalItem(
    petal: Petal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 根据内容长度计算花瓣大小
    val contentLength = petal.content.length
    val baseSize = when {
        contentLength <= 6 -> 110.dp
        contentLength <= 10 -> 130.dp
        contentLength <= 15 -> 150.dp
        else -> 170.dp
    }

    val fontSize = when {
        contentLength <= 6 -> 16.sp
        contentLength <= 10 -> 14.sp
        contentLength <= 15 -> 12.sp
        else -> 11.sp
    }

    // 匹配动画
    val scale by animateFloatAsState(
        targetValue = when {
            petal.matched -> 1.5f
            isSelected -> 1.08f
            else -> 1f
        },
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (petal.matched) 0f else 1f,
        animationSpec = tween(500),
        label = "alpha"
    )

    // 抖动动画
    val shakeOffset by animateFloatAsState(
        targetValue = if (petal.shaking) 5f else 0f,
        animationSpec = if (petal.shaking) {
            repeatable(
                iterations = 4,
                animation = tween(80),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(0)
        },
        label = "shake"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (petal.x / 100f * 300).dp + shakeOffset.dp,
                y = (petal.y / 100f * 550).dp
            )
            .size(baseSize)
            .scale(scale)
            .alpha(alpha)
            .rotate(petal.rotation)
            .clickable(enabled = !petal.matched) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 花瓣形状背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isSelected) 12.dp else 4.dp,
                    shape = PetalShape(),
                    clip = false
                )
                .background(
                    color = when {
                        isSelected -> Color(0xFFFB7185)
                        petal.type == PetalType.QUESTION -> Color.White
                        else -> Color(0xFFFCE7F3)
                    },
                    shape = PetalShape()
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) Color(0xFFFB7185) else Color(0xFFFDA4AF),
                    shape = PetalShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = petal.content,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF1e293b),
                textAlign = TextAlign.Center,
                lineHeight = (fontSize.value + 2).sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun VictoryOverlay(
    moveCount: Int,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 弹跳的奖杯图标
                val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                val bounce by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounce"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(y = bounce.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFfbbf24)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "お見事！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1e293b)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "すべての桜を集めました。",
                    fontSize = 14.sp,
                    color = Color(0xFF64748b)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFB7185)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "もう一度遊ぶ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFf1f5f9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "戻る",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF64748b)
                    )
                }
            }
        }
    }
}

// 辅助函数：创建新游戏
private fun createNewGame(words: List<Words>): List<Petal> {
    // 随机选择6个单词（如果单词不足则选择全部）
    val gameWords = words.shuffled().take(min(6, words.size))

    // 创建花瓣
    val newPetals = mutableListOf<Petal>()
    gameWords.forEach { word ->
        // 问题花瓣（汉字/假名）
        newPetals.add(
            Petal(
                id = "${word.id}-q",
                wordId = word.id,
                content = word.kanji.ifEmpty { word.kana },
                type = PetalType.QUESTION,
                x = 0f,
                y = 0f,
                rotation = 0f,
                matched = false,
                shaking = false
            )
        )
        // 答案花瓣（意思）
        newPetals.add(
            Petal(
                id = "${word.id}-a",
                wordId = word.id,
                content = word.meaning,
                type = PetalType.ANSWER,
                x = 0f,
                y = 0f,
                rotation = 0f,
                matched = false,
                shaking = false
            )
        )
    }

    // 使用网格布局分配位置并添加随机偏移
    val cols = 3
    val shuffledPetals = newPetals.shuffled()

    return shuffledPetals.mapIndexed { index, petal ->
        val col = index % cols
        val row = index / cols

        // 基础网格位置（百分比）
        val baseX = (col * 33f) + 16f
        val baseY = (row * 20f) + 10f

        // 添加随机抖动
        val jitterX = (Random.nextFloat() * 10f) - 5f
        val jitterY = (Random.nextFloat() * 8f) - 4f

        petal.copy(
            x = baseX + jitterX,
            y = baseY + jitterY,
            rotation = (Random.nextFloat() * 40f) - 20f
        )
    }
}

