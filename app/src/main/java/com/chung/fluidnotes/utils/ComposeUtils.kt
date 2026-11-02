package com.chung.fluidnotes.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

private class LastRef<T>(var value: T)

/**
 * Returns [list] while it has items, and the last non-empty list once it becomes empty,
 * so an outgoing "content" animation still has something to draw while it fades out.
 */
@Composable
fun <T> rememberLastNonEmpty(list: List<T>): List<T> {
    val ref = remember { LastRef(list) }
    if (list.isNotEmpty()) ref.value = list
    return ref.value
}

@Composable
fun isLargeDevice(): Boolean {
    val configuration = LocalConfiguration.current
    // Use smallestScreenWidthDp to get a stable indicator of device class
    // (e.g., typically >= 600dp for tablets/foldables)
    val smallestWidth = configuration.smallestScreenWidthDp

    return smallestWidth >= 600
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun getUIScale(): Float {
    val activity = LocalActivity.current
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val isTablet = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact
    return if (isTablet) 1.05f else 1f
}