package com.example.coursework.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trashNotes")
data class TrashNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val deletedAt: Long = System.currentTimeMillis()
)
