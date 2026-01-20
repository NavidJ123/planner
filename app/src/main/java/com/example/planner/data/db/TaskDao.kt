package com.example.planner.data.db

import androidx.room.*
import com.example.planner.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ---- Templates ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplate(template: TaskTemplateEntity)

    @Query("SELECT * FROM task_templates")
    fun getAllTemplates(): Flow<List<TaskTemplateEntity>>

    // ---- Instances ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstances(instances: List<TaskInstanceEntity>)

    @Query("""
        SELECT * FROM task_instances
        WHERE startDate <= :date AND endDate >= :date
        ORDER BY dueDate
    """)
    fun getActiveTasks(date: String): Flow<List<TaskInstanceEntity>>

    @Query("UPDATE task_instances SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus)

    @Query("DELETE FROM task_instances")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM task_templates")
    suspend fun deleteAllTemplates()

    @Query("SELECT * FROM task_instances WHERE templateId = :templateId")
    suspend fun getInstancesForTemplate(templateId: String): List<TaskInstanceEntity>
}
