package com.example.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planner.data.db.AppDb
import com.example.planner.data.entity.CourseEntity
import com.example.planner.data.entity.TaskInstanceEntity
import com.example.planner.data.repository.PlannerRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(appDb: AppDb) {
    val repo = remember(appDb) { PlannerRepository(appDb) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Fetch data for the whole month
    val startOfMonth = currentMonth.atDay(1)
    val endOfMonth = currentMonth.atEndOfMonth()

    val tasks by appDb.taskDao().observeTasksInDateRange(startOfMonth, endOfMonth)
        .collectAsState(initial = emptyList())

    val courses by appDb.courseDao().observeCourses()
        .collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize()) {
        // --- Month Header ---
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, null)
            }
            Text(
                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, null)
            }
        }

        // --- Days of Week Header ---
        Row(Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Calendar Grid ---
        val daysInMonth = startOfMonth.lengthOfMonth()
        val firstDayOfWeek = startOfMonth.dayOfWeek.value % 7 // adjust so Sun=0
        val totalCells = daysInMonth + firstDayOfWeek

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {
            // Empty cells for offset
            items(firstDayOfWeek) { Box(Modifier.size(40.dp)) }

            // Actual days
            items(daysInMonth) { index ->
                val date = startOfMonth.plusDays(index.toLong())
                val isToday = date == LocalDate.now()
                val isSelected = date == selectedDate

                // Find tasks for this day
                val dayTasks = tasks.filter { it.dueDate == date }

                Column(
                    modifier = Modifier
                        .aspectRatio(0.8f) // Taller cells like Google Cal
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedDate = date }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Date Number
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.White,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )

                    Spacer(Modifier.height(4.dp))

                    // Dots for tasks
                    dayTasks.take(3).forEach { task ->
                        val color = getTaskColor(task, courses)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }

        Divider(color = Color.DarkGray)

        // --- Bottom Sheet: Agenda for Selected Date ---
        Text(
            "Schedule for $selectedDate",
            Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        val selectedTasks = tasks.filter { it.dueDate == selectedDate }

        LazyColumn(Modifier.weight(0.8f).padding(horizontal = 16.dp)) {
            if (selectedTasks.isEmpty()) {
                item { Text("No plans", color = Color.Gray) }
            }
            items(selectedTasks) { task ->
                val course = courses.find { it.id == task.courseId }
                val color = course?.let { Color(it.colorArgb) } ?: MaterialTheme.colorScheme.secondary

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color Line
                    Box(Modifier.width(4.dp).height(40.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(task.title, style = MaterialTheme.typography.bodyLarge)
                        if (course != null) {
                            Text(course.name, style = MaterialTheme.typography.labelSmall, color = color)
                        }
                    }
                }
            }
        }
    }
}

fun getTaskColor(task: TaskInstanceEntity, courses: List<CourseEntity>): Color {
    val course = courses.find { it.id == task.courseId }
    return if (course != null) Color(course.colorArgb) else Color(0xFFCCC2DC)
}