package com.example.coursework.ui.reminder

import androidx.lifecycle.ViewModel

class ReminderViewModel : ViewModel() {

    // Summary data (top grid)
    val summaryList = listOf(
        "Today" to 0,
        "Scheduled" to 2,
        "Important" to 1,
        "Place" to 0,
        "No alert" to 1,
        "Completed" to 0
    )

    // Reminder list (main list)
    val reminderList = listOf(
        "Inga" to "Thu, Oct 2, 2025",
        "Maris dzimšanas d" to "Tue, Dec 15, 8:00 AM",
        "Learn figma basics" to "No alert"
    )
}