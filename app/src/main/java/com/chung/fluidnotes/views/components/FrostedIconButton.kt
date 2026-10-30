package com.chung.fluidnotes.views.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

@Composable
fun FrostedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = AppTheme.controlsBackground,
    tint: Color = AppTheme.controlsForeground
) {
    val buttonSize = (48 * Utils.getUIScale()).dp
    val blurTexture = AppTheme.frostedTexture
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(buttonSize)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .padding(1.dp)
            .clip(CircleShape)
            .then(
                if (!isLowEnd && hazeState != null) {
                    Modifier.hazeBlur(
                        input = HazeInput.Sources(hazeState),
                        style = blurTexture.then { blurRadius(10.dp) },
                        performanceMode = HazePerformanceMode.Performance
                    )
                } else Modifier
            )
            .background(
                if (isLowEnd || hazeState == null) backgroundColor.copy(alpha = 0.92f)
                else backgroundColor
            )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}