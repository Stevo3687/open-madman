// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val planId: String,
    val goal: String,
    val estimatedDuration: String,
    val estimatedSteps: Int,
    val stepsJson: String, // Serialized List<PlanStep>
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED, PAUSED, CANCELLED
    val createdAt: Long
)
