// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_events",
    indices = [
        Index("timestamp"),
        Index("dayOfWeek"),
        Index("hourOfDay")
    ]
)
data class HabitEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val packageName: String,
    val actionName: String,
    val timestamp: Long,
    val dayOfWeek: Int,
    val hourOfDay: Int,
    val minuteOfHour: Int,
    val metadataJson: String = "{}"
)
