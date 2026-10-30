package com.chung.fluidnotes.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteViewModel
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.OpenNoteState
import com.chung.fluidnotes.views.HomeScreenView
import com.chung.fluidnotes.views.HomeScreenViewPreview
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        // Get DAO from Application
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
                // Track initial loading state
                var isLoading by remember { mutableStateOf(true) }

                val notes by remember { dao.getAllNotes() }
                    .collectAsState(initial = emptyList())

                // Keep the skeleton loader visible for a smooth 600ms window on startup
                LaunchedEffect(Unit) {
                    delay(600.milliseconds)
                    isLoading = false
                }

                HomeScreenView(
                    notes = notes,
                    isLoading = isLoading,
                    onTrashNotes = { ids ->
                        OpenNoteState.requestClose(ids)
                        viewModel.trashNotes(ids)
                    }
                )
            }
        }
    }
}

// Optional preview inside MainActivity
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    AppTheme {
        HomeScreenViewPreview()
    }
}