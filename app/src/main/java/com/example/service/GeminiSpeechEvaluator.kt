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

    const val DEFAULT_MODEL = "gemini-2.5-flash"

    val AVAILABLE_MODELS = listOf(
        "gemini-2.5-flash" to "Gemini 2.5 Flash (Recommended - Ultra Fast)",
        "gemini-2.0-flash" to "Gemini 2.0 Flash (Fast & Balanced)",
        "gemini-1.5-flash" to "Gemini 1.5 Flash (Standard)"
    )

    /**
     * Verifies whether an API key is valid by making a lightweight test ping to the Gemini API.
     */
    suspend fun testApiKey(apiKey: String, model: String = DEFAULT_MODEL): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"

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
                Result.success("Connection Successful! Model $model is active and ready for real-time speech evaluation.")
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP error ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "HTTP error ${response.code}: ${response.message}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (textOutput.isBlank()) {
                return@withContext Result.failure(Exception("Gemini returned an empty response."))
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

            Result.success(evaluation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
