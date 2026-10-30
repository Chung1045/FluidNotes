package com.chung.fluidnotes.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.R
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.views.components.FrostedFAB
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState


@Composable
fun OneUIBottomBar(
    hazeState: HazeState,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf("Home", "Albums")
    val focusedIcons = listOf(Icons.Default.Home, Icons.Default.PhotoLibrary)
    val navIcons = listOf(Icons.Outlined.Home, Icons.Outlined.PhotoLibrary)

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
            .wrapContentWidth()
            .clip(CircleShape)
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = HazeBlurStyle { blurRadius(10.dp) }
            ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(76.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 6.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = focusedIcons[index],
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = navIcons[index],
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedItemCount2(
    count: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    // 1. Read the composable theme color here
    val tintColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .height(height * Utils.getUIScale())
            .clip(CircleShape)
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = HazeBlurStyle {
                    blurRadius(10.dp)
                    colorEffects(
                        listOf(HazeColorEffect.tint(tintColor))
                    )
                }
            ),
        contentAlignment = Alignment.Center
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
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(
                text = "$count",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



@Preview
@Composable
fun TestHazeWithPhoto() {
    val hazeState = rememberHazeState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Image covers the whole screen
        Image(
            painter = painterResource(id = R.drawable.test_photo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState) // Haze captures this photo
        )

        // 2. Center controls (SelectedItemCount2 & HazeFAB stacked vertically)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectedItemCount2(
                count = 5,
                isChecked = true,
                onCheckedChange = { },
                hazeState = hazeState
            )

            FrostedFAB(
                hazeState = hazeState
            )
        }

        // 3. Bottom Navigation Bar anchored to bottom
        OneUIBottomBar(
            hazeState = hazeState,
            selectedIndex = 0,
            onItemSelected = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

