package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SupabaseConnectionState {
    object Disconnected : SupabaseConnectionState()
    object Connecting : SupabaseConnectionState()
    data class Connected(val url: String, val message: String) : SupabaseConnectionState()
    data class Error(val message: String) : SupabaseConnectionState()
}

class SupabaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    // Configurable endpoint & key (defaults to user project or BuildConfig)
    var supabaseUrl: String = try {
        val field = BuildConfig::class.java.getField("SUPABASE_URL")
        (field.get(null) as? String)?.takeIf { it.isNotBlank() && !it.contains("your-project") }
            ?: "https://bdodzycvbbwmzstbgllg.supabase.co"
    } catch (_: Exception) { "https://bdodzycvbbwmzstbgllg.supabase.co" }

    var supabaseAnonKey: String = try {
        val field = BuildConfig::class.java.getField("SUPABASE_ANON_KEY")
        (field.get(null) as? String)?.takeIf { it.isNotBlank() && !it.contains("your-supabase") }
            ?: "sb_publishable_ugS6TWEzgOAeQJl1yrEEEw_FSbfgxP_"
    } catch (_: Exception) { "sb_publishable_ugS6TWEzgOAeQJl1yrEEEw_FSbfgxP_" }

    fun isConfigured(): Boolean {
        return supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank() && !supabaseUrl.contains("your-project")
    }

    suspend fun testConnection(url: String = supabaseUrl, key: String = supabaseAnonKey): SupabaseConnectionState = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim().removeSuffix("/")
        if (cleanUrl.isBlank() || key.isBlank()) {
            return@withContext SupabaseConnectionState.Error("Please provide both Supabase URL and Anon Key.")
        }

        try {
            var connected = false
            var statusCode = 0

            // 1. Health check endpoint (fast and accessible with anon / publishable keys)
            try {
                val healthRequest = Request.Builder()
                    .url("$cleanUrl/auth/v1/health")
                    .addHeader("apikey", key.trim())
                    .addHeader("Authorization", "Bearer ${key.trim()}")
                    .get()
                    .build()

                client.newCall(healthRequest).execute().use { response ->
                    statusCode = response.code
                    if (response.isSuccessful || response.code == 200) {
                        connected = true
                    }
                }
            } catch (_: Exception) { }

            // 2. Fallback check: students table query
            if (!connected) {
                try {
                    val restRequest = Request.Builder()
                        .url("$cleanUrl/rest/v1/students?select=id&limit=1")
                        .addHeader("apikey", key.trim())
                        .addHeader("Authorization", "Bearer ${key.trim()}")
                        .get()
                        .build()

                    client.newCall(restRequest).execute().use { response ->
                        statusCode = response.code
                        if (response.isSuccessful || response.code == 200 || response.code == 206) {
                            connected = true
                        }
                    }
                } catch (_: Exception) { }
            }

            if (connected) {
                supabaseUrl = cleanUrl
                supabaseAnonKey = key.trim()
                SupabaseConnectionState.Connected(cleanUrl, "Connected successfully to Supabase PostgreSQL backend (HTTP $statusCode)")
            } else {
                SupabaseConnectionState.Error("Supabase returned status code $statusCode")
            }
        } catch (e: Exception) {
            SupabaseConnectionState.Error("Network error connecting to Supabase: ${e.localizedMessage}")
        }
    }

    // =========================================================================
    // FETCH DATA FROM SUPABASE
    // =========================================================================

    suspend fun fetchStudents(): List<StudentManagementInfo> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<StudentManagementInfo>()
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/students?select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optString("id", "std_${System.currentTimeMillis()}_$i")
                        val name = obj.optString("name", "Student")
                        val email = obj.optString("email", "")
                        val batch = obj.optString("batch", AcademicClasses.DEFAULT_CLASS)
                        val pin = obj.optString("security_pin", "1234")
                        val isActive = obj.optBoolean("is_active", true)
                        val xp = obj.optInt("xp_points", 100)
                        val joined = obj.optString("created_at", "Nov 2026").take(10)

                        list.add(
                            StudentManagementInfo(
                                id = id,
                                name = name,
                                email = email,
                                avatarEmoji = "🎓",
                                batch = batch,
                                level = LearningLevel.INTERMEDIATE,
                                isActive = isActive,
                                speakingScore = 75,
                                pronunciationScore = 75,
                                fluencyScore = 75,
                                grammarScore = 75,
                                vocabularyScore = 75,
                                listeningScore = 75,
                                streakDays = 1,
                                xpPoints = xp,
                                joinedDate = joined,
                                teacherNotes = "Synced from Supabase database",
                                securityPin = pin
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching students from Supabase: ${e.message}")
        }
        list
    }

    suspend fun fetchTeachers(): List<User> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<User>()
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/teachers?select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optString("id", "tch_${System.currentTimeMillis()}_$i")
                        val name = obj.optString("name", "Faculty Instructor")
                        val email = obj.optString("email", "")
                        val dept = obj.optString("department", "Spoken English Dept")
                        val pin = obj.optString("security_pin", "7321")
                        val isActive = obj.optBoolean("is_active", true)
                        val joined = obj.optString("created_at", "Nov 2026").take(10)

                        list.add(
                            User(
                                id = id,
                                name = name,
                                email = email,
                                role = Role.TEACHER,
                                avatarEmoji = "👨‍🏫",
                                batch = dept,
                                isActive = isActive,
                                joinedDate = joined,
                                securityPin = pin
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching teachers from Supabase: ${e.message}")
        }
        list
    }

    suspend fun fetchAssignments(): List<Assignment> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<Assignment>()
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/assignments?select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            Assignment(
                                id = obj.optString("id", "asg_${System.currentTimeMillis()}"),
                                title = obj.optString("title", "Assignment"),
                                teacherName = obj.optString("teacher_name", "Faculty Instructor"),
                                batch = obj.optString("batch", AcademicClasses.DEFAULT_CLASS),
                                dueDate = obj.optString("due_date", "In 3 Days"),
                                instructions = obj.optString("instructions", ""),
                                topicType = "Speaking & Academic",
                                maxPoints = obj.optInt("max_points", 100),
                                submissionsCount = 0,
                                totalAssigned = 25
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching assignments: ${e.message}")
        }
        list
    }

    suspend fun saveAssignment(assignment: Assignment): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val escapedTitle = assignment.title.replace("\"", "\\\"").trim()
            val escapedTeacher = assignment.teacherName.replace("\"", "\\\"").trim()
            val escapedBatch = assignment.batch.replace("\"", "\\\"").trim()
            val escapedDueDate = assignment.dueDate.replace("\"", "\\\"").trim()
            val escapedInstructions = assignment.instructions.replace("\"", "\\\"").trim()

            val jsonPayload = """
                {
                    "id": "${assignment.id}",
                    "title": "$escapedTitle",
                    "teacher_name": "$escapedTeacher",
                    "batch": "$escapedBatch",
                    "due_date": "$escapedDueDate",
                    "instructions": "$escapedInstructions",
                    "max_points": ${assignment.maxPoints}
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/assignments")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error saving assignment to Supabase: ${e.message}")
            false
        }
    }

    suspend fun syncStudentProgress(
        studentId: String,
        xp: Int,
        coins: Int,
        speakingScore: Int,
        completedStages: Int
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        try {
            val jsonPayload = """
                {
                    "student_id": "$studentId",
                    "xp_points": $xp,
                    "coins": $coins,
                    "speaking_score": $speakingScore,
                    "completed_stages": $completedStages,
                    "last_active": "now()"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/student_progress")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveStudentAccount(
        id: String,
        name: String,
        email: String,
        batch: String,
        securityPin: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        try {
            val cleanEmail = email.trim().lowercase()
            val escapedName = name.replace("\"", "\\\"").trim()
            val escapedBatch = batch.replace("\"", "\\\"").trim()

            // 1. Save to students profile table
            val studentPayload = """
                {
                    "id": "$id",
                    "name": "$escapedName",
                    "email": "$cleanEmail",
                    "batch": "$escapedBatch",
                    "security_pin": "$securityPin",
                    "role": "STUDENT",
                    "xp_points": 100,
                    "coins": 50,
                    "is_active": true,
                    "created_at": "now()"
                }
            """.trimIndent()

            val studentReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/students")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(studentPayload.toRequestBody("application/json".toMediaType()))
                .build()

            var saved = false
            client.newCall(studentReq).execute().use { resp ->
                saved = resp.isSuccessful
            }

            // 2. Also record in student_progress table by student id
            syncStudentProgress(id, xp = 100, coins = 50, speakingScore = 75, completedStages = 0)

            // 3. Also record in student_progress table by email for immediate cross-lookup
            syncStudentProgress(cleanEmail, xp = 100, coins = 50, speakingScore = 75, completedStages = 0)

            saved
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error saving student account: ${e.message}")
            false
        }
    }

    suspend fun saveTeacherAccount(
        id: String,
        name: String,
        email: String,
        department: String,
        securityPin: String,
        isActive: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        try {
            val cleanEmail = email.trim().lowercase()
            val escapedName = name.replace("\"", "\\\"").trim()
            val escapedDept = department.replace("\"", "\\\"").trim()

            val teacherPayload = """
                {
                    "id": "$id",
                    "name": "$escapedName",
                    "email": "$cleanEmail",
                    "department": "$escapedDept",
                    "security_pin": "$securityPin",
                    "role": "TEACHER",
                    "is_active": $isActive,
                    "created_at": "now()"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/teachers")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(teacherPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error saving teacher account: ${e.message}")
            false
        }
    }

    suspend fun deleteTeacherAccount(id: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/teachers?id=eq.$id")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    // =========================================================================
    // VOCABULARY WORDS SYNC (ENGLISH & ASSAMESE)
    // =========================================================================

    suspend fun fetchVocabularyWords(): List<VocabularyWord> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<VocabularyWord>()
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/vocabulary_words?select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            VocabularyWord(
                                id = obj.optString("id", "vocab_${System.currentTimeMillis()}_$i"),
                                word = obj.optString("word", ""),
                                phonetic = obj.optString("phonetic", ""),
                                partOfSpeech = obj.optString("part_of_speech", "noun"),
                                meaning = obj.optString("meaning", ""),
                                exampleSentence = obj.optString("example_sentence", ""),
                                category = obj.optString("category", "General"),
                                isFavorite = obj.optBoolean("is_favorite", false),
                                isMastered = obj.optBoolean("is_mastered", false),
                                assameseMeaning = obj.optString("assamese_meaning", ""),
                                assameseTransliteration = obj.optString("assamese_transliteration", ""),
                                syllables = obj.optString("syllables", ""),
                                coachingTip = obj.optString("coaching_tip", ""),
                                addedByTeacher = obj.optString("added_by_teacher", "Faculty Instructor")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching vocabulary words: ${e.message}")
        }
        list
    }

    suspend fun saveVocabularyWord(word: VocabularyWord): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val obj = JSONObject().apply {
                put("id", word.id)
                put("word", word.word)
                put("phonetic", word.phonetic)
                put("part_of_speech", word.partOfSpeech)
                put("meaning", word.meaning)
                put("example_sentence", word.exampleSentence)
                put("category", word.category)
                put("is_favorite", word.isFavorite)
                put("is_mastered", word.isMastered)
                put("assamese_meaning", word.assameseMeaning)
                put("assamese_transliteration", word.assameseTransliteration)
                put("syllables", word.syllables)
                put("coaching_tip", word.coachingTip)
                put("added_by_teacher", word.addedByTeacher)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/vocabulary_words")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error saving vocabulary word: ${e.message}")
            false
        }
    }

    suspend fun deleteVocabularyWord(wordId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/vocabulary_words?id=eq.$wordId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    // =========================================================================
    // GAME CHALLENGES SYNC (SENTENCE BUILDER, WORD MATCH, MISSING LETTERS, ETC.)
    // =========================================================================

    suspend fun fetchGameChallenges(): List<SupabaseGameChallenge> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<SupabaseGameChallenge>()
        val seenIds = mutableSetOf<String>()

        // 1. First attempt dedicated game_challenges table
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/game_challenges?select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optString("id", "")
                        if (id.isNotBlank()) {
                            val optArray = obj.optJSONArray("options")
                            val options = mutableListOf<String>()
                            if (optArray != null) {
                                for (j in 0 until optArray.length()) options.add(optArray.getString(j))
                            }
                            list.add(
                                SupabaseGameChallenge(
                                    id = id,
                                    type = obj.optString("type", "SENTENCE_BUILDER"),
                                    titleOrWord = obj.optString("title_or_word", ""),
                                    hintOrMeaning = obj.optString("hint_or_meaning", ""),
                                    category = obj.optString("category", "General"),
                                    options = options,
                                    correctIndex = obj.optInt("correct_index", 0),
                                    extraData = obj.optString("extra_data", ""),
                                    createdBy = obj.optString("created_by", "Faculty")
                                )
                            )
                            seenIds.add(id)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Also query resilient fallback sync channel in speaking_evaluations (ensures 100% sync even before user runs SQL migrations)
        try {
            val fallbackReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.CHALLENGE_SYNC&select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(fallbackReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val row = jsonArray.getJSONObject(i)
                        val feedback = row.optString("feedback", "")
                        val targetId = row.optString("target_phrase", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val id = obj.optString("id", targetId)
                                if (id.isNotBlank() && id !in seenIds) {
                                    val optArr = obj.optJSONArray("options")
                                    val options = mutableListOf<String>()
                                    if (optArr != null) {
                                        for (j in 0 until optArr.length()) options.add(optArr.getString(j))
                                    }
                                    list.add(
                                        SupabaseGameChallenge(
                                            id = id,
                                            type = obj.optString("type", row.optString("spoken_text", "SENTENCE_BUILDER")),
                                            titleOrWord = obj.optString("titleOrWord", ""),
                                            hintOrMeaning = obj.optString("hintOrMeaning", ""),
                                            category = obj.optString("category", "General"),
                                            options = options,
                                            correctIndex = obj.optInt("correctIndex", row.optInt("overall_score", 0)),
                                            extraData = obj.optString("extraData", ""),
                                            createdBy = obj.optString("createdBy", "Faculty")
                                        )
                                    )
                                    seenIds.add(id)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun saveGameChallenge(challenge: SupabaseGameChallenge): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false

        // 1. Try primary game_challenges table
        try {
            val obj = JSONObject().apply {
                put("id", challenge.id)
                put("type", challenge.type)
                put("title_or_word", challenge.titleOrWord)
                put("hint_or_meaning", challenge.hintOrMeaning)
                put("category", challenge.category)
                val optArr = JSONArray()
                challenge.options.forEach { optArr.put(it) }
                put("options", optArr)
                put("correct_index", challenge.correctIndex)
                put("extra_data", challenge.extraData)
                put("created_by", challenge.createdBy)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/game_challenges")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                savedInPrimary = response.isSuccessful
            }
        } catch (_: Exception) {}

        // 2. Always synchronize with the accessible cloud sync channel (ensures immediate cross-device student availability)
        try {
            val jsonPayload = JSONObject().apply {
                put("id", challenge.id)
                put("type", challenge.type)
                put("titleOrWord", challenge.titleOrWord)
                put("hintOrMeaning", challenge.hintOrMeaning)
                put("category", challenge.category)
                val optArr = JSONArray()
                challenge.options.forEach { optArr.put(it) }
                put("options", optArr)
                put("correctIndex", challenge.correctIndex)
                put("extraData", challenge.extraData)
                put("createdBy", challenge.createdBy)
            }

            val evalObj = JSONObject().apply {
                put("student_id", "CHALLENGE_SYNC")
                put("target_phrase", challenge.id)
                put("spoken_text", challenge.type)
                put("feedback", jsonPayload.toString())
                put("pronunciation_score", 100)
                put("fluency_score", 100)
                put("grammar_score", 100)
                put("vocabulary_score", 100)
                put("overall_score", challenge.correctIndex)
            }

            val fallbackReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(fallbackReq).execute().use { response ->
                if (response.isSuccessful) return@withContext true
            }
        } catch (_: Exception) {}

        savedInPrimary
    }

    suspend fun deleteGameChallenge(challengeId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false

        // 1. Delete from primary game_challenges
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/game_challenges?id=eq.$challengeId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        // 2. Delete from fallback channel
        try {
            val fbReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?target_phrase=eq.$challengeId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(fbReq).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        success
    }

    companion object {
        val SQL_SCHEMA_BLUEPRINT = """
            -- Speakly Supabase PostgreSQL Schema
            -- Run this in your Supabase SQL Editor:

            -- 1. Students / Learners Table
            CREATE TABLE IF NOT EXISTS students (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                batch TEXT DEFAULT 'Batch Alpha',
                security_pin TEXT NOT NULL,
                role TEXT DEFAULT 'STUDENT',
                xp_points INT DEFAULT 100,
                coins INT DEFAULT 50,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 2. Faculty / Teachers Table
            CREATE TABLE IF NOT EXISTS teachers (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                department TEXT DEFAULT 'Spoken English Dept',
                security_pin TEXT NOT NULL,
                role TEXT DEFAULT 'TEACHER',
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 3. Student Progress & Gamification Table
            CREATE TABLE IF NOT EXISTS student_progress (
                student_id TEXT PRIMARY KEY,
                xp_points INT DEFAULT 0,
                coins INT DEFAULT 0,
                speaking_score INT DEFAULT 0,
                completed_stages INT DEFAULT 0,
                last_active TIMESTAMPTZ DEFAULT NOW()
            );

            -- 4. Speaking AI Evaluations Table
            CREATE TABLE IF NOT EXISTS speaking_evaluations (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                student_id TEXT,
                spoken_text TEXT,
                target_phrase TEXT,
                pronunciation_score INT,
                fluency_score INT,
                grammar_score INT,
                vocabulary_score INT,
                overall_score INT,
                feedback TEXT,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 5. Academic Assignments Table
            CREATE TABLE IF NOT EXISTS assignments (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                teacher_name TEXT,
                batch TEXT,
                due_date TEXT,
                instructions TEXT,
                max_points INT DEFAULT 100
            );

            -- 6. English Adventure Progress Table
            CREATE TABLE IF NOT EXISTS student_adventure_progress (
                student_id TEXT PRIMARY KEY,
                current_world TEXT,
                completed_levels INT DEFAULT 0,
                total_stars INT DEFAULT 0,
                total_coins INT DEFAULT 0,
                total_xp INT DEFAULT 0,
                last_updated TIMESTAMPTZ DEFAULT NOW()
            );

            -- 7. English & Assamese Vocabulary Words Table
            CREATE TABLE IF NOT EXISTS vocabulary_words (
                id TEXT PRIMARY KEY,
                word TEXT NOT NULL,
                phonetic TEXT,
                part_of_speech TEXT DEFAULT 'noun',
                meaning TEXT,
                example_sentence TEXT,
                category TEXT DEFAULT 'General',
                is_favorite BOOLEAN DEFAULT FALSE,
                is_mastered BOOLEAN DEFAULT FALSE,
                assamese_meaning TEXT,
                assamese_transliteration TEXT,
                syllables TEXT,
                coaching_tip TEXT,
                added_by_teacher TEXT,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 8. Game Challenges Table (Sentence Builder, Word Match, etc.)
            CREATE TABLE IF NOT EXISTS game_challenges (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                title_or_word TEXT NOT NULL,
                hint_or_meaning TEXT,
                category TEXT DEFAULT 'General',
                options JSONB,
                correct_index INT DEFAULT 0,
                extra_data TEXT,
                created_by TEXT DEFAULT 'Faculty',
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- Enable Row Level Security (RLS) & Allow Read/Write
            ALTER TABLE students ENABLE ROW LEVEL SECURITY;
            ALTER TABLE teachers ENABLE ROW LEVEL SECURITY;
            ALTER TABLE student_progress ENABLE ROW LEVEL SECURITY;
            ALTER TABLE speaking_evaluations ENABLE ROW LEVEL SECURITY;
            ALTER TABLE assignments ENABLE ROW LEVEL SECURITY;
            ALTER TABLE student_adventure_progress ENABLE ROW LEVEL SECURITY;
            ALTER TABLE vocabulary_words ENABLE ROW LEVEL SECURITY;
            ALTER TABLE game_challenges ENABLE ROW LEVEL SECURITY;

            -- Policies with full SELECT, INSERT, UPDATE, DELETE permissions
            DROP POLICY IF EXISTS "Allow public students" ON students;
            CREATE POLICY "Allow public students" ON students FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public teachers" ON teachers;
            CREATE POLICY "Allow public teachers" ON teachers FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public student_progress" ON student_progress;
            CREATE POLICY "Allow public student_progress" ON student_progress FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public speaking_evals" ON speaking_evaluations;
            CREATE POLICY "Allow public speaking_evals" ON speaking_evaluations FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public assignments" ON assignments;
            CREATE POLICY "Allow public assignments" ON assignments FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public adventure" ON student_adventure_progress;
            CREATE POLICY "Allow public adventure" ON student_adventure_progress FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public vocabulary_words" ON vocabulary_words;
            CREATE POLICY "Allow public vocabulary_words" ON vocabulary_words FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public game_challenges" ON game_challenges;
            CREATE POLICY "Allow public game_challenges" ON game_challenges FOR ALL USING (true) WITH CHECK (true);
        """.trimIndent()
    }

    suspend fun syncAdventureProgress(
        studentId: String,
        currentWorld: String,
        completedLevels: Int,
        totalStars: Int,
        totalCoins: Int,
        totalXp: Int
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val jsonPayload = """
                {
                    "student_id": "$studentId",
                    "current_world": "$currentWorld",
                    "completed_levels": $completedLevels,
                    "total_stars": $totalStars,
                    "total_coins": $totalCoins,
                    "total_xp": $totalXp,
                    "last_updated": "now()"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/student_adventure_progress")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }
}

