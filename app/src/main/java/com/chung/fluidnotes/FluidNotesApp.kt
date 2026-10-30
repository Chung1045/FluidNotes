package com.chung.fluidnotes

import android.app.Application
import androidx.room3.Room
import androidx.window.embedding.RuleController
import com.chung.fluidnotes.data.AppDatabase
import kotlin.jvm.java

class FluidNotesApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "fluidnotes_database")
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        RuleController.getInstance(this)
            .setRules(RuleController.parseRules(this, R.xml.split_config))
    }
}