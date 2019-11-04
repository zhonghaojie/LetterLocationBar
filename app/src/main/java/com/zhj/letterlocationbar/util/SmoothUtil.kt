package com.zhj.letterlocationbar.util
import android.support.v7.widget.RecyclerView

/**
 * Description:
 * Created by zhonghaojie on 2018/9/19.
 */
object SmoothUtil {
    fun smoothToSpecificPosition(recycele: RecyclerView, position: Int) {
        val toPosition = if (position == -1) 0 else position
        var shouldScroll = false
        val firstVisibleItem = recycele.getChildLayoutPosition(recycele.getChildAt(0))//可视范围内第一个item在整个list中的position
        val lastVisibleItem = recycele.getChildLayoutPosition(recycele.getChildAt(recycele.childCount - 1))//可视范围内最后一个item在整个list中的position

        when {
        //当指定位置在可视区域之前，直接滚动到指定位置即可
            toPosition < firstVisibleItem -> {
                recycele.smoothScrollToPosition(toPosition)
            }
        //当指定位置在可视区域之间，需要计算滚动距离，调用smoothScrollBy来进行置顶
            toPosition <= lastVisibleItem -> {
                //getChildAt的position，是可视范围内的view的位置，不是整个list中的位置
                //所以，要计算滚动距离，要把position换算成当前可视界面中的position
                val movePosition = toPosition - firstVisibleItem
                if (movePosition in 0..(recycele.childCount - 1)) {
                    val top = recycele.getChildAt(movePosition).top
                    recycele.smoothScrollBy(0, top)
                }
            }
        //当指定位置在可视区域之后，先滚动到指定区域，此时，该位置位于可视区域最底部，需要再滚动一下置顶
        //可以用滚动监听来实现，当他停止滚动，但是shouldScroll = true时，再调用一次smoothToPosition
            else -> {
                recycele.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (shouldScroll && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            shouldScroll = false
                            smoothToSpecificPosition(recycele,toPosition)
                        }
                    }
                })
                shouldScroll = true
                recycele.smoothScrollToPosition(toPosition)
            }
        }
    }
}