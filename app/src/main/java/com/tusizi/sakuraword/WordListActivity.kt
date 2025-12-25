package com.tusizi.sakuraword

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class WordItem(
    val kanji: String,
    val hiragana: String,
    val meaning: String,
    val icon: String // emoji或图标标识
)

enum class JLPTLevel(val displayName: String, val subtitle: String) {
    N5("N5", "入門"),
    N4("N4", "基礎"),
    N3("N3", "中級"),
    N2("N2", "上級"),
    N1("N1", "超上級")
}

class WordListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordListAdapter
    private var currentLevel = JLPTLevel.N3

    // 模拟不同等级的单词数据
    private val wordDataMap = mapOf(
        JLPTLevel.N5 to listOf(
            WordItem("こんにちは", "こんにちは", "Hello", "👋"),
            WordItem("ありがとう", "ありがとう", "Thank you", "🙏"),
            WordItem("水", "みず", "Water", "💧"),
            WordItem("食べる", "たべる", "Eat", "🍽️"),
            WordItem("学校", "がっこう", "School", "🏫")
        ),
        JLPTLevel.N4 to listOf(
            WordItem("天気", "てんき", "Weather", "☀️"),
            WordItem("買い物", "かいもの", "Shopping", "🛍️"),
            WordItem("料理", "りょうり", "Cooking", "🍳"),
            WordItem("旅行", "りょこう", "Travel", "✈️"),
            WordItem("趣味", "しゅみ", "Hobby", "🎨")
        ),
        JLPTLevel.N3 to listOf(
            WordItem("花見", "はなみ", "Flower Viewing", "🌸"),
            WordItem("春", "はる", "Spring", "🌺"),
            WordItem("卒業", "そつぎょう", "Graduation", "🎓"),
            WordItem("約束", "やくそく", "Promise", "🤝"),
            WordItem("夢", "ゆめ", "Dream", "💭")
        ),
        JLPTLevel.N2 to listOf(
            WordItem("努力", "どりょく", "Effort", "💪"),
            WordItem("経験", "けいけん", "Experience", "📚"),
            WordItem("成功", "せいこう", "Success", "🏆"),
            WordItem("挑戦", "ちょうせん", "Challenge", "🎯"),
            WordItem("達成", "たっせい", "Achievement", "⭐")
        ),
        JLPTLevel.N1 to listOf(
            WordItem("洗練", "せんれん", "Refinement", "✨"),
            WordItem("曖昧", "あいまい", "Ambiguous", "❓"),
            WordItem("顕著", "けんちょ", "Remarkable", "🌟"),
            WordItem("妥当", "だとう", "Reasonable", "⚖️"),
            WordItem("概念", "がいねん", "Concept", "💡")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_word_list)

        setupRecyclerView()
        setupLevelTabs()
    }

    private fun setupLevelTabs() {
        val levels = listOf(
            Triple(R.id.levelN5, JLPTLevel.N5, R.id.levelN5Card),
            Triple(R.id.levelN4, JLPTLevel.N4, R.id.levelN4Card),
            Triple(R.id.levelN3, JLPTLevel.N3, R.id.levelN3Card),
            Triple(R.id.levelN2, JLPTLevel.N2, R.id.levelN2Card),
            Triple(R.id.levelN1, JLPTLevel.N1, R.id.levelN1Card)
        )

        levels.forEach { (viewId, level, cardId) ->
            findViewById<View>(cardId).setOnClickListener {
                selectLevel(level)
            }
        }

        selectLevel(currentLevel)
    }

    private fun selectLevel(level: JLPTLevel) {
        currentLevel = level

        // 更新所有Tab的状态
        val levels = listOf(
            Triple(R.id.levelN5Card, JLPTLevel.N5, R.color.level_n5),
            Triple(R.id.levelN4Card, JLPTLevel.N4, R.color.level_n4),
            Triple(R.id.levelN3Card, JLPTLevel.N3, R.color.level_n3),
            Triple(R.id.levelN2Card, JLPTLevel.N2, R.color.level_n2),
            Triple(R.id.levelN1Card, JLPTLevel.N1, R.color.level_n1)
        )

        levels.forEach { (cardId, tabLevel, colorRes) ->
            val card = findViewById<CardView>(cardId)
            val layoutParams = card.layoutParams as ViewGroup.MarginLayoutParams

            if (tabLevel == level) {
                // 选中状态：更高、更亮、完全不透明
                layoutParams.height = (180 * resources.displayMetrics.density).toInt()
                layoutParams.width = (110 * resources.displayMetrics.density).toInt()
                card.layoutParams = layoutParams
                card.cardElevation = 8f * resources.displayMetrics.density
                card.setCardBackgroundColor(getColor(colorRes))
                card.alpha = 1.0f
            } else {
                // 未选中状态：更矮、半透明
                layoutParams.height = (160 * resources.displayMetrics.density).toInt()
                layoutParams.width = (100 * resources.displayMetrics.density).toInt()
                card.layoutParams = layoutParams
                card.cardElevation = 4f * resources.displayMetrics.density
                card.setCardBackgroundColor(getColor(colorRes))
                card.alpha = 0.7f
            }
        }

        loadWords(level)
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.wordRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WordListAdapter()
        recyclerView.adapter = adapter
    }

    private fun loadWords(level: JLPTLevel) {
        val words = wordDataMap[level] ?: emptyList()
        adapter.updateWords(words)
    }

    inner class WordListAdapter : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

        private var words = listOf<WordItem>()

        fun updateWords(newWords: List<WordItem>) {
            words = newWords
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_word, parent, false)
            return WordViewHolder(view)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(words[position], position + 1)
        }

        override fun getItemCount() = words.size

        inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val numberText: TextView = itemView.findViewById(R.id.numberText)
            private val iconText: TextView = itemView.findViewById(R.id.iconText)
            private val kanjiText: TextView = itemView.findViewById(R.id.kanjiText)
            private val meaningText: TextView = itemView.findViewById(R.id.meaningText)
            private val soundButton: ImageView = itemView.findViewById(R.id.soundButton)

            fun bind(word: WordItem, number: Int) {
                numberText.text = number.toString()
                iconText.text = word.icon
                kanjiText.text = "${word.kanji}（${word.hiragana}）"
                meaningText.text = word.meaning

                soundButton.setOnClickListener {
                    // TODO: 播放发音
                }

                itemView.setOnClickListener {
                    // TODO: 进入学习界面
                    startActivity(Intent(this@WordListActivity, WordActivity::class.java))
                }
            }
        }
    }
}



