package com.chung.fluidnotes.views.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chung.fluidnotes.R
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.views.NoteEditDropMenu
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

enum class GradientTopBarMode {
    Default,
    Actionless,
    ViewerMode
}

@Composable
fun gradientTopBarHeight(): Dp = (120 * Utils.getUIScale()).dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun BoxScope.GradientTopBar(
    topBarMode: GradientTopBarMode = GradientTopBarMode.Default,
    hazeState: HazeState,
    isUiChromeVisible: Boolean = true,
    showMenu: Boolean = false,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    onShowMenuChange: (Boolean) -> Unit = {},
    onClose: () -> Unit = {},
    menuContent: @Composable () -> Unit = {},
) {
    val buttonSize = (48 * Utils.getUIScale()).dp
    val horizontalPadding = (16 * Utils.getUIScale()).dp
    val topPadding = (8 * Utils.getUIScale()).dp
    val blurTexture = AppTheme.frostedTexture

    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }

    AnimatedVisibility(
        visible = isUiChromeVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .zIndex(3f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gradientTopBarHeight())
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .statusBarsPadding()
                    .padding(top = topPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape)
                            .then(
                                if (!isLowEnd) {
                                    Modifier.hazeBlur(
                                        input = HazeInput.Sources(hazeState),
                                        style = blurTexture.then { blurRadius(10.dp) },
                                        performanceMode = HazePerformanceMode.Performance
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .background(if (isLowEnd) AppTheme.controlsBackground.copy(alpha = 0.92f) else AppTheme.controlsBackground)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = AppTheme.controlsForeground
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(buttonSize))
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))

                if (topBarMode == GradientTopBarMode.Default || topBarMode == GradientTopBarMode.ViewerMode) {
                    IconButton(
                        onClick = { onShowMenuChange(true) },
                        enabled = !showMenu,
                        modifier = Modifier
                            .alpha(if (showMenu) 0f else 1f)
                            .size(buttonSize)
                            .clip(CircleShape)
                            .then(
                                if (!isLowEnd) {
                                    Modifier.hazeBlur(
                                        input = HazeInput.Sources(hazeState),
                                        style = blurTexture.then { blurRadius(10.dp) },
                                        performanceMode = HazePerformanceMode.Performance
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .background(if (isLowEnd) AppTheme.controlsBackground.copy(alpha = 0.92f) else AppTheme.controlsBackground)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More Options",
                            tint = AppTheme.controlsForeground
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(buttonSize))
                }
            }
        }
    }

    // Overlay menu: lives in the parent Box (outside the top bar's AnimatedVisibility)
    // so its fillMaxSize covers the whole screen and tap-outside-to-dismiss works everywhere.
    // The anchor mirrors the top bar Row layout, so the menu's top-right corner lands exactly
    // on the top-right corner of the ⋮ button: status bar inset + topPadding from the top,
    // horizontalPadding from the end.
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    CompositionLocalProvider(
        LocalDropdownAnchor provides DropdownAnchor(
            top = statusBarTop + topPadding,
            end = horizontalPadding
        )
    ) {
        menuContent()
    }
}

@Composable
fun BoxScope.GradientTopBarContainer(
    hazeState: HazeState,
    isUiChromeVisible: Boolean = true,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val horizontalPadding = (16 * Utils.getUIScale()).dp
    val topPadding = (8 * Utils.getUIScale()).dp

    AnimatedVisibility(
        visible = isUiChromeVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .zIndex(3f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gradientTopBarHeight())
        ) {
            // Gradient Backdrop
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .statusBarsPadding()
                    .padding(top = topPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation / Left Slot
                navigationIcon?.invoke()

                // Optional Middle Title Slot
                if (title != null) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        title()
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Actions / Right Slot
                actions?.invoke(this)
            }
        }
    }
}

@Composable
@Preview(name = "Phone - Default", showBackground = true, device = Devices.PIXEL_6)
fun PreviewTopBarPhone() {
    val hazeState = rememberHazeState()

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.test_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            )

            GradientTopBar(
                topBarMode = GradientTopBarMode.Default,
                hazeState = hazeState,
                isUiChromeVisible = true,
                showMenu = false,
                onShowMenuChange = {},
                onClose = {}
            )
        }
    }
}

// ----------------------------------------------------
// Expanded Screen Preview (Tablet - Embedded ActionBar)
// ----------------------------------------------------
@Composable
@Preview(name = "Tablet - Embedded Viewer ActionBar", showBackground = true, device = Devices.TABLET)
fun PreviewTopBarTabletWithActionBar() {
    val hazeState = rememberHazeState()

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.test_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            )

            GradientTopBar(
                topBarMode = GradientTopBarMode.ViewerMode,
                hazeState = hazeState,
                isUiChromeVisible = true,
                showMenu = false,
                onShowMenuChange = {},
                onClose = {}
            )
        }
    }
}

@Composable
@Preview(name = "Embedded Viewer ActionBar with Menu", showBackground = true, device = Devices.PIXEL_6)
fun PreviewTopBarWithActionBarMenu() {
    val hazeState = rememberHazeState()

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.test_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            )

            GradientTopBar(
                topBarMode = GradientTopBarMode.ViewerMode,
                hazeState = hazeState,
                isUiChromeVisible = true,
                showMenu = true,
                onShowMenuChange = {},
                onClose = {},
                menuContent = {
                    NoteEditDropMenu(
                        hazeState = hazeState,
                        expanded = true,
                        onDismissRequest = { },
                        onTrashClick = { },
                    ) }
            )
        }
    }
}