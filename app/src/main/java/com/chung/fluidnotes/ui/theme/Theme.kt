package com.chung.fluidnotes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.AppThemeMode
import com.chung.fluidnotes.data.SettingsStore
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.materials.HazeMaterials

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AppTheme(
    darkThemeOverride: Boolean? = null, // handy for previews; null = use saved setting
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? FluidNotesApp // null in @Preview

    val settingsStore = remember { app?.settingsStore ?: SettingsStore(context.applicationContext) }
    val followSystem by settingsStore.followSystemTheme
        .collectAsState(initial = app?.initialFollowSystem ?: true)
    val themeStyle by settingsStore.themeStyle
        .collectAsState(initial = app?.initialStyle ?: AppThemeMode.LIGHT)

    val darkTheme = darkThemeOverride ?: if (followSystem) {
        isSystemInDarkTheme()
    } else {
        themeStyle == AppThemeMode.DARK
    }

    val colorScheme = when {
        dynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Keep status/navigation bar icons readable when the in-app theme differs from the system theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object AppTheme {

    val frostedTexture: HazeBlurStyle
        @Composable
        @ReadOnlyComposable
        get() = HazeMaterials.ultraThin(
            containerColor = controlsBackground
        )

    val controlsBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)

    val controlsBackgroundHigh: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)

    val controlsForeground: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val controlsTint: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val danger: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error
}