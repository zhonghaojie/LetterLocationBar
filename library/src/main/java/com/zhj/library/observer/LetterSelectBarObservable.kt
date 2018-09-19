package com.zhj.library.observer

/**
 * Description:
 * Created by zhonghaojie on 2018/9/19.
 */
interface LetterSelectBarObservable {
    fun registObserver(observer: SelectObserver)
    fun unRegistObserver(observer: SelectObserver)
    fun notify(selectedLetter:String)
    fun upSelect(selectedLetter:String)
}