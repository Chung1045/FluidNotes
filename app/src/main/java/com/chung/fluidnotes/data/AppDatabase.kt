package com.chung.fluidnotes.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities = [NoteItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDAO
}