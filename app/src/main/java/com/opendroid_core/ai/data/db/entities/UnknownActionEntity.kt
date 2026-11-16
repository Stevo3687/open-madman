// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unknown_actions")
data class UnknownActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attemptedAction: String,
    val goal: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fixStatus: String, // "AUTO_FIXED" | "REPLANNED" | "FAILED"
    val wasAutoFixed: Boolean = false,
    val fixedWith: String? = null
)
