
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.*
import com.example.planner.data.entity.*

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: CourseEntity)

    @Query("SELECT * FROM courses ORDER BY name")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourse(id: String): CourseEntity?

    @Delete suspend fun deleteCourse(course: CourseEntity)
}

@Dao
interface MeetingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPattern(p: CourseMeetingPatternEntity)

    @Query("SELECT * FROM course_meeting_patterns WHERE courseId = :courseId")
    suspend fun getPatterns(courseId: String): List<CourseMeetingPatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSkip(skip: CourseMeetingSkipEntity)

    @Query("SELECT * FROM course_meeting_skips WHERE courseId = :courseId")
    suspend fun getSkips(courseId: String): List<CourseMeetingSkipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInstances(instances: List<CourseMeetingInstanceEntity>)

    @Query("DELETE FROM course_meeting_instances WHERE courseId = :courseId AND startDateTime >= :from")
    suspend fun deleteFutureInstances(courseId: String, from: LocalDateTime)

    @Query("""
    SELECT * FROM course_meeting_instances
    WHERE startDateTime < :to AND endDateTime > :from
    ORDER BY startDateTime
  """)
    fun observeInstancesInRange(from: LocalDateTime, to: LocalDateTime): Flow<List<CourseMeetingInstanceEntity>>
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplate(t: TaskTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInstances(instances: List<TaskInstanceEntity>)

    @Query("DELETE FROM task_instances WHERE templateId = :templateId AND startDate >= :fromDate")
    suspend fun deleteFutureTaskInstances(templateId: String, fromDate: LocalDate)

    @Query("""
    SELECT * FROM task_instances
    WHERE NOT (endDate < :start OR startDate > :end)
    ORDER BY dueDate, priority DESC
  """)
    fun observeTasksInDateRange(start: LocalDate, end: LocalDate): Flow<List<TaskInstanceEntity>>

    @Query("UPDATE task_instances SET status = :status WHERE id = :id")
    suspend fun setTaskStatus(id: String, status: TaskStatus)
}

@Dao
interface SpecialEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(e: SpecialEventEntity)

    @Query("""
    SELECT * FROM special_events
    WHERE startDateTime < :to AND endDateTime > :from
    ORDER BY startDateTime
  """)
    fun observeEventsInRange(from: LocalDateTime, to: LocalDateTime): Flow<List<SpecialEventEntity>>
}
