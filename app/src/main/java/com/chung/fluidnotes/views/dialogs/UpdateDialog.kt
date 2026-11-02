package com.chung.fluidnotes.views.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

sealed interface UpdateState {
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class NewRelease(val version: String, val url: String, val notes: String) : UpdateState
    data object Error : UpdateState
}

@Composable
fun UpdateDialog(
    hazeState: HazeState,
    state: UpdateState,
    currentVersion: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }
    val blurTexture = AppTheme.frostedTexture
    val uriHandler = LocalUriHandler.current
    val checking = state is UpdateState.Checking

    val title = when (state) {
        UpdateState.Checking -> "Checking for updates"
        UpdateState.UpToDate -> "You're up to date"
        is UpdateState.NewRelease -> "Update available"
        UpdateState.Error -> "Couldn't check for updates"
    }

    AlertDialog(
        onDismissRequest = { if (!checking) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .clip(AlertDialogDefaults.shape)
            .width(300.dp)
            .then(
                if (!isLowEnd) Modifier.hazeBlur(
                    input = HazeInput.Sources(hazeState),
                    style = blurTexture.then { blurRadius(20.dp) },
                    performanceMode = HazePerformanceMode.Performance
                ) else Modifier
            ),
        containerColor = AppTheme.controlsBackgroundHigh,
        icon = {
            Icon(imageVector = Icons.Outlined.Update, contentDescription = "Update Icon")
        },
        title = { Text(title, color = AppTheme.controlsForeground) },
        text = {
            when (state) {
                UpdateState.Checking -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Text("Please wait…", color = AppTheme.controlsForeground)
                }
                UpdateState.UpToDate -> BodyText("You're on the latest version ($currentVersion).")
                is UpdateState.NewRelease -> Column {
                    BodyText("Version ${state.version} is available. You're currently on $currentVersion.")
                    if (state.notes.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "What's new",
                            color = AppTheme.controlsForeground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                state.notes.stripMarkdown(),
                                color = AppTheme.controlsForeground.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                UpdateState.Error -> BodyText("Check your internet connection and try again.")
            }
        },
        confirmButton = {
            when (state) {
                UpdateState.Checking -> Unit
                is UpdateState.NewRelease -> TextButton(onClick = {
                    uriHandler.openUri(state.url)
                    onDismiss()
                }) { Text("Download") }
                else -> TextButton(onClick = onDismiss) { Text("OK") }
            }
        },
        dismissButton = {
            when (state) {
                UpdateState.Checking -> TextButton(onClick = onDismiss) { Text("Cancel") }
                is UpdateState.NewRelease -> TextButton(onClick = onDismiss) { Text("Later") }
                else -> Unit
            }
        }
    )
}

private fun String.stripMarkdown(): String = lines().joinToString("\n") { line ->
    line.replace(Regex("^#{1,6}\\s*"), "")
        .replace(Regex("^\\s*[-*]\\s+"), "• ")
        .replace("**", "")
}

@Composable
private fun BodyText(text: String) {
    Text(text, color = AppTheme.controlsForeground)
}