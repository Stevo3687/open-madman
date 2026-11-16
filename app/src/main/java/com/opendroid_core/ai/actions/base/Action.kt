// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.actions.base

import android.content.Context

interface Action {
    val name: String
    suspend fun execute(params: Map<String, String>, context: Context): ActionResult
}
