package com.example.coursework.database.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val date: String,
    val time: String,

    val isImportant: Boolean = false,
    val hasAlert: Boolean = true,
    val isCompleted: Boolean = false,

    // --- Location API Modification ---
    val hasLocationAlert: Boolean = false,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float? = null
)
