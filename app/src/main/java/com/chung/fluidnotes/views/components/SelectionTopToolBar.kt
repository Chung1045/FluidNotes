package com.chung.fluidnotes.views.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

@Composable
fun SelectedItemCountToolbar(
    hazeState: HazeState,
    count: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val scale = Utils.getUIScale()
    val targetHeight = (48 * scale).dp
    val blurTexture = AppTheme.frostedTexture
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        SelectedItemCount(
            count = count,
            isChecked = isChecked,
            onCheckedChange = onCheckedChange,
            height = targetHeight,
            hazeState = hazeState,
        )

        Spacer(modifier = Modifier.weight(1f))
        Surface(
            onClick = onClose,
            modifier = Modifier
                .height(targetHeight)
                .clip(CircleShape)
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
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = if (isLowEnd) AppTheme.controlsBackground.copy(alpha = 0.92f) else AppTheme.controlsBackground
        ) {
            Box(
                modifier = Modifier.padding(horizontal = (20 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = AppTheme.controlsForeground
                )
            }
        }
    }
}

@Composable
fun SelectedItemCount(
    count: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val context = LocalContext.current
    val isLowEnd = remember(context) { Utils.isLowEndDevice(context) }
    val blurTexture = AppTheme.frostedTexture

    Surface(
        modifier = modifier
            .height(height * Utils.getUIScale())
            // Removed animateContentSize here to stop rectangular clipping,
            // relying on the inner content layout and smooth text transition.
            .clip(CircleShape)
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
                shape = CircleShape
            ),
        shape = CircleShape,
        color = if (isLowEnd) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(start = 4.dp, end = 16.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = AppTheme.controlsTint,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            if (!isLowEnd) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                                (slideOutVertically { -it } + fadeOut())
                    },
                    label = "CounterAnimation"
                ) { targetCount ->
                    Text(
                        text = "$targetCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.controlsForeground
                    )
                }
            } else {
                Text(
                    text = "$count",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.controlsForeground
                )
            }

        }
    }
}