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
import com.example.planner.data.entity.*
import com.example.planner.data.repository.PlannerRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import com.example.planner.data.generator.DaysOfWeekMask

@Composable
fun BackendMvpScreen(appDb: AppDb) {
    val scope = rememberCoroutineScope()
    val repo = remember(appDb) { PlannerRepository(appDb) }

    val today = remember { LocalDate.now() }
    val rangeStart = remember(today) { today.atStartOfDay() }
    val rangeEnd = remember(today) { today.plusDays(7).atStartOfDay() }

    val meetingInstances: List<CourseMeetingInstanceEntity> by appDb.meetingDao()
        .observeInstancesInRange(rangeStart, rangeEnd)
        .collectAsState(initial = emptyList())

    val taskInstances: List<TaskInstanceEntity> by appDb.taskDao()
        .observeTasksInDateRange(today, today.plusDays(7))
        .collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Backend MVP", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        // Seed a sample course + repeating HW template
                        val courseId = repo.createCourse(
                            name = "CS 310",
                            colorArgb = 0xFF4FA3FF,
                            location = "ENG 110",
                            isVirtual = false,
                            startDate = today.minusDays(7),
                            endDate = today.plusMonths(4),
                            meetingPatterns = listOf(
                                Triple(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 45)),
                                Triple(DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 45))
                            )
                        )

                        val template = TaskTemplateEntity(
                            id = UUID.randomUUID().toString(),
                            title = "HW",
                            courseId = courseId,
                            priority = 3,
                            startDate = today.minusDays(7),
                            endDate = today.plusMonths(4),
                            freq = RecurrenceFreq.WEEKLY,
                            interval = 1,
                            daysOfWeekMask = DaysOfWeekMask.of(DayOfWeek.MONDAY),
                            dueOffsetDays = 4, // due = anchor + 4 days
                            reminderOffsetMinutes = null,
                            isAllDayBar = true
                        )

                        repo.createTaskTemplateAndGenerate(template, generateAheadWeeks = 8)
                    }
                }
            ) { Text("Seed sample data") }

            OutlinedButton(
                onClick = { scope.launch { repo.nukeAll() } }
            ) { Text("Clear DB") }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.fillMaxSize()) {

            item {
                Text("Meetings (next 7 days)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(
                items = meetingInstances,
                key = { it.id }
            ) { m: CourseMeetingInstanceEntity ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${m.startDateTime.toLocalDate()}  ${m.startDateTime.toLocalTime()}–${m.endDateTime.toLocalTime()}")
                        Text("courseId=${m.courseId}", color = Color.Gray)
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text("Tasks / Assignments (next 7 days)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(
                items = taskInstances,
                key = { it.id }
            ) { t: TaskInstanceEntity ->
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
                        Text("Status: ${t.status}  (tap to toggle)", color = Color.Gray)
                    }
                }
            }
        }
    }
}
