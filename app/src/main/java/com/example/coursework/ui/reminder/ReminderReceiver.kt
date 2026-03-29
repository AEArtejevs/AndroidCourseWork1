package com.example.coursework.ui.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.coursework.MainActivity
import com.example.coursework.R
import com.example.coursework.database.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // If the system sends a BOOT_COMPLETED broadcast, we just reschedule alarms
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllReminders(context)
            return
        }

        // For actual reminder alarms, we expect a valid ID and title
        // Default ID is -1 to distinguish from a generic/empty intent
        val title = intent.getStringExtra("title")
        val reminderId = intent.getIntExtra("id", -1)

        // SAFETY CHECK: Only show notification if title is NOT null and ID is NOT -1 or 0
        // (0 is sometimes passed as a default by the system or stale intents)
        if (!title.isNullOrBlank() && reminderId > 0) {
            showNotification(context, title, reminderId)
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        val scheduler = ReminderScheduler(context)
        val db = NoteDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = db.reminderDao().getAllRemindersOnce()
            reminders.forEach { reminder ->
                // Reschedule only future, non-completed reminders
                if (!reminder.isCompleted && reminder.hasAlert && !reminder.hasLocationAlert) {
                    scheduler.schedule(reminder)
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, reminderId: Int) {
        val channelId = "reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(reminderId, builder.build())
    }
}
