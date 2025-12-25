package com.tusizi.sakuraword

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.tusizi.sakuraword.R

data class Word(
    val kanji: String,
    val hiragana: String,
    val meaning: String,
    val reading: String,
    val example: String
)

class WordActivity : AppCompatActivity() {

    private lateinit var frontCard: CardView
    private lateinit var backCard: CardView
    private lateinit var progressText: TextView
    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnShowAnswer: View
    private lateinit var btnGoNext: View

    private var isFront = true
    private var currentIndex = 0

    private val wordList = listOf(
        Word("学校", "がっこう", "School", "gakkou", "「学校へ行きます。」"),
        Word("先生", "せんせい", "Teacher", "sensei", "「先生は優しいです。」"),
        Word("勉強", "べんきょう", "Study", "benkyou", "「毎日勉強します。」"),
        Word("友達", "ともだち", "Friend", "tomodachi", "「友達と遊びます。」"),
        Word("図書館", "としょかん", "Library", "toshokan", "「図書館で本を読みます。」")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateCard()
    }

    private fun initViews() {
        frontCard = findViewById(R.id.frontCard)
        backCard = findViewById(R.id.backCard)
        progressText = findViewById(R.id.progressText)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnShowAnswer = findViewById(R.id.btnShowAnswer)
        btnGoNext = findViewById(R.id.btnGoNext)

        // 初始化背面卡片为不可见
        backCard.visibility = View.INVISIBLE
        backCard.rotationY = 180f
    }

    private fun setupListeners() {
        frontCard.setOnClickListener { flipCard() }
        backCard.setOnClickListener { flipCard() }
        btnShowAnswer.setOnClickListener { flipCard() }
        btnGoNext.setOnClickListener { nextWord() }

        btnPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                resetCard()
            }
        }

        btnNext.setOnClickListener {
            nextWord()
        }

        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnRefresh).setOnClickListener {
            currentIndex = 0
            resetCard()
        }
    }

    private fun updateCard() {
        val word = wordList[currentIndex]
        progressText.text = "${currentIndex + 1}/${wordList.size}"

        findViewById<TextView>(R.id.kanjiText).text = word.kanji
        findViewById<TextView>(R.id.hiraganaText).text = word.hiragana

        findViewById<TextView>(R.id.meaningText).text = word.meaning
        findViewById<TextView>(R.id.readingText).text = word.reading
        findViewById<TextView>(R.id.exampleText).text = word.example
    }

    private fun flipCard() {
        val scale = resources.displayMetrics.density

        frontCard.cameraDistance = 8000 * scale
        backCard.cameraDistance = 8000 * scale

        if (isFront) {
            // 翻转到背面
            backCard.visibility = View.VISIBLE

            val flipOutFront = AnimatorInflater.loadAnimator(this, R.animator.card_flip_out) as AnimatorSet
            val flipInBack = AnimatorInflater.loadAnimator(this, R.animator.card_flip_in) as AnimatorSet

            flipOutFront.setTarget(frontCard)
            flipInBack.setTarget(backCard)

            flipOutFront.start()
            flipInBack.start()

            // 动画结束后隐藏正面
            flipOutFront.childAnimations[0].addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    frontCard.visibility = View.INVISIBLE
                }
            })

        } else {
            // 翻转到正面
            frontCard.visibility = View.VISIBLE

            val flipOutBack = AnimatorInflater.loadAnimator(this, R.animator.card_flip_out) as AnimatorSet
            val flipInFront = AnimatorInflater.loadAnimator(this, R.animator.card_flip_in) as AnimatorSet

            flipOutBack.setTarget(backCard)
            flipInFront.setTarget(frontCard)

            flipOutBack.start()
            flipInFront.start()

            // 动画结束后隐藏背面
            flipOutBack.childAnimations[0].addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    backCard.visibility = View.INVISIBLE
                }
            })
        }

        isFront = !isFront

        // 切换按钮显示
        btnShowAnswer.visibility = if (isFront) View.VISIBLE else View.GONE
        btnGoNext.visibility = if (isFront) View.GONE else View.VISIBLE
    }

    private fun nextWord() {
        if (currentIndex < wordList.size - 1) {
            currentIndex++
            resetCard()
        }
    }

    private fun resetCard() {
        // 重置到正面状态
        if (!isFront) {
            // 立即重置，不需要动画
            frontCard.visibility = View.VISIBLE
            backCard.visibility = View.INVISIBLE
            frontCard.rotationY = 0f
            backCard.rotationY = 180f
            isFront = true

            btnShowAnswer.visibility = View.VISIBLE
            btnGoNext.visibility = View.GONE
        }
        updateCard()
    }
}