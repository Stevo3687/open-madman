// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Macro(
    val id: String,
    val name: String,
    val trigger: String, // voice trigger or cron expression
    val steps: List<PlanStep>,
    val isSystem: Boolean = false,
    val isEnabled: Boolean = true
)
