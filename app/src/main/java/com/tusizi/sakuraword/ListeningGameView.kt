package com.tusizi.sakuraword

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tusizi.sakuraword.data.Words
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs


data class FallingWord(
    val id: String,
    val word: Words,
    val x: Float,
    var y: Float,
    val speed: Float,
    val isTarget: Boolean,
    val state: FallingState
)

enum class FallingState {
    FALLING, CLICKED_CORRECT, CLICKED_WRONG, MISSED
}

enum class GameState {
    INTRO, PLAYING, GAME_OVER
}

// 辨音
@Composable
fun ListeningGameView(
    words: List<Words>,
    onBack: () -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.INTRO) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var fallingItems by remember { mutableStateOf<List<FallingWord>>(emptyList()) }
    var currentRoundTarget by remember { mutableStateOf<Words?>(null) }
    var roundActive by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // 初始化TTS
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.JAPANESE
                tts?.setSpeechRate(0.9f)
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // 游戏循环
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            while (isActive && gameState == GameState.PLAYING) {
                delay(16L) // ~60fps

                fallingItems = fallingItems.map { item ->
                    if (item.state == FallingState.FALLING) {
                        val newY = item.y + item.speed

                        if (newY > 110f) {
                            if (item.isTarget) {
                                lives--
                                if (lives <= 0) {
                                    gameState = GameState.GAME_OVER
                                } else {
                                    roundActive = false
                                    kotlinx.coroutines.GlobalScope.launch {
                                        delay(1500)
                                        startNewRound(
                                            words, lives, score,
                                            onTargetSet = { currentRoundTarget = it },
                                            onItemsSet = { fallingItems = it },
                                            onRoundActiveSet = { roundActive = it },
                                            tts = tts
                                        )
                                    }
                                }
                                item.copy(state = FallingState.MISSED)
                            } else {
                                item.copy(state = FallingState.MISSED)
                            }
                        } else {
                            item.copy(y = newY)
                        }
                    } else {
                        item
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF312e81),
                        Color(0xFF0f172a)
                    )
                )
            )
    ) {
        // 背景氛围效果
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4f46e5),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 1.2f)
                    )
                )
        )

        when (gameState) {
            GameState.INTRO -> IntroScreen(
                onStart = {
                    gameState = GameState.PLAYING
                    score = 0
                    lives = 3
                    fallingItems = emptyList()
                    roundActive = false
                    startNewRound(
                        words, 3, score,
                        onTargetSet = { currentRoundTarget = it },
                        onItemsSet = { fallingItems = it },
                        onRoundActiveSet = { roundActive = it },
                        tts = tts
                    )
                },
                onBack = onBack
            )

            GameState.GAME_OVER -> GameOverScreen(
                score = score,
                onRestart = {
                    gameState = GameState.PLAYING
                    score = 0
                    lives = 3
                    fallingItems = emptyList()
                    roundActive = false
                    startNewRound(
                        words, 3, score,
                        onTargetSet = { currentRoundTarget = it },
                        onItemsSet = { fallingItems = it },
                        onRoundActiveSet = { roundActive = it },
                        tts = tts
                    )
                },
                onBack = onBack
            )

            GameState.PLAYING -> {
                GameScreen(
                    score = score,
                    lives = lives,
                    fallingItems = fallingItems,
                    onBack = onBack,
                    onItemClick = { id ->
                        if (roundActive) {
                            val item = fallingItems.find { it.id == id }
                            if (item?.state == FallingState.FALLING) {
                                if (item.isTarget) {
                                    score += 10
                                    roundActive = false
                                    fallingItems = fallingItems.map {
                                        if (it.id == id) it.copy(state = FallingState.CLICKED_CORRECT)
                                        else it
                                    }
                                    kotlinx.coroutines.GlobalScope.launch {
                                        delay(1000)
                                        startNewRound(
                                            words, lives, score,
                                            onTargetSet = { currentRoundTarget = it },
                                            onItemsSet = { fallingItems = it },
                                            onRoundActiveSet = { roundActive = it },
                                            tts = tts
                                        )
                                    }
                                } else {
                                    lives--
                                    if (lives <= 0) {
                                        gameState = GameState.GAME_OVER
                                        roundActive = false
                                    }
                                    fallingItems = fallingItems.map {
                                        if (it.id == id) it.copy(state = FallingState.CLICKED_WRONG)
                                        else it
                                    }
                                }
                            }
                        }
                    },
                    onReplayAudio = {
                        currentRoundTarget?.let {
                            tts?.speak(it.kana, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IntroScreen(onStart: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // 脉动图标
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFDA4AF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "落樱辨音",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "聞こえた単語を選んでください。\nListen and catch the falling sakura petals!",
                fontSize = 14.sp,
                color = Color(0xFFcbd5e1),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB7185)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("スタート", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBack) {
                Text("戻る", color = Color(0xFF94a3b8))
            }
        }
    }
}

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFfbbf24)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Game Over",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Score: $score",
                fontSize = 20.sp,
                color = Color(0xFFcbd5e1)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB7185)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("もう一度", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF475569)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("やめる", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GameScreen(
    score: Int,
    lives: Int,
    fallingItems: List<FallingWord>,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    onReplayAudio: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 顶部HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFB7185),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = lives.toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = score.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // 游戏区域 - 下落的物品
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            fallingItems.forEach { item ->
                FallingWordItem(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }

        // 底部控制按钮
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = onReplayAudio,
                containerColor = Color(0xFFFB7185),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Replay audio",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "音声を再生する",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun FallingWordItem(item: FallingWord, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = when (item.state) {
            FallingState.CLICKED_CORRECT -> 1.5f
            else -> 1f
        },
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = when (item.state) {
            FallingState.CLICKED_CORRECT, FallingState.MISSED -> 0f
            else -> 1f
        },
        label = "alpha"
    )

    // 抖动动画
    val shakeOffset by animateFloatAsState(
        targetValue = if (item.state == FallingState.CLICKED_WRONG) 10f else 0f,
        animationSpec = if (item.state == FallingState.CLICKED_WRONG) {
            repeatable(
                iterations = 3,
                animation = tween(100),
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
                x = (item.x / 100f * 360).dp + shakeOffset.dp,
                y = (item.y / 100f * 700).dp
            )
            .scale(scale)
            .alpha(alpha)
            .size(80.dp)
            .clickable(enabled = item.state == FallingState.FALLING) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 花瓣形状
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush =
                        if (item.state == FallingState.CLICKED_WRONG) {
                            SolidColor(Color(0xFFcbd5e1))
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFfbcfe8),
                                    Color(0xFFf9a8d4)
                                )
                            )
                        }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.word.kanji,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1e293b)
            )

            // 正确点击的波纹效果
            if (item.state == FallingState.CLICKED_CORRECT) {
                val infiniteTransition = rememberInfiniteTransition(label = "ping")
                val pingScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ping"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pingScale)
                        .alpha(0.75f)
                        .clip(CircleShape)
                        .background(Color(0xFFFDA4AF))
                )
            }
        }

        // 底部文字（隐藏假名）
        Text(
            text = "???",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// 辅助函数：开始新一轮
private fun startNewRound(
    words: List<Words>,
    lives: Int,
    score: Int,
    onTargetSet: (Words) -> Unit,
    onItemsSet: (List<FallingWord>) -> Unit,
    onRoundActiveSet: (Boolean) -> Unit,
    tts: TextToSpeech?
) {
    if (lives <= 0) return

    // 选择目标单词
    val targetWord = words.random()
    onTargetSet(targetWord)

    // 选择干扰词
    val distractors = words.filter { it.id != targetWord.id }.shuffled().take(2)

    // 创建下落物品
    val itemsToSpawn = (listOf(targetWord) + distractors).shuffled()
    val newItems = itemsToSpawn.mapIndexed { idx, word ->
        FallingWord(
            id = UUID.randomUUID().toString(),
            word = word,
            x = 15f + (idx * 35f) + ((-5..5).random().toFloat()),
            y = -20f - (0..20).random().toFloat(),
            speed = 0.15f + (0..5).random() / 100f + (score * 0.005f),
            isTarget = word.id == targetWord.id,
            state = FallingState.FALLING
        )
    }

    onItemsSet(newItems)
    onRoundActiveSet(true)

    // 延迟播放音频
    kotlinx.coroutines.GlobalScope.launch {
        delay(500)
        tts?.speak(targetWord.kana, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}