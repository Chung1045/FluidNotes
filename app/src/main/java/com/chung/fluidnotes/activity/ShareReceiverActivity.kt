package com.chung.fluidnotes.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chung.fluidnotes.FluidNotesApp
import com.chung.fluidnotes.R
import com.chung.fluidnotes.data.NoteItem
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.views.ShareSheetView
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.attributes = window.attributes.apply {
            blurBehindRadius = 60
        }

        val isSendTextIntent = intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain"

        val sharedTitle = if (isSendTextIntent) {
            intent.getStringExtra(Intent.EXTRA_SUBJECT)
                ?: intent.getStringExtra(Intent.EXTRA_TITLE)
                ?: intent.clipData?.description?.label?.toString()
                ?: ""
        } else ""

        val sharedText = if (isSendTextIntent) {
            intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        } else ""

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    ShareSheetView(
                        initialTitle = sharedTitle,
                        initialText = sharedText,
                        onCancel = { finish() },
                        onSave = { title, content ->
                            saveNoteAndFinish(title, content)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 18.dp)
                    )
                }
            }
        }
    }

    private fun saveNoteAndFinish(title: String, content: String) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()

        if (trimmedTitle.isEmpty() && trimmedContent.isEmpty()) {
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = (application as FluidNotesApp).database

            val now = System.currentTimeMillis()
            val note = NoteItem(
                title = trimmedTitle,
                content = trimmedContent,
                timestamp = now,
                lastEdit = now,
                isTrashed = false
            )

            db.notesDao().insertNote(note)

            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}


@Composable
@Preview
fun ShareScreenSheetPreview2() {
    val hazeState = rememberHazeState()

    AppTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)) {
            Image(
                painter = painterResource(id = R.drawable.test_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            ShareSheetView(
                initialTitle = "New Note",
                initialText = "https://www.google.com",
                onCancel = {},
                onSave = { _, _ -> },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}