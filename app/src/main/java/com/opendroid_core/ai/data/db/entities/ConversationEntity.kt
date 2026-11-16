// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["sessionId"])]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val text: String,
    val sender: String, // "USER" or "AGENT"
    val timestamp: Long,
    val modelBadge: String? = null,
    val contactPickerData: String? = null,
    // Which chat history this message belongs to. See ChatSessionEntity.
    val sessionId: String
)
