package com.zhj.library.view.circleselecterbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.zhj.library.R
import com.zhj.library.observer.LetterSelectBarObservable
import com.zhj.library.observer.SelectObserver
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Description:
 * Created by zhonghaojie on 2018/9/18.
 */
interface CircleSelectorObserver {
    fun upSelect(percent: String)
}

interface CircleSelectorObservable {
    fun registerObserver(observer: CircleSelectorObserver)
    fun unRegisterObserver(observer: CircleSelectorObserver)
}

class CircleSelecterBar : View, CircleSelectorObservable {
    override fun registerObserver(observer: CircleSelectorObserver) {
        observers.add(observer)
    }

    override fun unRegisterObserver(observer: CircleSelectorObserver) {
        observers.remove(observer)
    }

    private var observers = ArrayList<CircleSelectorObserver>()
    private var circleSize = 20
    private var paint: Paint = Paint(ANTI_ALIAS_FLAG)
    private var bigCirclePaint: Paint = Paint(ANTI_ALIAS_FLAG)
    private var bigCircleStroke = 5
    private var bigCircleStrokeColor = Color.WHITE
    private var bigCircleSize = 60
    private var circleColor = Color.GRAY
    private var bigCircleColor = Color.parseColor("#44000000")
    private var circlePadding = 20

    private var circleCount = -1
    private var selectTop = -1f
    private var selectBottom = -1f
    private var lastCircleCenterY = -1f
    private var currentY: Float = 10f
        get() {
            return when {
                field <= (bigCircleSize / 2f) -> bigCircleSize / 2f
                field >= lastCircleCenterY -> lastCircleCenterY
                else -> field
            }
        }
    var drawBigCircleWithTouchMove = true//画大圆是否根据手指来
    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CircleSelecterBar)
            circleSize = typedArray.getDimensionPixelSize(R.styleable.CircleSelecterBar_circle_size, 20)
            bigCircleSize = typedArray.getDimensionPixelSize(R.styleable.CircleSelecterBar_big_circle_size, 60)
            circleColor = typedArray.getColor(R.styleable.CircleSelecterBar_circle_color, Color.GRAY)
            bigCircleStroke = typedArray.getDimensionPixelSize(R.styleable.CircleSelecterBar_big_circle_stroke_width, Color.GRAY)
            circlePadding = typedArray.getDimensionPixelSize(R.styleable.CircleSelecterBar_circle_padding, 20)
            bigCircleStrokeColor = typedArray.getColor(R.styleable.CircleSelecterBar_big_circle_stroke_color, Color.GRAY)
            bigCircleColor = typedArray.getColor(R.styleable.CircleSelecterBar_big_circle_color, Color.GRAY)
            paint.color = circleColor
            paint.style = Paint.Style.FILL
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = paddingStart + paddingEnd + bigCircleSize + 10
        val specSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = getDefaultSize(specSize, heightMeasureSpec)
        circleCount = (height - circlePadding) / (circlePadding + circleSize)
        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas?) {

        canvas?.let {

            bigCirclePaint.style = Paint.Style.FILL
            bigCirclePaint.color = bigCircleColor
            it.drawCircle(measuredWidth / 2f, currentY, bigCircleSize / 2f - bigCircleStroke.toFloat() / 2f, bigCirclePaint)
            bigCirclePaint.style = Paint.Style.STROKE
            bigCirclePaint.color = bigCircleStrokeColor
            bigCirclePaint.strokeWidth = bigCircleStroke.toFloat()
            it.drawCircle(measuredWidth / 2f, currentY, bigCircleSize / 2f - bigCircleStroke.toFloat() / 2f, bigCirclePaint)
            val selectedPositionList = ArrayList<Int>()
            var centerY = bigCircleSize / 2f + bigCircleStroke
            (0 until circleCount).forEachIndexed { index, s ->
                //选中状态
                val radius = if (selectTop <= (centerY - circleSize / 2f) && selectBottom >= (centerY + circleSize / 2f)) {
                    selectedPositionList.add(index)
                    circleSize / 1.2f
                } else {
                    circleSize / 2f
                }
                val nextSpace = Math.max(circlePadding + circleSize.toFloat(), bigCircleSize / 2f)

                if (centerY + nextSpace < (measuredHeight)) {
                    lastCircleCenterY = centerY
                    it.drawCircle(measuredWidth / 2f, centerY.toFloat(), radius, paint)
                    centerY += circlePadding + circleSize
                }
            }
            onSelect(selectedPositionList)
        }

    }

    private var currentAction = -1
    private fun onSelect(selectedPositionList : ArrayList<Int>) {
        if(!drawBigCircleWithTouchMove){
            return
        }
        //由于大圈可能会包含多个小圆，所以这里加了判断
        if(selectedPositionList.isEmpty()){
            return
        }
        if(selectedPositionList.size == 1){//size  1没什么好说的
            observers.forEach { it.upSelect(remain2(selectedPositionList[0].toFloat() / (circleCount - 1).toFloat())) }
        }else {
            if(selectedPositionList[selectedPositionList.size-1] == circleCount-1){
                //如果待选列表中包含最后一个小圆，则说明到底了
                observers.forEach { it.upSelect(remain2(selectedPositionList[selectedPositionList.size-1].toFloat() / (circleCount - 1).toFloat())) }
            }else{
                //其他情况就取第一个小圆的index
                observers.forEach { it.upSelect(remain2(selectedPositionList[0].toFloat() / (circleCount - 1).toFloat())) }
            }
        }

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        drawBigCircleWithTouchMove = true

        event?.let {
            currentAction = it.action
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    calculate(it)
                }
                MotionEvent.ACTION_UP -> {
                    calculate(it)
                }
                MotionEvent.ACTION_MOVE -> {
                    calculate(it)
                }
                else -> {
                }
            }
        }
        return true
    }

    /**
     * 计算手指滑动状态下大圆位置
     */
    private fun calculate(event: MotionEvent) {
        if (drawBigCircleWithTouchMove) {
            currentY = event.y
            selectTop = currentY - bigCircleSize / 2f
            selectBottom = currentY + bigCircleSize / 2f
            invalidate()
        }
    }


    /**
     * 计算RecyclerView滚动状态下大圆位置
     */
    private fun changeFromOutside(topVisiblePosition: Int, lastVisiblePosition: Int, totalSize: Int) {
        drawBigCircleWithTouchMove = false
        if (totalSize == 0) {
            return
        }
        if (topVisiblePosition == 0) {
            currentY = 0f
            selectTop = 0f
            selectBottom = selectTop + bigCircleSize
            invalidate()
            return
        }
        if (lastVisiblePosition == totalSize - 1) {
            currentY = lastCircleCenterY
            selectTop = currentY - bigCircleSize / 2f
            selectBottom = currentY + bigCircleSize / 2f
            invalidate()
            return
        }
        val percent = topVisiblePosition.toFloat() / totalSize.toFloat()
        currentY = measuredHeight * percent
        selectTop = currentY - bigCircleSize / 2f
        selectBottom = currentY + bigCircleSize / 2f
        invalidate()
    }


    private fun remain2(num: Float): String {
        val format = DecimalFormat("0.00")
        format.roundingMode = RoundingMode.HALF_UP
        return format.format(num)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bindRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setOnTouchListener { v, event ->
            drawBigCircleWithTouchMove = false
            drawBigCircleWithTouchMove
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!drawBigCircleWithTouchMove) {
                    val position = when {
                        recyclerView?.layoutManager is LinearLayoutManager -> (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        recyclerView?.layoutManager is GridLayoutManager -> (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                        recyclerView?.layoutManager is StaggeredGridLayoutManager -> {
                            val array = emptyArray<Int>().toIntArray()
                            (recyclerView.layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(array)
                            if(array.isNotEmpty()){
                                array[0]
                            }else{
                                0
                            }
                        }
                        else -> 0
                    }
                    val lastVisiblePosition = when {
                        recyclerView?.layoutManager is LinearLayoutManager -> (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                        recyclerView?.layoutManager is GridLayoutManager -> (recyclerView.layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                        recyclerView?.layoutManager is StaggeredGridLayoutManager -> {
                            val array = emptyArray<Int>().toIntArray()
                            (recyclerView.layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(array)
                            if(array.isNotEmpty()){
                                array[0]
                            }else{
                                0
                            }
                        }
                        else -> recyclerView?.adapter?.itemCount ?: 0
                    }
                    changeFromOutside(position, lastVisiblePosition, recyclerView?.adapter?.itemCount
                            ?: 0)
                }

            }
        })
    }

    /**
     * 清除选中状态
     */
    fun clear() {
        currentY = -1f
        invalidate()
    }


}