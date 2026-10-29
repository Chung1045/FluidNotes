package com.chung.fluidnotes.views.components

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chung.fluidnotes.activity.NoteEditViewActivity
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.utils.Utils

@Composable
fun NoteCard(
    noteId: Int = 0,
    noteTitle: String = "",
    noteContent: String = "",
    noteLastEditTimestamp: Long = -1,
    isSelecting: Boolean = false,
    isSelected: Boolean = false,
    isTrashed: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onCheckedChange: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = MaterialTheme.shapes.medium
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val openNote = {
        context.startActivity(
            Intent(context, NoteEditViewActivity::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("is_existing_note", true)
                putExtra("is_trashed_view", isTrashed)
            }
        )
    }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) AppTheme.controlsTint else MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.4f
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick ?: openNote,
                onLongClick = onLongClick
            )
            .hoverable(interactionSource = interactionSource)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (noteTitle.isNotEmpty()) {
                    // Note Title
                    Text(
                        text = noteTitle,
                        modifier = Modifier.padding(end = if (isSelecting) 32.dp else 0.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (noteContent.isNotEmpty()) {
                    // Body Preview
                    Text(
                        text = noteContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (noteLastEditTimestamp != -1L) {
                    // Timestamp
                    Text(
                        text = Utils.formatRelativeTime(noteLastEditTimestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelecting || isHovered,
                modifier = Modifier.align(Alignment.TopEnd),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onCheckedChange?.invoke() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppTheme.controlsTint,
                        uncheckedColor = Color.White,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun NoteCardPreview() {
    NoteCard()
}