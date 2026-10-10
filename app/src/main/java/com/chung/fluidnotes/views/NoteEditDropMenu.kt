package com.chung.fluidnotes.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.chung.fluidnotes.R
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.views.components.AppDropdownMenu
import com.chung.fluidnotes.views.components.AppDropdownMenuItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
internal fun NoteEditDropMenu(
    hazeState: HazeState,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onTrashClick: () -> Unit = {}
) {
    AppDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        hazeState = hazeState,
        modifier = modifier
    ) {
        AppDropdownMenuItem(
            text = "Trash",
            onClick = {
                onDismissRequest()
                onTrashClick()
            }
        )
    }
}

@Composable
@Preview
private fun PreviewNoteEditDropMenu() {
    val hazeState = rememberHazeState()

    AppTheme {
        Box {
            Image(
                painter = painterResource(id = R.drawable.test_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            )

            NoteEditDropMenu(
                hazeState = hazeState,
                expanded = true,
                onDismissRequest = {},
                onTrashClick = {}
            )
        }
    }
}