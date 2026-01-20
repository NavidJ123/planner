package com.example.planner.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border   // <--- IMPORTANT: Fixes the border error
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.planner.data.db.AppDb
import com.example.planner.data.entity.TaskInstanceEntity
import com.example.planner.data.entity.TaskStatus
import com.example.planner.data.repository.CalendarRepository
import com.example.planner.data.repository.PlannerRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@Composable
fun TodoListScreen(appDb: AppDb) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Repositories
    val repo = remember(appDb) { PlannerRepository(appDb) }
    val calendarRepo = remember(context) { CalendarRepository(context) }

    // State
    val today = remember { LocalDate.now() }
    val tasks by appDb.taskDao()
        .observeTasksInDateRange(today.minusMonths(1), today.plusMonths(1))
        .collectAsState(initial = emptyList())

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.WRITE_CALENDAR] == true
        if (granted) Toast.makeText(context, "Calendar Sync Active", Toast.LENGTH_SHORT).show()
    }

    // Request permissions on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val newId = UUID.randomUUID().toString()
                        val taskTitle = "New Task"
                        val taskDate = LocalDate.now()

                        // 1. Save to DB
                        repo.createOneOffTask(
                            TaskInstanceEntity(
                                id = newId,
                                templateId = null,
                                title = taskTitle,
                                courseId = null,
                                priority = 1,
                                startDate = taskDate,
                                endDate = taskDate,
                                dueDate = taskDate,
                                status = TaskStatus.TODO,
                                overrideColorArgb = null,
                                reminderAt = null
                            )
                        )

                        // 2. Sync to Calendar
                        calendarRepo.exportTaskToCalendar(
                            title = taskTitle,
                            description = "Created from Planner App",
                            startDateTime = taskDate.atTime(9, 0),
                            endDateTime = taskDate.atTime(10, 0)
                        )

                        Toast.makeText(context, "Task added & synced!", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "My Tasks",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            val overdue = tasks.filter { it.dueDate.isBefore(today) && it.status != TaskStatus.DONE }
            val todaysTasks = tasks.filter { it.dueDate.isEqual(today) }
            val upcoming = tasks.filter { it.dueDate.isAfter(today) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                if (overdue.isNotEmpty()) {
                    item { SectionHeader("Overdue", Color(0xFFFF5252)) }
                    items(overdue) { t -> TaskRow(t, repo) }
                }

                item { SectionHeader("Today", MaterialTheme.colorScheme.primary) }
                if (todaysTasks.isEmpty()) {
                    item { EmptyState("No tasks for today. Relax!") }
                } else {
                    items(todaysTasks) { t -> TaskRow(t, repo) }
                }

                if (upcoming.isNotEmpty()) {
                    item { SectionHeader("Upcoming", Color.Gray) }
                    items(upcoming) { t -> TaskRow(t, repo) }
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: TaskInstanceEntity, repo: PlannerRepository) {
    val scope = rememberCoroutineScope()
    val isDone = task.status == TaskStatus.DONE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable {
                scope.launch {
                    val newStatus = if (isDone) TaskStatus.TODO else TaskStatus.DONE
                    repo.setTaskStatus(task.id, newStatus)
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox with Border
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .border( // <--- This should now work
                    2.dp,
                    if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                color = if (isDone) Color.Gray else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.dueDate.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                if (task.priority <= 2) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "High Priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun EmptyState(msg: String) {
    Text(
        text = msg,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}