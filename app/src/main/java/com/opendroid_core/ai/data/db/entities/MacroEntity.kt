// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey val id: String,
    val name: String,
    val trigger: String,
    val stepsJson: String, // Serialized List<PlanStep>
    val isSystem: Boolean,
    val isEnabled: Boolean
)
