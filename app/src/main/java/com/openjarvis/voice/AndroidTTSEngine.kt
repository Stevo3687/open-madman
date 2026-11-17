package com.openjarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.openjarvis.AssistantUpgrade
import java.util.Locale
import java.util.UUID

class AndroidTTSEngine(private val context: Context) {

    private val assistantUpgrade = AssistantUpgrade(context)
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var speechRate = 1.05f
    private var pitch = 0.95f

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                tts?.language = Locale.US
                applyActivePersonaVoice()
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        applyActivePersonaVoice()
        
        val clean = text
            .replace(Regex("\\[\\{.*?\\}\\]"), "")
            .replace(Regex("[\\[\\]{}\":]"), " ")
            .trim()
        
        if (clean.isBlank()) return
        
        val utteranceId = "jarvis_${UUID.randomUUID()}"
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakQueued(text: String) {
        if (!isReady) return
        applyActivePersonaVoice()
        
        val clean = text
            .replace(Regex("\\[\\{.*?\\}\\]"), "")
            .replace(Regex("[\\[\\]{}\":]"), " ")
            .trim()
        
        if (clean.isBlank()) return
        
        val utteranceId = "jarvis_${UUID.randomUUID()}"
        tts?.speak(clean, TextToSpeech.QUEUE_ADD, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(speechRate)
    }

    fun setPitch(pitchValue: Float) {
        pitch = pitchValue.coerceIn(0.5f, 2.0f)
        tts?.setPitch(pitch)
    }

    private fun applyActivePersonaVoice() {
        val persona = assistantUpgrade.activePersona
        setPitch(persona.pitch)
        setSpeechRate(persona.speechRate)
    }

    fun getSpeechRate(): Float = speechRate
    fun getPitch(): Float = pitch

    fun isSpeaking(): Boolean = tts?.isSpeaking ?: false

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}