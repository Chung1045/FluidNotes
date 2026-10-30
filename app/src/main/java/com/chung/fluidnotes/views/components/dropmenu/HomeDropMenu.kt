package com.chung.fluidnotes.views.components.dropmenu

import androidx.compose.runtime.Composable
import dev.chrisbanes.haze.HazeState

@Composable
fun HomeDropMenu(
    hazeState: HazeState,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    onTrashedNotesClick: () -> Unit,
) {
    AppDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        hazeState = hazeState
    ) {
        AppDropdownMenuItem(text = "Settings", onClick = onSettingsClick)
        AppDropdownMenuItem(text = "Trashed Notes", onClick = onTrashedNotesClick)
    }
}