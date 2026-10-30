package com.chung.fluidnotes.views

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.activity.NoteEditViewActivity
import com.chung.fluidnotes.activity.TrashedNotesActivity
import com.chung.fluidnotes.data.NoteItem
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.OpenNoteState
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.utils.rememberLastNonEmpty
import com.chung.fluidnotes.views.components.BottomActionBar
import com.chung.fluidnotes.views.components.FrostedFAB
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.NoteCard
import com.chung.fluidnotes.views.components.NoteCardSkeleton
import com.chung.fluidnotes.views.components.TopScrim
import com.chung.fluidnotes.views.components.dropmenu.HomeDropMenu
import com.chung.fluidnotes.views.components.gradientTopBarHeight
import com.chung.fluidnotes.views.components.selectiontoolbar.SelectedItemCountToolbar
import com.chung.fluidnotes.views.components.selectiontoolbar.rememberSelectionState
import com.chung.fluidnotes.views.dialogs.TrashDialog
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private enum class NotesContentState { Loading, Empty, Content }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenView(
    modifier: Modifier = Modifier,
    notes: List<NoteItem> = emptyList(),
    isLoading: Boolean = false,
    onTrashNotes: (List<Int>) -> Unit = {}
) {
    val displayedNotes = rememberLastNonEmpty(notes)
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLargeDevice = Utils.isLargeDevice()
    val columnCells = remember(context) {
        if (isLargeDevice) {
            StaggeredGridCells.Adaptive(200.dp)
        } else {
            StaggeredGridCells.Adaptive(160.dp)
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }
    val selection = rememberSelectionState()

    LaunchedEffect(notes) {
        selection.retainOnly(notes.map { it.id })
    }

    val navBars = WindowInsets.navigationBarsIgnoringVisibility
    val navBarInset = navBars.asPaddingValues().calculateBottomPadding()
    val imeInsets = WindowInsets.ime
    val fabDensity = LocalDensity.current
    val baseScrimHeight = if (Utils.isLargeDevice()) 20.dp else 80.dp

    Box(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !selection.isSelecting,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    FrostedFAB(
                        modifier = Modifier
                            // Read in the layout phase: follows the keyboard frame-by-frame
                            // without recomposing the whole screen.
                            .offset {
                                val lift = maxOf(
                                    navBars.getBottom(fabDensity),
                                    imeInsets.getBottom(fabDensity)
                                )
                                IntOffset(0, -lift)
                            },
                        hazeState = hazeState,
                        onClick = {
                            val intent = Intent(context, NoteEditViewActivity::class.java).apply {
                                putExtra("is_existing_note", false)
                                putExtra("note_id", 0)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentState = when {
                    isLoading -> NotesContentState.Loading
                    notes.isEmpty() -> NotesContentState.Empty
                    else -> NotesContentState.Content
                }
                Crossfade(
                    targetState = contentState,
                    modifier = Modifier.fillMaxSize(),
                    animationSpec = tween(300),
                    label = "notesContent"
                ) { state ->
                    when (state) {
                        NotesContentState.Loading -> {
                            // Random card height for skeleton note cards
                            val skeletonHeights = remember {
                                val random = kotlin.random.Random(System.currentTimeMillis())
                                // Generates 12 random heights between 120.dp and 230.dp
                                List(12) { random.nextInt(120, 231).dp }
                            }

                            // Display staggered grid with skeleton cards while loading
                            LazyVerticalStaggeredGrid(
                                columns = columnCells,
                                state = rememberLazyStaggeredGridState(),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = gradientTopBarHeight() + 8.dp,
                                    bottom = 80.dp + navBarInset
                                ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                items(12) {  index ->
                                    NoteCardSkeleton(
                                        modifier = Modifier.height(
                                            skeletonHeights[index % skeletonHeights.size]
                                        )
                                    )
                                }
                            }
                        }
                        NotesContentState.Empty -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = "No notes",
                                    fontSize = 18.sp,
                                    color = AppTheme.controlsForeground,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Create new note",
                                    fontSize = 14.sp,
                                    color = AppTheme.controlsForeground.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        NotesContentState.Content -> {
                            LazyVerticalStaggeredGrid(
                                columns = columnCells,
                                state = rememberLazyStaggeredGridState(),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = gradientTopBarHeight() + 8.dp,
                                    bottom = 80.dp + navBarInset
                                ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                items(
                                    items = displayedNotes,
                                    key = { it.id }
                                ) { note ->
                                    NoteCard(
                                        noteId = note.id,
                                        noteTitle = note.title,
                                        noteContent = note.content,
                                        noteLastEditTimestamp = note.timestamp,
                                        isSelecting = selection.isSelecting,
                                        isSelected = note.id in selection.selectedIds,
                                        onClick = if (selection.isSelecting) ({ selection.toggle(note.id) }) else null,
                                        onLongClick = { selection.start(note.id) },
                                        onCheckedChange = { selection.start(note.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(baseScrimHeight + navBarInset)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 1f)
                                )
                            )
                        )
                )
            }
        }

        TopScrim(Modifier.align(Alignment.TopCenter))

        Crossfade (
            targetState = selection.isSelecting,
            modifier = Modifier.fillMaxWidth(),
            label = "topBarSwap"
        ) { selecting ->
            if (selecting) {
                SelectedItemCountToolbar(
                    hazeState = hazeState,
                    count = selection.count,
                    isChecked = notes.isNotEmpty() && selection.count == notes.size,
                    onCheckedChange = { checked ->
                        if (checked) selection.selectAll(notes.map { it.id })
                        else selection.deselectAll()
                    },
                    onClose = { selection.clear() }
                )
            } else {
                // GradientTopBar
                GradientTopBar(
                    hazeState = hazeState,
                    showScrim = false,
                    showBackButton = false,
                    showMenu = menuExpanded,
                    onShowMenuChange = { menuExpanded = it },
                    onClose = {},
                    menuContent = {
                        HomeDropMenu(
                            hazeState = hazeState,
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            onSettingsClick = { menuExpanded = false },
                            onTrashedNotesClick = {
                                menuExpanded = false
                                // Ask the right-pane editor to close, but don't wait for its
                                // onDestroy - launch Trash right away so there's no delay.
                                OpenNoteState.openNoteId.value?.let { openId ->
                                    OpenNoteState.requestClose(listOf(openId))
                                }
                                context.startActivity(Intent(context, TrashedNotesActivity::class.java))
                            }
                        )
                    }
                )
            }
        }

        if (showTrashDialog) {
            TrashDialog(
                count = selection.count,
                onDismissRequest = { showTrashDialog = false },
                onConfirmation = {
                    onTrashNotes(selection.selectedIds.toList())
                    showTrashDialog = false
                    selection.clear()
                }
            )
        }

        BottomActionBar(
            hazeState = hazeState,
            selectedCount = selection.count,
            visible = selection.isSelecting,
            onTrashClick = {
                showTrashDialog = true
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenViewPreview() {
    val sampleNotes = listOf(
        NoteItem(
            id = 1,
            title = "First Note",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            timestamp = System.currentTimeMillis(),
            isTrashed = false
        ),
        NoteItem(
            id = 2,
            title = "Microsoft",
            content = "Where do you want to go today?",
            timestamp = System.currentTimeMillis(),
            isTrashed = false
        ),
        NoteItem(
            id = 3,
            title = "Xiaomi",
            content = "Always believe that something wonderful is about to happen",
            timestamp = System.currentTimeMillis(),
            isTrashed = false
        )
    )

    HomeScreenView(notes = sampleNotes)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenViewLoadingPreview() {
    HomeScreenView(notes = emptyList(), isLoading = true)
}