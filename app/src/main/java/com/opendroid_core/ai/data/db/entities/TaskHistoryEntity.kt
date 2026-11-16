// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stepId: String,
    val planId: String,
    val description: String,
    val actionType: String,
    val paramsJson: String,
    val success: Boolean,
    val resultData: String?,
    val errorMessage: String?,
    val timestamp: Long = System.currentTimeMillis()
)
