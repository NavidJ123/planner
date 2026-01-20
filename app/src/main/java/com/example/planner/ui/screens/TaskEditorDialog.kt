package com.example.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.planner.data.entity.CourseEntity
import com.example.planner.data.entity.TaskInstanceEntity
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    task: TaskInstanceEntity,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (TaskInstanceEntity) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var priority by remember { mutableStateOf(task.priority) }
    var selectedCourseId by remember { mutableStateOf(task.courseId) }

    // Simple Date Parser (MVP)
    var dateStr by remember { mutableStateOf(task.dueDate.toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Edit Task", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Course Selector
            Text("Assign to Class:", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // "None" option
                FilterChip(
                    selected = selectedCourseId == null,
                    onClick = { selectedCourseId = null },
                    label = { Text("None") }
                )

                // Existing Courses
                courses.forEach { course ->
                    FilterChip(
                        selected = selectedCourseId == course.id,
                        onClick = { selectedCourseId = course.id },
                        label = { Text(course.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(course.colorArgb).copy(alpha = 0.3f),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date & Priority Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = priority.toString(),
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) priority = it.toIntOrNull() ?: 1
                    },
                    label = { Text("Priority") },
                    modifier = Modifier.width(100.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Delete Task")
                }

                Button(onClick = {
                    val newDate = try { LocalDate.parse(dateStr) } catch(e: Exception) { task.dueDate }

                    val updatedTask = task.copy(
                        title = title,
                        priority = priority,
                        courseId = selectedCourseId,
                        dueDate = newDate,
                        startDate = newDate // Sync start/due for MVP
                    )
                    onSave(updatedTask)
                }) {
                    Text("Save Changes")
                }
            }
        }
    }
}