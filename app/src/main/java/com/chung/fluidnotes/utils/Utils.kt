package com.chung.fluidnotes.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings
import android.util.Patterns
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.net.toUri
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Utils {

    @Composable
    fun isLargeDevice (): Boolean {
        val configuration = LocalConfiguration.current
        // Use smallestScreenWidthDp to get a stable indicator of device class
        // (e.g., typically >= 600dp for tablets/foldables)
        val smallestWidth = configuration.smallestScreenWidthDp

        return smallestWidth >= 600
    }

    fun isLowEndDevice(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Catches true low-RAM / Android Go devices
        if (activityManager.isLowRamDevice) return true

        // Catches low-RAM devices where the OEM didn't set the low_ram flag (<= 3GB RAM)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024 * 1024 * 1024.0)
        if (totalRamGb <= 3.0) return true

        // Catches weak quad-core processors
        if (Runtime.getRuntime().availableProcessors() <= 4) return true

        return false
    }

    /**
     * Checks if the app has permission to delete/modify media silently (Android 12+).
     */
    fun hasManageMediaPermission(context: Context): Boolean {
        return MediaStore.canManageMedia(context)
    }

    /**
     * Opens system settings so the user can grant MANAGE_MEDIA access.
     */
    fun openManageMediaSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA).apply {
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun getUIScale(): Float {
        val activity = LocalActivity.current
        val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
        val isTablet = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact
        return if (isTablet) 1.05f else 1f
    }

    fun formatRelativeTime(timestampMillis: Long): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

        val now = System.currentTimeMillis()
        val diffMillis = now - timestampMillis
        val diffMinutes = diffMillis / 60_000
        val diffHours = diffMillis / 3_600_000

        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes minute${if (diffMinutes == 1L) "" else "s"} ago"
            diffHours < 24 -> "$diffHours hour${if (diffHours == 1L) "" else "s"} ago"
            else -> Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        }
    }

    fun formatAbsoluteDateTime(timestampMillis: Long): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    fun extractLinks(text: String): List<String> {
        val matcher = Patterns.WEB_URL.matcher(text)
        val links = mutableListOf<String>()
        while (matcher.find()) {
            links.add(matcher.group())
        }
        return links.distinct()
    }

}
