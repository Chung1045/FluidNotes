package com.chung.fluidnotes.views.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.chung.fluidnotes.ui.theme.AppTheme

@Composable
fun DeleteDialog(
    count: Int = 1,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        title = {
            if (count > 1) {
                Text("Delete $count notes?")
            } else {
                Text("Delete note?")
            }
        },
        text = {
            if (count > 1) {
                Text(text = "Selected notes will be gone forever.")
            } else {
                Text(text = "Selected note will be gone forever.")
            }

        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() },
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.danger)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("Cancel")
            }
        }
    )
}