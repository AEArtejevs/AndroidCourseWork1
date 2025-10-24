package com.example.coursework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.database.Note

class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit,
    private val onFavoriteClick: (Note) -> Unit

) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    var showFavorites = false
    var showCheckBoxes = false
    private val selectedNotes = mutableSetOf<Note>()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val content: TextView = itemView.findViewById(R.id.textContent)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val buttonFavorite: View = itemView.findViewById(R.id.favorite)
        private val favorite: ImageButton = itemView.findViewById(R.id.favorite)

        fun bind(note: Note) {
            title.text = note.title
            content.text = note.content
            checkBox.isChecked = selectedNotes.contains(note)

            buttonFavorite.visibility = if (showFavorites) View.VISIBLE else View.GONE
            checkBox.visibility = if (showCheckBoxes) View.VISIBLE else View.GONE

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedNotes.add(note)
                else selectedNotes.remove(note)
            }

            favorite.setOnClickListener {
                onFavoriteClick(note)
            }
            favorite.setImageResource(
                if (note.isFavorite) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }
    }

    fun toggleFavoritesVisibility() {
        showFavorites = !showFavorites
        showCheckBoxes = !showCheckBoxes
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<Note> = selectedNotes.toList()
}
