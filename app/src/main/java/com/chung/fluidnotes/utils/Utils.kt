package com.chung.fluidnotes.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Patterns
import androidx.activity.compose.LocalActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.net.toUri
import com.chung.fluidnotes.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Utils {

    @Composable
    fun isLargeDevice(): Boolean {
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
            val candidate = matcher.group()
                .trimEnd('.', ',', ')', ']', '!', '?', ';', ':')
            if (isLikelyUrl(candidate)) {
                links.add(candidate)
            }
        }
        return links.distinct()
    }

    // TLDs likely to show up in real links pasted into notes. Anything not in this
    // set (e.g. "1" in "v18.1", "g" in "e.g.") is treated as not a URL.
    private val COMMON_TLDS = setOf(
        "com", "org", "net", "edu", "gov", "mil", "int",
        "io", "co", "dev", "app", "ai", "me", "so", "to", "sh",
        "xyz", "info", "biz", "site", "online", "tech", "store",
        "blog", "news", "shop", "cloud", "link", "tv",
        "us", "uk", "ca", "au", "de", "jp", "fr", "es", "it", "nl",
        "ru", "in", "cn", "br", "mx", "eu", "ch", "se", "no", "fi",
        "dk", "pl", "kr", "nz", "za", "ie", "pt", "gr", "be", "at",
        "hk", "tw", "sg", "kr"
    )

    private fun isLikelyUrl(candidate: String): Boolean {
        if (candidate.startsWith("http://", ignoreCase = true) ||
            candidate.startsWith("https://", ignoreCase = true) ||
            candidate.startsWith("www.", ignoreCase = true)
        ) return true

        val host = candidate.substringBefore("/")
        val labels = host.split(".")
        if (labels.size < 2) return false

        val tld = labels.last().lowercase()
        if (tld !in COMMON_TLDS) return false

        // reject purely numeric "domains" like "18.1" or "3.14"
        if (labels.dropLast(1).all { it.isNotEmpty() && it.all(Char::isDigit) }) return false

        // reject single-character labels like "e.g" / "a.m"
        if (labels[labels.size - 2].length < 2) return false

        return true
    }
}

object NoteDeleteHelper {

    @Composable
    fun TrashNoteDialog(
        context: Context,
        coroutineScope: CoroutineScope,
        showTrashDialog: Boolean,
        trashLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onFetchUris: suspend () -> List<Uri>, // Lambda for async fetch
        onDismissDelete: () -> Unit
    ) {
        if (showTrashDialog) {
            val fetchedUris by produceState(initialValue = emptyList(), showTrashDialog) {
                value = onFetchUris()
            }
            val noteCount = fetchedUris.size

            AlertDialog(
                onDismissRequest = onDismissDelete,
                title = {
                    Text(
                        if (noteCount > 0) "Move $noteCount notes to Trash?"
                        else "Move this note to Trash?"
                    )
                },
                text = {
                    Text(
                        if (noteCount > 0) "These note(s) will be moved to Trash and can be restored."
                        else "This note will be moved to Trash and can be restored."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDismissDelete()

                        }
                    ) {
                        Text("Move to Trash", color = AppTheme.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDelete) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}