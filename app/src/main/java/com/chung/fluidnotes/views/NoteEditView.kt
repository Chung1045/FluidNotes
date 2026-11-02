package com.chung.fluidnotes.views

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.window.embedding.SplitController
import androidx.window.layout.WindowMetricsCalculator
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils
import com.chung.fluidnotes.utils.isLargeDevice
import com.chung.fluidnotes.views.components.GradientTopBar
import com.chung.fluidnotes.views.components.dropmenu.NoteEditDropMenu
import com.chung.fluidnotes.views.components.gradientTopBarHeight
import com.chung.fluidnotes.views.dialogs.DeleteDialog
import com.chung.fluidnotes.views.dialogs.TrashDialog
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration.Companion.milliseconds

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Mirrors split_config.xml: min width/smallest width 600dp, no split in tall portrait (aspect > 1.4). */
private fun displayCanSplit(activity: Activity): Boolean {
    val bounds = WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(activity).bounds
    val density = activity.resources.displayMetrics.density
    val w = bounds.width() / density
    val h = bounds.height() / density
    val result = minOf(w, h) >= 600f && (w >= h || h / w <= 1.4f)
    return result
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun rememberIsInSplit(): Boolean {
    if (LocalInspectionMode.current) return false
    val activity = LocalContext.current.findActivity() ?: return false

    val predicted = remember(activity) { displayCanSplit(activity) }
    val inSplit by remember(activity) {
        SplitController.getInstance(activity)
            .splitInfoList(activity)
            .map { splits ->
                val inSplit = splits.any { it.contains(activity) }
                inSplit
            }
            .transformLatest { inSplit ->
                // The first emission can arrive before the split exists.
                // Only believe "not in a split" if it stays that way.
                if (!inSplit) delay(300)
                emit(inSplit)
            }
    }.collectAsState(initial = predicted)
    return inSplit && predicted
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
    onDeleteConfirmed: () -> Unit = {},
    onRecoverClick: () -> Unit = {},
    onBackPressed: () -> Unit,
    onShowMenuChange: (Boolean) -> Unit,
    showMenu: Boolean,
) {

    val scrollState = rememberScrollState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current

    var isContentFocused by remember { mutableStateOf(false) }
    var columnHeightPx by remember { mutableIntStateOf(0) }
    var contentTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val contentBringIntoViewRequester = remember { BringIntoViewRequester() }

    // Boolean only: reading the raw pixel value recomposed the editor every keyboard frame
    val imeInsets = WindowInsets.ime
    val imeVisible by remember { derivedStateOf { imeInsets.getBottom(density) > 0 } }
    val navBarInset = WindowInsets.navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()
    val baseScrimHeight = if (isLargeDevice()) 20.dp else 80.dp

    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showTrashDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    val detectedLinks = remember(noteContent.text) {
        Utils.extractLinks(noteContent.text)
    }

    val linkColor = AppTheme.controlsTint // read here, in @Composable context
    val linkTransformation = remember(linkColor) { LinkHighlightTransformation(linkColor) }

    val isInSplit = rememberIsInSplit()

    LaunchedEffect(isContentFocused, imeVisible, noteContent.selection, contentTextLayout) {
        if (!isContentFocused || !imeVisible) return@LaunchedEffect
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
            withFrameNanos { }   // wait for the first frame instead of a fixed 100ms
            titleFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isInSplit) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { columnHeightPx = it.size.height }
                .hazeSource(hazeState)
                .background(if (isInSplit) MaterialTheme.colorScheme.surfaceContainerLow
                else MaterialTheme.colorScheme.surface)
                .verticalScroll(scrollState)
                .padding(top = gradientTopBarHeight())
                .padding(horizontal = 16.dp)
        ) {
            if (isTrashedView) {
                Text(text = "Read only",
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

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
                                text = if (!isExistingNote) "New Note Title" else "Title",
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
                fontSize = 12.sp,
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
                    .padding(top = 12.dp)
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
            onClose = {
                keyboardController?.hide()
                onBackPressed()
            },
            isInSplit = isInSplit,
            menuContent = {
                NoteEditDropMenu(
                    hazeState = hazeState,
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) },
                    isTrashedView = isTrashedView,
                    onTrashClick = { showTrashDialog = true },
                    onRecoverClick = onRecoverClick,
                    onDeleteClick = { showDeleteDialog = true }
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

        if (showDeleteDialog) {
            DeleteDialog(
                onDismissRequest = { showDeleteDialog = false },
                onConfirmation = {
                    showDeleteDialog = false
                    onDeleteConfirmed()
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