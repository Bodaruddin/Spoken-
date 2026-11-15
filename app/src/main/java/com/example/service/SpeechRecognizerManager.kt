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

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    init {
        mainHandler.post { initRecognizer() }
    }

    private fun initRecognizer() {
        try {
            if (speechRecognizer != null) {
                try {
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
                speechRecognizer = null
            }
            lastPartialText = ""

            val available = SpeechRecognizer.isRecognitionAvailable(context)
            speechRecognizer = if (available) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                try {
                    SpeechRecognizer.createSpeechRecognizer(context)
                } catch (e: Exception) {
                    null
                }
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    lastPartialText = ""
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
                    _speechState.value = SpeechState.Processing
                }

                override fun onError(error: Int) {
                    val fallbackPartial = lastPartialText.trim()
                    lastPartialText = ""
                    if ((error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) &&
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
                        else -> "Speech capture error ($error). Please tap the mic and try again."
                    }
                    _speechState.value = SpeechState.Error(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull { !it.isNullOrBlank() }?.trim()
                        ?: lastPartialText.trim()
                    lastPartialText = ""
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
                        if (_speechState.value is SpeechState.Listening) {
                            _rmsVolume.value = 5f
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (e: Exception) {
            speechRecognizer = null
        }
    }

    fun startListening() {
        mainHandler.post {
            lastPartialText = ""
            if (speechRecognizer == null) {
                initRecognizer()
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
                _speechState.value = SpeechState.Error("Failed to start recording: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                _speechState.value = SpeechState.Idle
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            lastPartialText = ""
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {}
            _speechState.value = SpeechState.Idle
        }
    }

    // Quick practice simulated answer injector
    fun simulateSpokenText(sample: String) {
        lastPartialText = ""
        _speechState.value = SpeechState.Processing
        mainHandler.postDelayed({
            _speechState.value = SpeechState.Success(sample)
        }, 300)
    }

    fun reset() {
        lastPartialText = ""
        _speechState.value = SpeechState.Idle
        _rmsVolume.value = 0f
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {}
            speechRecognizer = null
        }
    }
}
