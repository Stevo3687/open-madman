// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.llm

import java.io.File

/**
 * Lets model storage drop the cached on-device engine before it deletes the files the engine
 * was loaded from, so a deleted model does not stay resident in memory.
 */
fun interface OnDeviceEngineReleaser {
    /** Closes the cached engine if it was loaded from a file in [modelDir]; otherwise does nothing. */
    suspend fun releaseIfLoadedFrom(modelDir: File)
}
