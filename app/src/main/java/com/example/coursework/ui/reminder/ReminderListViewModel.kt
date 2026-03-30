package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Calendar
import java.util.Locale

class ReminderListViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>> {
        val today = getTodayString()
        return when (category) {
            "Today" -> dao.getTodayReminders(today)
            "Scheduled" -> dao.getScheduledReminders(today)
            "Important" -> dao.getImportantRemindersFlow()
            "No alert" -> dao.getNoAlertRemindersFlow()
            "Place" -> dao.getPlaceRemindersFlow()
            else -> emptyFlow()
        }
    }

    private fun getTodayString(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
}
