// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.models

import kotlinx.serialization.Serializable

enum class RoutineStatus {
    SUGGESTED,  // Detected and awaiting user approval
    APPROVED,   // User approved and active as automation
    ACTIVE,     // Actively enabled
    PAUSED,     // Temporarily paused by user
    DISMISSED   // Dismissed by user
}

@Serializable
data class HabitRoutine(
    val id: String,
    val name: String,
    val description: String,
    val triggerLabel: String,
    val triggerCron: String = "",
    val detectedActions: List<String> = emptyList(),
    val suggestedSteps: List<PlanStep> = emptyList(),
    val repetitionCount: Int = 1,
    val confidence: Float = 0.5f,
    val status: RoutineStatus = RoutineStatus.SUGGESTED,
    val suggestionMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastDetectedAt: Long = System.currentTimeMillis(),
    val lastExecutedAt: Long? = null,
    val macroId: String? = null
)
