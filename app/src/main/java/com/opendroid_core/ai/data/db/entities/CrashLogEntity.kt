// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.opendroid_core.ai.core.crash.DeviceMetadata

@Entity(
    tableName = "crash_logs",
    indices = [Index(value = ["timestamp"])]
)
data class CrashLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val exceptionClass: String,
    val message: String?,
    val threadName: String,
    val stackTrace: String,
    @Embedded val device: DeviceMetadata
)
