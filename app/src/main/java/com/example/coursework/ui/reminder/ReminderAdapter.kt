package com.example.coursework.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.reminder.ReminderEntity

class ReminderAdapter(
    private var items: List<ReminderEntity>,
    private val onItemClick: (ReminderEntity) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title

        val displayDate = try {
            val parts = item.date.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1].toInt()}/${parts[0]}"
            } else {
                item.date
            }
        } catch (e: Exception) {
            item.date
        }
        
        holder.tvDate.text = "$displayDate ${item.time}"
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReminderEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
