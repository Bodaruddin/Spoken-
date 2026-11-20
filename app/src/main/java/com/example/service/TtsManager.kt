package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.PlaybackParams
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
    val stylePrompt: String,
    val localeBadge: String = "en-IN",
    val preferredCountryCode: String = "IN",
    val shortAccentLabel: String = "Indian Voice"
) {
    ANANYA(
        id = "ANANYA",
        displayName = "Ananya",
        subtitle = "Warm & Clear Female Voice",
        basePitch = 1.00f,
        baseRate = 0.96f,
        geminiVoiceName = "Aoede",
        stylePrompt = "Warm, natural, crystal-clear Indian English female mentor voice",
        localeBadge = "en-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Indian Voice"
    ),
    PRIYA(
        id = "PRIYA",
        displayName = "Priya",
        subtitle = "Bright & Lively Female Voice",
        basePitch = 1.03f,
        baseRate = 1.00f,
        geminiVoiceName = "Zephyr",
        stylePrompt = "Bright, cheerful, expressive Indian English female friend voice",
        localeBadge = "en-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Indian Voice"
    ),
    MEERA(
        id = "MEERA",
        displayName = "Meera",
        subtitle = "Calm & Articulate Female Voice",
        basePitch = 0.98f,
        baseRate = 0.92f,
        geminiVoiceName = "Kore",
        stylePrompt = "Calm, gentle, articulate Indian English female teacher voice",
        localeBadge = "en-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Indian Voice"
    ),
    CLARA(
        id = "CLARA",
        displayName = "Clara",
        subtitle = "Crisp British English Female",
        basePitch = 1.00f,
        baseRate = 0.95f,
        geminiVoiceName = "Leda",
        stylePrompt = "Crisp, clear British English female pronunciation coach voice",
        localeBadge = "en-GB",
        preferredCountryCode = "GB",
        shortAccentLabel = "British English"
    ),
    EMMA(
        id = "EMMA",
        displayName = "Emma",
        subtitle = "Natural American English Female",
        basePitch = 1.01f,
        baseRate = 0.97f,
        geminiVoiceName = "Aoede",
        stylePrompt = "Natural, fluent American English female conversation partner voice",
        localeBadge = "en-US",
        preferredCountryCode = "US",
        shortAccentLabel = "American English"
    ),
    SOPHIA(
        id = "SOPHIA",
        displayName = "Sophia",
        subtitle = "Warm Studio English Female",
        basePitch = 0.99f,
        baseRate = 0.94f,
        geminiVoiceName = "Zephyr",
        stylePrompt = "Warm, studio-clear American English female mentor voice",
        localeBadge = "en-US",
        preferredCountryCode = "US",
        shortAccentLabel = "American English"
    ),
    GRACE(
        id = "GRACE",
        displayName = "Grace",
        subtitle = "Gentle Everyday English Female",
        basePitch = 1.02f,
        baseRate = 0.96f,
        geminiVoiceName = "Kore",
        stylePrompt = "Gentle, relaxed Australian English female speaking tutor voice",
        localeBadge = "en-AU",
        preferredCountryCode = "AU",
        shortAccentLabel = "Australian English"
    );

    val isIndianVoice: Boolean
        get() = preferredCountryCode == "IN"

    companion object {
        fun fromId(id: String?): NovaVoicePersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ANANYA
        }
    }
}

internal data class PcmAudioPayload(
    val pcmBytes: ByteArray,
    val sampleRate: Int
)

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
    private var cloudTtsAuthBlocked = false
    private var cloudTtsCooldownUntilMs = 0L

    private val cloudAudioCacheLock = Any()
    private val cloudAudioCache = object : LinkedHashMap<String, PcmAudioPayload>(24, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PcmAudioPayload>?): Boolean {
            return size > 24
        }
    }

    private val cloudTtsClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    fun setGeminiApiKeyProvider(provider: () -> String) {
        apiKeyProvider = provider
        refreshCloudVoiceAvailability()
    }

    fun refreshCloudVoiceAvailability() {
        cloudTtsAuthBlocked = false
        cloudTtsCooldownUntilMs = 0L
        updateActiveVoiceLabel()
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
        if (enabled) {
            cloudTtsAuthBlocked = false
            cloudTtsCooldownUntilMs = 0L
        }
        updateActiveVoiceLabel()
    }

    private fun isCloudTtsReady(): Boolean {
        val now = System.currentTimeMillis()
        return _cloudHumanVoiceEnabled.value && !cloudTtsAuthBlocked && now >= cloudTtsCooldownUntilMs
    }

    private fun updateActiveVoiceLabel() {
        val p = _voicePersona.value
        val hasKey = try {
            apiKeyProvider?.invoke()?.trim()?.isNotBlank() == true
        } catch (_: Throwable) {
            false
        }
        val modeSuffix = when {
            isCloudTtsReady() && hasKey -> "HD Female (${p.localeBadge})"
            p.isIndianVoice && primaryIndianVoice?.locale?.country.equals("IN", ignoreCase = true) -> "Indian Female (en-IN)"
            else -> "${p.shortAccentLabel} (${p.localeBadge})"
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
        // If a network voice failed (e.g. offline device), gracefully switch to local female voice and retry once
        val fallback = localFallbackVoice
        val current = try { tts?.voice } catch (_: Throwable) { null }
        if (fallback != null && current != null && current.name != fallback.name && lastSpokenText.isNotBlank()) {
            try {
                tts?.voice = fallback
                primaryIndianVoice = fallback
                activeAndroidVoice = fallback
                activeAndroidVoiceConfirmedFemale = isConfirmedFemaleVoice(fallback)
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

    private fun voiceDescriptor(voice: Voice): String {
        val name = voice.name.lowercase(Locale.ROOT)
        val features = try {
            voice.features?.joinToString(" ")?.lowercase(Locale.ROOT).orEmpty()
        } catch (_: Throwable) {
            ""
        }
        return "$name $features"
    }

    internal fun isConfirmedFemaleVoice(voice: Voice): Boolean {
        return isConfirmedFemaleSignature(voiceDescriptor(voice))
    }

    private fun scoreIndianFemaleVoice(voice: Voice): Int {
        val combined = voiceDescriptor(voice)
        val country = voice.locale?.country.orEmpty().uppercase(Locale.ROOT)

        var score = 0
        // Locale priority: Indian English first, then UK, AU, US
        score += when (country) {
            "IN" -> 1200
            "GB", "UK" -> 420
            "AU" -> 260
            "US" -> 240
            else -> 100
        }

        if (femaleTokenRegex.containsMatchIn(combined)) {
            score += 750
        }

        if (matchesMaleSignature(combined)) {
            score -= 1500
        }

        // Voice synthesis quality
        score += when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> 380
            voice.quality >= Voice.QUALITY_HIGH -> 260
            voice.quality >= Voice.QUALITY_NORMAL -> 120
            else -> 20
        }

        // Prefer natural neural / wavenet voices; prefer offline-ready high-quality voices for zero lag & no dropouts
        if (combined.contains("neural") || combined.contains("wavenet")) score += 180
        if (!voice.isNetworkConnectionRequired) score += 90

        return score
    }

    private fun scoreVoiceForCountry(voice: Voice, preferredCountry: String): Int {
        val combined = voiceDescriptor(voice)
        val country = voice.locale?.country.orEmpty().uppercase(Locale.ROOT)
        val normalizedCountry = if (country == "UK") "GB" else country
        val target = if (preferredCountry == "UK") "GB" else preferredCountry

        var score = 0
        score += when {
            normalizedCountry == target -> 1400
            normalizedCountry == "GB" -> 480
            normalizedCountry == "US" -> 440
            normalizedCountry == "AU" -> 420
            normalizedCountry == "IN" -> 400
            else -> 120
        }
        if (femaleTokenRegex.containsMatchIn(combined)) {
            score += 750
        }
        if (matchesMaleSignature(combined)) {
            score -= 1500
        }
        score += when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> 380
            voice.quality >= Voice.QUALITY_HIGH -> 260
            voice.quality >= Voice.QUALITY_NORMAL -> 120
            else -> 20
        }
        if (combined.contains("neural") || combined.contains("wavenet")) score += 180
        if (!voice.isNetworkConnectionRequired) score += 90
        return score
    }

    private fun applyBestVoiceForPersona() {
        val engine = tts ?: return
        val persona = _voicePersona.value
        try {
            val voices = engine.voices?.toList().orEmpty()
            val targetCountry = if (persona.preferredCountryCode == "UK") "GB" else persona.preferredCountryCode
            val preferredFemales = voices.filter { v ->
                val vCountry = v.locale?.country.orEmpty().uppercase(Locale.ROOT).let { if (it == "UK") "GB" else it }
                v.locale?.language.equals("en", ignoreCase = true) &&
                    vCountry == targetCountry &&
                    !matchesMaleSignature(voiceDescriptor(v)) &&
                    scoreVoiceForCountry(v, targetCountry) > 1000
            }.sortedByDescending { scoreVoiceForCountry(it, targetCountry) }

            val anyEnglishFemales = if (preferredFemales.isEmpty()) {
                voices.filter { v ->
                    v.locale?.language.equals("en", ignoreCase = true) &&
                        !matchesMaleSignature(voiceDescriptor(v))
                }.sortedByDescending { scoreVoiceForCountry(it, targetCountry) }
            } else {
                emptyList()
            }

            val chosen = when {
                preferredFemales.isNotEmpty() -> when (persona) {
                    NovaVoicePersona.ANANYA -> preferredFemales[0]
                    NovaVoicePersona.PRIYA -> preferredFemales.getOrElse(1) { preferredFemales[0] }
                    NovaVoicePersona.MEERA -> preferredFemales.getOrElse(2) { preferredFemales[0] }
                    NovaVoicePersona.SOPHIA -> preferredFemales.getOrElse(1) { preferredFemales[0] }
                    NovaVoicePersona.CLARA,
                    NovaVoicePersona.EMMA,
                    NovaVoicePersona.GRACE -> preferredFemales[0]
                }
                anyEnglishFemales.isNotEmpty() -> anyEnglishFemales.first()
                else -> primaryIndianVoice
            }

            if (chosen != null) {
                activeAndroidVoice = chosen
                activeAndroidVoiceConfirmedFemale = isConfirmedFemaleVoice(chosen)
                chosen.locale?.let { loc ->
                    try {
                        if (engine.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
                            engine.setLanguage(loc)
                        }
                    } catch (_: Throwable) { }
                }
                engine.voice = chosen
            } else {
                val targetLocale = Locale("en", targetCountry)
                try {
                    if (engine.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        engine.setLanguage(targetLocale)
                    }
                } catch (_: Throwable) { }
            }

            val effectivePitch = if (activeAndroidVoiceConfirmedFemale) {
                persona.basePitch
            } else {
                (persona.basePitch + 0.08f).coerceAtMost(1.12f)
            }
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
     * Splits cleaned text into sentence-sized chunks so long replies never hit TTS buffer limits,
     * keeping pitch & rate uniform across the whole utterance for natural coarticulation.
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
                val subParts = sentence.split(Regex("(?<=[,;:])\\s+"))
                var buffer = StringBuilder()
                for (part in subParts) {
                    if (buffer.isNotEmpty() && buffer.length + part.length > 140) {
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

        // If Cloud Human Voice is enabled and a direct Gemini API key is configured, synthesize natural female human audio
        // for both normal conversations and slow 0.78x Phonetics drills, with instant Android neural fallback.
        if (isCloudTtsReady() &&
            key.isNotBlank() &&
            !key.startsWith("cc_") &&
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
                "Hello! I'm Meera. Take a deep breath and speak comfortably with me. I'll help you sound clear and confident."
            NovaVoicePersona.CLARA ->
                "Hello! I'm Clara. Let's practice crisp British English pronunciation and natural everyday expressions together."
            NovaVoicePersona.EMMA ->
                "Hi! I'm Emma. We'll practice smooth, fluent American English conversations so you sound confident everywhere."
            NovaVoicePersona.SOPHIA ->
                "Hey there, I'm Sophia! Speak naturally with me, and I'll guide you step by step with warm feedback."
            NovaVoicePersona.GRACE ->
                "Hello! I'm Grace. Relax and chat with me in easy everyday English, one clear sentence at a time."
        }
        speak(sample, slowSpeed = false)
    }

    private fun speakWithAndroidProsody(clean: String, slowSpeed: Boolean) {
        val engine = tts ?: return
        if (!isInitialized) return

        val persona = _voicePersona.value
        val baseRate = if (slowSpeed) 0.72f else persona.baseRate
        val effectivePitch = if (activeAndroidVoiceConfirmedFemale) {
            persona.basePitch
        } else {
            (persona.basePitch + 0.08f).coerceAtMost(1.12f)
        }
        val chunks = splitIntoProsodyChunks(clean)
        val groupId = utteranceCounter.incrementAndGet()
        lastUtteranceGroupId = groupId

        try {
            engine.stop()
            // Re-assert Indian female voice and stable studio pitch/rate ONCE per utterance group
            activeAndroidVoice?.let { engine.voice = it }
            engine.setPitch(effectivePitch)
            engine.setSpeechRate(baseRate)
        } catch (_: Throwable) { }

        val bundle = Bundle()
        chunks.forEachIndexed { idx, chunk ->
            val utteranceId = "Speakly_${groupId}_$idx"
            if (idx == chunks.lastIndex) {
                lastChunkIdForGroup = utteranceId
            }

            try {
                val queueMode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                engine.speak(chunk, queueMode, bundle, utteranceId)

                if (idx < chunks.lastIndex) {
                    engine.playSilentUtterance(90L, TextToSpeech.QUEUE_ADD, "Speakly_${groupId}_pause_$idx")
                }
            } catch (_: Throwable) { }
        }
    }

    private fun speakWithCloudHumanVoice(cleanText: String, apiKey: String, slowSpeed: Boolean) {
        cloudTtsJob = scope.launch {
            _isSpeaking.value = true
            try {
                val persona = _voicePersona.value
                val cacheKey = "${persona.id}|$cleanText"
                val cached = synchronized(cloudAudioCacheLock) { cloudAudioCache[cacheKey] }

                val audioPayload = cached ?: withContext(Dispatchers.IO) {
                    fetchGeminiAudioBytes(cleanText, apiKey, persona)?.also { fetched ->
                        synchronized(cloudAudioCacheLock) {
                            cloudAudioCache[cacheKey] = fetched
                        }
                    }
                }

                if (!isActive) return@launch

                if (audioPayload != null && audioPayload.pcmBytes.isNotEmpty()) {
                    val played = withContext(Dispatchers.IO) {
                        playPcmAudio(audioPayload, slowSpeed)
                    }
                    if (!isActive) return@launch
                    _isSpeaking.value = false
                    if (!played && isInitialized && tts != null) {
                        speakWithAndroidProsody(cleanText, slowSpeed)
                    }
                } else {
                    if (!isActive) return@launch
                    _isSpeaking.value = false
                    if (isInitialized && tts != null) {
                        speakWithAndroidProsody(cleanText, slowSpeed)
                    }
                }
            } catch (_: CancellationException) {
                // Intentionally suppressed when a new utterance or stop() cancels playback
            } catch (_: Throwable) {
                if (isActive) {
                    _isSpeaking.value = false
                    if (isInitialized && tts != null) {
                        speakWithAndroidProsody(cleanText, slowSpeed)
                    }
                }
            }
        }
    }

    private fun fetchGeminiAudioBytes(
        cleanText: String,
        apiKey: String,
        persona: NovaVoicePersona
    ): PcmAudioPayload? {
        val candidateModels = listOf(
            "gemini-2.5-flash-preview-tts",
            "gemini-2.5-pro-preview-tts"
        )
        var anyAttempted = false
        for (modelName in candidateModels) {
            anyAttempted = true
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                // Pass clean spoken text directly so Gemini TTS never speaks stage directions aloud
                val body = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", cleanText) })
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
                cloudTtsClient.newCall(request).execute().use { response ->
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
                                val mimeType = inlineData?.optString("mimeType").orEmpty()
                                if (b64.isNotBlank()) {
                                    val rawBytes = Base64.decode(b64, Base64.DEFAULT)
                                    val payload = extractPcmFromAudioPayload(rawBytes, mimeType)
                                    if (payload.pcmBytes.isNotEmpty()) {
                                        return payload
                                    }
                                }
                            }
                        }
                    } else {
                        val lowerErr = respStr.lowercase(Locale.ROOT)
                        val isPermanentAuthError = response.code == 401 ||
                            response.code == 403 ||
                            lowerErr.contains("api_key_invalid") ||
                            lowerErr.contains("api key not valid") ||
                            lowerErr.contains("permission_denied")
                        if (isPermanentAuthError) {
                            cloudTtsAuthBlocked = true
                            updateActiveVoiceLabel()
                            return null
                        }
                        // On model-specific 400/404/429, continue to the next candidate model
                    }
                }
            } catch (_: Throwable) { }
        }
        if (anyAttempted && !cloudTtsAuthBlocked) {
            // Brief 60-second cooldown on transient/model outage instead of permanent session disable
            cloudTtsCooldownUntilMs = System.currentTimeMillis() + 60_000L
            updateActiveVoiceLabel()
        }
        return null
    }

    private fun playPcmAudio(payload: PcmAudioPayload, slowSpeed: Boolean = false): Boolean {
        val pcmBytes = payload.pcmBytes
        val sampleRate = payload.sampleRate.coerceIn(16000, 48000)
        return try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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

            try {
                track.setVolume(AudioTrack.getMaxVolume())
            } catch (_: Throwable) { }

            if (slowSpeed) {
                try {
                    track.playbackParams = PlaybackParams().apply {
                        speed = 0.78f
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

            // Pad ~150ms of trailing silence so the hardware buffer drains the final syllable cleanly without clipping
            if (activeAudioTrack === track) {
                val rawSilenceBytes = ((sampleRate * 2 * 150L) / 1000L).toInt()
                val silenceBytes = ByteArray(if (rawSilenceBytes % 2 == 0) rawSilenceBytes else rawSilenceBytes + 1)
                track.write(silenceBytes, 0, silenceBytes.size)
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

    companion object {
        private val femaleTokenRegex = Regex(
            "(^|[^a-z0-9])(female|woman|enc|ene|ena|ef|sangeeta|veena|lekha|neelam|aditi|kajal|raveena|priya|gba|gbc|gbg|fis|iog|sfg|tpf|tessa|samantha|karen|moira|serena|victoria|f00|f01|f02|f)([^a-z0-9]|$)"
        )
        private val maleTokenRegex = Regex(
            "(^|[^a-z0-9])(male|man|rishi|prabhat|end|ahp|daniel|arthur|oliver|george|alex|fred|m00|m01|iol|iom|rjs|tpd)([^a-z0-9]|$)"
        )
        private val sampleRateRegex = Regex("rate=(\\d+)", RegexOption.IGNORE_CASE)

        internal fun isConfirmedFemaleSignature(descriptor: String): Boolean {
            val combined = descriptor.lowercase(Locale.ROOT)
            if (matchesMaleSignature(combined)) return false
            return femaleTokenRegex.containsMatchIn(combined)
        }

        internal fun matchesMaleSignature(descriptor: String): Boolean {
            val strippedFemale = descriptor.lowercase(Locale.ROOT)
                .replace("female", " ")
                .replace("woman", " ")
            return maleTokenRegex.containsMatchIn(strippedFemale)
        }

        internal fun extractPcmFromAudioPayload(rawBytes: ByteArray, mimeType: String?): PcmAudioPayload {
            var sampleRate = sampleRateRegex.find(mimeType.orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 24000
            var pcm = rawBytes

            // Strip 44-byte RIFF/WAVE header if the API returns audio/wav instead of raw PCM
            if (rawBytes.size > 44 &&
                rawBytes[0] == 'R'.code.toByte() &&
                rawBytes[1] == 'I'.code.toByte() &&
                rawBytes[2] == 'F'.code.toByte() &&
                rawBytes[3] == 'F'.code.toByte() &&
                rawBytes[8] == 'W'.code.toByte() &&
                rawBytes[9] == 'A'.code.toByte() &&
                rawBytes[10] == 'V'.code.toByte() &&
                rawBytes[11] == 'E'.code.toByte()
            ) {
                val wavRate = (rawBytes[24].toInt() and 0xFF) or
                    ((rawBytes[25].toInt() and 0xFF) shl 8) or
                    ((rawBytes[26].toInt() and 0xFF) shl 16) or
                    ((rawBytes[27].toInt() and 0xFF) shl 24)
                if (wavRate in 8000..48000) {
                    sampleRate = wavRate
                }
                // Locate "data" subchunk dynamically in case extra chunks precede data
                var offset = 12
                var dataStart = 44
                while (offset + 8 <= rawBytes.size) {
                    val chunkId = String(rawBytes, offset, 4, Charsets.US_ASCII)
                    val chunkSize = (rawBytes[offset + 4].toInt() and 0xFF) or
                        ((rawBytes[offset + 5].toInt() and 0xFF) shl 8) or
                        ((rawBytes[offset + 6].toInt() and 0xFF) shl 16) or
                        ((rawBytes[offset + 7].toInt() and 0xFF) shl 24)
                    if (chunkId == "data") {
                        dataStart = offset + 8
                        break
                    }
                    if (chunkSize < 0) break
                    offset += 8 + chunkSize + (chunkSize and 1)
                }
                if (dataStart < rawBytes.size) {
                    pcm = rawBytes.copyOfRange(dataStart, rawBytes.size)
                }
            }

            // Ensure even byte count for 16-bit PCM samples
            if (pcm.size % 2 != 0 && pcm.size > 1) {
                pcm = pcm.copyOfRange(0, pcm.size - 1)
            }
            return PcmAudioPayload(pcmBytes = pcm, sampleRate = sampleRate)
        }
    }
}
