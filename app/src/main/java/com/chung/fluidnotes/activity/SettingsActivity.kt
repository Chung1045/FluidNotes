package com.chung.fluidnotes.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chung.fluidnotes.ui.theme.AppTheme
import com.chung.fluidnotes.views.SettingsView

class SettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SettingsView(
                    onClose = {
                        finish()
                    }
                )
            }
        }
    }
}