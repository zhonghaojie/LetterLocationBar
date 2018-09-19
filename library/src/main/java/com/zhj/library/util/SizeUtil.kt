package com.zhj.library.util

import android.content.Context
import android.util.TypedValue

/**
 * Description:
 * Created by zhonghaojie on 2018/9/18.
 */
object SizeUtil {

    /**
     * dp = px/scale
     */
    fun px2Dp(context: Context, pxValue: Int):Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dp2px(context: Context, dpValue: Int):Int{
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(),displayMetrics).toInt()
    }

    fun sp2px(context: Context, dpSize: Int):Int{
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_SP, dpSize.toFloat(),displayMetrics).toInt()
    }
}