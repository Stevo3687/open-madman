// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.memory

import com.opendroid_core.ai.data.models.Macro
import com.opendroid_core.ai.data.repository.MemoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProceduralMemory @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend fun saveMacro(macro: Macro) {
        memoryRepository.saveMacro(macro)
    }

    suspend fun getMacro(name: String): Macro? {
        return memoryRepository.getMacroByName(name)
    }
}
