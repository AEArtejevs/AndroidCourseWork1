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

    @Query("DELETE FROM reminders WHERE id IN (:ids)")
    suspend fun deleteRemindersByIds(ids: List<Int>)

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Int): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE date < :today ORDER BY date DESC, time DESC")
    fun getPastReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE date = :today ORDER BY time ASC")
    fun getTodayReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE date > :today ORDER BY date ASC, time ASC")
    fun getScheduledReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isImportant = 1 ORDER BY date ASC, time ASC")
    fun getImportantRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE hasAlert = 0 ORDER BY date ASC, time ASC")
    fun getNoAlertRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY date ASC, time ASC")
    fun getCompletedRemindersFlow(): Flow<List<ReminderEntity>>

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
