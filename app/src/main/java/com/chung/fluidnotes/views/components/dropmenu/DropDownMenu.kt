package com.chung.fluidnotes.views.components.dropmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.utils.getUIScale
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * Optional anchor offsets (distance from the top/end edge of the menu's container to the
 * top-right corner of the anchor button). When provided, AppDropdownMenu aligns its top-right
 * corner to it; when null, the legacy default offsets are used.
 */
data class DropdownAnchor(val top: Dp, val end: Dp)

val LocalDropdownAnchor = compositionLocalOf<DropdownAnchor?> { null }

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }
    val blurTexture = AppTheme.frostedTexture

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier.zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismissRequest() })
                }
        ) {
            val anchor = LocalDropdownAnchor.current
            val topPadding = anchor?.top ?: (46 * getUIScale()).dp
            val horizontalPadding = anchor?.end ?: (16 * getUIScale()).dp

            val menuShape = RoundedCornerShape(26.dp)
            val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = topPadding, end = horizontalPadding)
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 200.dp)
                    .animateEnterExit(
                        enter = scaleIn(
                            initialScale = 0.85f,
                            transformOrigin = TransformOrigin(1f, 0f),
                            animationSpec = tween(250)
                        ),
                        exit = scaleOut(
                            targetScale = 0.90f,
                            transformOrigin = TransformOrigin(1f, 0f),
                            animationSpec = tween(200)
                        )
                    )
                    .clip(menuShape)
                    .then(
                        if (!isLowEnd && hazeState != null) {
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
                        shape = menuShape
                    ),
                shape = menuShape,
                color = containerColor,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    content = content
                )
            }
        }
    }
}

@Composable
fun AppDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = { Text(text = text, fontSize = 14.sp) },
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    )
}