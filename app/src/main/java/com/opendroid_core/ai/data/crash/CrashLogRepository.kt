// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.crash

import com.opendroid_core.ai.data.db.entities.CrashLogEntity
import com.opendroid_core.ai.core.crash.CrashLogSink
import kotlinx.coroutines.flow.Flow

/** Storage boundary for local crash diagnostics. UI and application wiring use this API. */
interface CrashLogRepository : CrashLogSink {
    fun getAllFlow(): Flow<List<CrashLogEntity>>
    suspend fun getAll(): List<CrashLogEntity>
    suspend fun clearAll()
}
