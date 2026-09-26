package com.chung.fluidnotes.views.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.chung.fluidnotes.activity.NoteEditViewActivity
import com.chung.fluidnotes.utils.Utils

@Composable
fun NoteCard(
    noteId: Int = 0,
    noteTitle: String = "",
    noteContent: String = "",
    noteLastEditTimestamp: Long = -1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        onClick = {
            val intent = Intent(context, NoteEditViewActivity::class.java)
            intent.putExtra("note_id", noteId)
            intent.putExtra("is_existing_note", true)
            context.startActivity(intent)
        }
    ) {
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (noteContent.isNotEmpty()) {
                // Body Preview
                Text(
                    text = noteContent,
                    style = MaterialTheme.typography.bodyMedium, // Balanced default line height for body text
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
    }
}

@Preview
@Composable
fun NoteCardPreview() {
    NoteCard()
}