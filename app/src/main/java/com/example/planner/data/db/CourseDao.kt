package com.example.planner.data.db

import androidx.room.*
import com.example.planner.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("SELECT * FROM courses ORDER BY startDate")
    fun getAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CourseEntity?

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
