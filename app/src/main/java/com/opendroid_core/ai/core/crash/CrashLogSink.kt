// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.crash

/**
 * Where recorded crashes go.
 *
 * Both methods are synchronous by design: the caller is a dying thread that has
 * to finish writing before it hands control to the system handler, which kills
 * the process. Implementations may throw - [CrashLogRecorder] contains failures.
 */
interface CrashLogSink {

    fun record(record: CrashLogRecord)

    /** Drops all but the [keepMostRecent] newest crashes. */
    fun prune(keepMostRecent: Int)
}
