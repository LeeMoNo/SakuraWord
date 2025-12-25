package com.tusizi.sakuraword.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View


class CurvedBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt() // 白色
        style = Paint.Style.FILL
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        path.reset()

        // 从左上角开始
        path.moveTo(0f, h * 0.35f)

        // 绘制波浪曲线 - 使用quadTo创建平滑的贝塞尔曲线
        path.quadTo(
            w / 2,          // 控制点x：屏幕中间
            h * 0.15f,      // 控制点y：波浪的最高点
            w,              // 结束点x：屏幕右边
            h * 0.35f       // 结束点y：和起点同高
        )

        // 绘制右下角
        path.lineTo(w, h)

        // 绘制底部
        path.lineTo(0f, h)

        // 闭合路径回到起点
        path.close()

        // 绘制路径
        canvas.drawPath(path, paint)
    }
}