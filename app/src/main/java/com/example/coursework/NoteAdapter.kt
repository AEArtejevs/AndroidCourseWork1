package com.example.coursework

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.database.Note

class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // Keeps track of selected notes
    private val selectedNotes = mutableSetOf<Note>()

    inner class NoteViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        ) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val content: TextView = itemView.findViewById(R.id.textContent)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(note: Note) {
            title.text = note.title
            content.text = note.content

            // Update checkbox state
            checkBox.isChecked = selectedNotes.contains(note)

            // Toggle selection
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedNotes.add(note)
                else selectedNotes.remove(note)
            }

            // Regular click for note details or whatever
            itemView.setOnClickListener { onNoteClick(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteViewHolder(parent)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) =
        holder.bind(notes[position])

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<Note> = selectedNotes.toList()
}
