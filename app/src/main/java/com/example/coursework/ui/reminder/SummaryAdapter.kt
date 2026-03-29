package com.example.coursework.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R

class SummaryAdapter(
    private var items: List<Pair<String, Int>>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvCount: TextView = view.findViewById(R.id.tvCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, count) = items[position]
        holder.tvTitle.text = title
        holder.tvCount.text = count.toString()
        holder.itemView.setOnClickListener { onItemClick(title) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newData: List<Pair<String, Int>>) {
        items = newData
        notifyDataSetChanged()
    }
}