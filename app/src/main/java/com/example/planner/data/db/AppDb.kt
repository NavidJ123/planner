package com.example.planner.data.db
import androidx.room.*
import com.example.planner.data.entity.*


@Database(
    entities = [
        CourseEntity::class,
        CourseMeetingPatternEntity::class,
        CourseMeetingSkipEntity::class,
        CourseMeetingInstanceEntity::class,
        TaskTemplateEntity::class,
        TaskInstanceEntity::class,
        SpecialEventEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun meetingDao(): MeetingDao
    abstract fun taskDao(): TaskDao
    abstract fun specialEventDao(): SpecialEventDao
}
