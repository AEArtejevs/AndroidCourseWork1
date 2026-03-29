package com.example.coursework.database.reminder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(reminderEntity: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminderEntity: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminderEntity: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id IN (:ids)")
    suspend fun deleteRemindersByIds(ids: List<Int>)

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersOnce(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Int): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE date < :today AND hasLocationAlert = 0 ORDER BY date DESC, time DESC")
    fun getPastReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE date = :today AND hasLocationAlert = 0 ORDER BY time ASC")
    fun getTodayReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE date > :today AND hasLocationAlert = 0 ORDER BY date ASC, time ASC")
    fun getScheduledReminders(today: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isImportant = 1 ORDER BY date ASC, time ASC")
    fun getImportantRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE hasAlert = 0 AND hasLocationAlert = 0 ORDER BY date ASC, time ASC")
    fun getNoAlertRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY date ASC, time ASC")
    fun getCompletedRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE hasLocationAlert = 1 ORDER BY id DESC")
    fun getPlaceRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE isImportant = 1")
    fun getImportantCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE hasAlert = 0 AND hasLocationAlert = 0")
    fun getNoAlertCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE date = :today AND hasLocationAlert = 0")
    fun getTodayCount(today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE date > :today AND hasLocationAlert = 0")
    fun getScheduledCount(today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE hasLocationAlert = 1")
    fun getPlaceCount(): Flow<Int>
}
