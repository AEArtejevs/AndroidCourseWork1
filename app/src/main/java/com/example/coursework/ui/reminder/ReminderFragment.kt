package com.example.coursework.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


class ReminderFragment : Fragment() {
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var bottomBar: LinearLayout
    private lateinit var deleteBar: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomBar = view.findViewById(R.id.bottomBar)
        deleteBar = view.findViewById(R.id.deleteBar)

        val summaryRecycler = view.findViewById<RecyclerView>(R.id.recyclerSummary)
        val summaryAdapter = SummaryAdapter(emptyList()) { category ->
            val action = ReminderFragmentDirections.actionNavRemindersToReminderListFragment(category)
            findNavController().navigate(action)
        }
        summaryRecycler.adapter = summaryAdapter

        val remindersRecycler = view.findViewById<RecyclerView>(R.id.recyclerReminders)

        reminderAdapter = ReminderAdapter(
            items = emptyList(),
            onItemClick = { reminder ->
                val action = ReminderFragmentDirections.actionNavRemindersToReminderDetailFragment(reminder.id)
                findNavController().navigate(action)
            },
            onSelectionModeChanged = { isSelectionMode ->
                if (isSelectionMode) {
                    deleteBar.visibility = View.VISIBLE
                    bottomBar.visibility = View.GONE
                } else {
                    deleteBar.visibility = View.GONE
                    bottomBar.visibility = View.VISIBLE
                }
            }
        )
        remindersRecycler.adapter = reminderAdapter

        summaryRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        remindersRecycler.layoutManager = LinearLayoutManager(requireContext())

        val btnAdd = view.findViewById<ImageButton>(R.id.btnAddReminder)
        btnAdd.setOnClickListener {
            showAddReminderDialog()
        }

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            deleteSelectedReminders()
        }

        view.findViewById<MaterialButton>(R.id.btnCancelDelete).setOnClickListener {
            reminderAdapter.clearSelection()
        }

        lifecycleScope.launch {
            viewModel.pastReminders.collect { reminders ->
                reminderAdapter.updateData(reminders)
            }
        }

        lifecycleScope.launch {
            viewModel.summaryFlow.collect { summary ->
                summaryAdapter.updateData(summary)
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

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val btnDate = dialogView.findViewById<MaterialButton>(R.id.tvDate)
        val btnTime = dialogView.findViewById<MaterialButton>(R.id.tvTime)
        val toggleImportant = dialogView.findViewById<ToggleButton>(R.id.toggleImportant)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        val calendar = Calendar.getInstance()
        var technicalDate: String? = null
        var technicalTime: String? = null

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), { _, y, m, d ->
                    technicalDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    btnDate.text = "$d/${m + 1}/$y"
                    btnDate.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTime.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, h, min ->
                    technicalTime = String.format(Locale.US, "%02d:%02d", h, min)
                    btnTime.text = technicalTime
                    btnTime.error = null
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            var isValid = true

            if (title.isBlank()) {
                etTitle.error = "Required"
                isValid = false
            }

            if (technicalDate == null) {
                btnDate.error = "Required"
                isValid = false
            }

            if (technicalTime == null) {
                btnTime.error = "Required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            saveReminder(title, technicalDate!!, technicalTime!!, toggleImportant.isChecked)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveReminder(title: String, date: String, time: String, isImportant: Boolean) {
        val db = NoteDatabase.getDatabase(requireContext())
        val dao = db.reminderDao()

        lifecycleScope.launch {
            dao.insertReminder(
                ReminderEntity(
                    title = title,
                    date = date,
                    time = time,
                    isImportant = isImportant
                )
            )
        }
    }
}
