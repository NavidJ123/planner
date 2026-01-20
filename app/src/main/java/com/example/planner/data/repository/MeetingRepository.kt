package com.example.planner.data.repository

import com.example.planner.data.db.MeetingDao
import com.example.planner.data.entity.*

class MeetingRepository(
    private val dao: MeetingDao
) {
    fun patternsForCourse(courseId: String) =
        dao.getPatternsForCourse(courseId)

    suspend fun savePattern(pattern: CourseMeetingPatternEntity) =
        dao.upsertPattern(pattern)

    suspend fun saveInstances(instances: List<CourseMeetingInstanceEntity>) =
        dao.insertInstances(instances)
}
