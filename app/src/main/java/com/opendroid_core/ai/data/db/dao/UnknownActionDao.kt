// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.opendroid_core.ai.data.db.entities.UnknownActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnknownActionDao {
    @Query("SELECT * FROM unknown_actions ORDER BY timestamp DESC")
    fun getAllUnknownActions(): Flow<List<UnknownActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnknownAction(unknownAction: UnknownActionEntity)

    @Query("SELECT COUNT(*) FROM unknown_actions")
    suspend fun getUnknownActionCount(): Int

    @Query("DELETE FROM unknown_actions")
    suspend fun clearAll()
}
