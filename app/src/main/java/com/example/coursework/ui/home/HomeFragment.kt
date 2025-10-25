package com.example.coursework.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coursework.AddNoteActivity
import com.example.coursework.NoteAdapter
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.TrashNote
import com.example.coursework.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        val recyclerView = binding.recyclerViewNotes

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val db = NoteDatabase.getDatabase(requireContext())

        adapter = NoteAdapter(
            emptyList(),
            onNoteClick = { note ->
                Toast.makeText(requireContext(), "Clicked: ${note.title}", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), AddNoteActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            },
            onNoteLongClick = { note ->
                adapter.toggleFavoritesVisibility()
                adapter.toggleCheckBoxesVisibility()
                if (adapter.showCheckBoxes) {
                    binding.fabDelete.show()
                } else {
                    binding.fabDelete.hide()
                }
                Toast.makeText(requireContext(), "Long pressed: ${note.title}", Toast.LENGTH_SHORT).show()

            },
            onFavoriteClick = { note ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val newFavState = !note.isFavorite
                    db.noteDao().setFavorite(note.id, newFavState)
                }
                Toast.makeText(requireContext(),
                    "Toggled favorite for: ${note.title}", Toast.LENGTH_SHORT
                ).show()
            }
        )

        recyclerView.adapter = adapter

        // Observe notes
        lifecycleScope.launch {
            db.noteDao().getAllNotes().collect { notes ->
                adapter.updateNotes(notes)
            }
        }

        // Handle delete button click
        binding.fabDelete.setOnClickListener {
            val selectedNotes = adapter.getSelectedNotes()
            if (selectedNotes.isEmpty()) {
                Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                selectedNotes.forEach { note ->
                    // Copy to trash first
                    db.trashNoteDao().insert(
                        TrashNote(
                            title = note.title,
                            content = note.content,
                            timestamp = note.timestamp,
                            isFavorite = note.isFavorite
                        )
                    )
                    // Then remove from main table
                    db.noteDao().deleteById(note.id)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Moved ${selectedNotes.size} notes to Trash",
                        Toast.LENGTH_SHORT
                    ).show()
                    adapter.toggleFavoritesVisibility() // hide checkboxes again
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
