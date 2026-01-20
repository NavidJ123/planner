package com.example.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.example.planner.data.db.AppDb
import com.example.planner.ui.screens.DebugHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDb::class.java,
            "planner.db"
        ).build()

        setContent {
            DebugHomeScreen(appDb = db)
        }
    }
}
