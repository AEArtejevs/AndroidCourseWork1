package com.example.coursework.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NoteDatabase.getDatabase(application).reminderDao()

    private val _reminder = MutableStateFlow<ReminderEntity?>(null)
    val reminder: StateFlow<ReminderEntity?> = _reminder.asStateFlow()

    fun loadReminder(id: Int) {
        viewModelScope.launch {
            dao.getReminderById(id).collect {
                _reminder.value = it
            }
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            dao.updateReminder(reminder)
        }
    }
}
