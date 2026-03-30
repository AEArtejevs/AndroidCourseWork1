package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.Locale

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    private val today = getTodayString()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // For the "Past" section (unfiltered when not searching)
    val pastReminders = dao.getPastReminders(today)

    // Global search across all reminders
    val searchResults = combine(dao.getAllReminders(), _searchQuery) { reminders, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            reminders.filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    val todayCount = dao.getTodayCount(today)
    val scheduledCount = dao.getScheduledCount(today)
    val important = dao.getImportantCount()
    val noAlert = dao.getNoAlertCount()
    val placeCount = dao.getPlaceCount()

    // Using explicitly defined results from flow combine
    val summaryFlow = combine(
        todayCount,
        scheduledCount,
        important,
        noAlert,
        placeCount
    ) { results: Array<Int> ->
        listOf(
            "Today" to results[0],
            "Scheduled" to results[1],
            "Important" to results[2],
            "Place" to results[4],
            "No alert" to results[3]
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
