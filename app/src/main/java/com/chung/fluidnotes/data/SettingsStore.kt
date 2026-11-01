package com.chung.fluidnotes.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppThemeMode {
    LIGHT,
    DARK
}

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val followSystemThemeKey = booleanPreferencesKey("follow_system_theme")
    private val themeStyleKey = stringPreferencesKey("theme_style")

    // Flow for the "Follow System Theme" toggle (defaults to true)
    val followSystemTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[followSystemThemeKey] ?: true
    }

    // Flow for the specific Light/Dark radio option (defaults to LIGHT)
    val themeStyle: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        prefs[themeStyleKey]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
            ?: AppThemeMode.LIGHT
    }

    // Setter for the system theme toggle
    suspend fun setFollowSystemTheme(follow: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[followSystemThemeKey] = follow
        }
    }

    // Setter for the Light/Dark theme option
    suspend fun setThemeStyle(theme: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeStyleKey] = theme.name
        }
    }
}