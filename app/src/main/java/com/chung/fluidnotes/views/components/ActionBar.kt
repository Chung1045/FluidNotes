package com.chung.fluidnotes.views.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.utils.getUIScale
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

enum class ActionBarMode {
    Home,
    TrashView
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ActionBar(
    selectedCount: Int = 0,
    hazeState: HazeState,
    visible: Boolean,
    mode: ActionBarMode = ActionBarMode.Home,
    modifier: Modifier = Modifier,
    onPinClick: () -> Unit = {},
    onTrashClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {}
) {
    val isSelecting = selectedCount > 0
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }

    val buttonSize = (48 * getUIScale()).dp
    val cornerRadius = (24 * getUIScale()).dp
    val horizontalContainerPadding = (4 * getUIScale()).dp
    val blurTexture = AppTheme.frostedTexture

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier.zIndex(3f)
    ) {
        Surface(
            shape = RoundedCornerShape(cornerRadius),
            color = if (isLowEnd) AppTheme.controlsBackground.copy(alpha = 0.92f) else AppTheme.controlsBackground,
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .then(
                    if (!isLowEnd) {
                        Modifier.hazeBlur(
                            input = HazeInput.Sources(hazeState),
                            style = blurTexture.then { blurRadius(10.dp) },
                            performanceMode = HazePerformanceMode.Fixed(0.25f)
                        )
                    } else {
                        Modifier
                    }
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = horizontalContainerPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (mode) {
                    ActionBarMode.Home -> {
                        IconButton(
                            onClick = onPinClick,
                            modifier = Modifier.size(buttonSize),
                            enabled = isSelecting
                        ) {
                            Icon(Icons.Outlined.PushPin,
                                contentDescription = "Pin",
                                tint = if (isSelecting)
                                    AppTheme.controlsForeground
                                else
                                    AppTheme.controlsForeground.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = onTrashClick,
                            modifier = Modifier.size(buttonSize),
                            enabled = isSelecting
                        ) {
                            Icon(Icons.Outlined.Delete,
                                contentDescription = "Trash",
                                tint = if (isSelecting)
                                    AppTheme.controlsForeground
                                else
                                    AppTheme.controlsForeground.copy(alpha = 0.3f)
                            )
                        }
                    }

                    ActionBarMode.TrashView -> {
                        IconButton(onClick = onRestoreClick, modifier = Modifier.size(buttonSize)) {
                            Icon(Icons.Outlined.Replay, contentDescription = "Restore", tint = AppTheme.controlsForeground)
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(buttonSize)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = AppTheme.controlsForeground)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Floating bottom-of-screen variant: wraps ActionBar with the
// gradient backdrop + bottom placement shared with BottomNavBar.
// Use this (not the plain ActionBar) whenever the bar floats over
// content at the bottom of the screen. GradientTopBar embeds the
// plain ActionBar directly since it's laid out inline in a Row, not
// floating over content.
// ----------------------------------------------------
@Composable
fun BoxScope.BottomActionBar(
    selectedCount: Int = 0,
    hazeState: HazeState,
    visible: Boolean,
    mode: ActionBarMode = ActionBarMode.Home,
    modifier: Modifier = Modifier,
    onTrashClick: () -> Unit = {},
    onPinClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {}
) {
    val bottomPadding = (16 * getUIScale()).dp
    val gradientHeight = (80 * getUIScale()).dp

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .zIndex(3f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ActionBar(
                selectedCount = selectedCount,
                hazeState = hazeState,
                visible = true,
                mode = mode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = bottomPadding),
                onTrashClick = onTrashClick,
                onPinClick = onPinClick,
                onDeleteClick = onDeleteClick,
                onRestoreClick = onRestoreClick,
            )
        }
    }
}

@Preview(name = "Home", showBackground = true, device = "id:pixel_6")
@Composable
fun PreviewActionBarMediaViewer() {
    AppTheme {
        ActionBarPreviewContainer { hazeState ->
            BottomActionBar(
                hazeState = hazeState,
                visible = true,
                mode = ActionBarMode.Home,
            )
        }
    }
}

// ----------------------------------------------------
// TrashView Mode
// ----------------------------------------------------
@Preview(name = "TrashView Mode", showBackground = true, device = "id:pixel_6")
@Composable
fun PreviewActionBarTrash() {
    AppTheme {
        ActionBarPreviewContainer { hazeState ->
            BottomActionBar(
                hazeState = hazeState,
                visible = true,
                mode = ActionBarMode.TrashView,
            )
        }
    }
}

// ----------------------------------------------------
// Helper Box Container to supply BoxScope & Haze Background
// ----------------------------------------------------
@Composable
private fun ActionBarPreviewContainer(
    content: @Composable BoxScope.(HazeState) -> Unit
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F1C2C),
                        Color(0xFF928DAB),
                        Color(0xFFE94560)
                    )
                )
            )
    ) {
        content(hazeState)
    }
}