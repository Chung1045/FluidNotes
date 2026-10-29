package com.chung.fluidnotes.views.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.chung.fluidnotes.ui.theme.AppTheme

@Composable
fun TrashDialog(
    count: Int = 1,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        title = {
            if (count > 1) {
                Text("Trash notes")
            } else {
                Text("Trash note")
            }
        },
        text = {
            if (count > 1) {
                Text(text = "Are you sure you want to trash $count notes?")
            } else {
                Text(text = "Are you sure you want to trash this note?")
            }

        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() },
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.danger)
            ) {
                Text("Trash")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("Cancel")
            }
        }
    )
}