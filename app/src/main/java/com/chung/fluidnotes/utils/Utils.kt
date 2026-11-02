package com.chung.fluidnotes.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Patterns
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Utils {

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
        "hk", "tw", "sg"
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