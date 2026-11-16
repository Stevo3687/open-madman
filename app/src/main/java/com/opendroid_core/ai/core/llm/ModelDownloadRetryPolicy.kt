// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.llm

import com.opendroid_core.ai.data.db.entities.ModelStatus
import java.io.IOException
import javax.net.ssl.SSLException

/** Classifies resumable model-download states and failures without weakening integrity checks. */
internal object ModelDownloadRetryPolicy {

    fun simulationStartProgress(status: ModelStatus?, progress: Int): Int = when (status) {
        ModelStatus.DOWNLOADING, ModelStatus.PAUSED -> progress.coerceIn(0, 100)
        else -> 0
    }

    fun isRetryableHttpStatus(statusCode: Int): Boolean =
        statusCode == 408 ||
            statusCode == 425 ||
            statusCode == 429 ||
            statusCode in 500..599

    fun isRetryableTransport(error: Throwable): Boolean =
        error is IOException && error !is SSLException
}
