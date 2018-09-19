package com.zhj.library.observer

/**
 * Description:
 * Created by zhonghaojie on 2018/9/19.
 */
interface SelectObserver {
    /**
     * 更新指示器上的字母
     */
    fun updateLetter(letter:String)

    /**
     * 选中某个字母
     */
    fun upSelect(selectedLetter:String)
}