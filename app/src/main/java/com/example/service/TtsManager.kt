package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(0.92f)
                tts?.setPitch(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    /**
     * Cleans text for natural speech synthesis:
     * - Strips emojis and decorative symbols that break Android TTS
     * - Strips markdown symbols (*, _, #, ~, `)
     * - Normalizes whitespace
     */
    fun cleanTextForSpeech(raw: String): String {
        return raw
            // Remove emojis & pictographs
            .replace(Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
            // Remove markdown bold/italic/headers
            .replace(Regex("[*#_~`]+"), "")
            // Remove bullet points / dashes at start of lines
            .replace(Regex("^[\\s*•-]+", RegexOption.MULTILINE), "")
            // Normalize spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, slowSpeed: Boolean = false) {
        if (!isInitialized || tts == null) return
        val clean = cleanTextForSpeech(text)
        if (clean.isBlank()) return
        tts?.setSpeechRate(if (slowSpeed) 0.75f else 0.92f)
        val utteranceId = "Speakly_${System.currentTimeMillis()}"
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
