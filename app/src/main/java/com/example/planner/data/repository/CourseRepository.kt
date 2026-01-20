package com.example.planner.data.repository

import com.example.planner.data.db.CourseDao
import com.example.planner.data.entity.CourseEntity

class CourseRepository(
    private val dao: CourseDao
) {
    fun getAllCourses() = dao.getAll()

    suspend fun save(course: CourseEntity) =
        dao.upsert(course)

    suspend fun delete(course: CourseEntity) =
        dao.delete(course)
}
