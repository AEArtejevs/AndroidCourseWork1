package com.example.coursework.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coursework.database.note.Note
import com.example.coursework.database.note.NoteDao
import com.example.coursework.database.reminder.ReminderDao
import com.example.coursework.database.reminder.ReminderEntity
import com.example.coursework.database.trash.TrashNote
import com.example.coursework.database.trash.TrashNoteDao

@Database(entities = [Note::class, TrashNote::class, ReminderEntity::class], version = 6, exportSchema = false)

abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun trashNoteDao(): TrashNoteDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                NoteDatabase::class.java,
                                "note_database"
                            ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }

    }
}