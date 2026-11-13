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
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_MODEL = "gemini-3.5-flash-lite"

    val AVAILABLE_MODELS = listOf(
        "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite (Ultra Fast & Recommended)",
        "gemini-3.5-flash" to "Gemini 3.5 Flash (High Intelligence)",
        "gemini-3.8-flash" to "Gemini 3.8 Flash (Latest Next-Gen)",
        "gemini-flash-latest" to "Gemini Flash Latest"
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

        // Helpful pre-check for common key prefix confusions
        if (cleanKey.startsWith("cc_")) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "This key starts with 'cc_', which is for an OpenAI-compatible proxy, not direct Google Gemini.\n\n" +
                    "To use Google Gemini, enter your Gemini API key from Google AI Studio.\n" +
                    "If your 'cc_' key is for an OpenAI-compatible proxy, switch to Custom Endpoint mode."
                )
            )
        }

        // Models to try in order of preference
        val modelsToTry = mutableListOf<String>()
        val cleanPreferred = preferredModel.trim().ifEmpty { DEFAULT_MODEL }
        if (cleanPreferred.isNotEmpty()) {
            modelsToTry.add(cleanPreferred)
        }
        val fallbackCandidates = listOf(
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.8-flash",
            "gemini-flash-latest"
        )
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
                        put("maxOutputTokens", 60)
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

                    val isNoLongerAvailable = errorMsg.contains("no longer available", true) ||
                                              errorMsg.contains("deprecated", true) ||
                                              errorMsg.contains("not found", true)

                    // If API key is truly invalid (not just model deprecated), stop trying
                    if (!isNoLongerAvailable && response.code == 400 && errorMsg.contains("API_KEY_INVALID", true)) {
                        isAuthError = true
                        break
                    }
                    if (response.code == 403 && !isNoLongerAvailable) {
                        isAuthError = true
                        break
                    }
                }
            } catch (e: Exception) {
                lastErrorMessage = e.message ?: "Network error"
            }
        }

        Result.failure(Exception(if (isAuthError) lastErrorMessage else "Connection failed: $lastErrorMessage"))
    }

    /**
     * Verifies connection to an OpenAI-compatible custom endpoint or proxy.
     */
    suspend fun testCustomEndpoint(
        endpointUrl: String,
        apiKey: String,
        modelName: String = "gpt-4o-mini"
    ): Result<ApiKeyTestResult> = withContext(Dispatchers.IO) {
        val cleanUrl = endpointUrl.trim().removeSuffix("/")
        val cleanKey = apiKey.trim()
        val cleanModel = modelName.trim().ifEmpty { "gpt-4o-mini" }

        if (cleanUrl.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Endpoint URL cannot be empty."))
        }
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        val completionsUrl = if (cleanUrl.endsWith("/chat/completions")) cleanUrl else "$cleanUrl/chat/completions"

        try {
            val jsonBody = JSONObject().apply {
                put("model", cleanModel)
                val messages = JSONArray().apply {
                    val msg = JSONObject().apply {
                        put("role", "user")
                        put("content", "Reply with only 'READY'")
                    }
                    put(msg)
                }
                put("messages", messages)
                put("max_tokens", 10)
                put("temperature", 0.0)
            }

            val request = Request.Builder()
                .url(completionsUrl)
                .addHeader("Authorization", "Bearer $cleanKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                return@withContext Result.success(
                    ApiKeyTestResult(
                        message = "Custom endpoint connected successfully with model $cleanModel!",
                        workingModel = cleanModel
                    )
                )
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception("Custom endpoint error: $errorMsg"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Connection error: ${e.message ?: "Failed to reach endpoint."}"))
        }
    }

    /**
     * Performs comprehensive real-time speech evaluation using Gemini AI with full multi-turn conversation history.
     */
    suspend fun evaluateSpeech(
        apiKey: String,
        spokenText: String,
        targetPhrase: String? = null,
        conversationContext: String? = null,
        conversationHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        previousNovaQuestion: String? = null,
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

        // Try primary model, then fall back to fast real-time Gemini models
        val modelsToTry = mutableListOf<String>()
        val cleanModel = model.trim().ifEmpty { DEFAULT_MODEL }
        modelsToTry.add(cleanModel)
        listOf("gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-3.8-flash", "gemini-flash-latest").forEach { fallback ->
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
                    conversationHistory = conversationHistory,
                    previousNovaQuestion = previousNovaQuestion,
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

    /**
     * Performs speech evaluation using an OpenAI-compatible custom endpoint with multi-turn history.
     */
    suspend fun evaluateSpeechCustomEndpoint(
        endpointUrl: String,
        apiKey: String,
        modelName: String,
        spokenText: String,
        targetPhrase: String? = null,
        conversationContext: String? = null,
        conversationHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        previousNovaQuestion: String? = null
    ): Result<SpeakingEvaluation> = withContext(Dispatchers.IO) {
        val cleanUrl = endpointUrl.trim().removeSuffix("/")
        val cleanKey = apiKey.trim()
        val cleanSpoken = spokenText.trim()
        val cleanModel = modelName.trim().ifEmpty { "gpt-4o-mini" }

        if (cleanUrl.isEmpty() || cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Custom endpoint or API key is not configured."))
        }
        if (cleanSpoken.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No speech input was detected."))
        }

        val completionsUrl = if (cleanUrl.endsWith("/chat/completions")) cleanUrl else "$cleanUrl/chat/completions"

        try {
            val systemInstructions = buildSystemInstructions()
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)
            val jsonBody = JSONObject().apply {
                put("model", cleanModel)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstructions)
                    })
                    // Add previous dialogue history turns for authentic multi-turn context
                    if (priorHistory.isNotEmpty()) {
                        priorHistory.forEach { turn ->
                            val role = if (turn.isNova) "assistant" else "user"
                            val content = turn.audioPrompt.ifBlank { turn.text }
                            if (content.isNotBlank()) {
                                put(JSONObject().apply {
                                    put("role", role)
                                    put("content", content)
                                })
                            }
                        }
                    } else if (!conversationContext.isNullOrBlank()) {
                        put(JSONObject().apply {
                            put("role", "assistant")
                            put("content", conversationContext)
                        })
                    }
                    // Current student input
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", buildUserPrompt(cleanSpoken, targetPhrase, conversationContext, previousNovaQuestion))
                    })
                }
                put("messages", messages)
                put("temperature", 0.65)
            }

            val request = Request.Builder()
                .url(completionsUrl)
                .addHeader("Authorization", "Bearer $cleanKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "Custom endpoint HTTP ${response.code}"
                } catch (_: Exception) {
                    "Custom endpoint HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val choices = rootJson.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val message = firstChoice?.optJSONObject("message")
            val textOutput = message?.optString("content") ?: ""

            parseEvaluationOutput(textOutput, cleanSpoken, targetPhrase, cleanModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun stripTrailingDuplicateStudentTurn(
        history: List<com.example.data.model.NovaChatMessage>,
        cleanSpoken: String
    ): List<com.example.data.model.NovaChatMessage> {
        if (history.isEmpty()) return history
        val last = history.last()
        return if (!last.isNova && last.text.trim().equals(cleanSpoken, ignoreCase = true)) {
            history.dropLast(1)
        } else {
            history
        }
    }

    private fun buildSystemInstructions(): String = buildString {
        append("You are 'Nova', a warm, curious, and natural-sounding human-like Spoken English partner having a real live conversation with a student. ")
        append("Your goal is to talk like a real friend — listening closely to what the student just said, responding directly to their details, and keeping the conversation flowing naturally.\n\n")
        append("ASSESSMENT CRITERIA (Score 0-100 each):\n")
        append("1. pronunciationScore: phoneme clarity, intelligibility, natural sentence stress.\n")
        append("2. fluencyScore: natural conversational pacing, absence of awkward long hesitations.\n")
        append("3. grammarScore: correct verb tense, prepositions, subject-verb agreement.\n")
        append("4. vocabularyScore: natural expression, relevance, and variety.\n")
        append("5. overallScore: weighted overall score out of 100.\n")
        append("6. cefrLevel: CEFR proficiency level ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'].\n")
        append("7. feedback: 1 encouraging, supportive sentence evaluating what the student just spoke.\n")
        append("8. suggestedImprovement: 1 actionable, gentle tip for pronunciation or grammar.\n\n")
        append("CONVERSATION RULES FOR NOVA (aiCoachResponse) - CRITICAL FOR REAL HUMAN FLOW:\n")
        append("Formulate Nova's spoken reply ('aiCoachResponse') in 2 to 3 natural spoken English sentences (max 45 words total, NO markdown, NO bullet points, NO emojis, because it is read aloud by TTS):\n")
        append("- Sound like a REAL human: Vary your reactions naturally ('Oh nice!', 'That sounds fun!', 'I totally get that!', 'Haha, awesome!'). NEVER use robotic clichés like 'Well spoken!', 'Let's continue practicing:', or 'What is another detail you would like to share about <topic>?'.\n")
        append("- Answer back first if the student asked YOU a question (e.g., 'How are you?', 'How about you?', 'Do you like cricket?'), then smoothly continue.\n")
        append("- React directly to the specific name, city, hobby, feeling, food, time, person, or reason the student shared.\n")
        append("- Light grammar nudge (optional): Only mention a quick gentle correction if there is an obvious grammar slip; otherwise skip lecturing and just connect warmly.\n")
        append("- End with ONE specific, curiosity-driven followUpQuestion that grows directly out of what the student just answered. NEVER repeat a question already asked earlier in the conversation.\n\n")
        append("CONTEXTUAL AIDS:\n")
        append("- followUpQuestion: The exact single question string asked at the end of aiCoachResponse.\n")
        append("- hint: 1 short sentence guiding the student on how to answer your NEW followUpQuestion.\n")
        append("- exampleAnswers: A JSON array of 2 realistic, natural example answers directly replying to your NEW followUpQuestion.\n")
        append("- detectedWeakWords: A list of up to 3 words from their speech to practice pronouncing better.\n\n")
        append("Return ONLY a valid JSON object matching this schema:\n")
        append("{\n")
        append("  \"overallScore\": 85,\n")
        append("  \"pronunciationScore\": 82,\n")
        append("  \"fluencyScore\": 80,\n")
        append("  \"grammarScore\": 88,\n")
        append("  \"vocabularyScore\": 86,\n")
        append("  \"cefrLevel\": \"B1\",\n")
        append("  \"feedback\": \"...\",\n")
        append("  \"suggestedImprovement\": \"...\",\n")
        append("  \"aiCoachResponse\": \"...\",\n")
        append("  \"followUpQuestion\": \"...\",\n")
        append("  \"hint\": \"...\",\n")
        append("  \"exampleAnswers\": [\"...\", \"...\"],\n")
        append("  \"detectedWeakWords\": [\"...\"]\n")
        append("}")
    }

    private fun buildUserPrompt(
        cleanSpoken: String,
        targetPhrase: String?,
        conversationContext: String?,
        previousNovaQuestion: String? = null
    ): String = buildString {
        if (!previousNovaQuestion.isNullOrBlank()) {
            append("Previous Question Nova Asked: \"${previousNovaQuestion.trim()}\"\n")
        }
        append("Student's Spoken Answer: \"$cleanSpoken\"\n")
        if (!targetPhrase.isNullOrBlank()) {
            append("Target / Expected Context: \"$targetPhrase\"\n")
        }
        if (!conversationContext.isNullOrBlank()) {
            append("Conversation Topic / Context: \"$conversationContext\"\n")
        }
        append("Respond naturally like a real human partner based directly on the student's spoken answer, and ask ONE specific follow-up question that builds on what they just said.")
    }

    private fun executeSingleEvaluation(
        cleanKey: String,
        cleanSpoken: String,
        targetPhrase: String?,
        conversationContext: String?,
        conversationHistory: List<com.example.data.model.NovaChatMessage>,
        previousNovaQuestion: String?,
        model: String
    ): Result<SpeakingEvaluation> {
        try {
            val systemInstructions = buildSystemInstructions()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)

            val jsonBody = JSONObject().apply {
                // System Instruction block for Gemini API
                put("system_instruction", JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstructions) })
                    }
                    put("parts", parts)
                })

                val contents = JSONArray()

                // If we have multi-turn history, build authentic alternating contents turns
                if (priorHistory.isNotEmpty()) {
                    // Turn 0: User starts the session topic
                    val initialContextText = "Hello Nova! Let's start a natural English conversation.\n" +
                        (conversationContext ?: "Topic: English Conversation Practice")
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", initialContextText) })
                        })
                    })

                    // Previous turns: alternating model and user
                    var lastRole = "user"
                    for (msg in priorHistory) {
                        val role = if (msg.isNova) "model" else "user"
                        val text = msg.audioPrompt.ifBlank { msg.text }.trim()
                        if (text.isNotBlank()) {
                            if (role == lastRole) {
                                val lastObj = contents.getJSONObject(contents.length() - 1)
                                val partsArr = lastObj.getJSONArray("parts")
                                val existingPart = partsArr.getJSONObject(0)
                                val existingText = existingPart.getString("text")
                                existingPart.put("text", "$existingText\n\n$text")
                            } else {
                                contents.put(JSONObject().apply {
                                    put("role", role)
                                    put("parts", JSONArray().apply {
                                        put(JSONObject().apply { put("text", text) })
                                    })
                                })
                                lastRole = role
                            }
                        }
                    }

                    // Final Turn: Current student spoken response to evaluate
                    val currentTurnPrompt = buildUserPrompt(cleanSpoken, targetPhrase, null, previousNovaQuestion)
                    if (lastRole == "user") {
                        val lastObj = contents.getJSONObject(contents.length() - 1)
                        val partsArr = lastObj.getJSONArray("parts")
                        val existingPart = partsArr.getJSONObject(0)
                        val existingText = existingPart.getString("text")
                        existingPart.put("text", "$existingText\n\n$currentTurnPrompt")
                    } else {
                        contents.put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", currentTurnPrompt) })
                            })
                        })
                    }
                } else {
                    // Single-turn fallback when no history exists yet
                    val userPrompt = buildUserPrompt(cleanSpoken, targetPhrase, conversationContext, previousNovaQuestion)
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "$systemInstructions\n\n$userPrompt") })
                        })
                    })
                }

                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.65)
                    put("maxOutputTokens", 1024)
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

            return parseEvaluationOutput(textOutput, cleanSpoken, targetPhrase, model)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun parseEvaluationOutput(
        textOutput: String,
        cleanSpoken: String,
        targetPhrase: String?,
        model: String
    ): Result<SpeakingEvaluation> {
        return try {
            val trimmedOutput = textOutput.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val startIdx = trimmedOutput.indexOf('{')
            val endIdx = trimmedOutput.lastIndexOf('}')
            val cleanJsonStr = if (startIdx >= 0 && endIdx > startIdx) {
                trimmedOutput.substring(startIdx, endIdx + 1)
            } else {
                trimmedOutput
            }
            val parsedJson = JSONObject(cleanJsonStr)

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

            val followUpQuestion = parsedJson.optString("followUpQuestion", "").ifBlank { null }
            val hint = parsedJson.optString("hint", "").ifBlank { null }
            val exampleAnswersArray = parsedJson.optJSONArray("exampleAnswers")
            val exampleAnswers = mutableListOf<String>()
            if (exampleAnswersArray != null) {
                for (i in 0 until exampleAnswersArray.length()) {
                    val ans = exampleAnswersArray.optString(i)
                    if (ans.isNotBlank()) exampleAnswers.add(ans)
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
                detectedWeakWords = weakWords,
                followUpQuestion = followUpQuestion,
                hint = hint,
                exampleAnswers = exampleAnswers
            )

            Result.success(evaluation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
