// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_routines",
    indices = [
        Index("status"),
        Index("lastDetectedAt")
    ]
)
data class HabitRoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val triggerLabel: String,
    val triggerCron: String,
    val detectedActionsJson: String,
    val suggestedStepsJson: String,
    val repetitionCount: Int,
    val confidence: Float,
    val status: String,
    val suggestionMessage: String,
    val createdAt: Long,
    val lastDetectedAt: Long,
    val lastExecutedAt: Long? = null,
    val macroId: String? = null
)
