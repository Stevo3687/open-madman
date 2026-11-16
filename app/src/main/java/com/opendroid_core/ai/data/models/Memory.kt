// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Memory(
    val key: String,
    val value: String,
    val type: MemoryType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MemoryType {
    WORKING, EPISODIC, SEMANTIC, PROCEDURAL
}
