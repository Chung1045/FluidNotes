package com.chung.fluidnotes.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import kotlinx.coroutines.launch

@Composable
fun AppNavDrawer(
    drawerState: DrawerState,
    onSettingsClick: () -> Unit,
    onTrashedNotesClick: () -> Unit,
    hazeState: HazeState,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }
    val blurTexture = AppTheme.frostedTexture

    fun navigate(action: () -> Unit) {
        scope.launch {
            action()
            drawerState.close()
        }
    }

    // Transparent items so the glass shows through (default selected/unselected colours are opaque-ish)
    val itemColors = NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        selectedContainerColor = Color.Transparent
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.Transparent,
                drawerTonalElevation = 0.dp,
                // Insets are handled inside the glass Column so the blur extends under the system bars
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (!isLowEnd) {
                                Modifier.hazeBlur(
                                    input = HazeInput.Sources(hazeState),
                                    style = blurTexture.then { blurRadius(35.dp) },
                                    performanceMode = HazePerformanceMode.Performance
                                )
                            } else {
                                Modifier.background(AppTheme.controlsBackground.copy(alpha = 1f))
                            }
                        )
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Fluid Notes",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.controlsForeground
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("Trashed Notes") },
                        selected = false,
                        icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
                        colors = itemColors,
                        onClick = { navigate(onTrashedNotesClick) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = { Icon(imageVector = Icons.Outlined.Settings, contentDescription = null) },
                        colors = itemColors,
                        onClick = { navigate(onSettingsClick) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        // The root-level hazeBlur was removed: it wrapped the hazeSource it was sampling
        // (ancestor of its own source), so it either blurred nothing or fought the sheet's blur.
        content = content
    )
}