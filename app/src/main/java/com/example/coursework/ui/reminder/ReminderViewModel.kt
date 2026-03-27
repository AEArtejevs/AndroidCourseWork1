package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.coursework.database.NoteDatabase

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    val reminders = dao.getAllReminders()

    // Summary data (top grid)
    val summaryList = listOf(
        "Today" to 0,
        "Scheduled" to 2,
        "Important" to 1,
        "Place" to 0,
        "No alert" to 1,
        "Completed" to 0
    )

}