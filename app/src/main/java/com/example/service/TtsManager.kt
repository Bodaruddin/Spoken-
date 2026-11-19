package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

enum class NovaVoicePersona(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val basePitch: Float,
    val baseRate: Float,
    val geminiVoiceName: String,
    val stylePrompt: String
) {
    ANANYA(
        id = "ANANYA",
        displayName = "Ananya",
        subtitle = "Warm & Natural Indian Female",
        basePitch = 1.07f,
        baseRate = 1.00f,
        geminiVoiceName = "Aoede",
        stylePrompt = "Speak in a warm, natural, friendly Indian English female voice with lifelike human expression and clear conversational pacing:"
    ),
    PRIYA(
        id = "PRIYA",
        displayName = "Priya",
        subtitle = "Lively & Cheerful Indian Female",
        basePitch = 1.12f,
        baseRate = 1.05f,
        geminiVoiceName = "Leda",
        stylePrompt = "Speak in an energetic, cheerful, expressive Indian English female voice like an encouraging friend:"
    ),
    MEERA(
        id = "MEERA",
        displayName = "Meera",
        subtitle = "Calm & Gentle Indian Female",
        basePitch = 1.03f,
        baseRate = 0.94f,
        geminiVoiceName = "Kore",
        stylePrompt = "Speak in a calm, gentle, soothing Indian English female teacher voice with crystal clear articulation:"
    );

    companion object {
        fun fromId(id: String?): NovaVoicePersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ANANYA
        }
    }
}

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _voicePersona = MutableStateFlow(NovaVoicePersona.ANANYA)
    val voicePersona: StateFlow<NovaVoicePersona> = _voicePersona.asStateFlow()

    private val _activeVoiceLabel = MutableStateFlow("Ananya • Indian Female (en-IN)")
    val activeVoiceLabel: StateFlow<String> = _activeVoiceLabel.asStateFlow()

    private val _cloudHumanVoiceEnabled = MutableStateFlow(true)
    val cloudHumanVoiceEnabled: StateFlow<Boolean> = _cloudHumanVoiceEnabled.asStateFlow()

    private var isInitialized = false
    private var primaryIndianVoice: Voice? = null
    private var localFallbackVoice: Voice? = null
    private var activeAndroidVoice: Voice? = null
    private var activeAndroidVoiceConfirmedFemale: Boolean = false
    private var lastUtteranceGroupId: Long = 0L
    private var lastChunkIdForGroup: String? = null
    private var lastSpokenText: String = ""
    private var lastSlowSpeed: Boolean = false
    private val utteranceCounter = AtomicLong(0L)

    // Optional Cloud Gemini Native Audio Human Voice support
    private var apiKeyProvider: (() -> String)? = null
    private var activeAudioTrack: AudioTrack? = null
    private var cloudTtsJob: Job? = null
    private var cloudTtsTemporarilyUnavailable = false

    private val cloudTtsClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(9, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    fun setGeminiApiKeyProvider(provider: () -> String) {
        apiKeyProvider = provider
    }

    fun setVoicePersona(persona: NovaVoicePersona) {
        _voicePersona.value = persona
        updateActiveVoiceLabel()
        if (isInitialized) {
            applyBestVoiceForPersona()
        }
    }

    fun setCloudHumanVoiceEnabled(enabled: Boolean) {
        _cloudHumanVoiceEnabled.value = enabled
        updateActiveVoiceLabel()
    }

    private fun updateActiveVoiceLabel() {
        val p = _voicePersona.value
        val hasKey = try {
            apiKeyProvider?.invoke()?.isNotBlank() == true
        } catch (_: Throwable) {
            false
        }
        val modeSuffix = when {
            _cloudHumanVoiceEnabled.value && hasKey && !cloudTtsTemporarilyUnavailable -> "HD Human (en-IN)"
            primaryIndianVoice?.locale?.country.equals("IN", ignoreCase = true) -> "Indian Female (en-IN)"
            else -> "Indian Persona"
        }
        _activeVoiceLabel.value = "${p.displayName} • $modeSuffix"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            val indiaLocale = Locale("en", "IN")
            val inStatus = try {
                engine.isLanguageAvailable(indiaLocale)
            } catch (_: Throwable) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            val result = if (inStatus >= TextToSpeech.LANG_AVAILABLE) {
                engine.setLanguage(indiaLocale)
            } else {
                val ukStatus = try {
                    engine.isLanguageAvailable(Locale.UK)
                } catch (_: Throwable) {
                    TextToSpeech.LANG_NOT_SUPPORTED
                }
                if (ukStatus >= TextToSpeech.LANG_AVAILABLE) {
                    engine.setLanguage(Locale.UK)
                } else {
                    engine.setLanguage(Locale.US)
                }
            }

            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                selectBestIndianFemaleVoices(engine)
                applyBestVoiceForPersona()
                updateActiveVoiceLabel()
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == null || utteranceId == lastChunkIdForGroup) {
                            _isSpeaking.value = false
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        handleTtsUtteranceError()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        handleTtsUtteranceError()
                    }
                })
            }
        }
    }

    private fun handleTtsUtteranceError() {
        // If a network voice failed (e.g. offline emulator), gracefully switch to local voice and retry once
        val fallback = localFallbackVoice
        val current = try { tts?.voice } catch (_: Throwable) { null }
        if (fallback != null && current != null && current.name != fallback.name && lastSpokenText.isNotBlank()) {
            try {
                tts?.voice = fallback
                primaryIndianVoice = fallback
                speakWithAndroidProsody(lastSpokenText, lastSlowSpeed)
                return
            } catch (_: Throwable) { }
        }
        _isSpeaking.value = false
    }

    private fun selectBestIndianFemaleVoices(engine: TextToSpeech) {
        val voices = try {
            engine.voices?.toList().orEmpty()
        } catch (_: Throwable) {
            emptyList()
        }
        if (voices.isEmpty()) return

        val englishVoices = voices.filter { v ->
            v.locale?.language.equals("en", ignoreCase = true)
        }
        val pool = englishVoices.ifEmpty { voices }

        val ranked = pool.sortedByDescending { scoreIndianFemaleVoice(it) }
        primaryIndianVoice = ranked.firstOrNull()
        localFallbackVoice = ranked.firstOrNull { !it.isNetworkConnectionRequired } ?: primaryIndianVoice
    }

    private fun scoreIndianFemaleVoice(voice: Voice): Int {
        val name = voice.name.lowercase()
        val features = try {
            voice.features?.joinToString(" ")?.lowercase().orEmpty()
        } catch (_: Throwable) {
            ""
        }
        val combined = "$name $features"
        val country = voice.locale?.country.orEmpty().uppercase()

        var score = 0
        // Locale priority: Indian English first, then UK, then others
        score += when (country) {
            "IN" -> 1200
            "GB", "UK" -> 350
            "AU" -> 220
            "US" -> 180
            else -> 100
        }

        // Known Google / Samsung / Android Indian & Commonwealth female neural voice signatures
        val femaleHints = listOf(
            "female", "woman", "enc", "ene", "ena", "ef", "sangeeta",
            "veena", "lekha", "neelam", "aditi", "kajal", "raveena", "priya",
            "gba", "gbc", "gbg", "fis", "iog", "sfg", "tpf",
            "tessa", "samantha", "karen", "moira", "serena", "f00", "f01", "f02"
        )
        if (femaleHints.any { combined.contains(it) }) {
            score += 650
        }

        // Strongly penalize known male voice signatures
        val maleHints = listOf(
            "male", "man", "rishi", "prabhat", "-end-", "_end_", "x-end",
            "-ahp-", "_ahp_", "x-ahp", "daniel", "arthur", "oliver", "george",
            "alex", "fred", "m00", "m01"
        )
        // Avoid matching "female" when checking "male"
        val strippedFemale = combined.replace("female", "")
        if (maleHints.any { strippedFemale.contains(it) }) {
            score -= 1400
        }

        // Voice synthesis quality
        score += when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> 360
            voice.quality >= Voice.QUALITY_HIGH -> 240
            voice.quality >= Voice.QUALITY_NORMAL -> 120
            else -> 20
        }

        // Prefer natural neural / wavenet / high-res voices; slightly prefer local if equal quality for instant zero-lag playback
        if (combined.contains("neural") || combined.contains("wavenet")) score += 180
        if (!voice.isNetworkConnectionRequired) score += 60

        return score
    }

    private fun applyBestVoiceForPersona() {
        val engine = tts ?: return
        val persona = _voicePersona.value
        try {
            val voices = engine.voices?.toList().orEmpty()
            val indianFemales = voices.filter { v ->
                v.locale?.language.equals("en", ignoreCase = true) &&
                    v.locale?.country.equals("IN", ignoreCase = true) &&
                    scoreIndianFemaleVoice(v) > 1000
            }.sortedByDescending { scoreIndianFemaleVoice(it) }

            val chosen = when {
                indianFemales.size >= 3 -> when (persona) {
                    NovaVoicePersona.ANANYA -> indianFemales[0]
                    NovaVoicePersona.PRIYA -> indianFemales[1]
                    NovaVoicePersona.MEERA -> indianFemales[2]
                }
                indianFemales.size == 2 -> when (persona) {
                    NovaVoicePersona.PRIYA -> indianFemales[1]
                    else -> indianFemales[0]
                }
                else -> primaryIndianVoice
            }
            if (chosen != null) {
                activeAndroidVoice = chosen
                val name = chosen.name.lowercase()
                val femaleTokens = listOf("female", "woman", "enc", "ene", "ena", "sangeeta", "veena", "lekha", "aditi", "kajal", "priya", "gba", "gbc", "gbg")
                activeAndroidVoiceConfirmedFemale = femaleTokens.any { name.contains(it) }
                engine.voice = chosen
            }
            val effectivePitch = if (activeAndroidVoiceConfirmedFemale) persona.basePitch else (persona.basePitch + 0.07f).coerceAtMost(1.20f)
            engine.setPitch(effectivePitch)
            engine.setSpeechRate(persona.baseRate)
        } catch (_: Throwable) { }
    }

    /**
     * Cleans text for natural, human-sounding speech synthesis:
     * - Converts structured coaching tags into warm spoken transitions
     * - Expands stiff abbreviations (e.g., i.e., etc.)
     * - Strips emojis and markdown symbols
     */
    fun cleanTextForSpeech(raw: String): String {
        return raw
            .replace("❌ Your Sentence:", "You said:")
            .replace("✅ Correct Sentence:", "Try saying:")
            .replace("📖 Explanation:", "Here is why:")
            .replace("🌟 Natural English:", "Even more naturally:")
            .replace(Regex("\\be\\.g\\.", RegexOption.IGNORE_CASE), "for example")
            .replace(Regex("\\bi\\.e\\.", RegexOption.IGNORE_CASE), "that is")
            .replace(Regex("\\betc\\.", RegexOption.IGNORE_CASE), "and so on")
            .replace(Regex("\\bvs\\.", RegexOption.IGNORE_CASE), "versus")
            .replace(" & ", " and ")
            // Remove emojis & pictographs
            .replace(Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
            // Remove markdown bold/italic/headers
            .replace(Regex("[*#_~`]+"), "")
            // Remove bullet points / dashes at start of lines
            .replace(Regex("^[\\s*•-]+", RegexOption.MULTILINE), "")
            // Convert newlines into subtle sentence breaks if needed
            .replace(Regex("\\n+"), ". ")
            .replace(Regex("\\.{2,}"), ".")
            // Normalize spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Splits cleaned text into expressive spoken chunks for natural human breathing & intonation.
     */
    internal fun splitIntoProsodyChunks(clean: String): List<String> {
        val rawSentences = clean
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (rawSentences.isEmpty()) return listOf(clean)

        val chunks = mutableListOf<String>()
        for (sentence in rawSentences) {
            if (sentence.length <= 160) {
                chunks.add(sentence)
            } else {
                // Break long sentences softly at commas/semicolons so TTS doesn't rush
                val subParts = sentence.split(Regex("(?<=[,;:])\\s+"))
                var buffer = StringBuilder()
                for (part in subParts) {
                    if (buffer.isNotEmpty() && buffer.length + part.length > 130) {
                        chunks.add(buffer.toString().trim())
                        buffer = StringBuilder(part)
                    } else {
                        if (buffer.isNotEmpty()) buffer.append(" ")
                        buffer.append(part)
                    }
                }
                if (buffer.isNotEmpty()) chunks.add(buffer.toString().trim())
            }
        }
        return chunks.ifEmpty { listOf(clean) }
    }

    @androidx.annotation.VisibleForTesting
    var speakAttemptCount: Int = 0
        private set

    fun speak(text: String, slowSpeed: Boolean = false) {
        val clean = cleanTextForSpeech(text)
        if (clean.isBlank()) return
        speakAttemptCount++
        lastSpokenText = clean
        lastSlowSpeed = slowSpeed

        // Cancel any in-flight cloud audio first
        stopCloudPlaybackOnly()

        val key = try {
            apiKeyProvider?.invoke()?.trim().orEmpty()
        } catch (_: Throwable) {
            ""
        }

        // If Cloud Human Voice is enabled and a Gemini API key is configured, synthesize natural human audio
        // for both normal conversations and slow 0.7x Phonetics drills, with instant Android en-IN fallback.
        if (_cloudHumanVoiceEnabled.value &&
            !cloudTtsTemporarilyUnavailable &&
            key.isNotBlank() &&
            clean.length <= 550
        ) {
            speakWithCloudHumanVoice(clean, key, slowSpeed)
            return
        }

        if (!isInitialized || tts == null) return
        speakWithAndroidProsody(clean, slowSpeed)
    }

    fun previewVoice() {
        val p = _voicePersona.value
        val sample = when (p) {
            NovaVoicePersona.ANANYA ->
                "Hi there! I'm Ananya, your Nova speaking buddy. Let's have a fun, natural conversation in English today!"
            NovaVoicePersona.PRIYA ->
                "Hey! I'm Priya! Ready to level up your spoken English? Tell me about your day, and let's practice together!"
            NovaVoicePersona.MEERA ->
                "Hello! I'm Meera. Take a deep breath and speak comfortably with me — I'll help you sound clear and confident."
        }
        speak(sample, slowSpeed = false)
    }

    private fun speakWithAndroidProsody(clean: String, slowSpeed: Boolean) {
        val engine = tts ?: return
        if (!isInitialized) return

        val persona = _voicePersona.value
        val baseRate = if (slowSpeed) 0.72f else persona.baseRate
        val basePitch = if (activeAndroidVoiceConfirmedFemale) persona.basePitch else (persona.basePitch + 0.07f).coerceAtMost(1.20f)
        val chunks = splitIntoProsodyChunks(clean)
        val groupId = utteranceCounter.incrementAndGet()
        lastUtteranceGroupId = groupId

        try {
            engine.stop()
            // Re-assert Indian female voice before speaking so Phonetics single-word drills never revert to default US/male voice
            activeAndroidVoice?.let { engine.voice = it }
        } catch (_: Throwable) { }

        val bundle = Bundle()
        chunks.forEachIndexed { idx, chunk ->
            val utteranceId = "Speakly_${groupId}_$idx"
            if (idx == chunks.lastIndex) {
                lastChunkIdForGroup = utteranceId
            }

            // Dynamic expressive pitch & pace per sentence type
            val chunkPitch = when {
                chunk.endsWith("?") -> (basePitch + 0.05f).coerceAtMost(1.22f)
                chunk.endsWith("!") -> (basePitch + 0.04f).coerceAtMost(1.20f)
                else -> basePitch
            }
            val chunkRate = when {
                slowSpeed -> baseRate
                chunk.endsWith("!") -> (baseRate + 0.03f).coerceAtMost(1.15f)
                else -> baseRate
            }

            try {
                engine.setPitch(chunkPitch)
                engine.setSpeechRate(chunkRate)
                val queueMode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                engine.speak(chunk, queueMode, bundle, utteranceId)

                // Natural breathing pause between sentences so Nova doesn't rush monotonically
                if (idx < chunks.lastIndex) {
                    val pauseMs = if (chunk.endsWith("?") || chunk.endsWith("!")) 210L else 145L
                    engine.playSilentUtterance(pauseMs, TextToSpeech.QUEUE_ADD, "Speakly_${groupId}_pause_$idx")
                }
            } catch (_: Throwable) { }
        }
    }

    private fun speakWithCloudHumanVoice(cleanText: String, apiKey: String, slowSpeed: Boolean) {
        cloudTtsJob = scope.launch {
            _isSpeaking.value = true
            val persona = _voicePersona.value
            val pcmBytes = withContext(Dispatchers.IO) {
                fetchGeminiAudioBytes(cleanText, apiKey, persona, slowSpeed)
            }
            if (pcmBytes != null && pcmBytes.isNotEmpty()) {
                val played = withContext(Dispatchers.IO) {
                    playPcm24kMono(pcmBytes, slowSpeed)
                }
                _isSpeaking.value = false
                if (!played && isInitialized && tts != null) {
                    speakWithAndroidProsody(cleanText, slowSpeed)
                }
            } else {
                // Seamless fallback to Indian Female Android Neural TTS
                _isSpeaking.value = false
                if (isInitialized && tts != null) {
                    speakWithAndroidProsody(cleanText, slowSpeed)
                }
            }
        }
    }

    private fun fetchGeminiAudioBytes(
        cleanText: String,
        apiKey: String,
        persona: NovaVoicePersona,
        slowSpeed: Boolean
    ): ByteArray? {
        val candidateModels = listOf(
            "gemini-3.1-flash-tts-preview",
            "gemini-2.5-flash-preview-tts"
        )
        for (modelName in candidateModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val prompt = if (slowSpeed) {
                    "Pronounce slowly, clearly, and syllable-by-syllable in a warm Indian English female voice for pronunciation practice: $cleanText"
                } else {
                    "${persona.stylePrompt} $cleanText"
                }
                val body = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply { put("AUDIO") })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", persona.geminiVoiceName)
                                })
                            })
                        })
                    })
                }
                val request = Request.Builder()
                    .url(url)
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                val response = cloudTtsClient.newCall(request).execute()
                val respStr = response.body?.string().orEmpty()
                if (response.isSuccessful && respStr.isNotBlank()) {
                    val root = JSONObject(respStr)
                    val parts = root.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val inlineData = parts.optJSONObject(i)?.optJSONObject("inlineData")
                            val b64 = inlineData?.optString("data").orEmpty()
                            if (b64.isNotBlank()) {
                                return Base64.decode(b64, Base64.DEFAULT)
                            }
                        }
                    }
                } else if (response.code == 400 || response.code == 403) {
                    // Key doesn't permit TTS or invalid key; skip retrying on every single tap
                    cloudTtsTemporarilyUnavailable = true
                    updateActiveVoiceLabel()
                    return null
                }
            } catch (_: Throwable) { }
        }
        return null
    }

    private fun playPcm24kMono(pcmBytes: ByteArray, slowSpeed: Boolean = false): Boolean {
        return try {
            val sampleRate = 24000
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf.coerceAtLeast(pcmBytes.size.coerceAtMost(minBuf * 4)))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (slowSpeed) {
                try {
                    track.playbackParams = android.media.PlaybackParams().apply {
                        speed = 0.80f
                        pitch = 1.0f
                    }
                } catch (_: Throwable) { }
            }

            activeAudioTrack = track
            track.play()

            var offset = 0
            val chunkSize = 4096
            while (offset < pcmBytes.size && activeAudioTrack === track) {
                val toWrite = minOf(chunkSize, pcmBytes.size - offset)
                val written = track.write(pcmBytes, offset, toWrite)
                if (written <= 0) break
                offset += written
            }
            if (activeAudioTrack === track) {
                try { track.stop() } catch (_: Throwable) { }
            }
            try { track.release() } catch (_: Throwable) { }
            if (activeAudioTrack === track) activeAudioTrack = null
            true
        } catch (_: Throwable) {
            activeAudioTrack = null
            false
        }
    }

    private fun stopCloudPlaybackOnly() {
        cloudTtsJob?.cancel()
        cloudTtsJob = null
        val track = activeAudioTrack
        activeAudioTrack = null
        if (track != null) {
            try { track.pause() } catch (_: Throwable) { }
            try { track.flush() } catch (_: Throwable) { }
            try { track.stop() } catch (_: Throwable) { }
            try { track.release() } catch (_: Throwable) { }
        }
    }

    fun stop() {
        stopCloudPlaybackOnly()
        try { tts?.stop() } catch (_: Throwable) { }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        scope.cancel()
        try { tts?.shutdown() } catch (_: Throwable) { }
        tts = null
    }
}
