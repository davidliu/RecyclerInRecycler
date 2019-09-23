package com.deviange.recyclerinrecycler

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = object : RecyclerView.Adapter<ListViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ListViewHolder {
                return ListViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.child,
                        parent,
                        false
                    )
                )
            }

            override fun getItemCount(): Int {
                return 100
            }

            override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
                holder.recyclerView.adapter = object :
                    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): RecyclerView.ViewHolder {
                        val view = LayoutInflater.from(parent.context)
                            .inflate(android.R.layout.simple_list_item_1, parent, false)
                        return EmptyViewHolder(view)
                    }

                    override fun getItemCount(): Int {
                        return 20
                    }

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        (holder.itemView as TextView).text = position.toString()
                    }

                }
            }

        }
    }
}

class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_view)

    init {
        recyclerView.background = ColorDrawable(0xFF000000.toInt() or Random.nextInt())
        recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        itemView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val factor = abs(min(0, top)).toFloat() / v.height
            val targetScale = 0.95f
            val newScale = 1 * (1 - factor) + (targetScale * factor)
            v.scaleX = newScale
            v.scaleY = newScale
        }
    }
}

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}