// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.memory

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clears the captured notification history and the patterns learned from it. Callers that
 * may clear history but must not read the learned profile (#177) depend on this, not on
 * [NotificationIntelligence].
 */
@Singleton
class NotificationHistoryCleaner @Inject constructor(
    private val notificationIntelligence: NotificationIntelligence
) {
    /** Returns the number of notification records deleted. */
    suspend fun clearHistory(): Int = notificationIntelligence.clearHistory()
}
