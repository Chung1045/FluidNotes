package com.chung.fluidnotes.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateResult {
    object UpToDate : UpdateResult
    data class NewRelease(val version: String, val url: String, val notes: String) : UpdateResult
    object Failed : UpdateResult
}

object UpdateChecker {
    suspend fun check(currentVersion: String): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val conn = URL(AppConfig.LATEST_RELEASE_URL).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val latest = json.getString("tag_name")
                if (isNewer(latest, currentVersion)) {
                    UpdateResult.NewRelease(
                        version = latest,
                        url = json.getString("html_url"),
                        notes = json.optString("body").trim() // empty if the release has no description
                    )
                } else UpdateResult.UpToDate
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Update check failed", e)
            UpdateResult.Failed
        }
    }

    // "v0.10" vs "0.9-Dev" -> compare numeric parts only
    private fun isNewer(latest: String, current: String): Boolean {
        fun parts(v: String) = v.removePrefix("v").substringBefore('-')
            .split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(latest); val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }; val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}