package com.chung.fluidnotes.views.components.selectiontoolbar

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class SelectionState {
    var selectedIds by mutableStateOf(emptySet<Int>()); private set
    var isSelecting by mutableStateOf(false); private set

    val count get() = selectedIds.size

    fun start(id: Int) { isSelecting = true; toggle(id) }

    fun toggle(id: Int) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll(ids: Collection<Int>) { selectedIds = ids.toSet() }

    /** Unchecks everything but stays in selection mode. */
    fun deselectAll() { selectedIds = emptySet() }

    /** Clears the selection and leaves selection mode. */
    fun clear() { isSelecting = false; selectedIds = emptySet() }

    /** Drops ids that no longer exist; leaves selection mode if the list is empty. */
    fun retainOnly(ids: Collection<Int>) {
        selectedIds = selectedIds intersect ids.toSet()
        if (ids.isEmpty()) isSelecting = false
    }
}

@Composable
fun rememberSelectionState(): SelectionState {
    val state = remember { SelectionState() }
    BackHandler(enabled = state.isSelecting) { state.clear() }
    return state
}