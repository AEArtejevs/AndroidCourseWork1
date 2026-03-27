package com.example.coursework.database.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(reminderEntity: ReminderEntity)

    @Update
    suspend fun updateReminder(reminderEntity: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminderEntity: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isImportant = 1")
    suspend fun getImportantReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE hasAlert = 0")
    suspend fun getNoAlertReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1")
    suspend fun getCompletedReminders(): List<ReminderEntity>


    @Query("SELECT COUNT(*) FROM reminders")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE isImportant = 1")
    fun getImportantCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE hasAlert = 0")
    fun getNoAlertCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE date = :today")
    fun getTodayCount(today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE date > :today")
    fun getScheduledCount(today: String): Flow<Int>


}
