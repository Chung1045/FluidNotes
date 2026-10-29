package com.chung.fluidnotes.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteViewModel
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.views.EditViewScreen

class NoteEditViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        val isExistingNote = intent.getBooleanExtra("is_existing_note", true)
        val noteId = intent.getIntExtra("note_id", 0)
        val dao = (application as FluidNotesApp).database.notesDao()

        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteViewModel(dao, noteId) as T
                }
            }
        )[NoteViewModel::class.java]

        setContent {
            AppTheme {
                val isDark = isSystemInDarkTheme()
                LaunchedEffect(isDark) {}

                val isTrashed by viewModel.isTrashed.collectAsState()
                val noteTitle by viewModel.title.collectAsState()
                val noteContent by viewModel.content.collectAsState()
                val noteLastEdit by viewModel.lastEdit.collectAsState()

                var showMenu by remember { mutableStateOf(false) }

                EditViewScreen(
                    isExistingNote = isExistingNote,
                    isTrashedView = isTrashed,
                    noteTitle = noteTitle,
                    noteContent = noteContent,
                    noteLastEdit = noteLastEdit,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                    onBackPressed = {
                        if (isTrashed) finish()
                        else viewModel.saveNote { finish() }
                    },
                    onTrashConfirmed = { viewModel.trashNote { finish() } },
                    onDeleteConfirmed = {viewModel.deleteNote { finish() } },
                    onRecoverClick = { viewModel.restoreNote { finish() } },
                    onShowMenuChange = { showMenu = it },
                    showMenu = showMenu
                )
            }
        }
    }
}