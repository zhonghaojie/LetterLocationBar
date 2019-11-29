package com.zhj.letterlocationbar

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import com.github.promeg.pinyinhelper.Pinyin
import com.zhj.library.observer.SelectObserver
import com.zhj.letterlocationbar.util.SmoothUtil
import com.zhj.library.view.circleselecterbar.CircleSelectorObserver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    private var strList = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private val handler: Handler = Handler()
    private val runnable = Runnable {

        val animate = AlphaAnimation(1f, 0f)
        animate.duration = 200
        animate.interpolator = LinearInterpolator()
        tv_selected_letter.startAnimation(animate)
        tv_selected_letter.visibility = View.GONE
        slide_bar.clear()
    }
    val observer: SelectObserver = object : SelectObserver {
        override fun upSelect(percent: String) {
            recycler_view.stopScroll()
            SmoothUtil.smoothToSpecificPosition(recycler_view,  ((percent.toFloat() * (adapter.itemCount)).toInt()))
        }

        override fun updateLetter(letter: String) {
            val position = adapter.letterList.indexOf(letter)
            recycler_view.smoothScrollToPosition(position)
            tv_selected_letter.text = letter
            tv_selected_letter.alpha = 1f
            tv_selected_letter.visibility = View.VISIBLE
            handler.removeCallbacks(runnable)
        }

    }


    private fun getData(): List<String> {

        val data = ArrayList<String>()
        for (str in strList) {
            (1..10).mapTo(data) { str + it }
        }
        return data
    }

    private lateinit var adapter: MyAdapter
    private var isScrollFromTouch=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slide_bar.registObserver(observer)
        adapter = MyAdapter(getData())
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        select_bar.bindRecyclerView(recycler_view)
        select_bar.registerObserver(object : CircleSelectorObserver {
            override fun upSelect(percent: String) {
                isScrollFromTouch = false
                Log.i("circle_selector", percent)
                recycler_view.smoothScrollToPosition(((percent.toFloat() * adapter.list.size).toInt()))
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        slide_bar.unRegistObserver(observer)
    }


    class MyAdapter(val list: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        val letterList: List<String> = list.map { Pinyin.toPinyin(it[0].toString(), "")[0].toString().toUpperCase() }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            return ViewHolder(view)
        }

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private val tv: TextView = view.findViewById(R.id.tv_item)

            fun bind(letter: String) {
                tv.text = letter
                view.setOnClickListener {
                    Toast.makeText(tv.context, letter, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
