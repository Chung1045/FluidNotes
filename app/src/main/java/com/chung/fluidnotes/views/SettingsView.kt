package com.chung.fluidnotes.views

/* Note:
    If BuildConfig is left un-resolved, please assemble the project first
    At: Build > Assemble Project
*/

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.BuildConfig
import com.chung.fluidnotes.data.AppThemeMode
import com.chung.fluidnotes.data.SettingsStore
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.AppConfig
import com.chung.fluidnotes.utils.UpdateChecker
import com.chung.fluidnotes.utils.UpdateResult
import com.chung.fluidnotes.utils.isLargeDevice
import com.chung.fluidnotes.views.components.BottomScrim
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.GradientTopBarMode
import com.chung.fluidnotes.views.components.TopScrim
import com.chung.fluidnotes.views.dialogs.UpdateDialog
import com.chung.fluidnotes.views.dialogs.UpdateState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun SettingsView(
    onClose: () -> Unit
) {
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val largeDevice = isLargeDevice()

    val columnCells = remember(context, largeDevice) {
        if (largeDevice) GridCells.Adaptive(200.dp)
        else GridCells.Adaptive(160.dp)
    }

    val settingsStore = remember { SettingsStore(context.applicationContext) }
    val followSystemTheme by settingsStore.followSystemTheme.collectAsState(initial = true)
    val selectedTheme by settingsStore.themeStyle.collectAsState(initial = AppThemeMode.LIGHT)
    // States for theme preferences

    var updateState by remember { mutableStateOf<UpdateState?>(null) }
    var updateJob by remember { mutableStateOf<Job?>(null) }
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .hazeSource(hazeState)
    ) {
        GradientTopBar(
            hazeState = hazeState,
            topBarMode = GradientTopBarMode.Actionless,
            showBackButton = true,
            onClose = onClose
        )

        LazyVerticalGrid(
            columns = columnCells,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 78.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header that spans the full width of the grid columns
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 135.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Settings",
                        color = AppTheme.controlsForeground,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    SettingsCategoryLabel(
                        labelText = "App Theme"
                    )

                    // 1. Follow System Theme (Switch)
                    SettingsRow(
                        position = SettingsRowPosition.Top,
                        onClick = { scope.launch { settingsStore.setFollowSystemTheme(!followSystemTheme) } }
                    ) {
                        Text(
                            text = "Follow System Theme",
                            color = AppTheme.controlsForeground,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = followSystemTheme,
                            onCheckedChange = { scope.launch { settingsStore.setFollowSystemTheme(it) } }
                        )
                    }

                    // 2. Light Theme (Radio Button)
                    SettingsRow(
                        position = SettingsRowPosition.Middle,
                        onClick = {
                            scope.launch { settingsStore.setThemeStyle(AppThemeMode.LIGHT) }
                        },
                        enabled = !followSystemTheme
                    ) {
                        Text(
                            text = "Light Theme",
                            modifier = Modifier.weight(1f),
                            color = if (followSystemTheme) {
                                AppTheme.controlsForeground.copy(alpha = 0.38f)
                            } else {
                                AppTheme.controlsForeground
                            }
                        )

                        RadioButton(
                            selected = selectedTheme == AppThemeMode.LIGHT,
                            onClick = null, // Row's clickable modifier handles selection
                            enabled = !followSystemTheme
                        )
                    }

                    // 3. Dark Theme (Radio Button)
                    SettingsRow(
                        position = SettingsRowPosition.Bottom,
                        onClick = {
                            scope.launch { settingsStore.setThemeStyle(AppThemeMode.DARK) }
                        },
                        enabled = !followSystemTheme
                    ) {
                        Text(
                            text = "Dark Theme",
                            modifier = Modifier.weight(1f),
                            color = if (followSystemTheme) {
                                AppTheme.controlsForeground.copy(alpha = 0.38f)
                            } else {
                                AppTheme.controlsForeground
                            }
                        )

                        RadioButton(
                            selected = selectedTheme == AppThemeMode.DARK,
                            onClick = null, // Row's clickable modifier handles selection
                            enabled = !followSystemTheme
                        )
                    }

                    SettingsCategoryLabel(
                        labelText = "App Info"
                    )

                    // 4. Version
                    SettingsRow(
                        position = SettingsRowPosition.Top,
                        onClick = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Version", color = AppTheme.controlsForeground)
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                color = AppTheme.controlsForeground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    SettingsRow(
                        position = SettingsRowPosition.Middle,
                        onClick = {
                            updateJob?.cancel()
                            updateState = UpdateState.Checking
                            updateJob = scope.launch {
                                updateState = when (val result =
                                    UpdateChecker.check(BuildConfig.VERSION_NAME)) {
                                    is UpdateResult.UpToDate -> UpdateState.UpToDate
                                    is UpdateResult.NewRelease -> UpdateState.NewRelease(
                                        result.version,
                                        result.url,
                                        result.notes
                                    )

                                    is UpdateResult.Failed -> UpdateState.Error
                                }
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Check for updates", color = AppTheme.controlsForeground)
                        }
                    }

                    SettingsRow(
                        position = SettingsRowPosition.Bottom,
                        onClick = {
                            uriHandler.openUri(AppConfig.REPO_URL)
                        }
                    ) {
                        Text(
                            text = "View on GitHub",
                            color = AppTheme.controlsForeground,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open GitHub repo url",
                            tint = AppTheme.controlsForeground
                        )
                    }
                }
            }

        }

        BottomScrim(Modifier.align(Alignment.BottomCenter))

        TopScrim(Modifier.align(Alignment.TopCenter))

        updateState?.let { state ->
            UpdateDialog(
                hazeState = hazeState,
                state = state,
                currentVersion = BuildConfig.VERSION_NAME,
                onDismiss = {
                    updateJob?.cancel()
                    updateJob = null
                    updateState = null
                }
            )
        }
    }
}


// Two-line rows (e.g. Version + subtitle) need ~44dp of content plus padding, so 54dp was too tight
private val SettingsRowHeight = 60.dp
private val LargeCorner = 24.dp
private val SmallCorner = 4.dp

/** Where a row sits inside its group; decides which corners are large and which are small. */
private enum class SettingsRowPosition(val shape: RoundedCornerShape) {
    Single(RoundedCornerShape(LargeCorner)),
    Top(RoundedCornerShape(LargeCorner, LargeCorner, SmallCorner, SmallCorner)),
    Middle(RoundedCornerShape(SmallCorner)),
    Bottom(RoundedCornerShape(SmallCorner, SmallCorner, LargeCorner, LargeCorner))
}

@Composable
fun SettingsCategoryLabel(
    labelText: String
) {
    Text(
        text = labelText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = AppTheme.controlsTint,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

/** Single settings option row. Every option shares the same height; category labels don't use this. */
@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    position: SettingsRowPosition = SettingsRowPosition.Single,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(position.shape)
            .background(AppTheme.controlsBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SettingsRowHeight)
                .then(
                    if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick)
                    else Modifier
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Preview
@Composable
fun PreviewSettingsView() {
    AppTheme {
        SettingsView(
            onClose = {}
        )
    }
}