package com.chung.fluidnotes.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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