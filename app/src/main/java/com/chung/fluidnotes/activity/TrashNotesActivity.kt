package com.chung.fluidnotes.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteViewModel
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.OpenNoteState
import com.chung.fluidnotes.views.TrashedNotesView

class TrashedNotesActivity : ComponentActivity() {

    /** Single exit path: close the right-panel editor (if any), then finish Trash. */
    private fun closeTrash() {
        OpenNoteState.openNoteId.value?.let { id ->
            OpenNoteState.requestClose(listOf(id))
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // This window has no text input. Opt out of input-method focus so tapping it
        // (e.g. the FAB) doesn't hide the keyboard of the editor in the other pane.
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        // System back button / gesture
        onBackPressedDispatcher.addCallback(this) { closeTrash() }

        val dao = (application as FluidNotesApp).database.notesDao()

        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteViewModel(dao) as T
                }
            }
        )[NoteViewModel::class.java]

        setContent {
            AppTheme {
                val notes by remember { dao.getAllTrashedNotes() }
                    .collectAsState(initial = null)

                TrashedNotesView(
                    notes = notes,
                    onDeleteNotes = { ids ->
                        OpenNoteState.requestClose(ids)
                        viewModel.deleteNotes(ids)
                    },
                    onRestoreNotes = { ids ->
                        OpenNoteState.requestClose(ids)
                        viewModel.restoreNotes(ids)
                    },
                    onClose = { closeTrash() }
                )
            }
        }
    }
}