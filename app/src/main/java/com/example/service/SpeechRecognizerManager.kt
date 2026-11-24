package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Listening : SpeechState()
    object Processing : SpeechState()
    data class Success(val spokenText: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}

class SpeechRecognizerManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastPartialText: String = ""
    private var listeningSessionToken: Long = 0L
    private var needsReinit: Boolean = false
    private var pendingStopTimeoutRunnable: Runnable? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    init {
        mainHandler.post { initRecognizer() }
    }

    private fun cancelStopTimeout() {
        pendingStopTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingStopTimeoutRunnable = null
    }

    private fun initRecognizer() {
        try {
            cancelStopTimeout()
            if (speechRecognizer != null) {
                try {
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
                speechRecognizer = null
            }
            lastPartialText = ""
            _partialText.value = ""

            val available = SpeechRecognizer.isRecognitionAvailable(context)
            speechRecognizer = if (available) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                try {
                    SpeechRecognizer.createSpeechRecognizer(context)
                } catch (_: Exception) {
                    null
                }
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    cancelStopTimeout()
                    lastPartialText = ""
                    _partialText.value = ""
                    _speechState.value = SpeechState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _speechState.value = SpeechState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _rmsVolume.value = (rmsdB + 2f).coerceIn(0f, 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _rmsVolume.value = 0f
                    if (_speechState.value is SpeechState.Listening) {
                        _speechState.value = SpeechState.Processing
                    }
                }

                override fun onError(error: Int) {
                    cancelStopTimeout()
                    needsReinit = true
                    _rmsVolume.value = 0f
                    val fallbackPartial = lastPartialText.trim()
                    lastPartialText = ""
                    _partialText.value = ""
                    if ((error == SpeechRecognizer.ERROR_NO_MATCH ||
                            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                            error == SpeechRecognizer.ERROR_CLIENT) &&
                        fallbackPartial.isNotBlank()
                    ) {
                        _speechState.value = SpeechState.Success(fallbackPartial)
                        return
                    }
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly and try again."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue. Please try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input heard. Tap the mic to try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Microphone was busy — resetting now. Tap to speak again."
                        SpeechRecognizer.ERROR_CLIENT -> "Speech capture reset. Please tap the mic and speak again."
                        else -> "Speech capture error ($error). Please tap the mic and try again."
                    }
                    _speechState.value = SpeechState.Error(message)
                }

                override fun onResults(results: Bundle?) {
                    cancelStopTimeout()
                    _rmsVolume.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull { !it.isNullOrBlank() }?.trim()
                        ?: lastPartialText.trim()
                    lastPartialText = ""
                    _partialText.value = ""
                    if (text.isNotBlank()) {
                        _speechState.value = SpeechState.Success(text)
                    } else {
                        _speechState.value = SpeechState.Error("No words detected. Please try speaking again.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull { !it.isNullOrBlank() }?.trim() ?: ""
                    if (text.isNotBlank()) {
                        lastPartialText = text
                        _partialText.value = text
                        if (_speechState.value is SpeechState.Listening) {
                            _rmsVolume.value = 5f
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (_: Exception) {
            speechRecognizer = null
        }
    }

    fun startListening() {
        mainHandler.post {
            cancelStopTimeout()
            listeningSessionToken++
            lastPartialText = ""
            _partialText.value = ""
            _rmsVolume.value = 0f
            if (speechRecognizer == null || needsReinit) {
                initRecognizer()
                needsReinit = false
            }

            if (speechRecognizer == null) {
                _speechState.value = SpeechState.Error("Speech recognizer unavailable on this device.")
                return@post
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            try {
                _speechState.value = SpeechState.Listening
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                needsReinit = true
                _speechState.value = SpeechState.Error("Failed to start recording: ${e.localizedMessage ?: "Mic error"}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            val currentToken = listeningSessionToken
            if (_speechState.value is SpeechState.Listening) {
                _speechState.value = SpeechState.Processing
            }
            _rmsVolume.value = 0f
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
                val fallback = lastPartialText.trim()
                lastPartialText = ""
                _partialText.value = ""
                if (fallback.isNotBlank()) {
                    _speechState.value = SpeechState.Success(fallback)
                } else {
                    _speechState.value = SpeechState.Idle
                }
                return@post
            }

            cancelStopTimeout()
            val timeoutRunnable = Runnable {
                if (currentToken == listeningSessionToken && _speechState.value is SpeechState.Processing) {
                    val fallback = lastPartialText.trim()
                    lastPartialText = ""
                    _partialText.value = ""
                    needsReinit = true
                    if (fallback.isNotBlank()) {
                        _speechState.value = SpeechState.Success(fallback)
                    } else {
                        _speechState.value = SpeechState.Error("No speech detected. Please speak clearly and try again.")
                    }
                }
            }
            pendingStopTimeoutRunnable = timeoutRunnable
            mainHandler.postDelayed(timeoutRunnable, 1800L)
        }
    }

    fun cancel() {
        mainHandler.post {
            cancelStopTimeout()
            listeningSessionToken++
            lastPartialText = ""
            _partialText.value = ""
            _rmsVolume.value = 0f
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {}
            _speechState.value = SpeechState.Idle
        }
    }

    // Quick practice simulated answer injector
    fun simulateSpokenText(sample: String) {
        cancelStopTimeout()
        listeningSessionToken++
        lastPartialText = ""
        _partialText.value = ""
        _rmsVolume.value = 0f
        _speechState.value = SpeechState.Processing
        mainHandler.postDelayed({
            _speechState.value = SpeechState.Success(sample)
        }, 250)
    }

    fun reset() {
        cancelStopTimeout()
        listeningSessionToken++
        lastPartialText = ""
        _partialText.value = ""
        _speechState.value = SpeechState.Idle
        _rmsVolume.value = 0f
    }

    fun destroy() {
        mainHandler.post {
            cancelStopTimeout()
            listeningSessionToken++
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
        }
    }
}
