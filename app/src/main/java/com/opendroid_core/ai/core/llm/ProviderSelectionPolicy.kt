// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.llm

import com.opendroid_core.ai.core.agent.ActionRisk
import com.opendroid_core.ai.core.agent.ActionRiskPolicy
import com.opendroid_core.ai.core.agent.ActionSchema
import com.opendroid_core.ai.core.llm.error.LLMError

/** Pure policy for provider fallback ordering and trust-boundary checks. */
object ProviderSelectionPolicy {
    fun explicitFallbacks(
        activeProvider: String,
        configuredFallbacks: Iterable<String>,
        risk: ActionRisk
    ): List<String> {
        if (!ActionRiskPolicy.allowsAutomaticFallback(risk)) return emptyList()

        val active = ProviderCatalog.canonicalName(activeProvider)
        return configuredFallbacks
            .map(ProviderCatalog::canonicalName)
            .filter { ProviderCatalog.isKnown(it) }
            .filter { it != active }
            .distinct()
    }

    /**
     * [explicitFallbacks] only sees the plan going in. A fallback's answer can add
     * steps, so it must stay within the same risk limit or be discarded.
     */
    fun acceptsOutput(isFallback: Boolean, outputActions: Collection<String>): Boolean =
        !isFallback || ActionRiskPolicy.allowsAutomaticFallback(ActionSchema.highestRisk(outputActions))

    fun shouldEscalateMalformed(error: LLMError, hasNextProvider: Boolean): Boolean =
        error == LLMError.MalformedResponse && hasNextProvider

    fun terminalError(activeProvider: String, providerCount: Int): LLMError =
        if (providerCount == 1 && ProviderCatalog.isOnDevice(activeProvider)) {
            LLMError.SafeFallbackUnavailable
        } else {
            LLMError.MalformedResponse
        }
}
