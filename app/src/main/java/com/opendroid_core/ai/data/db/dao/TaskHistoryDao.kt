// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.dao

import androidx.room.*
import com.opendroid_core.ai.data.db.entities.TaskHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskHistoryDao {
    @Query("SELECT * FROM task_history ORDER BY timestamp DESC")
    fun getTaskHistoryFlow(): Flow<List<TaskHistoryEntity>>

    @Query("SELECT * FROM task_history WHERE planId = :planId ORDER BY timestamp ASC, id ASC")
    suspend fun getTaskHistoryForPlan(planId: String): List<TaskHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(task: TaskHistoryEntity)

    @Query("DELETE FROM task_history")
    suspend fun clearAll()
}
