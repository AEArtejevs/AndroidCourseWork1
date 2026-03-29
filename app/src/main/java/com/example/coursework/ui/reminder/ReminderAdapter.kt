package com.example.coursework.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.reminder.ReminderEntity

class ReminderAdapter(
    private var items: List<ReminderEntity>,
    private val onItemClick: (ReminderEntity) -> Unit,
    private val onSelectionModeChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Int>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
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

        // Selection UI
        holder.checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = selectedItems.contains(item.id)

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(item.id)
            } else {
                onItemClick(item)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                onSelectionModeChanged(true)
                toggleSelection(item.id)
                notifyDataSetChanged()
            }
            true
        }

        holder.checkBox.setOnClickListener {
            toggleSelection(item.id)
        }
    }

    private fun toggleSelection(reminderId: Int) {
        if (selectedItems.contains(reminderId)) {
            selectedItems.remove(reminderId)
        } else {
            selectedItems.add(reminderId)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReminderEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<Int> = selectedItems.toList()

    fun clearSelection() {
        isSelectionMode = false
        selectedItems.clear()
        onSelectionModeChanged(false)
        notifyDataSetChanged()
    }
}
