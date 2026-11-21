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
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_MODEL = "gemini-2.5-flash"

    val AVAILABLE_MODELS = listOf(
        "gemini-2.5-flash" to "Gemini 2.5 Flash (Fast, Smart & All-Topics)",
        "gemini-2.5-flash-lite" to "Gemini 2.5 Flash Lite (Ultra Fast)",
        "gemini-flash-latest" to "Gemini Flash Latest (Auto-Updated)",
        "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite",
        "gemini-3.5-flash" to "Gemini 3.5 Flash",
        "gemini-3.8-flash" to "Gemini 3.8 Flash"
    )

    data class ApiKeyTestResult(
        val message: String,
        val workingModel: String
    )

    data class GrammarEvaluationResult(
        val hasMistake: Boolean,
        val correctedSentence: String?,
        val explanation: String,
        val naturalEnglish: String?,
        val overallScore: Int,
        val pronunciationScore: Int,
        val fluencyScore: Int,
        val grammarScore: Int,
        val vocabularyScore: Int,
        val cefrLevel: String,
        val feedback: String,
        val suggestedImprovement: String,
        val detectedWeakWords: List<String>
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
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-flash-latest",
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.8-flash"
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
     * Performs comprehensive two-step real-time speech/text evaluation using Gemini AI:
     * Step 1: Dedicated grammar & mistake evaluation on the complete student response.
     * Step 2: Nova's teacher reply & ONE connected question using the full conversation history.
     */
    suspend fun evaluateSpeech(
        apiKey: String,
        spokenText: String,
        targetPhrase: String? = null,
        conversationContext: String? = null,
        conversationHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        previousNovaQuestion: String? = null,
        model: String = DEFAULT_MODEL,
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1",
        isVoiceInput: Boolean = false
    ): Result<SpeakingEvaluation> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val cleanSpoken = spokenText.trim()

        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        if (cleanSpoken.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No speech input was detected."))
        }

        val modelsToTry = mutableListOf<String>()
        val cleanModel = model.trim().ifEmpty { DEFAULT_MODEL }
        modelsToTry.add(cleanModel)
        listOf(
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-flash-latest",
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.8-flash"
        ).forEach { fallback ->
            if (!modelsToTry.contains(fallback)) modelsToTry.add(fallback)
        }

        var lastError: Exception? = null

        for (activeModel in modelsToTry) {
            try {
                val evalResult = executeTwoStepEvaluationGemini(
                    cleanKey = cleanKey,
                    cleanSpoken = cleanSpoken,
                    targetPhrase = targetPhrase,
                    conversationContext = conversationContext,
                    conversationHistory = conversationHistory,
                    previousNovaQuestion = previousNovaQuestion,
                    model = activeModel,
                    topicTitle = topicTitle,
                    topicCategory = topicCategory,
                    difficultyLevel = difficultyLevel,
                    currentTurn = currentTurn,
                    targetTurns = targetTurns,
                    studentCefrLevel = studentCefrLevel,
                    isVoiceInput = isVoiceInput
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
     * Performs two-step speech/text evaluation using an OpenAI-compatible custom endpoint with full multi-turn history.
     */
    suspend fun evaluateSpeechCustomEndpoint(
        endpointUrl: String,
        apiKey: String,
        modelName: String,
        spokenText: String,
        targetPhrase: String? = null,
        conversationContext: String? = null,
        conversationHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        previousNovaQuestion: String? = null,
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1",
        isVoiceInput: Boolean = false
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
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)
            val previouslyAsked = collectPreviouslyAskedQuestions(priorHistory, previousNovaQuestion)

            // STEP 1: Dedicated Grammar Evaluation Step
            val grammarStepResult = evaluateGrammarStepCustomEndpoint(
                completionsUrl = completionsUrl,
                cleanKey = cleanKey,
                cleanModel = cleanModel,
                cleanSpoken = cleanSpoken,
                previousNovaQuestion = previousNovaQuestion,
                topicTitle = topicTitle,
                studentCefrLevel = studentCefrLevel,
                isVoiceInput = isVoiceInput
            )
            val grammarEval = grammarStepResult.getOrElse { err ->
                return@withContext Result.failure(err)
            }

            // STEP 2: Nova Conversational Reply & Follow-up Question with COMPLETE history
            val systemInstructions = buildSystemInstructions(
                topicTitle = topicTitle,
                topicCategory = topicCategory,
                difficultyLevel = difficultyLevel,
                currentTurn = currentTurn,
                targetTurns = targetTurns,
                studentCefrLevel = studentCefrLevel,
                grammarEval = grammarEval
            )

            val jsonBody = JSONObject().apply {
                put("model", cleanModel)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstructions)
                    })
                    if (priorHistory.isNotEmpty()) {
                        priorHistory.forEach { turn ->
                            val role = if (turn.isNova) "assistant" else "user"
                            val content = turn.text.trim().ifBlank { turn.audioPrompt.trim() }
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
                    put(JSONObject().apply {
                        put("role", "user")
                        put(
                            "content",
                            buildUserPrompt(
                                cleanSpoken = cleanSpoken,
                                targetPhrase = targetPhrase,
                                conversationContext = conversationContext,
                                previousNovaQuestion = previousNovaQuestion,
                                topicTitle = topicTitle,
                                currentTurn = currentTurn,
                                targetTurns = targetTurns,
                                previouslyAskedQuestions = previouslyAsked,
                                grammarEval = grammarEval
                            )
                        )
                    })
                }
                put("messages", messages)
                put("temperature", 0.4)
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
            val textOutput = rootJson.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?: ""

            parseEvaluationOutput(
                textOutput = textOutput,
                cleanSpoken = cleanSpoken,
                targetPhrase = targetPhrase,
                model = cleanModel,
                isClosingTurn = !topicTitle.isNullOrBlank() && currentTurn >= targetTurns,
                grammarEval = grammarEval
            )
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

    private fun collectPreviouslyAskedQuestions(
        history: List<com.example.data.model.NovaChatMessage>,
        previousNovaQuestion: String?
    ): List<String> {
        val seen = linkedSetOf<String>()
        history.filter { it.isNova }.forEach { msg ->
            msg.evaluation?.followUpQuestion?.trim()?.takeIf { it.isNotBlank() }?.let { seen.add(it) }
            Regex("[^.!?\\n]+\\?").findAll(msg.text).forEach { m ->
                val q = m.value.trim()
                if (q.length >= 6) seen.add(q)
            }
        }
        previousNovaQuestion?.trim()?.takeIf { it.isNotBlank() }?.let { prev ->
            val cleanPrev = Regex("[^.!?\\n]+\\?").find(prev)?.value?.trim() ?: prev
            if (cleanPrev.length >= 6) seen.add(cleanPrev)
        }
        return seen.toList()
    }

    // =========================================================================
    // STEP 1: DEDICATED GRAMMAR & MISTAKE EVALUATION STEP
    // =========================================================================

    private fun buildGrammarEvaluatorSystemInstruction(isVoiceInput: Boolean): String = buildString {
        append("You are an expert English Grammar & Usage Evaluator for school students.\n")
        append("CRITICAL: You MUST analyze the student's COMPLETE sentence/response and NEVER ignore any genuine English mistake.\n\n")
        append("CHECK CAREFULLY FOR:\n")
        append("1. Grammar mistakes: wrong tense, wrong verb form, missing linking verbs (is/am/are/was/were), extra auxiliary before base verb (e.g. 'I am go' -> 'I go'), subject-verb agreement.\n")
        append("2. Missing words: missing articles (a/an/the), missing prepositions (to/in/on/at/for), missing subjects/objects.\n")
        append("3. Incorrect word usage & vocabulary: wrong prepositions, wrong word choice, singular/plural confusion.\n")
        append("4. Sentence structure errors: broken word order, fragments where a full verb is needed.\n")
        if (isVoiceInput) {
            append("5. VOICE TRANSCRIPTION TOLERANCE: This response came from speech-to-text. Evaluate the spoken sentence accurately, but DO NOT mark a sentence as mistaken ONLY for missing punctuation (period/comma) or lowercase letters from voice transcription. Focus on real grammar, word choice, missing words, and verb errors.\n\n")
        } else {
            append("5. Spelling mistakes: check spelling carefully in typed responses (e.g., 'scool' -> 'school', 'tommorow' -> 'tomorrow').\n\n")
        }
        append("STRICT CORRECTION RULES:\n")
        append("- Never ignore grammatical mistakes.\n")
        append("- Never invent mistakes in a grammatically correct sentence.\n")
        append("- Preserve the student's original meaning.\n")
        append("- Explain mistakes in simple, student-friendly language.\n")
        append("- Avoid correcting informal expressions that are completely acceptable in natural conversation (e.g., \"I'm doing great, thanks!\", \"Pretty good!\").\n")
        append("- Suggest \"naturalEnglish\" ONLY if it genuinely improves the phrasing or makes it more natural (otherwise set \"naturalEnglish\": \"\").\n\n")
        append("Return ONLY a valid JSON object:\n")
        append("{\n")
        append("  \"hasMistake\": true,\n")
        append("  \"correctedSentence\": \"Exact corrected English sentence, or empty string if hasMistake is false\",\n")
        append("  \"explanation\": \"Simple 1-2 sentence explanation of the exact mistake(s), or brief confirmation if correct\",\n")
        append("  \"naturalEnglish\": \"Optional more natural everyday phrasing ONLY if it genuinely improves the sentence, otherwise empty string\",\n")
        append("  \"overallScore\": 78,\n")
        append("  \"pronunciationScore\": 84,\n")
        append("  \"fluencyScore\": 82,\n")
        append("  \"grammarScore\": 68,\n")
        append("  \"vocabularyScore\": 80,\n")
        append("  \"cefrLevel\": \"B1\",\n")
        append("  \"feedback\": \"1 encouraging sentence about the student's response.\",\n")
        append("  \"suggestedImprovement\": \"1 clear, actionable tip in simple English.\",\n")
        append("  \"detectedWeakWords\": [\"word1\"]\n")
        append("}")
    }

    private fun buildGrammarEvaluatorUserPrompt(
        cleanSpoken: String,
        previousNovaQuestion: String?,
        topicTitle: String?,
        studentCefrLevel: String,
        isVoiceInput: Boolean
    ): String = buildString {
        append("Input Mode: ${if (isVoiceInput) "Voice Transcription (STT)" else "Typed Text"}\n")
        append("Student Level: $studentCefrLevel\n")
        if (!topicTitle.isNullOrBlank()) append("Conversation Topic: \"$topicTitle\"\n")
        if (!previousNovaQuestion.isNullOrBlank()) append("Question Student Was Answering: \"${previousNovaQuestion.trim()}\"\n")
        append("Student's Complete Response: \"$cleanSpoken\"\n\n")
        append("Analyze the complete response above strictly for grammar, spelling, vocabulary, sentence structure, tense, missing words, and verb forms.")
    }

    private fun evaluateGrammarStepGemini(
        cleanKey: String,
        model: String,
        cleanSpoken: String,
        previousNovaQuestion: String?,
        topicTitle: String?,
        studentCefrLevel: String,
        isVoiceInput: Boolean
    ): Result<GrammarEvaluationResult> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
        val sysInstr = buildGrammarEvaluatorSystemInstruction(isVoiceInput)
        val userPrompt = buildGrammarEvaluatorUserPrompt(
            cleanSpoken = cleanSpoken,
            previousNovaQuestion = previousNovaQuestion,
            topicTitle = topicTitle,
            studentCefrLevel = studentCefrLevel,
            isVoiceInput = isVoiceInput
        )
        val jsonBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", sysInstr) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 512)
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
                JSONObject(responseBody).optJSONObject("error")?.optString("message")
                    ?: "Gemini API HTTP ${response.code}"
            } catch (_: Exception) {
                "Gemini API HTTP ${response.code}"
            }
            return Result.failure(Exception(errorMsg))
        }

        val textOutput = JSONObject(responseBody)
            .optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?: ""

        if (textOutput.isBlank()) {
            return Result.failure(Exception("Gemini grammar evaluator returned an empty response."))
        }

        return parseGrammarEvaluationJson(textOutput, cleanSpoken, isVoiceInput)
    }

    private fun evaluateGrammarStepCustomEndpoint(
        completionsUrl: String,
        cleanKey: String,
        cleanModel: String,
        cleanSpoken: String,
        previousNovaQuestion: String?,
        topicTitle: String?,
        studentCefrLevel: String,
        isVoiceInput: Boolean
    ): Result<GrammarEvaluationResult> {
        val sysInstr = buildGrammarEvaluatorSystemInstruction(isVoiceInput)
        val userPrompt = buildGrammarEvaluatorUserPrompt(
            cleanSpoken = cleanSpoken,
            previousNovaQuestion = previousNovaQuestion,
            topicTitle = topicTitle,
            studentCefrLevel = studentCefrLevel,
            isVoiceInput = isVoiceInput
        )
        val jsonBody = JSONObject().apply {
            put("model", cleanModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", sysInstr)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("temperature", 0.1)
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
                JSONObject(responseBody).optJSONObject("error")?.optString("message")
                    ?: "Custom endpoint HTTP ${response.code}"
            } catch (_: Exception) {
                "Custom endpoint HTTP ${response.code}"
            }
            return Result.failure(Exception(errorMsg))
        }

        val textOutput = JSONObject(responseBody)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            ?: ""

        if (textOutput.isBlank()) {
            return Result.failure(Exception("Endpoint grammar evaluator returned an empty response."))
        }

        return parseGrammarEvaluationJson(textOutput, cleanSpoken, isVoiceInput)
    }

    private fun normalizeVoiceTokens(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    internal fun parseGrammarEvaluationJson(
        textOutput: String,
        cleanSpoken: String,
        isVoiceInput: Boolean = false
    ): Result<GrammarEvaluationResult> {
        return try {
            val cleanJsonStr = extractJsonObjectString(textOutput)
            val json = JSONObject(cleanJsonStr)

            val rawHasMistake = json.optBoolean("hasMistake", false)
            val rawCorrected = json.optString("correctedSentence", "").trim()
                .takeIf { it.isNotBlank() && !it.equals(cleanSpoken, ignoreCase = true) }
            val rawExplanation = json.optString("explanation", "").trim()
            val rawNatural = json.optString("naturalEnglish", "").trim()
                .ifBlank { json.optString("naturalExpression", "").trim() }
                .takeIf {
                    it.isNotBlank() &&
                        !it.equals(cleanSpoken, ignoreCase = true) &&
                        !it.equals(rawCorrected.orEmpty(), ignoreCase = true)
                }

            val overallScore = json.optInt("overallScore", 82).coerceIn(10, 100)
            val pronunciationScore = json.optInt("pronunciationScore", 84).coerceIn(10, 100)
            val fluencyScore = json.optInt("fluencyScore", 82).coerceIn(10, 100)
            val grammarScore = json.optInt("grammarScore", 80).coerceIn(10, 100)
            val vocabularyScore = json.optInt("vocabularyScore", 84).coerceIn(10, 100)
            val cefrLevel = json.optString("cefrLevel", "B1").ifBlank { "B1" }
            val feedback = json.optString("feedback", "Good effort!").trim()
            val suggestedImprovement = json.optString("suggestedImprovement", rawExplanation).trim()
                .ifBlank { "Keep speaking in full, clear sentences." }

            val weakWords = mutableListOf<String>()
            val weakWordsArray = json.optJSONArray("detectedWeakWords")
            if (weakWordsArray != null) {
                for (i in 0 until weakWordsArray.length()) {
                    val w = weakWordsArray.optString(i).trim()
                    if (w.isNotBlank()) weakWords.add(w)
                }
            }

            // Verification guardrails:
            // 1) For voice STT, don't flag missing punctuation/capitalization alone as a grammar mistake.
            var verifiedHasMistake = rawHasMistake && rawCorrected != null
            var verifiedCorrected = if (verifiedHasMistake) rawCorrected else null
            var verifiedExplanation = rawExplanation
            var verifiedNatural = rawNatural
            var verifiedGrammarScore = grammarScore
            var verifiedOverallScore = overallScore

            if (verifiedHasMistake && verifiedCorrected != null && isVoiceInput) {
                if (normalizeVoiceTokens(verifiedCorrected) == normalizeVoiceTokens(cleanSpoken)) {
                    verifiedHasMistake = false
                    verifiedCorrected = null
                    verifiedExplanation = "Your spoken sentence is grammatically correct."
                }
            }

            // 2) Safety net: Never ignore obvious English mistakes even if an LLM missed one.
            if (!verifiedHasMistake) {
                val deterministic = HumanConversationEngine.analyzeGrammarDeterministic(
                    spoken = cleanSpoken,
                    topic = null,
                    isVoiceInput = isVoiceInput
                )
                if (deterministic.hasError && !deterministic.correctedSentence.isNullOrBlank()) {
                    verifiedHasMistake = true
                    verifiedCorrected = deterministic.correctedSentence
                    verifiedExplanation = deterministic.explanation ?: deterministic.shortTip
                        ?: "Use the corrected grammar structure."
                    verifiedNatural = verifiedNatural ?: deterministic.naturalExpression
                    verifiedGrammarScore = verifiedGrammarScore.coerceAtMost(72)
                    verifiedOverallScore = ((pronunciationScore * 0.30) + (fluencyScore * 0.25) +
                        (verifiedGrammarScore * 0.25) + (vocabularyScore * 0.20)).toInt().coerceIn(45, 84)
                }
            }

            if (verifiedHasMistake && verifiedExplanation.isBlank()) {
                verifiedExplanation = suggestedImprovement.ifBlank {
                    "Use the corrected grammar structure so the sentence is accurate."
                }
            } else if (!verifiedHasMistake && verifiedExplanation.isBlank()) {
                verifiedExplanation = "Your sentence is grammatically correct."
            }

            Result.success(
                GrammarEvaluationResult(
                    hasMistake = verifiedHasMistake,
                    correctedSentence = verifiedCorrected,
                    explanation = verifiedExplanation,
                    naturalEnglish = verifiedNatural,
                    overallScore = verifiedOverallScore,
                    pronunciationScore = pronunciationScore,
                    fluencyScore = fluencyScore,
                    grammarScore = verifiedGrammarScore,
                    vocabularyScore = vocabularyScore,
                    cefrLevel = cefrLevel,
                    feedback = feedback,
                    suggestedImprovement = suggestedImprovement,
                    detectedWeakWords = weakWords
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // STEP 2: NOVA CONVERSATIONAL REPLY & FOLLOW-UP QUESTION
    // =========================================================================

    private fun buildSystemInstructions(
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1",
        grammarEval: GrammarEvaluationResult? = null
    ): String = buildString {
        val isTopicChat = !topicTitle.isNullOrBlank()
        val isClosingTurn = isTopicChat && currentTurn >= targetTurns

        append("You are 'Nova', a warm, lively, encouraging Indian female English-speaking mentor tutoring a student ($studentCefrLevel level).\n")
        append("Speak like a real, friendly human tutor chatting over coffee — natural, expressive, relatable to Indian students, and genuinely curious (never stiff or robotic).\n")
        append("A dedicated grammar evaluation has ALREADY analyzed the student's sentence. Follow its result strictly.\n\n")

        append("MANDATORY CORRECTION & FEEDBACK RULES:\n")
        if (grammarEval?.hasMistake == true) {
            append("- A genuine English mistake WAS detected in the student's sentence.\n")
            append("- NEVER say \"Excellent!\", \"Perfect!\", or pretend the sentence had no mistakes.\n")
            append("- Preserve the student's original meaning.\n")
            append("- The app will render the mandatory ❌/✅/📖/🌟 correction header automatically. So in \"aiCoachResponse\", DO NOT repeat \"Excellent\" or contradict the correction — react warmly to their idea in 1 natural sentence, then ask ONE lively, connected follow-up question.\n")
        } else {
            append("- The student's sentence IS grammatically correct.\n")
            append("- Cheerfully appreciate what they shared and confirm their English was spot-on (e.g., \"Love that! Your sentence is grammatically correct.\").\n")
            append("- Do NOT invent mistakes in correct sentences.\n")
            if (!grammarEval?.naturalEnglish.isNullOrBlank()) {
                append("- A genuinely helpful natural expression was found: \"${grammarEval?.naturalEnglish}\". You may keep it as optional natural phrasing.\n")
            }
        }
        append("\n")

        if (isTopicChat) {
            append("STRICT TOPIC & CONVERSATION RULES:\n")
            append("1. FOLLOW THE SELECTED TOPIC STRICTLY: Stay 100% focused on \"$topicTitle\"")
            if (!topicCategory.isNullOrBlank()) append(" (Category: $topicCategory)")
            if (!difficultyLevel.isNullOrBlank()) append(" [Target Difficulty: $difficultyLevel]")
            append(".\n")
            append("2. NATURAL CONVERSATION FLOW:\n")
            append("   - Ask ONLY ONE clear, relevant question at a time.\n")
            append("   - Connect your next question directly to what the student just said.\n")
            append("   - NEVER repeat any question from ALREADY ASKED QUESTIONS.\n")
            append("   - NEVER ask multiple questions in one message (no double '?' or stacked questions).\n")
            append("   - Progress naturally across turns (Turn $currentTurn of $targetTurns).\n\n")

            if (isClosingTurn) {
                append("3. SESSION COMPLETION (FINAL SUMMARY TURN - Turn $currentTurn of $targetTurns):\n")
                append("   - Set \"isSessionSummary\": true.\n")
                append("   - Give a concise performance summary (max 80 words in \"aiCoachResponse\") highlighting grammar and vocabulary progress in \"$topicTitle\", and end with the single question: \"Would you like to practise another topic?\"\n")
                append("   - Set \"followUpQuestion\" to \"Would you like to practise another topic?\" and populate \"improvementSuggestions\" with 2 to 3 practical tips.\n\n")
            } else {
                append("3. TURN REPLY FORMAT:\n")
                append("   - Keep \"aiCoachResponse\" short and natural (1 to 3 concise sentences, max 45 words total), ending with ONE connected question on \"$topicTitle\".\n")
                append("   - Set \"isSessionSummary\": false and \"followUpQuestion\" to that exact single question.\n\n")
            }
        } else {
            append("Keep \"aiCoachResponse\" concise, warm, and encouraging (max 40 words) ending with ONE relevant follow-up question.\n\n")
        }

        append("Return ONLY a valid JSON object:\n")
        append("{\n")
        append("  \"aiCoachResponse\": \"...\",\n")
        append("  \"followUpQuestion\": \"...\",\n")
        append("  \"hint\": \"1 short simple tip guiding how to answer followUpQuestion.\",\n")
        append("  \"exampleAnswers\": [\"Example answer 1\", \"Example answer 2\"],\n")
        append("  \"isSessionSummary\": ${isClosingTurn},\n")
        append("  \"improvementSuggestions\": [\"...\"]\n")
        append("}")
    }

    private fun buildUserPrompt(
        cleanSpoken: String,
        targetPhrase: String?,
        conversationContext: String?,
        previousNovaQuestion: String? = null,
        topicTitle: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        previouslyAskedQuestions: List<String> = emptyList(),
        grammarEval: GrammarEvaluationResult? = null
    ): String = buildString {
        if (!topicTitle.isNullOrBlank()) {
            append("Selected Topic: \"$topicTitle\"\n")
            append("Turn Progress: Student response #$currentTurn of $targetTurns")
            if (currentTurn >= targetTurns) {
                append(" (FINAL TURN — provide performance summary & 2-3 improvement suggestions)")
            }
            append("\n")
        }
        if (!previousNovaQuestion.isNullOrBlank()) {
            append("Question Nova Just Asked: \"${previousNovaQuestion.trim()}\"\n")
        }
        if (previouslyAskedQuestions.isNotEmpty()) {
            append("ALREADY ASKED QUESTIONS (DO NOT REPEAT ANY OF THESE):\n")
            previouslyAskedQuestions.forEachIndexed { idx, q ->
                append("${idx + 1}. $q\n")
            }
        }
        append("Student's Complete Answer: \"$cleanSpoken\"\n")
        if (grammarEval != null) {
            append("DEDICATED GRAMMAR EVALUATION RESULT:\n")
            append("- hasMistake: ${grammarEval.hasMistake}\n")
            if (grammarEval.hasMistake && !grammarEval.correctedSentence.isNullOrBlank()) {
                append("- correctedSentence: \"${grammarEval.correctedSentence}\"\n")
                append("- explanation: \"${grammarEval.explanation}\"\n")
            }
            if (!grammarEval.naturalEnglish.isNullOrBlank()) {
                append("- naturalEnglish: \"${grammarEval.naturalEnglish}\"\n")
            }
        }
        if (!targetPhrase.isNullOrBlank()) {
            append("Target / Expected Context: \"$targetPhrase\"\n")
        }
        if (!conversationContext.isNullOrBlank()) {
            append("Topic Coaching Details: \"$conversationContext\"\n")
        }
        if (!topicTitle.isNullOrBlank() && currentTurn >= targetTurns) {
            append("Provide the final session summary and ask: \"Would you like to practise another topic?\"")
        } else if (grammarEval?.hasMistake == true) {
            append("Remember: Do NOT say 'Excellent!'. Briefly acknowledge the student's meaning/idea and ask ONE relevant, non-repeated follow-up question on the topic.")
        } else {
            append("Appreciate the student, confirm their sentence is grammatically correct, and ask ONE relevant, non-repeated follow-up question on the topic.")
        }
    }

    private fun executeTwoStepEvaluationGemini(
        cleanKey: String,
        cleanSpoken: String,
        targetPhrase: String?,
        conversationContext: String?,
        conversationHistory: List<com.example.data.model.NovaChatMessage>,
        previousNovaQuestion: String?,
        model: String,
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1",
        isVoiceInput: Boolean = false
    ): Result<SpeakingEvaluation> {
        try {
            // STEP 1: Dedicated Grammar Evaluation Step
            val grammarStepResult = evaluateGrammarStepGemini(
                cleanKey = cleanKey,
                model = model,
                cleanSpoken = cleanSpoken,
                previousNovaQuestion = previousNovaQuestion,
                topicTitle = topicTitle,
                studentCefrLevel = studentCefrLevel,
                isVoiceInput = isVoiceInput
            )
            val grammarEval = grammarStepResult.getOrElse { err ->
                return Result.failure(err)
            }

            // STEP 2: Nova Reply & Next Question using COMPLETE conversation history
            val systemInstructions = buildSystemInstructions(
                topicTitle = topicTitle,
                topicCategory = topicCategory,
                difficultyLevel = difficultyLevel,
                currentTurn = currentTurn,
                targetTurns = targetTurns,
                studentCefrLevel = studentCefrLevel,
                grammarEval = grammarEval
            )
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)
            val previouslyAsked = collectPreviouslyAskedQuestions(priorHistory, previousNovaQuestion)

            val jsonBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstructions) })
                    }
                    put("parts", parts)
                })

                val contents = JSONArray()

                if (priorHistory.isNotEmpty()) {
                    val initialContextText = "Hello Nova! Let's practise speaking English on the selected topic.\n" +
                        (conversationContext ?: topicTitle?.let { "Selected Topic: $it" } ?: "Topic: English Conversation Practice")
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", initialContextText) })
                        })
                    })

                    // Maintain the COMPLETE conversation history (never truncate prior turns)
                    var lastRole = "user"
                    for (msg in priorHistory) {
                        val role = if (msg.isNova) "model" else "user"
                        val text = msg.text.trim().ifBlank { msg.audioPrompt.trim() }
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

                    val currentTurnPrompt = buildUserPrompt(
                        cleanSpoken = cleanSpoken,
                        targetPhrase = targetPhrase,
                        conversationContext = conversationContext,
                        previousNovaQuestion = previousNovaQuestion,
                        topicTitle = topicTitle,
                        currentTurn = currentTurn,
                        targetTurns = targetTurns,
                        previouslyAskedQuestions = previouslyAsked,
                        grammarEval = grammarEval
                    )
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
                    val userPrompt = buildUserPrompt(
                        cleanSpoken = cleanSpoken,
                        targetPhrase = targetPhrase,
                        conversationContext = conversationContext,
                        previousNovaQuestion = previousNovaQuestion,
                        topicTitle = topicTitle,
                        currentTurn = currentTurn,
                        targetTurns = targetTurns,
                        previouslyAskedQuestions = previouslyAsked,
                        grammarEval = grammarEval
                    )
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "$systemInstructions\n\n$userPrompt") })
                        })
                    })
                }

                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
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

            return parseEvaluationOutput(
                textOutput = textOutput,
                cleanSpoken = cleanSpoken,
                targetPhrase = targetPhrase,
                model = model,
                isClosingTurn = !topicTitle.isNullOrBlank() && currentTurn >= targetTurns,
                grammarEval = grammarEval
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun extractJsonObjectString(raw: String): String {
        val trimmedOutput = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val startIdx = trimmedOutput.indexOf('{')
        val endIdx = trimmedOutput.lastIndexOf('}')
        return if (startIdx >= 0 && endIdx > startIdx) {
            trimmedOutput.substring(startIdx, endIdx + 1)
        } else {
            trimmedOutput
        }
    }

    private fun extractSingleQuestion(rawQuestion: String?): String? {
        val clean = rawQuestion?.trim()?.replace(Regex("\\s+"), " ") ?: return null
        if (clean.isBlank()) return null
        val firstQ = Regex("[^.!?]+\\?").find(clean)?.value?.trim()
        return (firstQ ?: clean).takeIf { it.isNotBlank() }
    }

    internal fun parseEvaluationOutput(
        textOutput: String,
        cleanSpoken: String,
        targetPhrase: String?,
        model: String,
        isClosingTurn: Boolean = false,
        grammarEval: GrammarEvaluationResult? = null
    ): Result<SpeakingEvaluation> {
        return try {
            val cleanJsonStr = extractJsonObjectString(textOutput)
            val parsedJson = JSONObject(cleanJsonStr)

            val overallScore = grammarEval?.overallScore ?: parsedJson.optInt("overallScore", 80).coerceIn(10, 100)
            val pronunciationScore = grammarEval?.pronunciationScore ?: parsedJson.optInt("pronunciationScore", 80).coerceIn(10, 100)
            val fluencyScore = grammarEval?.fluencyScore ?: parsedJson.optInt("fluencyScore", 80).coerceIn(10, 100)
            val grammarScore = grammarEval?.grammarScore ?: parsedJson.optInt("grammarScore", 80).coerceIn(10, 100)
            val vocabularyScore = grammarEval?.vocabularyScore ?: parsedJson.optInt("vocabularyScore", 80).coerceIn(10, 100)
            val cefrLevel = grammarEval?.cefrLevel ?: parsedJson.optString("cefrLevel", "B1")
            val feedback = grammarEval?.feedback ?: parsedJson.optString("feedback", "Good try! Keep speaking clearly.")
            val suggestedImprovement = grammarEval?.suggestedImprovement
                ?: parsedJson.optString("suggestedImprovement", "Practice full sentences with clear verbs.")

            val correctedSentence = if (grammarEval != null) {
                grammarEval.correctedSentence?.takeIf { grammarEval.hasMistake }
            } else {
                parsedJson.optString("correctedSentence", "")
                    .trim()
                    .takeIf { it.isNotBlank() && !it.equals(cleanSpoken, ignoreCase = true) }
            }
            val hasMistake = grammarEval?.hasMistake ?: (correctedSentence != null)
            val grammarExplanation = grammarEval?.explanation
                ?: if (hasMistake) suggestedImprovement else "Your sentence is grammatically correct."

            val naturalExpression = (grammarEval?.naturalEnglish ?: parsedJson.optString("naturalExpression", "").trim())
                .takeIf {
                    it.isNotBlank() &&
                        !it.equals(cleanSpoken, ignoreCase = true) &&
                        !it.equals(correctedSentence.orEmpty(), ignoreCase = true)
                }

            val isSessionSummary = parsedJson.optBoolean("isSessionSummary", isClosingTurn) || isClosingTurn

            val weakWords = grammarEval?.detectedWeakWords ?: run {
                val weakWordsArray = parsedJson.optJSONArray("detectedWeakWords")
                val list = mutableListOf<String>()
                if (weakWordsArray != null) {
                    for (i in 0 until weakWordsArray.length()) {
                        val w = weakWordsArray.optString(i)
                        if (w.isNotBlank()) list.add(w)
                    }
                }
                list
            }

            val suggestionsArray = parsedJson.optJSONArray("improvementSuggestions")
            val improvementSuggestions = mutableListOf<String>()
            if (suggestionsArray != null) {
                for (i in 0 until suggestionsArray.length()) {
                    val item = suggestionsArray.optString(i).trim()
                    if (item.isNotBlank()) improvementSuggestions.add(item)
                }
            }

            val rawFollowUp = parsedJson.optString("followUpQuestion", "").ifBlank { null }
            val followUpQuestion = if (isSessionSummary) {
                "Would you like to practise another topic?"
            } else {
                extractSingleQuestion(rawFollowUp)
            }
            val hint = parsedJson.optString("hint", "").ifBlank { null }
            val exampleAnswersArray = parsedJson.optJSONArray("exampleAnswers")
            val exampleAnswers = mutableListOf<String>()
            if (exampleAnswersArray != null) {
                for (i in 0 until exampleAnswersArray.length()) {
                    val ans = exampleAnswersArray.optString(i)
                    if (ans.isNotBlank()) exampleAnswers.add(ans)
                }
            }

            val aiCoachContinuationRaw = parsedJson.optString("aiCoachResponse", "")
                .replace("**", "")
                .trim()
                .let { HumanConversationEngine.extractContinuationAfterCorrection(it) }
                .let { text ->
                    val withQuestion = if (!followUpQuestion.isNullOrBlank() && !text.contains("?")) {
                        "$text $followUpQuestion".trim()
                    } else {
                        text
                    }
                    // Never allow "Excellent!" / "Perfect!" praise when a grammar mistake exists
                    if (hasMistake) {
                        withQuestion
                            .replace(Regex("""^(?i)(excellent|perfect|great job|amazing|wonderful|well done)[!.]?\s*"""), "")
                            .trim()
                    } else {
                        withQuestion
                    }
                }

            val formattedCoachResponse = if (hasMistake && !correctedSentence.isNullOrBlank()) {
                val continuation = aiCoachContinuationRaw.ifBlank { followUpQuestion.orEmpty() }
                HumanConversationEngine.formatMandatoryMistakeResponse(
                    originalSentence = cleanSpoken,
                    correctedSentence = correctedSentence,
                    explanation = grammarExplanation,
                    naturalEnglish = naturalExpression,
                    continuationText = continuation
                )
            } else {
                val baseContinuation = aiCoachContinuationRaw.ifBlank {
                    "Great job! Your sentence is grammatically correct. ${followUpQuestion.orEmpty()}".trim()
                }
                val confirmedContinuation = if (
                    baseContinuation.contains("correct", ignoreCase = true) ||
                    baseContinuation.contains("great", ignoreCase = true) ||
                    baseContinuation.contains("well done", ignoreCase = true) ||
                    baseContinuation.contains("good", ignoreCase = true)
                ) {
                    baseContinuation
                } else {
                    "Great job! Your sentence is grammatically correct. $baseContinuation".trim()
                }
                if (naturalExpression != null && !confirmedContinuation.contains(naturalExpression, ignoreCase = true)) {
                    "$confirmedContinuation\n\n🌟 Natural English: \"$naturalExpression\""
                } else {
                    confirmedContinuation
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
                aiCoachResponse = formattedCoachResponse,
                cefrLevel = cefrLevel,
                detectedWeakWords = weakWords,
                followUpQuestion = followUpQuestion,
                hint = hint,
                exampleAnswers = exampleAnswers,
                correctedSentence = correctedSentence,
                naturalExpression = naturalExpression,
                isSessionSummary = isSessionSummary,
                improvementSuggestions = improvementSuggestions,
                hasGrammarMistake = hasMistake,
                grammarExplanation = grammarExplanation
            )

            Result.success(evaluation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * General-purpose Gemini-style conversational AI assistant for students ("Nova AI").
     * Supports multi-turn context, multimodal image/PDF inline_data, and extracted document text.
     */
    suspend fun askNovaGemini(
        apiKey: String,
        userPrompt: String,
        chatHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        attachments: List<com.example.util.NovaAttachment> = emptyList(),
        studentName: String = "Student",
        studentClass: String = "",
        cefrLevel: String = "B1",
        mode: String = "General AI",
        model: String = DEFAULT_MODEL
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val cleanPrompt = userPrompt.trim().ifBlank {
            if (attachments.isNotEmpty()) "Please inspect the attached content and explain or help me with it." else ""
        }
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }
        if (cleanPrompt.isEmpty() && attachments.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Prompt cannot be empty."))
        }

        val modelsToTry = mutableListOf<String>()
        val preferred = model.trim().ifEmpty { DEFAULT_MODEL }
        modelsToTry.add(preferred)
        listOf(
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-flash-latest",
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.8-flash"
        ).forEach { fallback ->
            if (!modelsToTry.contains(fallback)) modelsToTry.add(fallback)
        }

        val systemPrompt = buildNovaGeminiSystemPrompt(studentName, studentClass, cefrLevel, mode)
        val combinedPromptText = buildAttachmentAugmentedPrompt(cleanPrompt, attachments)
        var lastError: Exception? = null

        for (activeModel in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$cleanKey"
                val contents = JSONArray()

                val fullHistory = chatHistory.takeLast(24)
                var lastRole = ""
                for (msg in fullHistory) {
                    val role = if (msg.isNova) "model" else "user"
                    val text = msg.text.trim()
                    if (text.isBlank()) continue
                    if (contents.length() == 0 && role == "model") {
                        continue
                    }
                    if (role == lastRole && contents.length() > 0) {
                        val lastObj = contents.getJSONObject(contents.length() - 1)
                        val partsArr = lastObj.getJSONArray("parts")
                        val existingPart = partsArr.getJSONObject(0)
                        existingPart.put("text", "${existingPart.getString("text")}\n\n$text")
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

                val currentParts = JSONArray().apply {
                    attachments.forEach { att ->
                        if (!att.base64Data.isNullOrBlank()) {
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", att.mimeType)
                                    put("data", att.base64Data)
                                })
                            })
                        }
                    }
                    put(JSONObject().apply { put("text", combinedPromptText) })
                }

                if (lastRole == "user" && contents.length() > 0) {
                    val lastObj = contents.getJSONObject(contents.length() - 1)
                    val existingParts = lastObj.getJSONArray("parts")
                    for (i in 0 until currentParts.length()) {
                        existingParts.put(currentParts.get(i))
                    }
                } else {
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", currentParts)
                    })
                }

                val jsonBody = JSONObject().apply {
                    put("system_instruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        })
                    })
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 8192)
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
                        errJson.optJSONObject("error")?.optString("message")
                            ?: "Gemini API HTTP ${response.code}"
                    } catch (_: Exception) {
                        "Gemini API HTTP ${response.code}"
                    }
                    lastError = Exception(errorMsg)
                    continue
                }

                val rootJson = JSONObject(responseBody)
                val partsArr = rootJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                val textOutput = buildString {
                    if (partsArr != null) {
                        for (i in 0 until partsArr.length()) {
                            val part = partsArr.optJSONObject(i) ?: continue
                            if (part.optBoolean("thought", false)) continue
                            val chunk = part.optString("text", "")
                            if (chunk.isNotEmpty()) append(chunk)
                        }
                    }
                }.trim()

                if (textOutput.isNotBlank()) {
                    return@withContext Result.success(textOutput to activeModel)
                } else {
                    lastError = Exception("Gemini returned an empty response.")
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Unable to reach Gemini right now."))
    }

    /**
     * OpenAI-compatible custom endpoint version of Nova Gemini Assistant with image & text attachments.
     */
    suspend fun askNovaGeminiCustomEndpoint(
        endpointUrl: String,
        apiKey: String,
        modelName: String,
        userPrompt: String,
        chatHistory: List<com.example.data.model.NovaChatMessage> = emptyList(),
        attachments: List<com.example.util.NovaAttachment> = emptyList(),
        studentName: String = "Student",
        studentClass: String = "",
        cefrLevel: String = "B1",
        mode: String = "General AI"
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val cleanUrl = endpointUrl.trim().removeSuffix("/")
        val cleanKey = apiKey.trim()
        val cleanPrompt = userPrompt.trim().ifBlank {
            if (attachments.isNotEmpty()) "Please inspect the attached content and help me with it." else ""
        }
        val cleanModel = modelName.trim().ifEmpty { "gpt-4o-mini" }

        if (cleanUrl.isEmpty() || cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Custom endpoint or API key is not configured."))
        }
        if (cleanPrompt.isEmpty() && attachments.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Prompt cannot be empty."))
        }

        val completionsUrl = if (cleanUrl.endsWith("/chat/completions")) cleanUrl else "$cleanUrl/chat/completions"

        try {
            val systemPrompt = buildNovaGeminiSystemPrompt(studentName, studentClass, cefrLevel, mode)
            val combinedPromptText = buildAttachmentAugmentedPrompt(cleanPrompt, attachments)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                chatHistory.forEach { msg ->
                    val text = msg.text.trim()
                    if (text.isNotBlank()) {
                        put(JSONObject().apply {
                            put("role", if (msg.isNova) "assistant" else "user")
                            put("content", text)
                        })
                    }
                }
                val imageAttachments = attachments.filter { it.isImage && !it.base64Data.isNullOrBlank() }
                if (imageAttachments.isNotEmpty()) {
                    val contentParts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", combinedPromptText)
                        })
                        imageAttachments.forEach { img ->
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:${img.mimeType};base64,${img.base64Data}")
                                })
                            })
                        }
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", contentParts)
                    })
                } else {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", combinedPromptText)
                    })
                }
            }

            val jsonBody = JSONObject().apply {
                put("model", cleanModel)
                put("messages", messages)
                put("temperature", 0.7)
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
                    errJson.optJSONObject("error")?.optString("message")
                        ?: "Custom endpoint HTTP ${response.code}"
                } catch (_: Exception) {
                    "Custom endpoint HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val textOutput = rootJson.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
                ?: ""

            if (textOutput.isBlank()) {
                return@withContext Result.failure(Exception("Endpoint returned an empty response."))
            }
            Result.success(textOutput to cleanModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildAttachmentAugmentedPrompt(
        prompt: String,
        attachments: List<com.example.util.NovaAttachment>
    ): String {
        val textDocs = attachments.filter { !it.extractedText.isNullOrBlank() }
        if (textDocs.isEmpty()) return prompt
        return buildString {
            append(prompt)
            textDocs.forEach { doc ->
                append("\n\n--- Attached File: ${doc.displayName} ---\n")
                append(doc.extractedText)
            }
        }
    }

    private fun buildNovaGeminiSystemPrompt(
        studentName: String,
        studentClass: String,
        cefrLevel: String,
        mode: String
    ): String = buildString {
        append("You are Nova AI, a super-intelligent, friendly, unrestricted general-purpose AI assistant powered by Gemini.\n")
        append("You help with EVERY topic the user asks about — just like Google Gemini — with NO subject restrictions.\n")
        append("User Profile: Name = ${studentName.ifBlank { "Friend" }}")
        if (studentClass.isNotBlank()) append(", Class/Batch = $studentClass")
        if (cefrLevel.isNotBlank()) append(", English Level = $cefrLevel")
        append(".\nSelected Mode: $mode.\n\n")

        append("CORE CAPABILITIES (ALL TOPICS WELCOME):\n")
        append("- Answer ANY topic thoroughly, accurately, and naturally: Mathematics (step-by-step solutions), Science (Physics, Chemistry, Biology, Space), Coding & Technology (Python, Kotlin, Java, Web, Debugging, AI), History, Geography, Civics, General Knowledge, Competitive Exams, Business & Finance, Career & Interviews, Creative Writing (stories, poems, speeches, essays, emails, scripts), Brainstorming & Project Ideas, Health/Study Productivity, Travel & Recipes, Analyzing attached Photos/PDFs/Documents, Multilingual help (English, Assamese, Hindi, Bengali, Hinglish, etc.), AND English speaking/grammar whenever asked.\n")
        append("- NEVER refuse a valid question or say you only teach English.\n")
        append("- NEVER force English grammar corrections or steer general questions back to English practice unless the user explicitly asks to check/improve their English OR is in 'Grammar Fix' / 'Speaking Tutor' mode.\n")
        append("- Reply in the language the user writes or requests (English, Hindi, Assamese, Bengali, etc.).\n")
        append("- Give complete, well-structured, genuinely helpful answers — use clear bullet points, numbered steps, or code blocks whenever appropriate.\n\n")

        append("MODE GUIDANCE:\n")
        when (mode) {
            "Study & Math" -> {
                append("- Act as an expert all-subjects tutor (Math, Science, History, Geography, Economics, Exam Prep).\n")
                append("- Show clear step-by-step reasoning, formulas, intuitive examples, and quick revision takeaways.\n")
            }
            "Coding & Tech" -> {
                append("- Act as a senior software engineer and tech mentor.\n")
                append("- Write clean, working, well-commented code, debug errors clearly, and explain concepts simply.\n")
            }
            "Creative & Ideas" -> {
                append("- Act as a creative writer, strategist, and brainstorming partner.\n")
                append("- Generate imaginative stories, project ideas, speeches, presentations, captions, or structured plans.\n")
            }
            "Roleplay" -> {
                append("- Jump straight into realistic, engaging interactive roleplay on any scenario the user picks (Café, Job Interview, Airport, Debate, Doctor, Historical figure, etc.). Stay in character and keep the conversation moving.\n")
            }
            "Grammar Fix" -> {
                append("- Focus on checking and polishing the user's English sentence using this clear format when there is a real mistake:\n")
                append("❌ Your Sentence: \"...\"\n")
                append("✅ Correct Sentence: \"...\"\n")
                append("📖 Explanation: ...\n")
                append("🌟 Natural English: \"...\"\n")
                append("- Never invent mistakes in already correct sentences.\n")
            }
            "Translator" -> {
                append("- Provide natural, accurate translations between any requested languages (English, Assamese, Hindi, Bengali, and global languages), plus helpful pronunciation or usage notes.\n")
            }
            "Writing Coach" -> {
                append("- Draft, rewrite, and polish essays, emails, applications, resumes, speeches, and reports with strong clarity and tone.\n")
            }
            "Speaking Tutor" -> {
                append("- Act as a warm, encouraging English speaking coach. If there is a clear grammar slip in English practice, gently show the ❌ / ✅ / 📖 / 🌟 correction and continue the conversation with an engaging follow-up question.\n")
            }
            else -> {
                append("- General AI Mode (Default): Behave like full Google Gemini. Answer ANY question on ANY subject directly, intelligently, and completely — whether academic, technical, creative, practical, or everyday chat. Only give English grammar corrections if the user explicitly asks for them.\n")
            }
        }
        append("- If the user attaches an image, photo, PDF, or document, inspect it carefully and answer their question directly (e.g., solve math from a photo, explain diagrams, summarize documents, debug screenshots).")
    }
}

