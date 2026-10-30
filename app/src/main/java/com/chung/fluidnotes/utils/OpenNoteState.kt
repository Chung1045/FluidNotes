package com.chung.fluidnotes.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object OpenNoteState {
    private val _openNoteId = MutableStateFlow<Int?>(null)
    val openNoteId: StateFlow<Int?> = _openNoteId.asStateFlow()
    @Volatile var wasInSplit = false

    private val _closeRequests = MutableSharedFlow<Set<Int>>(extraBufferCapacity = 1)
    val closeRequests: SharedFlow<Set<Int>> = _closeRequests.asSharedFlow()

    fun open(id: Int) { _openNoteId.value = id }

    /** Only clears if [id] is still the open note, so a replaced editor can't wipe the new one. */
    fun close(id: Int) {
        if (_openNoteId.value == id) _openNoteId.value = null
    }

    /** Ask the editor to close (without saving) if its note is one of [ids]. */
    fun requestClose(ids: Collection<Int>) {
        _closeRequests.tryEmit(ids.toSet())
    }
}