package com.example.planner.data.repository

import com.example.planner.data.db.TaskDao
import com.example.planner.data.entity.*

class TaskRepository(
    private val dao: TaskDao
) {
    fun templates() = dao.getAllTemplates()

    fun activeTasks(date: String) =
        dao.getActiveTasks(date)

    suspend fun saveTemplate(template: TaskTemplateEntity) =
        dao.upsertTemplate(template)

    suspend fun saveInstances(instances: List<TaskInstanceEntity>) =
        dao.insertInstances(instances)

    suspend fun updateStatus(id: String, status: TaskStatus) =
        dao.updateStatus(id, status)
}
