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
        model: String = DEFAULT_MODEL,
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1"
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
                    model = activeModel,
                    topicTitle = topicTitle,
                    topicCategory = topicCategory,
                    difficultyLevel = difficultyLevel,
                    currentTurn = currentTurn,
                    targetTurns = targetTurns,
                    studentCefrLevel = studentCefrLevel
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
        previousNovaQuestion: String? = null,
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1"
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
            val systemInstructions = buildSystemInstructions(
                topicTitle = topicTitle,
                topicCategory = topicCategory,
                difficultyLevel = difficultyLevel,
                currentTurn = currentTurn,
                targetTurns = targetTurns,
                studentCefrLevel = studentCefrLevel
            )
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)
            val previouslyAsked = collectPreviouslyAskedQuestions(priorHistory, previousNovaQuestion)
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
                                previouslyAskedQuestions = previouslyAsked
                            )
                        )
                    })
                }
                put("messages", messages)
                put("temperature", 0.45)
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

            parseEvaluationOutput(
                textOutput = textOutput,
                cleanSpoken = cleanSpoken,
                targetPhrase = targetPhrase,
                model = cleanModel,
                isClosingTurn = !topicTitle.isNullOrBlank() && currentTurn >= targetTurns
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

    private fun buildSystemInstructions(
        topicTitle: String? = null,
        topicCategory: String? = null,
        difficultyLevel: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6,
        studentCefrLevel: String = "B1"
    ): String = buildString {
        val isTopicChat = !topicTitle.isNullOrBlank()
        val isClosingTurn = isTopicChat && currentTurn >= targetTurns

        append("You are 'Nova', a professional, warm, and encouraging English-speaking teacher tutoring a school student ($studentCefrLevel level) in spoken English.\n")
        append("Your goal is NOT to generate random questions — every reply must help the student learn, correct mistakes gently, and practise natural English in a focused conversation.\n\n")

        if (isTopicChat) {
            append("STRICT TOPIC & TEACHING RULES:\n")
            append("1. FOLLOW THE SELECTED TOPIC STRICTLY: Stay 100% focused on \"$topicTitle\"")
            if (!topicCategory.isNullOrBlank()) append(" (Category: $topicCategory)")
            if (!difficultyLevel.isNullOrBlank()) append(" [Target Difficulty: $difficultyLevel]")
            append(". NEVER ask unrelated, random, or off-topic questions. If the student briefly mentions something unrelated, acknowledge kindly in 1 short phrase and guide the next question smoothly back to \"$topicTitle\".\n")
            append("2. NATURAL CONVERSATION FLOW:\n")
            append("   - Ask ONLY ONE clear, relevant question at a time.\n")
            append("   - Logically connect your next question directly to what the student just said.\n")
            append("   - NEVER repeat any question from ALREADY ASKED QUESTIONS.\n")
            append("   - NEVER ask multiple questions in one message (no double '?' or stacked 'and what/where...?').\n")
            append("   - NEVER answer your own question before the student responds.\n")
            append("   - Gradually progress depth across turns (Turn $currentTurn of $targetTurns): start simple, then ask follow-up reasons/details/examples within \"$topicTitle\".\n")
            append("3. CORRECT THE STUDENT'S ENGLISH:\n")
            append("   - Understand the student's intended meaning first.\n")
            append("   - Identify grammatical mistakes (missing 'is/am/are', wrong verb tense, articles 'a/an/the', prepositions 'in/on/at', singular/plural, agreement).\n")
            append("   - If there IS a mistake: set \"correctedSentence\" to the exact corrected English sentence, explain the fix in 1 short, simple sentence inside \"aiCoachResponse\" (e.g., 'Good try! A better sentence is: \"My name is Ali.\" Remember to use \"is\" here.'), and never embarrass the student.\n")
            append("   - If the sentence is grammatically fine: praise warmly (e.g., 'Excellent! Your sentence is correct.' or 'Very good!') and if helpful, put a more natural everyday expression in \"naturalExpression\" (e.g., 'You can also say, \"I am in Class 9.\"').\n")
            append("4. TEACHING STYLE:\n")
            append("   - Use simple, practical everyday English suitable for school students.\n")
            append("   - Avoid difficult vocabulary for beginners or lengthy grammar lectures.\n")
            append("   - Plain text only in \"aiCoachResponse\" (NO markdown asterisks, bullet points, headers, or emojis, because it is spoken aloud by TTS).\n\n")

            if (isClosingTurn) {
                append("5. SESSION COMPLETION (FINAL SUMMARY TURN - Turn $currentTurn of $targetTurns):\n")
                append("   - Set \"isSessionSummary\": true.\n")
                append("   - First give brief praise/correction on the student's final answer.\n")
                append("   - Then give a warm, concise performance summary (max 90 words total in \"aiCoachResponse\") highlighting grammar and vocabulary progress from this \"$topicTitle\" conversation, give an encouraging closing line, and end with the single question: \"Would you like to practise another topic?\"\n")
                append("   - Set \"followUpQuestion\" to \"Would you like to practise another topic?\" and populate \"improvementSuggestions\" with 2 to 3 short, practical tips tailored to this student.\n\n")
            } else {
                append("5. TURN REPLY FORMAT:\n")
                append("   - Keep \"aiCoachResponse\" short and natural (2 to 4 brief plain sentences, max 50 words total), ending with ONE connected question on \"$topicTitle\".\n")
                append("   - Set \"isSessionSummary\": false and \"followUpQuestion\" to that exact single question.\n\n")
            }
        } else {
            append("EVALUATE THE STUDENT'S SPOKEN ENGLISH AND PROVIDE HELPFUL TEACHER FEEDBACK:\n")
            append("- Correct any clear grammatical errors gently in \"correctedSentence\" and offer a natural alternative in \"naturalExpression\".\n")
            append("- Keep \"aiCoachResponse\" concise, warm, plain English (max 45 words, no markdown/emojis for TTS) ending with ONE relevant follow-up question.\n\n")
        }

        append("ASSESSMENT CRITERIA (Score 0-100 each):\n")
        append("1. pronunciationScore: phoneme clarity, intelligibility, natural sentence stress.\n")
        append("2. fluencyScore: natural conversational pacing, coherence.\n")
        append("3. grammarScore: verb tense, linking verbs, prepositions, articles, agreement.\n")
        append("4. vocabularyScore: natural expression, topic relevance, variety.\n")
        append("5. overallScore: weighted overall score out of 100.\n")
        append("6. cefrLevel: CEFR proficiency level ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'].\n")
        append("7. feedback: 1 encouraging sentence evaluating what the student spoke.\n")
        append("8. suggestedImprovement: 1 clear, actionable tip in simple English.\n")
        append("9. correctedSentence: Exact corrected sentence if there was a grammar error, otherwise \"\".\n")
        append("10. naturalExpression: A more natural everyday phrasing if useful, otherwise \"\".\n")
        append("11. followUpQuestion: The exact SINGLE question asked at the end of aiCoachResponse.\n")
        append("12. hint: 1 short simple tip guiding how to answer followUpQuestion.\n")
        append("13. exampleAnswers: JSON array of 2 realistic, level-appropriate example answers replying directly to followUpQuestion.\n")
        append("14. detectedWeakWords: Up to 3 words from the student's speech to practice.\n")
        append("15. isSessionSummary: boolean (true ONLY on the final closing turn).\n")
        append("16. improvementSuggestions: JSON array of 2-3 specific English improvement tips (required when isSessionSummary is true).\n\n")

        append("Return ONLY a valid JSON object matching this schema:\n")
        append("{\n")
        append("  \"overallScore\": 85,\n")
        append("  \"pronunciationScore\": 84,\n")
        append("  \"fluencyScore\": 82,\n")
        append("  \"grammarScore\": 80,\n")
        append("  \"vocabularyScore\": 86,\n")
        append("  \"cefrLevel\": \"B1\",\n")
        append("  \"feedback\": \"...\",\n")
        append("  \"suggestedImprovement\": \"...\",\n")
        append("  \"correctedSentence\": \"...\",\n")
        append("  \"naturalExpression\": \"...\",\n")
        append("  \"aiCoachResponse\": \"...\",\n")
        append("  \"followUpQuestion\": \"...\",\n")
        append("  \"hint\": \"...\",\n")
        append("  \"exampleAnswers\": [\"...\", \"...\"],\n")
        append("  \"detectedWeakWords\": [\"...\"],\n")
        append("  \"isSessionSummary\": ${isClosingTurn},\n")
        append("  \"improvementSuggestions\": [\"...\", \"...\"]\n")
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
        previouslyAskedQuestions: List<String> = emptyList()
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
            previouslyAskedQuestions.takeLast(10).forEachIndexed { idx, q ->
                append("${idx + 1}. $q\n")
            }
        }
        append("Student's Spoken Answer: \"$cleanSpoken\"\n")
        if (!targetPhrase.isNullOrBlank()) {
            append("Target / Expected Context: \"$targetPhrase\"\n")
        }
        if (!conversationContext.isNullOrBlank()) {
            append("Topic Coaching Details: \"$conversationContext\"\n")
        }
        if (!topicTitle.isNullOrBlank() && currentTurn >= targetTurns) {
            append("Evaluate the student's final answer, summarize their performance across this session cleanly, share 2-3 practical improvement suggestions, and ask if they want to practise another topic.")
        } else {
            append("First evaluate and gently correct grammar or offer a more natural sentence in simple English, then respond encouragingly and ask ONE connected, non-repeated question strictly on the selected topic.")
        }
    }

    private fun executeSingleEvaluation(
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
        studentCefrLevel: String = "B1"
    ): Result<SpeakingEvaluation> {
        try {
            val systemInstructions = buildSystemInstructions(
                topicTitle = topicTitle,
                topicCategory = topicCategory,
                difficultyLevel = difficultyLevel,
                currentTurn = currentTurn,
                targetTurns = targetTurns,
                studentCefrLevel = studentCefrLevel
            )
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
            val priorHistory = stripTrailingDuplicateStudentTurn(conversationHistory, cleanSpoken)
            val previouslyAsked = collectPreviouslyAskedQuestions(priorHistory, previousNovaQuestion)

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
                    val initialContextText = "Hello Nova! Let's practise speaking English on the selected topic.\n" +
                        (conversationContext ?: topicTitle?.let { "Selected Topic: $it" } ?: "Topic: English Conversation Practice")
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", initialContextText) })
                        })
                    })

                    // Previous turns: alternating model and user
                    var lastRole = "user"
                    for (msg in priorHistory.takeLast(16)) {
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

                    // Final Turn: Current student spoken response + strict topic & turn guardrails
                    val currentTurnPrompt = buildUserPrompt(
                        cleanSpoken = cleanSpoken,
                        targetPhrase = targetPhrase,
                        conversationContext = conversationContext,
                        previousNovaQuestion = previousNovaQuestion,
                        topicTitle = topicTitle,
                        currentTurn = currentTurn,
                        targetTurns = targetTurns,
                        previouslyAskedQuestions = previouslyAsked
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
                    // Single-turn fallback when no history exists yet
                    val userPrompt = buildUserPrompt(
                        cleanSpoken = cleanSpoken,
                        targetPhrase = targetPhrase,
                        conversationContext = conversationContext,
                        previousNovaQuestion = previousNovaQuestion,
                        topicTitle = topicTitle,
                        currentTurn = currentTurn,
                        targetTurns = targetTurns,
                        previouslyAskedQuestions = previouslyAsked
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
                    put("temperature", 0.45)
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
                isClosingTurn = !topicTitle.isNullOrBlank() && currentTurn >= targetTurns
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun extractSingleQuestion(rawQuestion: String?): String? {
        val clean = rawQuestion?.trim()?.replace(Regex("\\s+"), " ") ?: return null
        if (clean.isBlank()) return null
        val firstQ = Regex("[^.!?]+\\?").find(clean)?.value?.trim()
        return (firstQ ?: clean).takeIf { it.isNotBlank() }
    }

    private fun parseEvaluationOutput(
        textOutput: String,
        cleanSpoken: String,
        targetPhrase: String?,
        model: String,
        isClosingTurn: Boolean = false
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
            val feedback = parsedJson.optString("feedback", "Good try! Keep speaking clearly.")
            val suggestedImprovement = parsedJson.optString("suggestedImprovement", "Practice full sentences with clear verbs.")
            val aiCoachResponseRaw = parsedJson.optString("aiCoachResponse", "Good effort! Let's keep going.")
                .replace("**", "")
                .trim()
            val correctedSentence = parsedJson.optString("correctedSentence", "")
                .trim()
                .takeIf { it.isNotBlank() && !it.equals(cleanSpoken, ignoreCase = true) }
            val naturalExpression = parsedJson.optString("naturalExpression", "")
                .trim()
                .takeIf {
                    it.isNotBlank() &&
                        !it.equals(cleanSpoken, ignoreCase = true) &&
                        !it.equals(correctedSentence.orEmpty(), ignoreCase = true)
                }
            val isSessionSummary = parsedJson.optBoolean("isSessionSummary", isClosingTurn) || isClosingTurn

            val weakWordsArray = parsedJson.optJSONArray("detectedWeakWords")
            val weakWords = mutableListOf<String>()
            if (weakWordsArray != null) {
                for (i in 0 until weakWordsArray.length()) {
                    val w = weakWordsArray.optString(i)
                    if (w.isNotBlank()) weakWords.add(w)
                }
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
                aiCoachResponse = aiCoachResponseRaw,
                cefrLevel = cefrLevel,
                detectedWeakWords = weakWords,
                followUpQuestion = followUpQuestion,
                hint = hint,
                exampleAnswers = exampleAnswers,
                correctedSentence = correctedSentence,
                naturalExpression = naturalExpression,
                isSessionSummary = isSessionSummary,
                improvementSuggestions = improvementSuggestions
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
        listOf("gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-3.8-flash", "gemini-flash-latest").forEach { fallback ->
            if (!modelsToTry.contains(fallback)) modelsToTry.add(fallback)
        }

        val systemPrompt = buildNovaGeminiSystemPrompt(studentName, studentClass, cefrLevel, mode)
        val combinedPromptText = buildAttachmentAugmentedPrompt(cleanPrompt, attachments)
        var lastError: Exception? = null

        for (activeModel in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$cleanKey"
                val contents = JSONArray()

                val recentHistory = chatHistory.takeLast(14)
                var lastRole = ""
                for (msg in recentHistory) {
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
                        put("maxOutputTokens", 2048)
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
                val textOutput = rootJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                    ?: ""

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
                chatHistory.takeLast(14).forEach { msg ->
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
        append("You are Nova AI, an intelligent, friendly, Gemini-powered personal AI assistant inside the Speakly learning app.\n")
        append("Student Profile: Name = ${studentName.ifBlank { "Student" }}")
        if (studentClass.isNotBlank()) append(", Class/Batch = $studentClass")
        if (cefrLevel.isNotBlank()) append(", English CEFR Level = $cefrLevel")
        append(".\nActive Assistant Mode: $mode.\n\n")
        append("INSTRUCTIONS:\n")
        append("- Behave like Google Gemini: helpful, accurate, creative, and easy to understand for a student.\n")
        append("- Answer ANY question the student asks — including English grammar, vocabulary, sentence correction, pronunciation tips, essay/letter/speech writing, translations (Assamese, Hindi, Bengali, etc.), exam preparation, science, math, coding, or general knowledge.\n")
        append("- If the student uploads an image, camera photo, PDF, or document file, carefully read and analyze its contents and help them clearly.\n")
        when (mode) {
            "Grammar Fix" -> append("- Mode focus: Analyze any sentence the student sends, show the corrected version clearly, and briefly explain why.\n")
            "Translator" -> append("- Mode focus: Provide clear, natural translations (especially English <-> Assamese / Hindi / Bengali when requested) along with pronunciation/usage notes.\n")
            "Writing Coach" -> append("- Mode focus: Help brainstorm, draft, or polish speeches, essays, paragraphs, emails, and dialogues tailored to their level.\n")
            "Speaking Tutor" -> append("- Mode focus: Give natural spoken English phrases, sample answers, and friendly conversational practice.\n")
            else -> append("- Mode focus: General-purpose AI assistant like Gemini.\n")
        }
        append("- Format cleanly for mobile reading using short paragraphs, numbered lists or '•' bullets, and clear labels instead of messy raw markdown symbols (avoid **bold** asterisks or ### headers; use clear section titles and clean spacing).\n")
        append("- Keep answers concise yet complete, encouraging, and directly useful.")
    }
}

