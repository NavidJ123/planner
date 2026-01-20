package com.example.planner.data.db

import androidx.room.*
import com.example.planner.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
@Dao
interface MeetingDao {

    // ---- Patterns ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPattern(pattern: CourseMeetingPatternEntity)

    @Query("SELECT * FROM course_meeting_patterns WHERE courseId = :courseId")
    fun getPatternsForCourse(courseId: String): List<CourseMeetingPatternEntity>

    // ---- Instances ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstances(instances: List<CourseMeetingInstanceEntity>)

    @Query("""
        SELECT * FROM course_meeting_instances
        WHERE startDateTime BETWEEN :start AND :end
        ORDER BY startDateTime
    """)
    fun getInstancesBetween(start: String, end: String): Flow<List<CourseMeetingInstanceEntity>>

    @Query("SELECT * FROM course_meeting_instances WHERE startDateTime >= :start AND startDateTime < :end ORDER BY startDateTime")
    fun observeInstancesInRange(start: LocalDateTime, end: LocalDateTime): Flow<List<CourseMeetingInstanceEntity>>


    @Query("DELETE FROM course_meeting_patterns")
    suspend fun deleteAllPatterns()

    @Query("DELETE FROM course_meeting_instances")
    suspend fun deleteAllInstances()

    @Query("DELETE FROM course_meeting_skips")
    suspend fun deleteAllSkips()
}
