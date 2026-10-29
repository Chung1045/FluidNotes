package com.chung.fluidnotes.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.NoteCard
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

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
                    when {
                        notes == null -> {
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
                        notes.isEmpty() -> {
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
                        else -> {
                            LazyVerticalStaggeredGrid(
                                columns = columnCells,
                                state = rememberLazyStaggeredGridState(),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                items(notes.size, key = { notes[it].id }) { index ->
                                    val note = notes[index]
                                    NoteCard(
                                        noteId = note.id,
                                        noteTitle = note.title,
                                        noteContent = note.content,
                                        noteLastEditTimestamp = note.timestamp,
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        GradientTopBar(
            hazeState = hazeState,
            showBackButton = true,
            onClose = onClose,
            showMenu = false,
            onShowMenuChange = { }
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