package com.chung.fluidnotes.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDAO {
    // Room automatically updates the UI if the data changes because of Flow
    @Query("SELECT * FROM fluidnotes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("SELECT * FROM fluidnotes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteItem?
}