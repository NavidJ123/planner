package com.example.planner.data.repository

import com.example.planner.data.db.AppDb
import com.example.planner.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import com.example.planner.data.generator.DaysOfWeekMask

class PlannerRepository(private val db: AppDb) {

    suspend fun nukeAll() {

        withContext(Dispatchers.IO) {
            db.specialEventDao().deleteAll()
            db.taskDao().deleteAllTasks()
            db.taskDao().deleteAllTemplates()
            db.meetingDao().deleteAllInstances()
            db.meetingDao().deleteAllSkips()
            db.meetingDao().deleteAllPatterns()
            db.courseDao().deleteAll()
        }
    }

    /**
     * Creates a course + meeting patterns.
     * Returns courseId.
     */
    suspend fun createCourse(
        name: String,
        colorArgb: Long,
        location: String?,
        isVirtual: Boolean,
        startDate: LocalDate,
        endDate: LocalDate,
        meetingPatterns: List<Triple<DayOfWeek, LocalTime, LocalTime>>,
    ): String = withContext(Dispatchers.IO) {

        val courseId = UUID.randomUUID().toString()

        db.courseDao().upsert(
            CourseEntity(
                id = courseId,
                name = name,
                colorArgb = colorArgb,
                location = location,
                isVirtual = isVirtual,
                startDate = startDate,
                endDate = endDate
            )
        )

        // store weekly patterns
        meetingPatterns.forEach { (dow, start, end) ->
            db.meetingDao().upsertPattern(
                CourseMeetingPatternEntity(
                    id = UUID.randomUUID().toString(),
                    courseId = courseId,
                    dayOfWeek = dow.value,
                    startTime = start,
                    endTime = end
                )
            )
        }

        // Generate instances only if NOT virtual and patterns exist
        if (!isVirtual && meetingPatterns.isNotEmpty()) {
            generateMeetingInstances(courseId, startDate, endDate)
        }

        courseId
    }

    /**
     * Generates meeting instances for a course from its patterns between [startDate, endDate].
     */
    suspend fun generateMeetingInstances(
        courseId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) = withContext(Dispatchers.IO) {

        val patterns = db.meetingDao().getPatternsForCourse(courseId)
        if (patterns.isEmpty()) return@withContext

        val instances = mutableListOf<CourseMeetingInstanceEntity>()

        var d = startDate
        while (!d.isAfter(endDate)) {
            val dow = d.dayOfWeek.value
            patterns.filter { it.dayOfWeek == dow }.forEach { p ->
                val startDt = LocalDateTime.of(d, p.startTime)
                val endDt = LocalDateTime.of(d, p.endTime)
                instances.add(
                    CourseMeetingInstanceEntity(
                        id = UUID.randomUUID().toString(),
                        courseId = courseId,
                        patternId = p.id,
                        startDateTime = startDt,
                        endDateTime = endDt,
                        isCancelled = false
                    )
                )
            }
            d = d.plusDays(1)
        }

        db.meetingDao().insertInstances(instances)
    }

    /**
     * Inserts template + generates instances.
     */
    suspend fun createTaskTemplateAndGenerate(
        template: TaskTemplateEntity,
        generateAheadWeeks: Int
    ) = withContext(Dispatchers.IO) {
        db.taskDao().upsertTemplate(template)
        generateTaskInstancesFromTemplate(template, generateAheadWeeks)
    }

    /**
     * Generates TaskInstanceEntity entries for the template.
     * Fix: Prevents duplicate generation by checking existing instance dates.
     */
    suspend fun generateTaskInstancesFromTemplate(
        template: TaskTemplateEntity,
        generateAheadWeeks: Int
    ) = withContext(Dispatchers.IO) {

        val endGen = template.startDate.plusWeeks(generateAheadWeeks.toLong())
            .coerceAtMost(template.endDate)

        val anchors = when (template.freq) {
            RecurrenceFreq.NONE -> listOf(template.startDate)

            RecurrenceFreq.WEEKLY -> {
                val wantedDows = DaysOfWeekMask.toDays(template.daysOfWeekMask)
                val out = mutableListOf<LocalDate>()
                var d = template.startDate
                while (!d.isAfter(endGen)) {
                    if (wantedDows.contains(d.dayOfWeek)) out.add(d)
                    d = d.plusDays(1)
                }
                out
            }

            else -> {
                // DAILY, BIWEEKLY, etc. can be implemented here later.
                emptyList()
            }
        }

        if (anchors.isEmpty()) return@withContext

        // Fix: Fetch existing instances to avoid duplicates on re-generation
        val existingDates = db.taskDao().getInstancesForTemplate(template.id)
            .map { it.startDate }
            .toSet()

        val newInstances = anchors
            .filter { anchor -> !existingDates.contains(anchor) }
            .map { anchor ->
                val due = anchor.plusDays(template.dueOffsetDays.toLong())
                TaskInstanceEntity(
                    id = UUID.randomUUID().toString(),
                    templateId = template.id,
                    title = template.title,
                    courseId = template.courseId,
                    priority = template.priority,
                    startDate = anchor,
                    endDate = anchor,
                    dueDate = due,
                    status = TaskStatus.TODO,
                    overrideColorArgb = null,
                    reminderAt = null
                )
            }

        if (newInstances.isNotEmpty()) {
            db.taskDao().insertInstances(newInstances)
        }
    }

    suspend fun createOneOffTask(task: TaskInstanceEntity) = withContext(Dispatchers.IO) {
        db.taskDao().insertInstances(listOf(task))
    }

    suspend fun setTaskStatus(taskId: String, status: TaskStatus) = withContext(Dispatchers.IO) {
        db.taskDao().updateStatus(taskId, status)
    }

    suspend fun updateTask(task: TaskInstanceEntity) = withContext(Dispatchers.IO) {
        db.taskDao().update(task)
    }

    suspend fun deleteCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        db.courseDao().delete(course)
    }
}