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
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.ui.theme.AppTheme
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

        setContent {
            AppTheme {
                // Track initial loading state
                var isLoading by remember { mutableStateOf(true) }

                // Collect the database Flow as a Compose State
                val notes by dao.getAllNotes().collectAsState(initial = emptyList())

                // Keep the skeleton loader visible for a smooth 600ms window on startup
                LaunchedEffect(Unit) {
                    delay(600.milliseconds)
                    isLoading = false
                }

                HomeScreenView(
                    notes = notes,
                    isLoading = isLoading
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