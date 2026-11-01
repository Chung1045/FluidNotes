package com.chung.fluidnotes.ui.theme

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

    // Keep status/navigation bar icons readable when the in-app theme differs from the system theme.
    // enableEdgeToEdge() decides icon colour from `darkTheme` here (not the system), and it is
    // re-applied on every ON_RESUME so coming back from another activity can't leave stale icons.
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    if (!view.isInEditMode) {
        val activity = remember(view) { view.context.findComponentActivity() }
        DisposableEffect(activity, lifecycleOwner, darkTheme) {
            fun applySystemBars() {
                val transparent = android.graphics.Color.TRANSPARENT
                activity?.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme }
                )
            }
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) applySystemBars()
            }
            applySystemBars()
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
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