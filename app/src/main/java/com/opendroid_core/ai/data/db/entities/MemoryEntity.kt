// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val key: String,
    val value: String,
    val type: String, // WORKING, EPISODIC, SEMANTIC, PROCEDURAL
    val timestamp: Long,
    val ttlHours: Int = -1, // -1 = permanent, otherwise hours until expiry
    val category: String = "FACT" // FACT, PREFERENCE, CONTEXT, ERROR
)
