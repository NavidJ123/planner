package com.example.planner.data.repository

import com.example.planner.data.db.SpecialEventDao
import com.example.planner.data.entity.SpecialEventEntity

class SpecialEventRepository(
    private val dao: SpecialEventDao
) {
    fun getBetween(start: String, end: String) =
        dao.getBetween(start, end)

    suspend fun save(event: SpecialEventEntity) =
        dao.upsert(event)

    suspend fun delete(event: SpecialEventEntity) =
        dao.delete(event)
}
