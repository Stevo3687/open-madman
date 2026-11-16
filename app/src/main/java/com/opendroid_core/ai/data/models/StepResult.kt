// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StepResult(
    val stepId: String,
    val success: Boolean,
    val data: String? = null,
    val error: String? = null
)
