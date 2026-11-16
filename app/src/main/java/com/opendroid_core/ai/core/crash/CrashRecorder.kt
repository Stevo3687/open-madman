// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.crash

/** Records a crash. Implementations must never throw. */
interface CrashRecorder {
    fun record(thread: Thread, throwable: Throwable)
}
