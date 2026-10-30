package com.chung.fluidnotes.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

@Composable
fun FrostedFAB(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val fabShape = RoundedCornerShape(20.dp)
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }
    val blurTexture = AppTheme.frostedTexture

    FloatingActionButton(
        onClick = onClick,
        shape = fabShape,
        containerColor = Color.Transparent, // Keeps inner FAB container disabled
        contentColor = MaterialTheme.colorScheme.onSecondary, // Ensures good contrast on primary
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier
            .clip(fabShape)
            // 1. First blur the image/content behind the FAB
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
            // 2. Tint with semi-transparent primary on top of the blur
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                shape = fabShape
            )
            // 3. Draw subtle border
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
                shape = fabShape
            )
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add note"
        )
    }
}