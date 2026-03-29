package com.example.coursework.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {

    private val viewModel: ReminderListViewModel by viewModels()
    private val args: ReminderListFragmentArgs by navArgs()

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

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerReminders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        val adapter = ReminderAdapter(emptyList()) { reminder ->
            val action = ReminderListFragmentDirections.actionReminderListFragmentToReminderDetailFragment(reminder.id)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.getRemindersByCategory(args.category).collect { reminders ->
                adapter.updateData(reminders)
            }
        }
    }
}
