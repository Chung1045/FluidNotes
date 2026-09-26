package com.chung.fluidnotes.data

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class NoteViewModel(
    private val notesDao: NotesDAO,
    private val noteId: Int? = null
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow(TextFieldValue(""))
    val content: StateFlow<TextFieldValue> = _content.asStateFlow()

    private val _lastEdit = MutableStateFlow(System.currentTimeMillis())
    val lastEdit: StateFlow<Long> = _lastEdit.asStateFlow()

    private var currentNote: NoteItem? = null
    private var debounceTimestampJob: Job? = null

    init {
        if (noteId != null && noteId != 0) {
            viewModelScope.launch {
                val notes = notesDao.getAllNotes().firstOrNull()
                val note = notes?.find { it.id == noteId }

                note?.let {
                    currentNote = it
                    _title.value = it.title
                    _content.value = TextFieldValue(it.content)
                    _lastEdit.value = it.lastEdit // 1. Load initial timestamp into state
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        triggerDebouncedTimestampUpdate() // 2. Trigger debounce on title change
    }

    fun onContentChange(newContent: TextFieldValue) {
        _content.value = newContent
        triggerDebouncedTimestampUpdate() // 3. Trigger debounce on content change
    }

    private fun triggerDebouncedTimestampUpdate() {
        debounceTimestampJob?.cancel()
        debounceTimestampJob = viewModelScope.launch {
            delay(500.milliseconds) // Wait 0.5s after typing stops
            _lastEdit.value = System.currentTimeMillis() // 4. Updates UI immediately via StateFlow
        }
    }

    fun saveNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            debounceTimestampJob?.cancel()

            val titleText = _title.value.trim()
            val contentText = _content.value.text.trim()

            if (titleText.isEmpty() && contentText.isEmpty()) {
                currentNote?.let { notesDao.deleteNote(it) }
                onComplete()
                return@launch
            }

            // 5. Save the latest edited timestamp to the database
            val finalLastEdit = _lastEdit.value
            val noteToSave = NoteItem(
                id = currentNote?.id ?: 0,
                title = titleText,
                content = contentText,
                timestamp = currentNote?.timestamp ?: finalLastEdit,
                lastEdit = finalLastEdit,
                isTrashed = currentNote?.isTrashed ?: false
            )

            notesDao.insertNote(noteToSave)
            onComplete()
        }
    }

    fun deleteNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            currentNote?.let {
                notesDao.deleteNote(it)
            }
            onComplete()
        }
    }
}