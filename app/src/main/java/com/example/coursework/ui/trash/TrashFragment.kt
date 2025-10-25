package com.example.coursework.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coursework.TrashNoteAdapter
import com.example.coursework.database.NoteDatabase
import com.example.coursework.databinding.FragmentTrashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TrashNoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        val view = binding.root

        val db = NoteDatabase.getDatabase(requireContext())
        val recyclerView = binding.recyclerViewNotes
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Reuse NoteAdapter for simplicity - just show read-only trash items
        adapter = TrashNoteAdapter(
            emptyList(),
            onNoteClick = { note ->
                Toast.makeText(requireContext(), "Trash: ${note.title}", Toast.LENGTH_SHORT).show()
            },
            onNoteLongClick = {
                adapter.toggleCheckBoxesVisibility()
                if (adapter.showCheckBoxes) {
                    binding.fabDelete.show()
                } else {
                    binding.fabDelete.hide()
                }
            },
        )
        recyclerView.adapter = adapter

        // Observe trash notes and display them using Note model copy
        lifecycleScope.launch(Dispatchers.IO) {
            db.trashNoteDao().getAll().collectLatest { trashNotes ->
                withContext(Dispatchers.Main) {
                    adapter.updateNotes(trashNotes)
                }
            }
        }

        binding.fabDelete.setOnClickListener {
            val selectedNotes = adapter.getSelectedNotes()
            if (selectedNotes.isEmpty()) {
                Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                selectedNotes.forEach { note ->
                    db.trashNoteDao().delete(note.id)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Permanently deleted ${selectedNotes.size} notes",
                        Toast.LENGTH_SHORT
                    ).show()
//                    adapter.toggleFavoritesVisibility()
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
