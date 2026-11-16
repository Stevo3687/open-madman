// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.social.core.audit

import com.opendroid_core.ai.data.repository.SocialRepository
import com.opendroid_core.ai.social.domain.model.SocialAuditEntry
import com.opendroid_core.ai.social.domain.model.SocialPlatform
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialAuditLogger @Inject constructor(
    private val repository: SocialRepository
) {
    suspend fun log(
        actor: String,
        action: String,
        platform: SocialPlatform,
        targetId: String? = null,
        details: String,
        status: String = "SUCCESS"
    ) {
        val entry = SocialAuditEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            actor = actor,
            action = action,
            platform = platform,
            targetId = targetId,
            details = details,
            status = status
        )
        repository.recordAuditLog(entry)
    }
}
