package com.chung.fluidnotes.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteItem
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils.isLargeDevice
import com.chung.fluidnotes.views.components.ActionBarMode
import com.chung.fluidnotes.views.components.BottomActionBar
import com.chung.fluidnotes.views.components.BottomScrim
import com.chung.fluidnotes.views.components.TopScrim
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.NoteCard
import com.chung.fluidnotes.views.components.SelectedItemCountToolbar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private enum class TrashContentState { Loading, Empty, Content }

class TrashedNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = (application as FluidNotesApp).database.notesDao()

        setContent {
            AppTheme {

                val notes by remember { dao.getAllTrashedNotes() }
                    .collectAsState(initial = null)

                TrashedNotesView(
                    notes = notes,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun TrashedNotesView(
    notes: List<NoteItem>?,
    onClose: () -> Unit
) {
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val isLargeDevice = isLargeDevice()
    val columnCells = remember(context) {
        if (isLargeDevice) StaggeredGridCells.Adaptive(200.dp)
        else StaggeredGridCells.Adaptive(160.dp)
    }

    val skeletonHeights = remember {
        val random = kotlin.random.Random(System.currentTimeMillis())
        List(12) { random.nextInt(120, 231).dp }
    }

    var selectedIds by remember { mutableStateOf(emptySet<Int>()) }
    var isSelecting by remember { mutableStateOf(false) }

    fun exitSelection() {
        isSelecting = false
        selectedIds = emptySet()
    }

    fun toggle(id: Int) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    BackHandler(enabled = isSelecting) { exitSelection() }

    LaunchedEffect(notes) {
        val safeNotes = notes.orEmpty()
        selectedIds = selectedIds intersect safeNotes.map { it.id }.toSet()
        if (safeNotes.isEmpty()) isSelecting = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 135.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Trash",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                ) {
                    val contentState = when {
                        notes == null -> TrashContentState.Loading
                        notes.isEmpty() -> TrashContentState.Empty
                        else -> TrashContentState.Content
                    }
                    Crossfade(
                        targetState = contentState,
                        modifier = Modifier.fillMaxSize(),
                        animationSpec = tween(300),
                        label = "notesContent"
                    ) { state ->
                        when (state) {
                            TrashContentState.Loading -> {
                                LazyVerticalStaggeredGrid(
                                    columns = columnCells,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalItemSpacing = 8.dp
                                ) {
                                    items(12) { index ->
                                        NoteCardSkeleton(
                                            modifier = Modifier.height(
                                                skeletonHeights[index % skeletonHeights.size]
                                            )
                                        )
                                    }
                                }
                            }
                            TrashContentState.Empty -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Trash is empty",
                                        fontSize = 18.sp,
                                        color = AppTheme.controlsForeground
                                    )
                                }
                            }
                            TrashContentState.Content -> {
                                val safeNotes = notes.orEmpty()
                                LazyVerticalStaggeredGrid(
                                    columns = columnCells,
                                    state = rememberLazyStaggeredGridState(),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalItemSpacing = 8.dp
                                ) {
                                    items(safeNotes.size, key = { safeNotes[it].id }) { index ->
                                        val note = safeNotes[index]
                                        NoteCard(
                                            noteId = note.id,
                                            noteTitle = note.title,
                                            noteContent = note.content,
                                            noteLastEditTimestamp = note.timestamp,
                                            isSelecting = isSelecting,
                                            isSelected = note.id in selectedIds,
                                            onClick = if (isSelecting) ({ toggle(note.id) }) else null,
                                            onLongClick = {
                                                isSelecting = true
                                                toggle(note.id)
                                            },
                                            onCheckedChange = {
                                                if (!isSelecting) {
                                                    isSelecting = true
                                                }
                                                toggle(note.id)
                                            },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BottomScrim(Modifier.align(Alignment.BottomCenter))
                }
            }
        }

        TopScrim(Modifier.align(Alignment.TopCenter))

        AnimatedContent(
            targetState = isSelecting,
            modifier = Modifier.fillMaxWidth(),
            label = "topBarSwap"
        ) { selecting ->
            if (selecting) {
                val safeNotes = notes.orEmpty()
                SelectedItemCountToolbar(
                    hazeState = hazeState,
                    count = selectedIds.size,
                    isChecked = safeNotes.isNotEmpty() && selectedIds.size == safeNotes.size,
                    onCheckedChange = { checked ->
                        selectedIds = if (checked) safeNotes.map { it.id }.toSet() else emptySet()
                    },
                    onClose = { exitSelection() }
                )
            } else {
                GradientTopBar(
                    hazeState = hazeState,
                    showScrim = false,
                    showBackButton = true,
                    onClose = onClose,
                    showMenu = false,
                    onShowMenuChange = { }
                )
            }
        }

        BottomActionBar(
            hazeState = hazeState,
            mode = ActionBarMode.TrashView,
            selectedCount = selectedIds.count(),
            visible = isSelecting,
        )
    }
}

@Composable
@Preview
fun PreviewTrashedNotesView() {
    AppTheme {
        TrashedNotesView(notes = null, onClose = {})
    }
}