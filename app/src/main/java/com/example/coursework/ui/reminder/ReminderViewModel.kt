package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.coursework.database.NoteDatabase
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    val reminders = dao.getAllReminders()

    private val today = getTodayString()

    val todayCount = dao.getTodayCount(today)
    val scheduledCount = dao.getScheduledCount(today)
    val important = dao.getImportantCount()
    val completed = dao.getCompletedCount()
    val noAlert = dao.getNoAlertCount()
    // Summary data (top grid)

    val summaryFlow = combine(
        todayCount,
        scheduledCount,
        important,
        noAlert,
        completed
    ) { today, scheduled, important, noAlert, completed ->

        listOf(
            "Today" to today,
            "Scheduled" to scheduled,
            "Important" to important,
            "Place" to 0,
            "No alert" to noAlert,
            "Completed" to completed
        )
    }
    private fun getTodayString(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        return "$day/$month/$year"
    }


}