package com.example.coursework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.database.TrashNote

class TrashNoteAdapter(
    private var trashNotes: List<TrashNote>,
    private val onNoteClick: (TrashNote) -> Unit,
    private val onNoteLongClick: (TrashNote) -> Unit
) : RecyclerView.Adapter<TrashNoteAdapter.TrashNoteViewHolder>() {

    var showCheckBoxes = false
    private val selectedNotes = mutableSetOf<TrashNote>()

    inner class TrashNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val content: TextView = itemView.findViewById(R.id.textContent)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(note: TrashNote) {
            title.text = note.title
            content.text = note.content
            checkBox.isChecked = selectedNotes.contains(note)
            checkBox.visibility = if (showCheckBoxes) View.VISIBLE else View.GONE


            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedNotes.add(note)
                else selectedNotes.remove(note)
            }

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }
    }

    fun toggleCheckBoxesVisibility() {
        showCheckBoxes = !showCheckBoxes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return TrashNoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrashNoteViewHolder, position: Int) {
        holder.bind(trashNotes[position])
    }

    override fun getItemCount() = trashNotes.size

    fun updateNotes(newNotes: List<TrashNote>) {
        trashNotes = newNotes
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<TrashNote> = selectedNotes.toList()
}
