package com.example.coursework.ui.reminder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import kotlinx.coroutines.launch
import java.util.Calendar


class ReminderFragment : Fragment() {
    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val summaryRecycler = view.findViewById<RecyclerView>(R.id.recyclerSummary)
//        summaryRecycler.adapter = SummaryAdapter(viewModel.summaryList)
        val summaryAdapter = SummaryAdapter(emptyList())
        summaryRecycler.adapter = summaryAdapter

        val remindersRecycler = view.findViewById<RecyclerView>(R.id.recyclerReminders)

        summaryRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        remindersRecycler.layoutManager = LinearLayoutManager(requireContext())

        val btnAdd = view.findViewById<Button>(R.id.btnAddReminder)
        btnAdd.setOnClickListener {
            showAddReminderDialog()
        }

        lifecycleScope.launch {
            viewModel.reminders.collect { reminders ->

                val list = reminders.map {
                    it.title to "${it.date} ${it.time}"
                }

                remindersRecycler.adapter = ReminderAdapter(list)
            }
        }
        lifecycleScope.launch {
            viewModel.summaryFlow.collect { summary ->
                summaryAdapter.updateData(summary)
            }
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)
        val tvTime = dialogView.findViewById<TextView>(R.id.tvTime)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val calendar = Calendar.getInstance()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()


        tvDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), { _, y, m, d ->
                    calendar.set(y, m, d)
                    tvDate.text = "$d/${m + 1}/$y"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tvTime.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, h, min ->
                    calendar.set(Calendar.HOUR_OF_DAY, h)
                    calendar.set(Calendar.MINUTE, min)
                    tvTime.text = "$h:${min.toString().padStart(2, '0')}"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()

            if (title.isBlank()) {
                etTitle.error = "Required"
                return@setOnClickListener
            }

            saveReminder(title, tvDate.text.toString(), tvTime.text.toString())
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveReminder(title: String, date: String, time: String) {
        val db = NoteDatabase.getDatabase(requireContext())
        val dao = db.reminderDao()

        lifecycleScope.launch {
            dao.insertReminder(
                ReminderEntity(
                    title = title,
                    date = date,
                    time = time
                )
            )
        }
    }


}