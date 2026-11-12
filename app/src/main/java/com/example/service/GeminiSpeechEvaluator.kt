package com.example.service

import com.example.data.model.SpeakingEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiSpeechEvaluator {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_MODEL = "gemini-3.5-flash-lite"

    val AVAILABLE_MODELS = listOf(
        "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite (Recommended - Ultra Fast & Stable)",
        "gemini-3.5-flash" to "Gemini 3.5 Flash (High Intelligence)",
        "gemini-3.8-flash" to "Gemini 3.8 Flash (Latest Flagship)",
        "gemini-3.1-flash-lite" to "Gemini 3.1 Flash Lite (Fast Fallback)"
    )

    data class ApiKeyTestResult(
        val message: String,
        val workingModel: String
    )

    /**
     * Verifies whether an API key is valid by making a lightweight test ping to the Gemini API.
     * Automatically attempts fallback models if the preferred model is retired or overloaded.
     */
    suspend fun testApiKey(apiKey: String, preferredModel: String = DEFAULT_MODEL): Result<ApiKeyTestResult> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        // Models to try in order of preference
        val modelsToTry = mutableListOf<String>()
        val cleanPreferred = preferredModel.trim()
        if (cleanPreferred.isNotEmpty()) {
            modelsToTry.add(cleanPreferred)
        }
        val fallbackCandidates = listOf("gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-3.8-flash", "gemini-3.1-flash-lite")
        for (candidate in fallbackCandidates) {
            if (!modelsToTry.contains(candidate)) {
                modelsToTry.add(candidate)
            }
        }

        var lastErrorMessage = ""
        var isAuthError = false

        for (candidateModel in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$candidateModel:generateContent?key=$cleanKey"
                val jsonBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", "Reply with only 'READY'") })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.0)
                        put("maxOutputTokens", 10)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val msg = if (candidateModel == cleanPreferred) {
                        "Connection Successful! Model $candidateModel is verified and ready for real-time speech evaluation."
                    } else {
                        "Connection Successful! Connected with $candidateModel (updated from $cleanPreferred)."
                    }
                    return@withContext Result.success(ApiKeyTestResult(msg, candidateModel))
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        val errorObj = errJson.optJSONObject("error")
                        errorObj?.optString("message") ?: "HTTP error ${response.code}: ${response.message}"
                    } catch (_: Exception) {
                        "HTTP error ${response.code}: ${response.message}"
                    }
                    lastErrorMessage = errorMsg

                    // If API key is invalid or permission denied, stop trying other models
                    if (response.code == 400 && (errorMsg.contains("API_KEY_INVALID", true) || errorMsg.contains("key", true))) {
                        isAuthError = true
                        break
                    }
                    if (response.code == 403) {
                        isAuthError = true
                        break
                    }
                    // If it was a 404 (model deprecated) or 503 (high demand), continue loop to next model
                }
            } catch (e: Exception) {
                lastErrorMessage = e.message ?: "Network error"
            }
        }

        Result.failure(Exception(if (isAuthError) lastErrorMessage else "Connection failed: $lastErrorMessage"))
    }

    /**
     * Performs comprehensive real-time speech evaluation using Gemini AI.
     */
    suspend fun evaluateSpeech(
        apiKey: String,
        spokenText: String,
        targetPhrase: String? = null,
        conversationContext: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<SpeakingEvaluation> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val cleanSpoken = spokenText.trim()

        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        if (cleanSpoken.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No speech input was detected."))
        }

        // Try primary model, then fall back to gemini-3.5-flash-lite / gemini-3.5-flash if unavailable
        val modelsToTry = mutableListOf<String>()
        val cleanModel = model.trim().ifEmpty { DEFAULT_MODEL }
        modelsToTry.add(cleanModel)
        listOf("gemini-3.5-flash-lite", "gemini-3.5-flash").forEach { fallback ->
            if (!modelsToTry.contains(fallback)) modelsToTry.add(fallback)
        }

        var lastError: Exception? = null

        for (activeModel in modelsToTry) {
            try {
                val evalResult = executeSingleEvaluation(
                    cleanKey = cleanKey,
                    cleanSpoken = cleanSpoken,
                    targetPhrase = targetPhrase,
                    conversationContext = conversationContext,
                    model = activeModel
                )
                if (evalResult.isSuccess) {
                    return@withContext evalResult
                } else {
                    lastError = evalResult.exceptionOrNull() as? Exception
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Speech evaluation failed."))
    }

    private fun executeSingleEvaluation(
        cleanKey: String,
        cleanSpoken: String,
        targetPhrase: String?,
        conversationContext: String?,
        model: String
    ): Result<SpeakingEvaluation> {
        try {
            val systemInstructions = buildString {
                append("You are 'Nova', an expert AI Spoken English Coach and CEFR-certified speech assessor. ")
                append("Evaluate the user's spoken sentence in real-time. ")
                append("Assess 4 key criteria (0-100 each): ")
                append("1. pronunciationScore: intelligibility, natural phoneme accuracy, syllable stress. ")
                append("2. fluencyScore: natural conversational flow, sentence rhythm, absence of unnatural filler. ")
                append("3. grammarScore: correct tense, subject-verb agreement, natural phrasing. ")
                append("4. vocabularyScore: word choice appropriateness, lexical variety. ")
                append("5. overallScore: weighted score out of 100. ")
                append("6. cefrLevel: one of ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']. ")
                append("7. feedback: 1-2 warm, encouraging sentences as Nova giving constructive feedback. ")
                append("8. suggestedImprovement: 1 concrete actionable phonetic or grammatical tip. ")
                append("9. aiCoachResponse: Nova's natural spoken conversational reply to continue the dialogue (1-2 sentences). ")
                append("10. detectedWeakWords: list of up to 3 words the speaker could pronounce better or practice. ")
                append("Return ONLY a valid JSON object with these exact keys: ")
                append("overallScore, pronunciationScore, fluencyScore, grammarScore, vocabularyScore, cefrLevel, feedback, suggestedImprovement, aiCoachResponse, detectedWeakWords.")
            }

            val userPrompt = buildString {
                append("Spoken Sentence by Student: \"$cleanSpoken\"\n")
                if (!targetPhrase.isNullOrBlank()) {
                    append("Expected / Target Sentence: \"$targetPhrase\"\n")
                }
                if (!conversationContext.isNullOrBlank()) {
                    append("Previous Coach Prompt / Topic Context: \"$conversationContext\"\n")
                }
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstructions\n\n$userPrompt")
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "Gemini API HTTP ${response.code}"
                } catch (_: Exception) {
                    "Gemini API HTTP ${response.code}"
                }
                return Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (textOutput.isBlank()) {
                return Result.failure(Exception("Gemini returned an empty response."))
            }

            val parsedJson = JSONObject(textOutput)

            val overallScore = parsedJson.optInt("overallScore", 80).coerceIn(10, 100)
            val pronunciationScore = parsedJson.optInt("pronunciationScore", 80).coerceIn(10, 100)
            val fluencyScore = parsedJson.optInt("fluencyScore", 80).coerceIn(10, 100)
            val grammarScore = parsedJson.optInt("grammarScore", 80).coerceIn(10, 100)
            val vocabularyScore = parsedJson.optInt("vocabularyScore", 80).coerceIn(10, 100)
            val cefrLevel = parsedJson.optString("cefrLevel", "B1")
            val feedback = parsedJson.optString("feedback", "Good effort! Keep speaking consistently.")
            val suggestedImprovement = parsedJson.optString("suggestedImprovement", "Focus on clear syllable pronunciation.")
            val aiCoachResponse = parsedJson.optString("aiCoachResponse", "Nice job! Let's continue practicing.")

            val weakWordsArray = parsedJson.optJSONArray("detectedWeakWords")
            val weakWords = mutableListOf<String>()
            if (weakWordsArray != null) {
                for (i in 0 until weakWordsArray.length()) {
                    val w = weakWordsArray.optString(i)
                    if (w.isNotBlank()) weakWords.add(w)
                }
            }

            // Target word accuracy analysis
            val phonemeMatches = mutableListOf<Pair<String, Boolean>>()
            if (!targetPhrase.isNullOrBlank()) {
                val targetWords = targetPhrase.lowercase().replace("[^a-z0-9 ]".toRegex(), "").split("\\s+".toRegex())
                val spokenLower = cleanSpoken.lowercase()
                for (tw in targetWords) {
                    phonemeMatches.add(tw to spokenLower.contains(tw))
                }
            }

            val evaluation = SpeakingEvaluation(
                spokenText = cleanSpoken,
                expectedText = targetPhrase,
                overallScore = overallScore,
                pronunciationScore = pronunciationScore,
                fluencyScore = fluencyScore,
                grammarScore = grammarScore,
                vocabularyScore = vocabularyScore,
                feedback = feedback,
                suggestedImprovement = suggestedImprovement,
                phonemeAccuracy = phonemeMatches,
                isAiEvaluated = true,
                aiModelUsed = model,
                aiCoachResponse = aiCoachResponse,
                cefrLevel = cefrLevel,
                detectedWeakWords = weakWords
            )

            return Result.success(evaluation)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
