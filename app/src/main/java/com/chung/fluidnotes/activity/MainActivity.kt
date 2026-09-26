package com.chung.fluidnotes.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.views.HomeScreenView
import com.chung.fluidnotes.views.HomeScreenViewPreview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        // Get DAO from Application
        val dao = (application as FluidNotesApp).database.notesDao()

        setContent {
            AppTheme {
                // Collect the database Flow as a Compose State
                val notes by dao.getAllNotes().collectAsState(initial = emptyList())

                HomeScreenView(notes = notes)
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