package com.example.coursework.database.reminder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(reminderEntity: ReminderEntity)

    @Update
    suspend fun updateReminder(reminderEntity: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminderEntity: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isImportant = 1")
    suspend fun getImportantReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE hasAlert = 0")
    suspend fun getNoAlertReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1")
    suspend fun getCompletedReminders(): List<ReminderEntity>
}