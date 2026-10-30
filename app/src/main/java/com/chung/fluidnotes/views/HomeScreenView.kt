package com.chung.fluidnotes.views

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chung.fluidnotes.R
import com.chung.fluidnotes.activity.NoteEditViewActivity
import com.chung.fluidnotes.data.NoteItem
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.views.components.BottomActionBar
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.HomeDropMenu
import com.chung.fluidnotes.views.components.NoteCard
import com.chung.fluidnotes.views.components.SelectedItemCountToolbar
import com.chung.fluidnotes.views.components.TopScrim
import com.chung.fluidnotes.views.components.gradientTopBarHeight
import com.chung.fluidnotes.views.dialogs.TrashDialog
import dev.chrisbanes.haze.HazeState
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
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val isLargeDevice = Utils.isLargeDevice()
    val columnCells = remember(context) {
        if (isLargeDevice) {
            StaggeredGridCells.Adaptive(200.dp)
        } else {
            StaggeredGridCells.Adaptive(160.dp)
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showTrashDialog by remember {mutableStateOf(false)}

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
        selectedIds = selectedIds intersect notes.map { it.id }.toSet()
        if (notes.isEmpty()) isSelecting = false
    }

    val navBarInset = WindowInsets.navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()

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
                    visible = !isSelecting,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    HazeFAB(
                        modifier = Modifier.padding(bottom = navBarInset),
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
                                    items = notes,
                                    key = { it.id }
                                ) { note ->
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
            targetState = isSelecting,
            modifier = Modifier.fillMaxWidth(),
            label = "topBarSwap"
        ) { selecting ->
            if (selecting) {
                SelectedItemCountToolbar(
                    hazeState = hazeState,
                    count = selectedIds.size,
                    isChecked = notes.isNotEmpty() && selectedIds.size == notes.size,
                    onCheckedChange = { checked ->
                        selectedIds = if (checked) notes.map { it.id }.toSet() else emptySet()
                    },
                    onClose = { exitSelection() }
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
                            onHelpClick = { menuExpanded = false },
                            onTrashedNotesClick = {
                                menuExpanded = false
                                context.startActivity(
                                    Intent(
                                        context,
                                        TrashedNotesActivity::class.java
                                    )
                                )
                            }
                        )
                    }
                )
            }
        }

        if (showTrashDialog) {
            TrashDialog(
                count = selectedIds.count(),
                onDismissRequest = { showTrashDialog = false },
                onConfirmation = {
                    onTrashNotes(selectedIds.toList())
                    showTrashDialog = false
                    exitSelection()
                }
            )
        }

        BottomActionBar(
            hazeState = hazeState,
            selectedCount = selectedIds.count(),
            visible = isSelecting,
            onTrashClick = {
                showTrashDialog = true
            }
        )
    }
}

@Composable
fun NoteCardSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Title placeholder bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .background(
                        color = placeholderColor.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    )
            )
            // Content placeholder lines
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .background(
                        color = placeholderColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .background(
                        color = placeholderColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {},
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { menuExpanded = false })
                    DropdownMenuItem(
                        text = { Text("Help & Feedback") },
                        onClick = { menuExpanded = false })
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TestAppBarPreview() {
    val hazeState = remember { HazeState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.test_photo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        )

        AppTopBar(scrollBehavior = scrollBehavior)
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