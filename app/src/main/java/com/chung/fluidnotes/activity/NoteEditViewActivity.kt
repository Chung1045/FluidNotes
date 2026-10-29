package com.chung.fluidnotes.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.data.NoteViewModel
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.views.NoteEditDropMenu
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.gradientTopBarHeight
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class NoteEditViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        val isExistingNote = intent.getBooleanExtra("is_existing_note", true)
        val noteId = intent.getIntExtra("note_id", 0)
        val dao = (application as FluidNotesApp).database.notesDao()

        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteViewModel(dao, noteId) as T
                }
            }
        )[NoteViewModel::class.java]

        setContent {
            AppTheme {
                val isDark = isSystemInDarkTheme()
                LaunchedEffect(isDark) {}

                val isTrashed by viewModel.isTrashed.collectAsState()
                val noteTitle by viewModel.title.collectAsState()
                val noteContent by viewModel.content.collectAsState()
                val noteLastEdit by viewModel.lastEdit.collectAsState()

                var showMenu by remember { mutableStateOf(false) }

                EditViewScreen(
                    isExistingNote = isExistingNote,
                    isTrashedView = isTrashed,
                    noteTitle = noteTitle,
                    noteContent = noteContent,
                    noteLastEdit = noteLastEdit,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                    onBackPressed = {
                        if (isTrashed) finish()
                        else viewModel.saveNote { finish() }
                    },
                    onTrashConfirmed = { viewModel.trashNote { finish() } },
                    onRecoverClick = { viewModel.restoreNote { finish() } },
                    onDeleteClick = { viewModel.deleteNote { finish() } },
                    onShowMenuChange = { showMenu = it },
                    showMenu = showMenu
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditViewScreen(
    isExistingNote: Boolean = true,
    isTrashedView: Boolean = false,
    noteTitle: String,
    noteContent: TextFieldValue,
    noteLastEdit: Long = System.currentTimeMillis(),
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onTrashConfirmed: () -> Unit = {},
    onRecoverClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onBackPressed: () -> Unit,
    onShowMenuChange: (Boolean) -> Unit,
    showMenu: Boolean,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current

    var isContentFocused by remember { mutableStateOf(false) }
    var columnHeightPx by remember { mutableIntStateOf(0) }
    var contentTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val contentBringIntoViewRequester = remember { BringIntoViewRequester() }

    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBarInset = WindowInsets.navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()
    val baseScrimHeight = if (Utils.isLargeDevice()) 20.dp else 80.dp

    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showTrashDialog by remember { mutableStateOf(false) }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    val detectedLinks = remember(noteContent.text) {
        Utils.extractLinks(noteContent.text)
    }

    val linkColor = AppTheme.controlsTint // read here, in @Composable context
    val linkTransformation = remember(linkColor) { LinkHighlightTransformation(linkColor) }

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK){

        }
    }

    LaunchedEffect(isContentFocused, imeBottomPx, noteContent.selection, contentTextLayout) {
        if (!isContentFocused || imeBottomPx <= 0) return@LaunchedEffect
        val layout = contentTextLayout ?: return@LaunchedEffect
        if (columnHeightPx <= 0) return@LaunchedEffect

        delay(80.milliseconds)

        val cursorOffset = noteContent.selection.end.coerceIn(0, layout.layoutInput.text.length)
        val cursorRect = layout.getCursorRect(cursorOffset)

        val halfViewport = columnHeightPx / 2f
        val expandedRect = Rect(
            left = cursorRect.left,
            top = cursorRect.top - halfViewport + cursorRect.height / 2f,
            right = cursorRect.right,
            bottom = cursorRect.bottom + halfViewport - cursorRect.height / 2f
        )

        contentBringIntoViewRequester.bringIntoView(expandedRect)
    }

    LaunchedEffect(isExistingNote) {
        if (!isExistingNote) {
            delay(100.milliseconds)
            titleFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { columnHeightPx = it.size.height }
                .hazeSource(hazeState)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(scrollState)
                .padding(top = gradientTopBarHeight())
                .padding(horizontal = 16.dp)
        ) {
            BasicTextField(
                value = noteTitle,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                interactionSource = titleInteractionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = noteTitle,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = false,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = titleInteractionSource,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        placeholder = {
                            Text(
                                text = "Title",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        container = {}
                    )
                },
                enabled = !isTrashedView
            )

            Text(
                text = Utils.formatAbsoluteDateTime(noteLastEdit),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            BasicTextField(
                value = noteContent,
                onValueChange = onContentChange,
                interactionSource = contentInteractionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { contentTextLayout = it },
                visualTransformation = remember { linkTransformation },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .bringIntoViewRequester(contentBringIntoViewRequester)
                    .onFocusChanged { isContentFocused = it.isFocused },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = noteContent.text,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = false,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = contentInteractionSource,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        placeholder = {
                            Text(
                                text = "Type your note here...",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        container = {}
                    )
                },
                enabled = !isTrashedView
            )

            LinkEntriesSection(
                links = detectedLinks,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        GradientTopBar(
            hazeState = hazeState,
            showBackButton = true,
            onClose = onBackPressed,
            menuContent = {
                NoteEditDropMenu(
                    hazeState = hazeState,
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) },
                    isTrashedView = isTrashedView,
                    onTrashClick = { showTrashDialog = true },
                    onRecoverClick = onRecoverClick,
                    onDeleteClick = onDeleteClick
                ) },
            showMenu = showMenu,
            onShowMenuChange = onShowMenuChange
        )

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

        if (showTrashDialog) {
            TrashDialog(
                onDismissRequest = { showTrashDialog = false },
                onConfirmation = {
                    showTrashDialog = false
                    onTrashConfirmed()
                }
            )
        }
    }
}

@Composable
fun LinkEntriesSection(
    links: List<String>,
    modifier: Modifier = Modifier
) {
    if (links.isEmpty()) return

    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        links.forEach { url ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable {
                        val fullUrl = if (!url.startsWith("http")) "https://$url" else url
                        val intent = Intent(Intent.ACTION_VIEW, fullUrl.toUri())
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TrashDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = "Trash Note") },
        text = { Text(text = "Are you sure you want to trash this note?") },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("No")
            }
        }
    )
}

// Auto text styling for URL
class LinkHighlightTransformation(
    private val linkColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val plain = text.text
        val annotated = buildAnnotatedString {
            append(text)
            for (url in Utils.extractLinks(plain)) {
                var start = plain.indexOf(url)
                while (start != -1) {
                    addStyle(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = start,
                        end = start + url.length
                    )
                    start = plain.indexOf(url, start + url.length)
                }
            }
        }
        // No characters added/removed — identity mapping is correct
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

@Composable
@Preview(showBackground = true, device = "id:pixel_9_pro_xl")
fun EditViewScreenPreview() {
    AppTheme {
        EditViewScreen(
            isExistingNote = true,
            noteTitle = "Sample Title",
            noteContent = TextFieldValue("Sample content..."),
            noteLastEdit = System.currentTimeMillis(),
            onTitleChange = {},
            onContentChange = {},
            onBackPressed = {},
            onShowMenuChange = {},
            showMenu = false
        )
    }
}

@Composable
@Preview(showBackground = true, device = "id:pixel_9_pro_xl")
fun EditViewScreenPreviewII() {
    AppTheme {
        EditViewScreen(
            isExistingNote = true,
            noteTitle = "Sample Title",
            noteContent = TextFieldValue("Sample content..."),
            noteLastEdit = System.currentTimeMillis(),
            onTitleChange = {},
            onContentChange = {},
            onBackPressed = {},
            onShowMenuChange = {},
            showMenu = true
        )
    }
}