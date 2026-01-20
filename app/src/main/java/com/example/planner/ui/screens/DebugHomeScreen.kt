package com.example.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.planner.data.db.AppDb

@Composable
fun DebugHomeScreen(appDb: AppDb) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Backend MVP", "Quick Create")

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (tab) {
            0 -> BackendMvpScreen(appDb)
            1 -> QuickCreateScreen(appDb)
        }
    }
}
