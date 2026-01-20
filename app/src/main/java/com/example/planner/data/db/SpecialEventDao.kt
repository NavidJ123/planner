package com.example.planner.data.db

import androidx.room.*
import com.example.planner.data.entity.SpecialEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: SpecialEventEntity)

    @Delete
    suspend fun delete(event: SpecialEventEntity)

    @Query("""
        SELECT * FROM special_events
        WHERE startDateTime BETWEEN :start AND :end
        ORDER BY startDateTime
    """)
    fun getBetween(start: String, end: String): Flow<List<SpecialEventEntity>>
}
