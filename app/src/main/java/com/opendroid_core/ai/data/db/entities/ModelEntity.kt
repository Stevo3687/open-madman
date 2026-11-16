// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ModelStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    LOADING,
    READY,
    FAILED,
    PAUSED
}

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val size: Long, // in bytes
    val downloadUrl: String,
    val localPath: String,
    val status: ModelStatus,
    val downloadProgress: Int, // 0..100
    val lastUsed: Long,
    val installedAt: Long,
    val downloadedSize: Long = 0L,
    val downloadSpeed: String = "",
    val etaString: String = ""
)
