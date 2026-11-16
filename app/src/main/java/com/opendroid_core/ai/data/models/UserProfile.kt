// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String? = null,
    val age: Int? = null,
    val location: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val contacts: Map<String, String> = emptyMap(), // Nickname -> Phone
    val preferences: Map<String, String> = emptyMap(), // key -> value
    val routines: Map<String, String> = emptyMap(), // trigger -> action
    val behaviorPatterns: List<String> = emptyList()
)
