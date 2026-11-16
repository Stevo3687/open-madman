// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db

import androidx.room.TypeConverter
import com.opendroid_core.ai.data.db.entities.ModelStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: ModelStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): ModelStatus {
        return try {
            ModelStatus.valueOf(value)
        } catch (e: Exception) {
            ModelStatus.NOT_DOWNLOADED
        }
    }
}
