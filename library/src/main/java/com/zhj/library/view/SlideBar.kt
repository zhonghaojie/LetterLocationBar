package com.zhj.library.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.zhj.library.R
import com.zhj.library.observer.LetterSelectBarObservable
import com.zhj.library.observer.SelectObserver
import com.zhj.library.util.SizeUtil
import java.util.*

/**
 * Description:
 * 我的理解为：手指抬起来的时候，表示选中了某个字母，点下去或者滑动的时候，只改变指示器上显示的字母
 * Created by zhonghaojie on 2018/9/18.
 */
class SlideBar : View, LetterSelectBarObservable {
    override fun upSelect(selectedLetter:String) {
        observers.forEach { it.upSelect(selectedLetter) }
    }

    private val observers: ArrayList<SelectObserver> = ArrayList()
    override fun registObserver(observer: SelectObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    override fun unRegistObserver(observer: SelectObserver) {
        if (observers.contains(observer)) {
            observers.remove(observer)
        }
    }

    override fun notify(selectedLetter: String) {
        observers.forEach {
            it.updateLetter(selectedLetter)
        }
    }

    private var strList = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private var paint: Paint = Paint(ANTI_ALIAS_FLAG)
    private var notSelectedColor = Color.parseColor("#c1c1c1")
    private var selectedColor = Color.parseColor("#F66220")
    private val circlePaint = Paint(ANTI_ALIAS_FLAG)
    private val topRadius = 10

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SlideBar)
            selectedColor = typedArray.getColor(R.styleable.SlideBar_selectedColor, Color.parseColor("#F66220"))
            typedArray.recycle()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var textSize = SizeUtil.dp2px(context, 10).toFloat()
        paint.textSize = textSize
        val cornerSize = SizeUtil.dp2px(context, topRadius)//圆角背景的radius
        val space = (measuredHeight - cornerSize * 2) / 26//每个字母所在矩形的高度，要先减去上下两个圆角的高度

        canvas?.let {
            circlePaint.color = selectedColor
            circlePaint.style = Paint.Style.FILL
            //画最上面那个橙色的点
            it.drawCircle((measuredWidth / 2).toFloat(), (cornerSize).toFloat(), (cornerSize / 2).toFloat(), circlePaint)
            strList.forEachIndexed { index, s ->
                //选中状态
                if (currentIndex == index) {
                    paint.color = selectedColor
                } else {
                    paint.color = notSelectedColor
                }
                val array = FloatArray(1)
                paint.getTextWidths(s, array)
                val letterWidth = array[0]//字的宽度
                //居中显示字母
                val vertical = space / 2 //垂直居中
                val horization = measuredWidth / 2 - letterWidth / 2//水平居中
                val verticalOffset = cornerSize * 2//竖直偏移，从顶部半圆一下开始画
                it.drawText(s, horization, verticalOffset + vertical + (space * index).toFloat(), paint)
            }
        }
    }

    private var currentY: Float = 10f
    private var currentIndex = -1
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentY = it.y
                    val selected = calculate(it.y)
                    if(!selected.isNullOrEmpty()){
                        notify(selected)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    currentY = it.y
                    val selected = calculate(it.y)
                    upSelect(selected)
                }
                MotionEvent.ACTION_MOVE -> {
                    currentY = it.y
                    val selected = calculate(it.y)
                    if(!selected.isNullOrEmpty()){
                        notify(selected)
                    }
                }
                else -> {
                }
            }
        }
        return true
    }

    /**
     * 清除选中状态
     */
    fun clear() {
        currentY = 0f
        currentIndex = -1
        invalidate()
    }

    /**
     * 计算手指位置对应的字母
     */
    private fun calculate(y: Float): String {
        val cornerSize = SizeUtil.dp2px(context, topRadius)//圆角背景的radius
        val space = (measuredHeight - cornerSize * 2) / 26
        var result = ""
        for (i in 0 until strList.size) {
            val startY = cornerSize + space * i
            val endY = cornerSize + space * (i + 1)
            if (y >= startY && y < endY) {
                result = strList[i]
                if (currentIndex != i) {
                    invalidate()
                }
                currentIndex = i
                break
            }
        }
        Log.i("SlideBar", result)
        return result
    }
}