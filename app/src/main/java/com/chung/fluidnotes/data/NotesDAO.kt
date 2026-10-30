package com.chung.fluidnotes.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDAO {
    // Room automatically updates the UI if the data changes because of Flow
    @Query("SELECT * FROM fluidnotes WHERE  isTrashed = 0 ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM fluidnotes WHERE isTrashed = 1 ORDER BY timestamp DESC")
    fun getAllTrashedNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("DELETE FROM fluidnotes WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<Int>)

    @Query("SELECT * FROM fluidnotes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteItem?

    @Query("UPDATE fluidnotes SET isTrashed = 1 WHERE id IN (:ids)")
    suspend fun setNoteTrashed(ids: List<Int>)

    @Query("UPDATE fluidnotes SET isTrashed = 0 WHERE id IN (:ids)")
    suspend fun setNoteUnTrashed(ids: List<Int>)
}