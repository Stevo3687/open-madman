package com.openjarvis.intelligence

import android.content.Context
import com.openjarvis.accessibility.JarvisAccessibilityService
import com.openjarvis.accessibility.ScreenReader
import kotlinx.coroutines.delay

class AIAppInteractor(private val context: Context) {
    
    private val workingMemory = TaskWorkingMemory()
    
    fun openAIApp(meta: AIAppMeta): Boolean {
        val service = JarvisAccessibilityService.instance ?: return false
        service.openAppByPackage(meta.packageName)
        return true
    }
    
    fun clearContext() {
        try {
            JarvisAccessibilityService.instance?.pressBack()
        } catch (e: Exception) { }
    }
    
    fun typePrompt(prompt: String): Boolean {
        return JarvisAccessibilityService.instance?.typeText(prompt) ?: false
    }
    
    suspend fun waitForResponse(timeoutMs: Long = 60_000): String {
        val startTime = System.currentTimeMillis()
        var lastText = ""
        var sameCount = 0
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            delay(1000)
            
            val currentText = readScreenText()
            
            if (currentText == lastText) {
                sameCount++
                if (sameCount >= 2) {
                    return currentText
                }
            } else {
                sameCount = 0
                lastText = currentText
            }
        }
        
        return lastText
    }
    
    fun extractResponse(meta: AIAppMeta): String {
        when (meta.responseExtraction) {
            ResponseExtraction.SCREEN_TEXT -> {
                return readScreenText()
            }
            ResponseExtraction.OCR_REQUIRED -> {
                return readScreenText()
            }
            ResponseExtraction.COPY_BUTTON -> {
                return tryCopyFromUI()
            }
            ResponseExtraction.SHARE_MENU -> {
                return tryShareFromUI()
            }
        }
        return ""
    }
    
    fun getWorkingMemory(): TaskWorkingMemory = workingMemory
    
    private fun tryCopyFromUI(): String {
        return readScreenText()
    }
    
    private fun tryShareFromUI(): String {
        return readScreenText()
    }

    private fun readScreenText(): String =
        JarvisAccessibilityService.instance?.let { ScreenReader(it).extractAllText() }.orEmpty()
}