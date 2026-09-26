package com.chung.fluidnotes.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "fluidnotes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lastEdit: Long = System.currentTimeMillis(),
    val isTrashed: Boolean
)