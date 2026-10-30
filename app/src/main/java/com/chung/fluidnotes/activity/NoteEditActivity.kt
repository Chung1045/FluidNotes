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
import androidx.lifecycle.lifecycleScope
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteViewModel
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.OpenNoteState
import com.chung.fluidnotes.views.EditViewScreen
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NoteEditViewActivity : ComponentActivity() {

    private var noteId = 0
    private lateinit var viewModel: NoteViewModel
    private var skipSaveOnStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        val isExistingNote = intent.getBooleanExtra("is_existing_note", true)
        val noteId = intent.getIntExtra("note_id", 0)

        OpenNoteState.open(noteId)   // 0 = new note, still tracked so Trash can close it
        this.noteId = noteId

        lifecycleScope.launch {
            OpenNoteState.closeRequests.collect { ids ->
                if (noteId in ids) {
                    skipSaveOnStop = true
                    finish()
                }
            }
        }

        val dao = (application as FluidNotesApp).database.notesDao()

        viewModel = ViewModelProvider(
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

                // Detect if the view is in split view
                val activity = this@NoteEditViewActivity
                val isInSplit by remember {
                    SplitController.getInstance(activity)
                        .splitInfoList(activity)
                        .map { splits -> splits.any { it.contains(activity) } }
                }.collectAsState(
                    initial = ActivityEmbeddingController.getInstance(activity).isActivityEmbedded(activity)
                )

                EditViewScreen(
                    isExistingNote = isExistingNote,
                    isTrashedView = isTrashed,
                    isInSplit = isInSplit,
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

    override fun onStop() {
        super.onStop()
        if (skipSaveOnStop || !::viewModel.isInitialized) return
        if (!viewModel.isTrashed.value) viewModel.saveNote { }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) OpenNoteState.close(noteId)
    }
}