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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    val shortAccentLabel: String = "Indian Voice",
    val preferredLanguageCode: String = "en",
    val nativeName: String = ""
) {
    ANANYA(
        id = "ANANYA",
        displayName = "Ananya",
        subtitle = "Warm & Clear Female Voice",
        basePitch = 1.00f,
        baseRate = 0.86f,
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
        baseRate = 0.89f,
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
        baseRate = 0.83f,
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
        baseRate = 0.85f,
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
        baseRate = 0.87f,
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
        baseRate = 0.85f,
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
        baseRate = 0.86f,
        geminiVoiceName = "Kore",
        stylePrompt = "Gentle, relaxed Australian English female speaking tutor voice",
        localeBadge = "en-AU",
        preferredCountryCode = "AU",
        shortAccentLabel = "Australian English"
    ),
    BARNALI(
        id = "BARNALI",
        displayName = "Barnali",
        subtitle = "Warm & Natural Assamese Female Voice",
        basePitch = 1.03f,
        baseRate = 0.88f,
        geminiVoiceName = "Aoede",
        stylePrompt = "warm, natural, authentic Assamese female voice from Assam",
        localeBadge = "as-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Assamese Voice",
        preferredLanguageCode = "as",
        nativeName = "বৰ্ণালী"
    ),
    JONALI(
        id = "JONALI",
        displayName = "Jonali",
        subtitle = "Sweet & Expressive Assamese Female Voice",
        basePitch = 1.05f,
        baseRate = 0.90f,
        geminiVoiceName = "Leda",
        stylePrompt = "sweet, lively, authentic Assamese female voice from Assam",
        localeBadge = "as-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Assamese Voice",
        preferredLanguageCode = "as",
        nativeName = "জোনালী"
    ),
    NAMRATA(
        id = "NAMRATA",
        displayName = "Namrata",
        subtitle = "Calm & Clear Assamese Teacher Voice",
        basePitch = 1.00f,
        baseRate = 0.85f,
        geminiVoiceName = "Zephyr",
        stylePrompt = "calm, gentle, authentic Assamese female teacher voice from Assam",
        localeBadge = "as-IN",
        preferredCountryCode = "IN",
        shortAccentLabel = "Assamese Voice",
        preferredLanguageCode = "as",
        nativeName = "নম্ৰতা"
    );

    val isAssameseVoice: Boolean
        get() = preferredLanguageCode.equals("as", ignoreCase = true)

    val isIndianVoice: Boolean
        get() = preferredCountryCode == "IN" && preferredLanguageCode.equals("en", ignoreCase = true)

    companion object {
        val DEFAULT: NovaVoicePersona = ANANYA

        fun fromId(id: String?): NovaVoicePersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}

enum class VoiceSpeedPreset(
    val id: String,
    val label: String,
    val androidMultiplier: Float,
    val cloudSpeed: Float
) {
    RELAXED(id = "RELAXED", label = "Slow & Clear", androidMultiplier = 0.88f, cloudSpeed = 0.80f),
    NATURAL(id = "NATURAL", label = "Natural", androidMultiplier = 1.00f, cloudSpeed = 0.90f),
    BRISK(id = "BRISK", label = "Standard", androidMultiplier = 1.12f, cloudSpeed = 1.00f);

    companion object {
        fun fromId(id: String?): VoiceSpeedPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NATURAL
        }
    }
}

internal data class PcmAudioPayload(
    val pcmBytes: ByteArray,
    val sampleRate: Int
)

private data class PendingSpeechRequest(
    val cleanText: String,
    val slowSpeed: Boolean
)

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _voicePersona = MutableStateFlow(NovaVoicePersona.ANANYA)
    val voicePersona: StateFlow<NovaVoicePersona> = _voicePersona.asStateFlow()

    private val _voiceSpeedPreset = MutableStateFlow(VoiceSpeedPreset.NATURAL)
    val voiceSpeedPreset: StateFlow<VoiceSpeedPreset> = _voiceSpeedPreset.asStateFlow()

    private val _activeVoiceLabel = MutableStateFlow("Ananya • Indian Female (en-IN)")
    val activeVoiceLabel: StateFlow<String> = _activeVoiceLabel.asStateFlow()

    private val _cloudHumanVoiceEnabled = MutableStateFlow(true)
    val cloudHumanVoiceEnabled: StateFlow<Boolean> = _cloudHumanVoiceEnabled.asStateFlow()

    private var isInitialized = false
    private var primaryIndianVoice: Voice? = null
    private var localFallbackVoice: Voice? = null
    private var activeAndroidVoice: Voice? = null
    private var activeAndroidVoiceConfirmedFemale: Boolean = false
    private var lastAppliedVoiceName: String? = null
    private var lastAppliedPitch: Float? = null
    private var lastAppliedRate: Float? = null
    private var pendingSpeechRequest: PendingSpeechRequest? = null
    private var lastUtteranceGroupId: Long = 0L
    private var lastChunkIdForGroup: String? = null
    private var lastSpokenText: String = ""
    private var lastSlowSpeed: Boolean = false
    private val utteranceCounter = AtomicLong(0L)

    // Optional Cloud Gemini Native Audio Human Voice support
    private var apiKeyProvider: (() -> String)? = null
    private var activeAudioTrack: AudioTrack? = null
    private var cloudTtsJob: Job? = null
    private var prewarmJob: Job? = null
    private var cloudTtsAuthBlocked = false
    private var cloudTtsCooldownUntilMs = 0L

    private val cloudAudioCacheLock = Any()
    private val cloudAudioCache = object : LinkedHashMap<String, PcmAudioPayload>(48, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PcmAudioPayload>?): Boolean {
            return size > 48
        }
    }

    private val cloudTtsClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(6500, TimeUnit.MILLISECONDS)
            .writeTimeout(3000, TimeUnit.MILLISECONDS)
            .callTimeout(8000, TimeUnit.MILLISECONDS)
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
        if (_voicePersona.value.isAssameseVoice) {
            prewarmPersonaPreview(_voicePersona.value)
        }
    }

    fun setVoicePersona(persona: NovaVoicePersona) {
        _voicePersona.value = persona
        updateActiveVoiceLabel()
        if (isInitialized) {
            applyBestVoiceForPersona()
        }
        if (persona.isAssameseVoice) {
            prewarmPersonaPreview(persona)
        }
    }

    private fun prewarmPersonaPreview(persona: NovaVoicePersona) {
        if (!isCloudTtsReady() || _isSpeaking.value) return
        val key = try {
            apiKeyProvider?.invoke()?.trim().orEmpty()
        } catch (_: Throwable) {
            ""
        }
        if (key.isBlank() || key.startsWith("cc_")) return

        val sample = cleanTextForSpeech(getPreviewSampleForPersona(persona))
        val firstChunk = splitIntoCloudChunks(sample).firstOrNull()?.takeIf { it.isNotBlank() } ?: return
        val cacheKey = buildCloudCacheKey(persona, firstChunk)
        val alreadyCached = synchronized(cloudAudioCacheLock) { cloudAudioCache.containsKey(cacheKey) }
        if (alreadyCached) return

        prewarmJob?.cancel()
        prewarmJob = scope.launch(Dispatchers.IO) {
            try {
                fetchOrCachedCloudChunk(firstChunk, key, persona)
            } catch (_: Throwable) { }
        }
    }

    fun setVoiceSpeedPreset(preset: VoiceSpeedPreset) {
        _voiceSpeedPreset.value = preset
        if (isInitialized) {
            try {
                val targetRate = effectiveAndroidSpeechRate(_voicePersona.value, slowSpeed = false)
                tts?.setSpeechRate(targetRate)
                lastAppliedRate = targetRate
            } catch (_: Throwable) { }
        }
    }

    internal fun effectiveAndroidSpeechRate(persona: NovaVoicePersona, slowSpeed: Boolean): Float {
        if (slowSpeed) return 0.68f
        return (persona.baseRate * _voiceSpeedPreset.value.androidMultiplier).coerceIn(0.68f, 1.05f)
    }

    internal fun effectiveCloudPlaybackSpeed(slowSpeed: Boolean): Float {
        if (slowSpeed) return 0.74f
        return _voiceSpeedPreset.value.cloudSpeed
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
            isCloudTtsReady() && hasKey && p.isAssameseVoice -> "HD Assamese Female (as-IN)"
            isCloudTtsReady() && hasKey -> "HD Female (${p.localeBadge})"
            p.isAssameseVoice -> "Assamese Female (as-IN)"
            p.isIndianVoice && primaryIndianVoice?.locale?.country.equals("IN", ignoreCase = true) -> "Indian Female (en-IN)"
            else -> "${p.shortAccentLabel} (${p.localeBadge})"
        }
        val titlePrefix = if (p.nativeName.isNotBlank()) "${p.displayName} (${p.nativeName})" else p.displayName
        _activeVoiceLabel.value = "$titlePrefix • $modeSuffix"
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

                val pending = pendingSpeechRequest
                pendingSpeechRequest = null
                if (pending != null) {
                    speakWithAndroidProsody(pending.cleanText, pending.slowSpeed)
                }
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
                lastAppliedVoiceName = fallback.name
                lastAppliedPitch = null
                lastAppliedRate = null
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
        return scoreVoiceSignature(
            descriptor = voiceDescriptor(voice),
            country = voice.locale?.country.orEmpty(),
            quality = voice.quality,
            isNetworkRequired = voice.isNetworkConnectionRequired,
            preferredCountry = "IN"
        )
    }

    private fun scoreVoiceForCountry(voice: Voice, preferredCountry: String): Int {
        return scoreVoiceSignature(
            descriptor = voiceDescriptor(voice),
            country = voice.locale?.country.orEmpty(),
            quality = voice.quality,
            isNetworkRequired = voice.isNetworkConnectionRequired,
            preferredCountry = preferredCountry
        )
    }

    private fun applyBestVoiceForPersona(forceAssameseScript: Boolean = false) {
        val engine = tts ?: return
        val persona = _voicePersona.value
        try {
            val voices = engine.voices?.toList().orEmpty()
            val wantAssamese = persona.isAssameseVoice || forceAssameseScript

            if (wantAssamese) {
                // 1. Prefer strictly confirmed female Assamese (as-IN) voices first
                val confirmedAssameseFemales = voices.filter { v ->
                    v.locale?.language.equals("as", ignoreCase = true) &&
                        isConfirmedFemaleVoice(v)
                }.sortedByDescending { scoreVoiceForCountry(it, "IN") }

                // 2. Next prefer strictly confirmed female Indian Bengali (bn-IN, never bn-BD male/neutral) voices
                val confirmedBengaliInFemales = if (confirmedAssameseFemales.isEmpty()) {
                    voices.filter { v ->
                        v.locale?.language.equals("bn", ignoreCase = true) &&
                            v.locale?.country.equals("IN", ignoreCase = true) &&
                            isConfirmedFemaleVoice(v)
                    }.sortedByDescending { scoreVoiceForCountry(it, "IN") }
                } else {
                    emptyList()
                }

                val confirmedPool = confirmedAssameseFemales.ifEmpty { confirmedBengaliInFemales }
                val fastConfirmedPool = confirmedPool.filter { !it.isNetworkConnectionRequired }.ifEmpty { confirmedPool }

                // 3. Last-resort Indic fallback: strictly non-male as-IN or bn-IN voices (never bn-BD or known male IDs)
                val nonMaleIndicFallback = if (fastConfirmedPool.isEmpty()) {
                    voices.filter { v ->
                        val lang = v.locale?.language.orEmpty()
                        val country = v.locale?.country.orEmpty()
                        (lang.equals("as", ignoreCase = true) ||
                            (lang.equals("bn", ignoreCase = true) && !country.equals("BD", ignoreCase = true))) &&
                            !matchesMaleSignature(voiceDescriptor(v))
                    }.sortedByDescending { scoreVoiceForCountry(it, "IN") }
                } else {
                    emptyList()
                }

                // Only pick alternate voice indices from CONFIRMED female local voices so we never land on a male or laggy voice
                val chosenIndic = when {
                    fastConfirmedPool.isNotEmpty() -> when (persona) {
                        NovaVoicePersona.BARNALI -> fastConfirmedPool[0]
                        NovaVoicePersona.JONALI -> fastConfirmedPool.getOrElse(1) { fastConfirmedPool[0] }
                        NovaVoicePersona.NAMRATA -> fastConfirmedPool.getOrElse(2) { fastConfirmedPool[0] }
                        else -> fastConfirmedPool[0]
                    }
                    nonMaleIndicFallback.isNotEmpty() -> nonMaleIndicFallback[0]
                    else -> null
                }

                if (chosenIndic != null) {
                    activeAndroidVoice = chosenIndic
                    activeAndroidVoiceConfirmedFemale = isConfirmedFemaleVoice(chosenIndic)
                    chosenIndic.locale?.let { loc ->
                        try {
                            if (engine.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
                                engine.setLanguage(loc)
                            }
                        } catch (_: Throwable) { }
                    }
                    if (lastAppliedVoiceName != chosenIndic.name) {
                        engine.voice = chosenIndic
                        lastAppliedVoiceName = chosenIndic.name
                    }
                } else {
                    activeAndroidVoice = null
                    activeAndroidVoiceConfirmedFemale = false
                    val asLocale = Locale("as", "IN")
                    val bnLocale = Locale("bn", "IN")
                    try {
                        when {
                            engine.isLanguageAvailable(asLocale) >= TextToSpeech.LANG_AVAILABLE -> engine.setLanguage(asLocale)
                            engine.isLanguageAvailable(bnLocale) >= TextToSpeech.LANG_AVAILABLE -> engine.setLanguage(bnLocale)
                            else -> {
                                engine.setLanguage(Locale("en", "IN"))
                                primaryIndianVoice?.let {
                                    engine.voice = it
                                    activeAndroidVoice = it
                                    activeAndroidVoiceConfirmedFemale = isConfirmedFemaleVoice(it)
                                    lastAppliedVoiceName = it.name
                                }
                            }
                        }
                        // Guard: if setLanguage() auto-selected a known male system voice, try to keep female metadata accurate
                        val postVoice = try { engine.voice } catch (_: Throwable) { null }
                        if (postVoice != null) {
                            activeAndroidVoice = postVoice
                            activeAndroidVoiceConfirmedFemale = isConfirmedFemaleVoice(postVoice)
                            lastAppliedVoiceName = postVoice.name
                        }
                    } catch (_: Throwable) { }
                }
            } else {
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
                        NovaVoicePersona.GRACE,
                        NovaVoicePersona.BARNALI,
                        NovaVoicePersona.JONALI,
                        NovaVoicePersona.NAMRATA -> preferredFemales[0]
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
                    if (lastAppliedVoiceName != chosen.name) {
                        engine.voice = chosen
                        lastAppliedVoiceName = chosen.name
                    }
                } else {
                    val targetLocale = Locale("en", targetCountry)
                    try {
                        if (engine.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                            engine.setLanguage(targetLocale)
                        }
                    } catch (_: Throwable) { }
                }
            }

            val effectivePitch = when {
                activeAndroidVoiceConfirmedFemale -> persona.basePitch
                wantAssamese -> (persona.basePitch + 0.14f).coerceIn(1.12f, 1.22f)
                else -> (persona.basePitch + 0.08f).coerceAtMost(1.12f)
            }
            val effectiveRate = effectiveAndroidSpeechRate(persona, slowSpeed = false)
            if (lastAppliedPitch != effectivePitch) {
                engine.setPitch(effectivePitch)
                lastAppliedPitch = effectivePitch
            }
            if (lastAppliedRate != effectiveRate) {
                engine.setSpeechRate(effectiveRate)
                lastAppliedRate = effectiveRate
            }
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
            // Add subtle conversational pauses around dashes
            .replace(Regex("\\s+[—–-]\\s+"), ", ")
            // Remove emojis & pictographs
            .replace(Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
            // Remove markdown bold/italic/headers
            .replace(Regex("[*#_~`]+"), "")
            // Remove bullet points / dashes at start of lines
            .replace(Regex("^[\\s*•-]+", RegexOption.MULTILINE), "")
            // Convert newlines into subtle sentence breaks if needed
            .replace(Regex("\\n+"), ". ")
            .replace(Regex("([।!?])\\s*\\."), "$1")
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
            .split(Regex("(?<=[.!?।])\\s+"))
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

    /**
     * Splits cleaned text into a compact first cloud-TTS bite (<= 48 chars for Assamese, <= 85 chars for English)
     * plus grouped follow-up chunks (<= 180 chars) so playback starts quickly on Chunk 0 while Chunk 1+ pipelines behind it.
     */
    internal fun splitIntoCloudChunks(clean: String): List<String> {
        val sentences = splitIntoProsodyChunks(clean)
        if (sentences.isEmpty()) return listOf(clean)

        val isAssamese = containsAssameseScript(clean)
        val firstChunkMax = if (isAssamese) 48 else 85

        val expandedFirst = if (sentences.first().length > firstChunkMax) {
            val firstSentence = sentences.first()
            val commaSplit = Regex("^(.{14,$firstChunkMax}?[,;:])\\s+(.+)$").find(firstSentence)
            if (commaSplit != null) {
                listOf(commaSplit.groupValues[1].trim(), commaSplit.groupValues[2].trim()) + sentences.drop(1)
            } else if (isAssamese) {
                // Split long Assamese opening sentence cleanly on a word boundary so Chunk 0 synthesizes fast
                val splitIdx = firstSentence.lastIndexOf(' ', firstChunkMax).takeIf { it >= 16 }
                if (splitIdx != null) {
                    listOf(
                        firstSentence.substring(0, splitIdx).trim(),
                        firstSentence.substring(splitIdx + 1).trim()
                    ) + sentences.drop(1)
                } else {
                    sentences
                }
            } else {
                sentences
            }
        } else {
            sentences
        }

        if (expandedFirst.size == 1) return expandedFirst

        val result = mutableListOf<String>()
        result.add(expandedFirst.first())

        var buffer = StringBuilder()
        val followUpMax = if (isAssamese) 130 else 180
        for (part in expandedFirst.drop(1)) {
            if (buffer.isNotEmpty() && buffer.length + part.length + 1 > followUpMax && result.size < 5) {
                result.add(buffer.toString().trim())
                buffer = StringBuilder(part)
            } else {
                if (buffer.isNotEmpty()) buffer.append(" ")
                buffer.append(part)
            }
        }
        if (buffer.isNotEmpty()) {
            result.add(buffer.toString().trim())
        }
        return result.ifEmpty { listOf(clean) }
    }

    @androidx.annotation.VisibleForTesting
    var speakAttemptCount: Int = 0
        private set

    internal fun hasPendingSpeechForTesting(): Boolean = pendingSpeechRequest != null

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

        // If Cloud Human Voice is enabled and a direct Gemini API key is configured, use chunk-pipelined HD female synthesis
        // with strict onset deadline and immediate offline fallback so speech never stalls.
        if (isCloudTtsReady() &&
            key.isNotBlank() &&
            !key.startsWith("cc_") &&
            clean.length <= 550
        ) {
            speakWithCloudHumanVoice(clean, key, slowSpeed)
            return
        }

        fallbackToAndroidSpeech(clean, slowSpeed)
    }

    private fun fallbackToAndroidSpeech(clean: String, slowSpeed: Boolean) {
        if (!isInitialized || tts == null) {
            pendingSpeechRequest = PendingSpeechRequest(clean, slowSpeed)
            return
        }
        pendingSpeechRequest = null
        speakWithAndroidProsody(clean, slowSpeed)
    }

    private fun getPreviewSampleForPersona(p: NovaVoicePersona): String = when (p) {
        NovaVoicePersona.BARNALI ->
            "নমস্কাৰ! মই বৰ্ণালী। মই আপোনাৰ নভা এআই বন্ধু, গণিত, বিজ্ঞান বা যিকোনো বিষয়ত মই আপোনাক সহজকৈ অসমীয়াত সহায় কৰিম!"
        NovaVoicePersona.JONALI ->
            "হেল্লো! মই জোনালী। আজি আমি একেলগে নতুন কথা শিকোঁ আহক, যিকোনো প্ৰশ্ন সোধক মই সাজু আছোঁ!"
        NovaVoicePersona.NAMRATA ->
            "নমস্কাৰ! মই নম্ৰতা। লাহে লাহে আৰু স্পষ্টকৈ আমি প্ৰতিটো পাঠ বুজি ল'ম, আপোনাৰ লগত থাকি ভাল লাগিছে।"
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

    fun previewVoice() {
        speak(getPreviewSampleForPersona(_voicePersona.value), slowSpeed = false)
    }

    private fun containsAssameseScript(text: String): Boolean {
        return text.any { ch -> ch in '\u0980'..'\u09FF' }
    }

    private fun buildCloudCacheKey(persona: NovaVoicePersona, chunk: String): String {
        return "v2|${persona.id}|${persona.geminiVoiceName}|$chunk"
    }

    private fun speakWithAndroidProsody(clean: String, slowSpeed: Boolean) {
        val engine = tts ?: return
        if (!isInitialized) return

        val persona = _voicePersona.value
        val textIsAssamese = containsAssameseScript(clean)
        val wantAssamese = textIsAssamese || persona.isAssameseVoice
        val currentVoiceLang = activeAndroidVoice?.locale?.language.orEmpty()
        val needsEnglishRestore = !wantAssamese && currentVoiceLang.isNotBlank() && !currentVoiceLang.equals("en", ignoreCase = true)
        if (wantAssamese || needsEnglishRestore) {
            applyBestVoiceForPersona(forceAssameseScript = textIsAssamese)
        }
        val baseRate = effectiveAndroidSpeechRate(persona, slowSpeed)
        val effectivePitch = when {
            activeAndroidVoiceConfirmedFemale -> persona.basePitch
            wantAssamese -> (persona.basePitch + 0.14f).coerceIn(1.12f, 1.22f)
            else -> (persona.basePitch + 0.08f).coerceAtMost(1.12f)
        }

        // If on-device Android TTS is using a bn-IN female voice for Assamese text, normalize Assamese-only glyphs
        // (ৰ, ৱ, and mid-word apostrophes like ল'ম) so the offline engine pronounces every syllable cleanly instead of skipping them.
        val activeLang = activeAndroidVoice?.locale?.language.orEmpty()
        val engineReadyText = if (textIsAssamese && !activeLang.equals("as", ignoreCase = true)) {
            prepareAssameseForBengaliFallback(clean)
        } else {
            clean
        }
        val chunks = splitIntoProsodyChunks(engineReadyText)
        val groupId = utteranceCounter.incrementAndGet()
        lastUtteranceGroupId = groupId

        try {
            // Avoid redundant engine.stop() / binder voice resets unless needed; QUEUE_FLUSH on chunk 0 clears prior speech immediately
            if (_isSpeaking.value) {
                engine.stop()
            }
            activeAndroidVoice?.let { v ->
                if (lastAppliedVoiceName != v.name) {
                    engine.voice = v
                    lastAppliedVoiceName = v.name
                }
            }
            if (lastAppliedPitch != effectivePitch) {
                engine.setPitch(effectivePitch)
                lastAppliedPitch = effectivePitch
            }
            if (lastAppliedRate != baseRate) {
                engine.setSpeechRate(baseRate)
                lastAppliedRate = baseRate
            }
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
                    val pauseMs = if (chunk.endsWith("?") || chunk.endsWith("!")) 220L else 170L
                    engine.playSilentUtterance(pauseMs, TextToSpeech.QUEUE_ADD, "Speakly_${groupId}_pause_$idx")
                }
            } catch (_: Throwable) { }
        }
    }

    private suspend fun fetchOrCachedCloudChunk(
        chunk: String,
        apiKey: String,
        persona: NovaVoicePersona
    ): PcmAudioPayload? {
        val chunkCacheKey = buildCloudCacheKey(persona, chunk)
        val cached = synchronized(cloudAudioCacheLock) { cloudAudioCache[chunkCacheKey] }
        if (cached != null) return cached
        return fetchGeminiAudioBytes(chunk, apiKey, persona)?.also { fetched ->
            if (fetched.pcmBytes.isNotEmpty() && fetched.pcmBytes.size <= 900_000) {
                synchronized(cloudAudioCacheLock) {
                    cloudAudioCache[chunkCacheKey] = fetched
                }
            }
        }
    }

    private fun speakWithCloudHumanVoice(cleanText: String, apiKey: String, slowSpeed: Boolean) {
        val requestGroupId = utteranceCounter.incrementAndGet()
        lastUtteranceGroupId = requestGroupId
        prewarmJob?.cancel()
        prewarmJob = null
        cloudTtsJob = scope.launch {
            _isSpeaking.value = true
            val persona = _voicePersona.value
            val chunks = splitIntoCloudChunks(cleanText)
            try {
                // Fetch ONLY Chunk 0 first (no concurrent burst that triggers Gemini TTS rate-limits/queueing)
                val firstChunkDeadlineMs = if (persona.isAssameseVoice || containsAssameseScript(cleanText)) {
                    CLOUD_ASSAMESE_FIRST_CHUNK_DEADLINE_MS
                } else {
                    CLOUD_FIRST_CHUNK_DEADLINE_MS
                }
                val firstPayload = withTimeoutOrNull(firstChunkDeadlineMs) {
                    withContext(Dispatchers.IO) {
                        chunks.firstOrNull()?.let { fetchOrCachedCloudChunk(it, apiKey, persona) }
                    }
                }

                if (!isActive || utteranceCounter.get() != requestGroupId) {
                    return@launch
                }

                if (firstPayload == null || firstPayload.pcmBytes.isEmpty()) {
                    _isSpeaking.value = false
                    fallbackToAndroidSpeech(cleanText, slowSpeed)
                    return@launch
                }

                val unplayedRemainder = withContext(Dispatchers.IO) {
                    streamCloudChunksToAudioTrack(
                        firstPayload = firstPayload,
                        chunks = chunks,
                        apiKey = apiKey,
                        persona = persona,
                        slowSpeed = slowSpeed,
                        requestGroupId = requestGroupId
                    )
                }

                if (!isActive || utteranceCounter.get() != requestGroupId) return@launch
                _isSpeaking.value = false
                if (!unplayedRemainder.isNullOrBlank()) {
                    fallbackToAndroidSpeech(unplayedRemainder, slowSpeed)
                }
            } catch (_: CancellationException) {
                // Intentionally suppressed when a new utterance or stop() cancels playback
            } catch (_: Throwable) {
                if (isActive && utteranceCounter.get() == requestGroupId) {
                    _isSpeaking.value = false
                    fallbackToAndroidSpeech(cleanText, slowSpeed)
                }
            }
        }
    }

    private suspend fun fetchGeminiAudioBytes(
        cleanText: String,
        apiKey: String,
        persona: NovaVoicePersona
    ): PcmAudioPayload? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=$apiKey"
        // Guide Gemini 2.5 Flash TTS with its official concise style director for Assamese so it uses genuine
        // Assamese female phonetics (Assam, India) rather than drifting into a Bengali accent.
        val ttsPrompt = when {
            containsAssameseScript(cleanText) ->
                "Say in a ${persona.stylePrompt}: $cleanText"
            persona.isAssameseVoice ->
                "Say in a warm, natural Indian Assamese female voice: $cleanText"
            else -> cleanText
        }
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", ttsPrompt) })
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

        return try {
            val (httpCode, isSuccess, respStr) = executeCancellableCall(cloudTtsClient.newCall(request))
            if (isSuccess && respStr.isNotBlank()) {
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
            } else if (!isSuccess) {
                val lowerErr = respStr.lowercase(Locale.ROOT)
                val isPermanentAuthError = httpCode == 401 ||
                    httpCode == 403 ||
                    lowerErr.contains("api_key_invalid") ||
                    lowerErr.contains("api key not valid") ||
                    lowerErr.contains("permission_denied")
                if (isPermanentAuthError) {
                    cloudTtsAuthBlocked = true
                    updateActiveVoiceLabel()
                    return null
                }
                if (httpCode >= 400) {
                    cloudTtsCooldownUntilMs = System.currentTimeMillis() + 10_000L
                    updateActiveVoiceLabel()
                }
            }
            null
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            if (currentCoroutineContext().isActive && !cloudTtsAuthBlocked) {
                cloudTtsCooldownUntilMs = System.currentTimeMillis() + 10_000L
                updateActiveVoiceLabel()
            }
            null
        }
    }

    private suspend fun executeCancellableCall(call: Call): Triple<Int, Boolean, String> =
        suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isActive) {
                        cont.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        val bodyStr = try {
                            resp.body?.string().orEmpty()
                        } catch (_: Throwable) {
                            ""
                        }
                        if (cont.isActive) {
                            cont.resume(Triple(resp.code, resp.isSuccessful, bodyStr))
                        }
                    }
                }
            })
        }

    private fun buildStreamAudioTrack(sampleRate: Int, slowSpeed: Boolean): AudioTrack {
        val cleanRate = sampleRate.coerceIn(16000, 48000)
        val minBuf = AudioTrack.getMinBufferSize(
            cleanRate,
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
                    .setSampleRate(cleanRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(8192))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            track.setVolume(AudioTrack.getMaxVolume())
        } catch (_: Throwable) { }

        val targetSpeed = effectiveCloudPlaybackSpeed(slowSpeed)
        if (kotlin.math.abs(targetSpeed - 1.0f) > 0.01f) {
            try {
                track.playbackParams = PlaybackParams().apply {
                    speed = targetSpeed
                    pitch = 1.0f
                }
            } catch (_: Throwable) { }
        }
        return track
    }

    private suspend fun streamCloudChunksToAudioTrack(
        firstPayload: PcmAudioPayload,
        chunks: List<String>,
        apiKey: String,
        persona: NovaVoicePersona,
        slowSpeed: Boolean,
        requestGroupId: Long
    ): String? = coroutineScope {
        var currentRate = firstPayload.sampleRate.coerceIn(16000, 48000)
        var cleanupTrack: AudioTrack? = null
        var nextDeferred: Deferred<PcmAudioPayload?>? = null
        try {
            var currentTrack = buildStreamAudioTrack(currentRate, slowSpeed)
            cleanupTrack = currentTrack
            activeAudioTrack = currentTrack
            currentTrack.play()

            // Pipeline ONLY 1 chunk ahead while Chunk 0 plays to keep speech gapless without rate-limit bursts
            if (chunks.size > 1) {
                nextDeferred = async(Dispatchers.IO) {
                    fetchOrCachedCloudChunk(chunks[1], apiKey, persona)
                }
            }

            if (!writePcmBytes(currentTrack, firstPayload.pcmBytes, requestGroupId)) {
                nextDeferred?.cancel()
                releaseAudioTrackSafely(currentTrack)
                return@coroutineScope null
            }

            for (i in 1 until chunks.size) {
                currentCoroutineContext().ensureActive()
                if (activeAudioTrack !== currentTrack || utteranceCounter.get() != requestGroupId) {
                    nextDeferred?.cancel()
                    releaseAudioTrackSafely(currentTrack)
                    return@coroutineScope null
                }

                val nextPayload = withTimeoutOrNull(3500L) {
                    nextDeferred?.await()
                }
                nextDeferred = null

                if (nextPayload == null || nextPayload.pcmBytes.isEmpty()) {
                    // Drain whatever played so far and hand off remainder cleanly to Android female TTS
                    drainAndReleaseTrack(currentTrack, currentRate, 80L, requestGroupId)
                    return@coroutineScope chunks.drop(i).joinToString(" ")
                }

                // Start fetching Chunk i+1 ONLY as Chunk i begins playing
                if (i + 1 < chunks.size) {
                    val upcomingChunk = chunks[i + 1]
                    nextDeferred = async(Dispatchers.IO) {
                        fetchOrCachedCloudChunk(upcomingChunk, apiKey, persona)
                    }
                }

                val nextRate = nextPayload.sampleRate.coerceIn(16000, 48000)
                if (nextRate != currentRate) {
                    drainAndReleaseTrack(currentTrack, currentRate, 80L, requestGroupId)
                    currentRate = nextRate
                    currentTrack = buildStreamAudioTrack(currentRate, slowSpeed)
                    cleanupTrack = currentTrack
                    activeAudioTrack = currentTrack
                    currentTrack.play()
                } else {
                    writeSilencePadding(currentTrack, currentRate, 85L, requestGroupId)
                }

                if (!writePcmBytes(currentTrack, nextPayload.pcmBytes, requestGroupId)) {
                    nextDeferred?.cancel()
                    releaseAudioTrackSafely(currentTrack)
                    return@coroutineScope null
                }
            }

            // Pad ~150ms of trailing silence so the hardware buffer drains the final syllable cleanly without clipping
            drainAndReleaseTrack(currentTrack, currentRate, 150L, requestGroupId)
            null
        } catch (ce: CancellationException) {
            nextDeferred?.cancel()
            releaseAudioTrackSafely(cleanupTrack)
            throw ce
        } catch (_: Throwable) {
            nextDeferred?.cancel()
            releaseAudioTrackSafely(cleanupTrack)
            chunks.joinToString(" ")
        }
    }

    private fun writePcmBytes(track: AudioTrack, pcmBytes: ByteArray, requestGroupId: Long): Boolean {
        var offset = 0
        val chunkSize = 4096
        while (offset < pcmBytes.size &&
            activeAudioTrack === track &&
            utteranceCounter.get() == requestGroupId
        ) {
            val toWrite = minOf(chunkSize, pcmBytes.size - offset)
            val written = track.write(pcmBytes, offset, toWrite)
            if (written <= 0) return false
            offset += written
        }
        return activeAudioTrack === track && utteranceCounter.get() == requestGroupId
    }

    private fun writeSilencePadding(track: AudioTrack, sampleRate: Int, ms: Long, requestGroupId: Long) {
        if (activeAudioTrack !== track || utteranceCounter.get() != requestGroupId) return
        val rawSilenceBytes = ((sampleRate * 2 * ms) / 1000L).toInt()
        val silenceBytes = ByteArray(if (rawSilenceBytes % 2 == 0) rawSilenceBytes else rawSilenceBytes + 1)
        track.write(silenceBytes, 0, silenceBytes.size)
    }

    private fun drainAndReleaseTrack(track: AudioTrack, sampleRate: Int, silenceMs: Long, requestGroupId: Long) {
        if (activeAudioTrack === track && utteranceCounter.get() == requestGroupId) {
            writeSilencePadding(track, sampleRate, silenceMs, requestGroupId)
            try { track.stop() } catch (_: Throwable) { }
        }
        releaseAudioTrackSafely(track)
    }

    private fun releaseAudioTrackSafely(track: AudioTrack?) {
        if (track == null) return
        try { track.release() } catch (_: Throwable) { }
        if (activeAudioTrack === track) activeAudioTrack = null
    }

    private fun stopCloudPlaybackOnly() {
        prewarmJob?.cancel()
        prewarmJob = null
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
        pendingSpeechRequest = null
        utteranceCounter.incrementAndGet()
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
        internal const val CLOUD_FIRST_CHUNK_DEADLINE_MS = 1500L
        internal const val CLOUD_ASSAMESE_FIRST_CHUNK_DEADLINE_MS = 2600L

        private val femaleTokenRegex = Regex(
            "(^|[^a-z0-9])(female|woman|enc|ene|ena|ef|asf|asx|bnf|bnx|sangeeta|veena|lekha|neelam|aditi|kajal|raveena|priya|piya|tanishaa|gba|gbc|gbg|fis|iog|sfg|tpf|tessa|samantha|karen|moira|serena|victoria|f00|f01|f02|f)([^a-z0-9]|$)"
        )
        private val maleTokenRegex = Regex(
            "(^|[^a-z0-9])(male|man|rishi|prabhat|end|ahp|asm|asb|asd|bnm|bnd|bin|ban|bna|bnb|bnc|bnj|bnk|bng|daniel|arthur|oliver|george|alex|fred|m00|m01|iol|iom|rjs|tpd)([^a-z0-9]|$)"
        )
        private val sampleRateRegex = Regex("rate=(\\d+)", RegexOption.IGNORE_CASE)
        private val assameseMidWordApostropheRegex = Regex("([\\u0980-\\u09FF])['’]([\\u0980-\\u09FF])")

        /**
         * Normalizes Assamese-only Unicode glyphs (ৰ, ৱ, and mid-word apostrophes) ONLY when offline Android
         * fallback uses a bn-IN female voice, preventing skipped letters and garbled words.
         */
        internal fun prepareAssameseForBengaliFallback(text: String): String {
            return text
                .replace(assameseMidWordApostropheRegex, "$1$2")
                .replace('\u09F0', '\u09B0') // Assamese ৰ -> র
                .replace("োৱা", "োয়া")
                .replace("াৱা", "াওয়া")
                .replace("েৱা", "েওয়া")
                .replace('\u09F1', '\u09AC') // Remaining Assamese ৱ -> ব
        }

        internal fun scoreVoiceSignature(
            descriptor: String,
            country: String,
            quality: Int,
            isNetworkRequired: Boolean,
            preferredCountry: String
        ): Int {
            val combined = descriptor.lowercase(Locale.ROOT)
            val normalizedCountry = country.uppercase(Locale.ROOT).let { if (it == "UK") "GB" else it }
            val target = preferredCountry.uppercase(Locale.ROOT).let { if (it == "UK") "GB" else it }

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
                quality >= Voice.QUALITY_VERY_HIGH -> 380
                quality >= Voice.QUALITY_HIGH -> 260
                quality >= Voice.QUALITY_NORMAL -> 120
                else -> 20
            }
            if (combined.contains("neural") || combined.contains("wavenet")) score += 180
            if (!isNetworkRequired && quality >= Voice.QUALITY_NORMAL) score += 320
            return score
        }

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
