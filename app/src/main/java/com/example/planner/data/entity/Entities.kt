package com.example.planner.data.entity

import androidx.room.*
import java.time.*

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val colorArgb: Long,        // store color as ARGB Long
    val location: String?,
    val isVirtual: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@Entity(
    tableName = "course_meeting_patterns",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class CourseMeetingPatternEntity(
    @PrimaryKey val id: String,     // UUID
    val courseId: String,
    val dayOfWeek: Int,             // 1=Mon ... 7=Sun (java.time.DayOfWeek)
    val startTime: LocalTime,
    val endTime: LocalTime
)

/**
 * Skip a single occurrence of a course meeting (holiday, cancellation).
 */
@Entity(
    tableName = "course_meeting_skips",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId"), Index("date")]
)
data class CourseMeetingSkipEntity(
    @PrimaryKey val id: String, // UUID
    val courseId: String,
    val date: LocalDate,
    val reason: String?
)

/**
 * Generated instances that the calendar UI renders.
 * If a course is virtual, you simply do not generate instances for it.
 */
@Entity(
    tableName = "course_meeting_instances",
    indices = [Index("courseId"), Index("startDateTime"), Index("endDateTime")]
)
data class CourseMeetingInstanceEntity(
    @PrimaryKey val id: String,      // UUID
    val courseId: String,
    val patternId: String,           // which weekly pattern created it
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val isCancelled: Boolean = false,
    val overrideTitle: String? = null,
    val overrideLocation: String? = null
)

/** ---------- Tasks / Assignments ---------- **/

enum class TaskStatus { TODO, DOING, DONE }

enum class RecurrenceFreq { NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY }

/**
 * Template for repeating assignments (HW weekly, etc).
 * If non-repeating: freq = NONE and you can create a single instance directly.
 */
@Entity(tableName = "task_templates", indices = [Index("courseId")])
data class TaskTemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val courseId: String?,      // null = general task
    val priority: Int,          // 1-5

    // Recurrence window (when anchors can be generated)
    val startDate: LocalDate,
    val endDate: LocalDate,

    // Recurrence rule for anchors
    val freq: RecurrenceFreq,
    val interval: Int = 1,
    val daysOfWeekMask: Int = 0, // for weekly-based anchors

    // Offsets relative to the anchor/start date
    val dueOffsetDays: Int,      // due = anchor + dueOffsetDays
    val reminderOffsetMinutes: Int? = null, // e.g. 24*60 for 24h before due-time (MVP can ignore)
    val isAllDayBar: Boolean = true
)


@Entity(
    tableName = "task_instances",
    indices = [Index("templateId"), Index("courseId"), Index("startDate"), Index("endDate")]
)
data class TaskInstanceEntity(
    @PrimaryKey val id: String, // UUID
    val templateId: String?,    // nullable if it’s a one-off task not tied to a template
    val title: String,
    val courseId: String?,
    val priority: Int,
    val startDate: LocalDate,   // available date or start span
    val endDate: LocalDate,     // end span (inclusive)
    val dueDate: LocalDate,
    val status: TaskStatus = TaskStatus.TODO,
    val overrideColorArgb: Long? = null, // normally null => inherit course color
    val reminderAt: LocalDateTime? = null
)

/** ---------- Special Events (quizzes/exams/etc) ---------- **/
@Entity(
    tableName = "special_events",
    indices = [Index("courseId"), Index("startDateTime"), Index("endDateTime")]
)
data class SpecialEventEntity(
    @PrimaryKey val id: String, // UUID
    val title: String,
    val courseId: String?,          // optional
    val colorArgb: Long,            // custom color (doesn't inherit by default)
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val location: String? = null,
    val notes: String? = null
)
