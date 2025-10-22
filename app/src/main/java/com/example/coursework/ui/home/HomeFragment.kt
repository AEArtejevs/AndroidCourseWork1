package com.example.coursework.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coursework.NoteAdapter
import com.example.coursework.database.NoteDatabase
import com.example.coursework.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        val adapter = NoteAdapter(emptyList()) { note ->
            Toast.makeText(requireContext(), "Clicked: ${note.title}", Toast.LENGTH_SHORT).show()
        }
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
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    selectedNotes.forEach { note ->
                        db.noteDao().deleteById(note.id)
                    }
                }
                Toast.makeText(requireContext(), "Deleted ${selectedNotes.size} notes", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
