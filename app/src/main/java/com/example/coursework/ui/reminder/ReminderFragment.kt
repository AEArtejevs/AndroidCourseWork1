package com.example.coursework.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R

class ReminderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ReminderViewModel()

        val summaryRecycler = view.findViewById<RecyclerView>(R.id.recyclerSummary)
        val remindersRecycler = view.findViewById<RecyclerView>(R.id.recyclerReminders)

        summaryRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        remindersRecycler.layoutManager = LinearLayoutManager(requireContext())

        summaryRecycler.adapter = SummaryAdapter(viewModel.summaryList)
        remindersRecycler.adapter = ReminderAdapter(viewModel.reminderList)

        val summaryList = listOf(
            "Today" to 0,
            "Scheduled" to 2,
            "Important" to 1,
            "Place" to 0,
            "No alert" to 1,
            "Completed" to 0
        )

        val reminderList = listOf(
            "Inga" to "Thu, Oct 2, 2025",
            "Maris dzimšanas d" to "Tue, Dec 15, 8:00 AM",
            "Learn figma basics" to "No alert"
        )

    }
}