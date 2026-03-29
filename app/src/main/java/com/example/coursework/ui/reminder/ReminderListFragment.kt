package com.example.coursework.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.NoteDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {

    private val viewModel: ReminderListViewModel by viewModels()
    private val args: ReminderListFragmentArgs by navArgs()
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var deleteBar: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvCategoryTitle)
        tvTitle.text = args.category

        deleteBar = view.findViewById(R.id.deleteBar)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerReminders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        reminderAdapter = ReminderAdapter(
            items = emptyList(),
            onItemClick = { reminder ->
                val action = ReminderListFragmentDirections.actionReminderListFragmentToReminderDetailFragment(reminder.id)
                findNavController().navigate(action)
            },
            onSelectionModeChanged = { isSelectionMode ->
                deleteBar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            }
        )
        recyclerView.adapter = reminderAdapter

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            deleteSelectedReminders()
        }

        view.findViewById<MaterialButton>(R.id.btnCancelDelete).setOnClickListener {
            reminderAdapter.clearSelection()
        }

        lifecycleScope.launch {
            viewModel.getRemindersByCategory(args.category).collect { reminders ->
                reminderAdapter.updateData(reminders)
            }
        }
    }

    private fun deleteSelectedReminders() {
        val selectedIds = reminderAdapter.getSelectedIds()
        if (selectedIds.isNotEmpty()) {
            val db = NoteDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                db.reminderDao().deleteRemindersByIds(selectedIds)
                reminderAdapter.clearSelection()
            }
        }
    }
}
