package com.example.planner.data.db

import androidx.room.*
import com.example.planner.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {

    // ---- Patterns ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPattern(pattern: CourseMeetingPatternEntity)

    @Query("SELECT * FROM course_meeting_patterns WHERE courseId = :courseId")
    fun getPatternsForCourse(courseId: String): Flow<List<CourseMeetingPatternEntity>>

    // ---- Instances ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstances(instances: List<CourseMeetingInstanceEntity>)

    @Query("""
        SELECT * FROM course_meeting_instances
        WHERE startDateTime BETWEEN :start AND :end
        ORDER BY startDateTime
    """)
    fun getInstancesBetween(
        start: String,
        end: String
    ): Flow<List<CourseMeetingInstanceEntity>>
}
