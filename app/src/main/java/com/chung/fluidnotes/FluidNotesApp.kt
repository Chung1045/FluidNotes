package com.chung.fluidnotes

import android.app.Application
import androidx.room3.Room
import androidx.window.embedding.RuleController
import com.chung.fluidnotes.data.AppDatabase
import com.chung.fluidnotes.data.AppThemeMode
import com.chung.fluidnotes.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.jvm.java
import kotlin.properties.Delegates

class FluidNotesApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "fluidnotes_database")
            .build()
    }

    val settingsStore by lazy { SettingsStore(this) }
    var initialFollowSystem by Delegates.notNull<Boolean>()
    lateinit var initialStyle: AppThemeMode

    override fun onCreate() {
        super.onCreate()
        RuleController.getInstance(this)
            .setRules(RuleController.parseRules(this, R.xml.split_config))
        runBlocking {
            initialFollowSystem = settingsStore.followSystemTheme.first()
            initialStyle = settingsStore.themeStyle.first()
        }
    }
}