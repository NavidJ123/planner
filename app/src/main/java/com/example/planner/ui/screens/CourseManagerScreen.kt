package com.example.planner.ui.screens
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.planner.data.db.AppDb
import com.example.planner.data.repository.PlannerRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CourseManagerScreen(appDb: AppDb) {
    val scope = rememberCoroutineScope()
    val repo = remember(appDb) { PlannerRepository(appDb) }

    // Fetch courses
    val courses by appDb.courseDao().observeCourses().collectAsState(initial = emptyList())

    // UI State for Dialog
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Class")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("My Classes", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            if (courses.isEmpty()) {
                Text("No classes added yet.", color = Color.Gray)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(courses) { course ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Color Dot
                                Box(
                                    Modifier
                                        .size(12.dp)
                                        .background(Color(course.colorArgb), CircleShape)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(course.name, style = MaterialTheme.typography.titleMedium)
                            }
                            IconButton(onClick = { scope.launch { repo.deleteCourse(course) } }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, color ->
                scope.launch {
                    repo.createCourse(
                        name = name,
                        colorArgb = color.value.toLong(),
                        location = null,
                        isVirtual = false,
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusMonths(4), // Default semester length
                        meetingPatterns = emptyList() // MVP: Add patterns later if needed
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onSave: (String, Color) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF4FA3FF)) } // Default Blue
    val colors = listOf(
        Color(0xFF4FA3FF), // Blue
        Color(0xFFE74C3C), // Red
        Color(0xFF2ECC71), // Green
        Color(0xFFF1C40F), // Yellow
        Color(0xFF9B59B6)  // Purple
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Class") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Class Name (e.g. CS 101)") }
                )
                Spacer(Modifier.height(16.dp))
                Text("Color Label:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .clickable { selectedColor = color }
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onSave(name, selectedColor) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}