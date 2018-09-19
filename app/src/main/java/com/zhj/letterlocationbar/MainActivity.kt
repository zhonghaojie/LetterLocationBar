package com.zhj.letterlocationbar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import com.github.promeg.pinyinhelper.Pinyin
import com.zhj.library.observer.SelectObserver
import com.zhj.library.util.SmoothUtil.smoothToSpecificPosition
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var strList = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private val handler: Handler = Handler()
    private val runnable = Runnable {

        val animate=AlphaAnimation(1f,0f)
        animate.duration=200
        animate.interpolator=LinearInterpolator()
        tv_selected_letter.startAnimation(animate)
        tv_selected_letter.visibility=View.GONE
        slide_bar.clear()
    }
    val observer: SelectObserver = object : SelectObserver {
        override fun upSelect(letter:String) {
            val position = adapter.letterList.indexOf(letter)
            smoothToSpecificPosition(recycler_view,position)
            handler.postDelayed(runnable, 2000)
        }

        override fun updateLetter(letter: String) {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slide_bar.registObserver(observer)
        adapter = MyAdapter(getData())
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
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
                    Toast.makeText(tv.context,letter,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}