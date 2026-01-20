package com.example.planner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.planner.data.db.AppDb
import com.example.planner.data.entity.TaskInstanceEntity
import com.example.planner.data.entity.TaskStatus
import com.example.planner.data.repository.PlannerRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@Composable
fun QuickCreateScreen(appDb: AppDb) {
    val scope = rememberCoroutineScope()
    val repo = remember(appDb) { PlannerRepository(appDb) }
    val today = remember { LocalDate.now() }

    // Courses list (simple)
    val courses by appDb.courseDao()
        .observeCourses()
        .collectAsState(initial = emptyList())

    // Tasks list (show next 30 days)
    val tasks by appDb.taskDao()
        .observeTasksInDateRange(today.minusDays(7), today.plusDays(30))
        .collectAsState(initial = emptyList())

    // UI state
    var courseName by remember { mutableStateOf("CS 367") }
    var isVirtual by remember { mutableStateOf(false) }
    var courseColorArgb by remember { mutableStateOf(0xFF7C4DFFL) } // purple-ish

    var taskTitle by remember { mutableStateOf("HW 1") }
    var taskPriority by remember { mutableStateOf(3) }
    var startDateStr by remember { mutableStateOf(today.toString()) }
    var dueOffsetDays by remember { mutableStateOf(4) }
    var selectedCourseId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quick Create (build off this)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // ----- Create Course -----
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create Course", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isVirtual, onCheckedChange = { isVirtual = it })
                    Text("Virtual (don’t generate meeting instances)")
                }

                // MVP “color picker”: just a few presets
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetColorChip("Blue", 0xFF4FA3FFL, courseColorArgb) { courseColorArgb = it }
                    PresetColorChip("Purple", 0xFF7C4DFFL, courseColorArgb) { courseColorArgb = it }
                    PresetColorChip("Green", 0xFF2ECC71L, courseColorArgb) { courseColorArgb = it }
                    PresetColorChip("Red", 0xFFE74C3CL, courseColorArgb) { courseColorArgb = it }
                }

                Button(
                    onClick = {
                        scope.launch {
                            val id = repo.createCourse(
                                name = courseName.trim().ifEmpty { "Untitled" },
                                colorArgb = courseColorArgb,
                                location = null,
                                isVirtual = isVirtual,
                                startDate = today.minusDays(7),
                                endDate = today.plusMonths(4),
                                meetingPatterns = emptyList() // this screen doesn’t do meeting patterns yet
                            )
                            selectedCourseId = id
                        }
                    }
                ) { Text("Add course") }

                if (courses.isNotEmpty()) {
                    Text("Courses:", color = Color.Gray)
                    courses.forEach { c ->
                        Text(
                            text = "• ${c.name}  (id=${c.id.take(6)}...)",
                            modifier = Modifier.clickable { selectedCourseId = c.id },
                            color = if (selectedCourseId == c.id) Color.Unspecified else Color.Gray
                        )
                    }
                    if (selectedCourseId == null) {
                        selectedCourseId = courses.first().id
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ----- Create One-off Task -----
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create One-off Task", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDateStr,
                    onValueChange = { startDateStr = it },
                    label = { Text("Start date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskPriority.toString(),
                        onValueChange = { v -> taskPriority = v.toIntOrNull()?.coerceIn(1, 5) ?: taskPriority },
                        label = { Text("Priority 1-5") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueOffsetDays.toString(),
                        onValueChange = { v -> dueOffsetDays = v.toIntOrNull() ?: dueOffsetDays },
                        label = { Text("Due offset days") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val start = runCatching { LocalDate.parse(startDateStr.trim()) }.getOrElse { today }
                            val due = start.plusDays(dueOffsetDays.toLong())

                            repo.createOneOffTask(
                                TaskInstanceEntity(
                                    id = UUID.randomUUID().toString(),
                                    templateId = null,
                                    title = taskTitle.trim().ifEmpty { "Untitled task" },
                                    courseId = selectedCourseId,
                                    priority = taskPriority.coerceIn(1, 5),
                                    startDate = start,
                                    endDate = start,
                                    dueDate = due,
                                    status = TaskStatus.TODO,
                                    overrideColorArgb = null,
                                    reminderAt = null
                                )
                            )
                        }
                    }
                ) { Text("Add task") }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Tasks (tap to toggle DONE)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.fillMaxSize()) {
            items(items = tasks, key = { it.id }) { t: TaskInstanceEntity ->
                val isDone = t.status == TaskStatus.DONE
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            scope.launch {
                                val newStatus = if (isDone) TaskStatus.TODO else TaskStatus.DONE
                                repo.setTaskStatus(t.id, newStatus)
                            }
                        }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "${t.title}  (P${t.priority})",
                            color = if (isDone) Color.Gray else Color.Unspecified
                        )
                        Text("Start: ${t.startDate}  Due: ${t.dueDate}", color = Color.Gray)
                        Text("Course: ${t.courseId?.take(6) ?: "None"}", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetColorChip(
    label: String,
    argb: Long,
    selected: Long,
    onPick: (Long) -> Unit
) {
    val isSel = argb == selected
    AssistChip(
        onClick = { onPick(argb) },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSel) Color(0x22000000) else Color.Unspecified
        )
    )
}
