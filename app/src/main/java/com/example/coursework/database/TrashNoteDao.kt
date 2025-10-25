package com.example.coursework.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao // data access object
interface TrashNoteDao {
    @Insert
    suspend fun insert(note: TrashNote)

    @Query("SELECT * FROM trashNotes")
    fun getAll(): Flow<List<TrashNote>>

    @Query("DELETE FROM trashNotes WHERE id = :id")
    suspend fun delete(id: Int)
}
