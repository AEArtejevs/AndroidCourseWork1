package com.example.coursework.ui.favorite

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
import com.example.coursework.databinding.FragmentFavoriteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val view: View = binding.root
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
                Toast.makeText(requireContext(), "Long pressed: ${note.title}", Toast.LENGTH_SHORT).show()
                adapter.toggleFavoritesVisibility()
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
            db.noteDao().getAllFavorites().collect { notes ->
                adapter.updateNotes(notes)
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}