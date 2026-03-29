package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.coursework.database.NoteDatabase
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.Locale

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    private val today = getTodayString()

    val reminders = dao.getAllReminders()
    val pastReminders = dao.getPastReminders(today)

    val todayCount = dao.getTodayCount(today)
    val scheduledCount = dao.getScheduledCount(today)
    val important = dao.getImportantCount()
    val completed = dao.getCompletedCount()
    val noAlert = dao.getNoAlertCount()

    val summaryFlow = combine(
        todayCount,
        scheduledCount,
        important,
        noAlert,
        completed
    ) { todayVal, scheduled, importantVal, noAlertVal, completedVal ->

        listOf(
            "Today" to todayVal,
            "Scheduled" to scheduled,
            "Important" to importantVal,
            "Place" to 0,
            "No alert" to noAlertVal,
            "Completed" to completedVal
        )
    }

    private fun getTodayString(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        // YYYY-MM-DD
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
}
