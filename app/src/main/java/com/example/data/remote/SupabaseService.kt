package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

sealed class SupabaseConnectionState {
    object Disconnected : SupabaseConnectionState()
    object Connecting : SupabaseConnectionState()
    data class Connected(val url: String, val message: String) : SupabaseConnectionState()
    data class Error(val message: String) : SupabaseConnectionState()
}

class SupabaseService {

    @Volatile
    internal var preferDoh: Boolean = false

    private data class CachedDnsEntry(
        val addresses: List<InetAddress>,
        val expiresAtMillis: Long
    )

    private val dnsCache = ConcurrentHashMap<String, CachedDnsEntry>()

    private val dohClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val resilientDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val cleanHost = hostname.trim()
            if (isLocalHost(cleanHost)) {
                return Dns.SYSTEM.lookup(cleanHost)
            }

            val now = System.currentTimeMillis()
            val cached = dnsCache[cleanHost]
            if (cached != null && cached.expiresAtMillis > now && cached.addresses.isNotEmpty()) {
                return cached.addresses
            }

            if (preferDoh) {
                val dohAddrs = resolveViaDoh(cleanHost)
                if (dohAddrs.isNotEmpty()) {
                    dnsCache[cleanHost] = CachedDnsEntry(dohAddrs, now + 5 * 60_000L)
                    return dohAddrs
                }
            }

            val systemAddrs = try {
                filterAndSortAddresses(cleanHost, Dns.SYSTEM.lookup(cleanHost))
            } catch (_: Exception) {
                emptyList()
            }

            if (systemAddrs.isNotEmpty()) {
                dnsCache[cleanHost] = CachedDnsEntry(systemAddrs, now + 5 * 60_000L)
                return systemAddrs
            }

            // System DNS failed or returned sinkhole/invalid address -> fallback to DoH (1.1.1.1 / 8.8.8.8)
            val dohFallback = resolveViaDoh(cleanHost)
            if (dohFallback.isNotEmpty()) {
                preferDoh = true
                dnsCache[cleanHost] = CachedDnsEntry(dohFallback, now + 5 * 60_000L)
                return dohFallback
            }

            throw UnknownHostException("Unable to resolve host '$cleanHost' via System DNS or DoH")
        }
    }

    private val client = OkHttpClient.Builder()
        .dns(resilientDns)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            try {
                chain.proceed(request)
            } catch (e: IOException) {
                val host = request.url.host
                if (!isLocalHost(host) && !preferDoh) {
                    preferDoh = true
                    dnsCache.remove(host)
                    chain.proceed(request)
                } else {
                    throw e
                }
            }
        }
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

    internal fun normalizeSupabaseUrl(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed = "https://$trimmed"
        }
        return trimmed
            .removeSuffix("/")
            .removeSuffix("/rest/v1")
            .removeSuffix("/auth/v1")
            .removeSuffix("/")
    }

    private fun isLocalHost(host: String): Boolean {
        return host.equals("localhost", ignoreCase = true) ||
            host == "127.0.0.1" ||
            host == "::1" ||
            host == "10.0.2.2"
    }

    private fun filterAndSortAddresses(host: String, raw: List<InetAddress>): List<InetAddress> {
        val valid = raw.filter { addr ->
            isLocalHost(host) || (
                !addr.isAnyLocalAddress &&
                    !addr.isLoopbackAddress &&
                    !addr.isLinkLocalAddress &&
                    !addr.isSiteLocalAddress &&
                    !addr.isMulticastAddress
                )
        }
        // Prefer IPv4 first to avoid slow/broken IPv6 stalls on dual-stack mobile networks
        return valid.sortedBy { if (it is Inet4Address) 0 else 1 }
    }

    private fun resolveViaDoh(hostname: String): List<InetAddress> {
        val encodedHost = try {
            java.net.URLEncoder.encode(hostname, "UTF-8")
        } catch (_: Exception) {
            hostname
        }
        val endpoints = listOf(
            "https://1.1.1.1/dns-query?name=$encodedHost&type=A",
            "https://8.8.8.8/resolve?name=$encodedHost&type=A"
        )
        for (dohUrl in endpoints) {
            try {
                val req = Request.Builder()
                    .url(dohUrl)
                    .addHeader("Accept", "application/dns-json")
                    .get()
                    .build()
                dohClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        val parsed = parseDohIps(body, hostname)
                        if (parsed.isNotEmpty()) return parsed
                    }
                }
            } catch (e: Exception) {
                Log.w("SupabaseService", "DoH lookup failed at $dohUrl: ${e.message}")
            }
        }
        return emptyList()
    }

    internal fun parseDohIps(jsonBody: String, hostname: String): List<InetAddress> {
        if (jsonBody.isBlank()) return emptyList()
        return try {
            val obj = JSONObject(jsonBody)
            val answers = obj.optJSONArray("Answer") ?: return emptyList()
            val results = mutableListOf<InetAddress>()
            val ipv4Regex = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")
            for (i in 0 until answers.length()) {
                val entry = answers.optJSONObject(i) ?: continue
                val type = entry.optInt("type", 0)
                val ipStr = entry.optString("data", "").trim()
                if (type == 1 && ipv4Regex.matches(ipStr)) {
                    try {
                        val addr = InetAddress.getByAddress(hostname, InetAddress.getByName(ipStr).address)
                        if (!addr.isAnyLocalAddress && !addr.isLoopbackAddress && !addr.isSiteLocalAddress) {
                            results.add(addr)
                        }
                    } catch (_: Exception) {}
                }
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun testConnection(url: String = supabaseUrl, key: String = supabaseAnonKey): SupabaseConnectionState = withContext(Dispatchers.IO) {
        val cleanUrl = normalizeSupabaseUrl(url)
        val cleanKey = key.trim()
        if (cleanUrl.isBlank() || cleanKey.isBlank()) {
            return@withContext SupabaseConnectionState.Error("Please provide both Supabase URL and Anon Key.")
        }
        val parsedHttpUrl = cleanUrl.toHttpUrlOrNull()
        if (parsedHttpUrl == null || parsedHttpUrl.host.isBlank()) {
            return@withContext SupabaseConnectionState.Error("Invalid Supabase URL. Expected format: https://your-project.supabase.co")
        }

        try {
            var connected = false
            var statusCode = 0
            var lastErrorDetail: String? = null
            var lastException: Exception? = null

            val probeUrls = listOf(
                "$cleanUrl/auth/v1/health",
                "$cleanUrl/rest/v1/students?select=id&limit=1",
                "$cleanUrl/rest/v1/"
            )

            for (probeUrl in probeUrls) {
                if (connected) break
                try {
                    val req = Request.Builder()
                        .url(probeUrl)
                        .addHeader("apikey", cleanKey)
                        .addHeader("Authorization", "Bearer $cleanKey")
                        .get()
                        .build()

                    client.newCall(req).execute().use { response ->
                        statusCode = response.code
                        if (response.isSuccessful || response.code == 200 || response.code == 206) {
                            connected = true
                        } else {
                            val snippet = response.body?.string()?.take(180)?.trim().orEmpty()
                            lastErrorDetail = snippet.ifBlank { response.message }
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.w("SupabaseService", "Probe failed ($probeUrl): ${e.javaClass.simpleName}: ${e.message}")
                }
            }

            if (connected) {
                supabaseUrl = cleanUrl
                supabaseAnonKey = cleanKey
                val via = if (preferDoh) " via Secure DoH" else ""
                SupabaseConnectionState.Connected(cleanUrl, "Connected successfully$via (HTTP $statusCode)")
            } else if (statusCode == 0) {
                val reason = lastException?.let {
                    "${it.javaClass.simpleName}: ${it.localizedMessage ?: "No response"}"
                } ?: "No response from server"
                SupabaseConnectionState.Error(
                    "Unable to reach Supabase server ($reason). Check your internet connection or Private DNS."
                )
            } else if (statusCode == 401 || statusCode == 403) {
                SupabaseConnectionState.Error(
                    "Authentication rejected (HTTP $statusCode). Check your Supabase Anon / Publishable key."
                )
            } else if (statusCode == 404) {
                SupabaseConnectionState.Error(
                    "Supabase endpoint returned HTTP 404. Verify your Project URL and run the SQL Schema Blueprint."
                )
            } else {
                val detailSuffix = lastErrorDetail?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
                SupabaseConnectionState.Error("Supabase returned HTTP $statusCode$detailSuffix")
            }
        } catch (e: Exception) {
            SupabaseConnectionState.Error("Network error connecting to Supabase: ${e.javaClass.simpleName}: ${e.localizedMessage}")
        }
    }

    // Helper to safely delete old records from speaking_evaluations sync channel before inserting
    private fun cleanSyncRows(studentId: String, targetPhrase: String) {
        try {
            val encodedPhrase = try { java.net.URLEncoder.encode(targetPhrase, "UTF-8") } catch (_: Exception) { targetPhrase }
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.$studentId&target_phrase=eq.$encodedPhrase")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()
            client.newCall(req).execute().close()
        } catch (_: Exception) {}
    }

    // =========================================================================
    // STUDENTS & TEACHERS SYNC
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

            syncStudentProgress(id, xp = 100, coins = 50, speakingScore = 75, completedStages = 0)
            syncStudentProgress(cleanEmail, xp = 100, coins = 50, speakingScore = 75, completedStages = 0)

            saved
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error saving student account: ${e.message}")
            false
        }
    }

    suspend fun updateStudentAccount(
        student: StudentManagementInfo,
        resetProgress: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val cleanEmail = student.email.trim().lowercase()
            val payload = JSONObject().apply {
                put("id", student.id)
                put("name", student.name.trim())
                put("email", cleanEmail)
                put("batch", student.batch.trim())
                put("security_pin", student.securityPin.ifBlank { "1234" })
                put("role", "STUDENT")
                put("xp_points", if (resetProgress) 0 else student.xpPoints)
                put("coins", if (resetProgress) 0 else (student.xpPoints / 4))
                put("is_active", student.isActive)
            }

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/students")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var saved = false
            client.newCall(req).execute().use { resp ->
                saved = resp.isSuccessful
            }

            if (resetProgress) {
                syncStudentProgress(student.id, xp = 0, coins = 0, speakingScore = 0, completedStages = 0)
                if (cleanEmail.isNotBlank()) {
                    syncStudentProgress(cleanEmail, xp = 0, coins = 0, speakingScore = 0, completedStages = 0)
                }
            }
            saved
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error updating student account: ${e.message}")
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            false
        }
    }

    // =========================================================================
    // ACADEMIC ASSIGNMENTS SYNC (WITH DUAL-CHANNEL FOR RLS RESILIENCE)
    // =========================================================================

    suspend fun fetchAssignments(): List<Assignment> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<Assignment>()
        val seenIds = mutableSetOf<String>()

        // 1. Primary assignments table
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
                        val id = obj.optString("id", "")
                        if (id.isNotBlank()) {
                            list.add(
                                Assignment(
                                    id = id,
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
                            seenIds.add(id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching primary assignments: ${e.message}")
        }

        // 2. Fallback channel in speaking_evaluations
        try {
            val fbReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.ASSIGNMENT_SYNC&select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(fbReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val row = jsonArray.getJSONObject(i)
                        val targetId = row.optString("target_phrase", "")
                        val feedback = row.optString("feedback", "")
                        if (targetId.isNotBlank() && targetId !in seenIds) {
                            val obj = try { JSONObject(feedback) } catch (_: Exception) { JSONObject() }
                            list.add(
                                Assignment(
                                    id = targetId,
                                    title = obj.optString("title", row.optString("spoken_text", "Assignment")),
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
                            seenIds.add(targetId)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun saveAssignment(assignment: Assignment): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false

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

        // 1. Try primary assignments table
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/assignments")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                savedInPrimary = response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Primary assignments insert failed: ${e.message}")
        }

        // 2. Always synchronize with the accessible channel (ensures saving even when RLS blocks assignments)
        try {
            cleanSyncRows("ASSIGNMENT_SYNC", assignment.id)

            val evalObj = JSONObject().apply {
                put("student_id", "ASSIGNMENT_SYNC")
                put("target_phrase", assignment.id)
                put("spoken_text", assignment.title)
                put("feedback", jsonPayload)
                put("pronunciation_score", 100)
                put("fluency_score", 100)
                put("grammar_score", 100)
                put("vocabulary_score", 100)
                put("overall_score", assignment.maxPoints)
            }

            val fallbackReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(fallbackReq).execute().use { response ->
                if (response.isSuccessful) return@withContext true
            }
        } catch (_: Exception) {}

        savedInPrimary
    }

    suspend fun deleteAssignment(assignmentId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false

        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/assignments?id=eq.$assignmentId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()
            client.newCall(req).execute().use { if (it.isSuccessful) success = true }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("ASSIGNMENT_SYNC", assignmentId)
            success = true
        } catch (_: Exception) {}

        success
    }

    // =========================================================================
    // VOCABULARY WORDS SYNC (ENGLISH & ASSAMESE)
    // =========================================================================

    suspend fun fetchVocabularyWords(): List<VocabularyWord> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<VocabularyWord>()
        val seenIds = mutableSetOf<String>()
        val seenWords = mutableSetOf<String>()

        // 1. Primary vocabulary_words table
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
                        val id = obj.optString("id", "")
                        val w = obj.optString("word", "").trim()
                        if (id.isNotBlank() && w.isNotBlank()) {
                            list.add(
                                VocabularyWord(
                                    id = id,
                                    word = w,
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
                            seenIds.add(id)
                            seenWords.add(w.lowercase())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Primary vocabulary_words fetch notice: ${e.message}")
        }

        // 2. Also fetch from speaking_evaluations sync channel (ensures words appear immediately)
        try {
            val fbReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.VOCABULARY_SYNC&select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(fbReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val row = jsonArray.getJSONObject(i)
                        val targetId = row.optString("target_phrase", "")
                        val feedback = row.optString("feedback", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val id = obj.optString("id", targetId)
                                val w = obj.optString("word", row.optString("spoken_text", "")).trim()
                                if (id.isNotBlank() && w.isNotBlank() && id !in seenIds && w.lowercase() !in seenWords) {
                                    list.add(
                                        VocabularyWord(
                                            id = id,
                                            word = w,
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
                                    seenIds.add(id)
                                    seenWords.add(w.lowercase())
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun saveVocabularyWord(word: VocabularyWord): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false

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

        // 1. Try primary vocabulary_words table
        val encodedWordId = try { java.net.URLEncoder.encode(word.id, "UTF-8") } catch (_: Exception) { word.id }
        try {
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/vocabulary_words?id=eq.$encodedWordId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInPrimary = true
                    }
                }
            }

            if (!patched) {
                val request = Request.Builder()
                    .url("$supabaseUrl/rest/v1/vocabulary_words")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(obj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    savedInPrimary = response.isSuccessful
                }
            }
        } catch (_: Exception) {}

        // 2. Always sync to speaking_evaluations (universal persistent channel in Supabase)
        try {
            val patchPayload = JSONObject().apply {
                put("spoken_text", word.word)
                put("feedback", obj.toString())
                put("overall_score", 100)
            }

            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.VOCABULARY_SYNC&target_phrase=eq.$encodedWordId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                    }
                }
            }

            if (!patched) {
                cleanSyncRows("VOCABULARY_SYNC", word.id)

                val evalObj = JSONObject().apply {
                    put("student_id", "VOCABULARY_SYNC")
                    put("target_phrase", word.id)
                    put("spoken_text", word.word)
                    put("feedback", obj.toString())
                    put("pronunciation_score", 100)
                    put("fluency_score", 100)
                    put("grammar_score", 100)
                    put("vocabulary_score", 100)
                    put("overall_score", 100)
                }

                val fallbackReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/speaking_evaluations")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(fallbackReq).execute().use { response ->
                    if (response.isSuccessful) return@withContext true
                }
            } else {
                return@withContext true
            }
        } catch (_: Exception) {}

        savedInPrimary
    }

    suspend fun deleteVocabularyWord(wordId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false
        val encodedWordId = try { java.net.URLEncoder.encode(wordId, "UTF-8") } catch (_: Exception) { wordId }

        // 1. Delete from primary
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/vocabulary_words?id=eq.$encodedWordId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        // 2. Delete from sync channel
        try {
            cleanSyncRows("VOCABULARY_SYNC", wordId)
            success = true
        } catch (_: Exception) {}

        success
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
                .url("$supabaseUrl/rest/v1/game_challenges?select=*&limit=1000")
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
                                    titleOrWord = obj.optString("title_or_word", obj.optString("titleOrWord", "")),
                                    hintOrMeaning = obj.optString("hint_or_meaning", obj.optString("hintOrMeaning", "")),
                                    category = obj.optString("category", "General"),
                                    options = options,
                                    correctIndex = if (obj.has("correct_index")) obj.optInt("correct_index", 0) else obj.optInt("correctIndex", 0),
                                    extraData = obj.optString("extra_data", obj.optString("extraData", "")),
                                    createdBy = obj.optString("created_by", obj.optString("createdBy", "Faculty"))
                                )
                            )
                            seenIds.add(id)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query fallback channel in speaking_evaluations
        try {
            val fallbackReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.CHALLENGE_SYNC&select=*&limit=1000")
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
                                            titleOrWord = obj.optString("titleOrWord", obj.optString("title_or_word", obj.optString("targetSentence", obj.optString("word", obj.optString("fullWord", ""))))),
                                            hintOrMeaning = obj.optString("hintOrMeaning", obj.optString("hint_or_meaning", obj.optString("hint", obj.optString("meaning", obj.optString("definition", ""))))),
                                            category = obj.optString("category", "General"),
                                            options = options,
                                            correctIndex = if (obj.has("correctIndex")) obj.optInt("correctIndex", 0) else if (obj.has("correct_index")) obj.optInt("correct_index", 0) else row.optInt("overall_score", 0),
                                            extraData = obj.optString("extraData", obj.optString("extra_data", obj.optString("assameseTranslation", obj.optString("missingIndices", "")))),
                                            createdBy = obj.optString("createdBy", obj.optString("created_by", "Faculty"))
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
        var savedInFallback = false
        val encodedId = try { java.net.URLEncoder.encode(challenge.id, "UTF-8") } catch (_: Exception) { challenge.id }

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

            // In-place PATCH for existing challenge
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/game_challenges?id=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInPrimary = true
                    }
                }
            }

            // If not found for patch, insert/upsert
            if (!patched) {
                val postReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/game_challenges")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(obj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(postReq).execute().use { resp ->
                    if (resp.isSuccessful) savedInPrimary = true
                }
            }
        } catch (_: Exception) {}

        // 2. Synchronize with the cloud sync channel in speaking_evaluations
        try {
            val jsonPayload = JSONObject().apply {
                put("id", challenge.id)
                put("type", challenge.type)
                put("titleOrWord", challenge.titleOrWord)
                put("title_or_word", challenge.titleOrWord)
                put("hintOrMeaning", challenge.hintOrMeaning)
                put("hint_or_meaning", challenge.hintOrMeaning)
                put("category", challenge.category)
                val optArr = JSONArray()
                challenge.options.forEach { optArr.put(it) }
                put("options", optArr)
                put("correctIndex", challenge.correctIndex)
                put("correct_index", challenge.correctIndex)
                put("extraData", challenge.extraData)
                put("extra_data", challenge.extraData)
                put("createdBy", challenge.createdBy)
                put("created_by", challenge.createdBy)
            }

            val patchPayload = JSONObject().apply {
                put("spoken_text", challenge.type)
                put("feedback", jsonPayload.toString())
                put("overall_score", challenge.correctIndex)
            }

            // In-place PATCH in speaking_evaluations
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.CHALLENGE_SYNC&target_phrase=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInFallback = true
                    }
                }
            }

            if (!patched) {
                cleanSyncRows("CHALLENGE_SYNC", challenge.id)

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
                    .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(fallbackReq).execute().use { response ->
                    if (response.isSuccessful) savedInFallback = true
                }
            }
        } catch (_: Exception) {}

        savedInPrimary || savedInFallback
    }

    suspend fun deleteGameChallenge(challengeId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false
        val encodedId = try { java.net.URLEncoder.encode(challengeId, "UTF-8") } catch (_: Exception) { challengeId }

        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/game_challenges?id=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("CHALLENGE_SYNC", challengeId)
            success = true
        } catch (_: Exception) {}

        success
    }

    // =========================================================================
    // PRONUNCIATION DRILLS SYNC (WORDS & SENTENCES FOR PHONETICS)
    // =========================================================================

    suspend fun fetchPronunciationDrills(): List<PronunciationDrill> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<PronunciationDrill>()
        val seenIds = mutableSetOf<String>()

        // 1. Try dedicated pronunciation_drills table
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/pronunciation_drills?select=*")
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
                            list.add(
                                PronunciationDrill(
                                    id = id,
                                    wordOrSentence = obj.optString("word_or_sentence", ""),
                                    ipaPhonetic = obj.optString("ipa_phonetic", ""),
                                    audioFocus = obj.optString("audio_focus", "Clarity"),
                                    difficulty = obj.optString("difficulty", "Medium"),
                                    tips = obj.optString("tips", "")
                                )
                            )
                            seenIds.add(id)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query speaking_evaluations sync channel
        try {
            val fbReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.DRILL_SYNC&select=*")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(fbReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val row = jsonArray.getJSONObject(i)
                        val targetId = row.optString("target_phrase", "")
                        val feedback = row.optString("feedback", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val id = obj.optString("id", targetId)
                                if (id.isNotBlank() && id !in seenIds) {
                                    list.add(
                                        PronunciationDrill(
                                            id = id,
                                            wordOrSentence = obj.optString("wordOrSentence", row.optString("spoken_text", "")),
                                            ipaPhonetic = obj.optString("ipaPhonetic", ""),
                                            audioFocus = obj.optString("audioFocus", "Clarity"),
                                            difficulty = obj.optString("difficulty", "Medium"),
                                            tips = obj.optString("tips", "")
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

    suspend fun savePronunciationDrill(drill: PronunciationDrill): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false
        var savedInFallback = false
        val encodedDrillId = try { java.net.URLEncoder.encode(drill.id, "UTF-8") } catch (_: Exception) { drill.id }

        val obj = JSONObject().apply {
            put("id", drill.id)
            put("word_or_sentence", drill.wordOrSentence)
            put("ipa_phonetic", drill.ipaPhonetic)
            put("audio_focus", drill.audioFocus)
            put("difficulty", drill.difficulty)
            put("tips", drill.tips)
            put("created_by", "Faculty")
        }

        // 1. Try dedicated table
        try {
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/pronunciation_drills?id=eq.$encodedDrillId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInPrimary = true
                    }
                }
            }

            if (!patched) {
                val request = Request.Builder()
                    .url("$supabaseUrl/rest/v1/pronunciation_drills")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(obj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    savedInPrimary = response.isSuccessful
                }
            }
        } catch (_: Exception) {}

        // 2. Synchronize with speaking_evaluations
        try {
            val jsonPayload = JSONObject().apply {
                put("id", drill.id)
                put("wordOrSentence", drill.wordOrSentence)
                put("word_or_sentence", drill.wordOrSentence)
                put("ipaPhonetic", drill.ipaPhonetic)
                put("ipa_phonetic", drill.ipaPhonetic)
                put("audioFocus", drill.audioFocus)
                put("audio_focus", drill.audioFocus)
                put("difficulty", drill.difficulty)
                put("tips", drill.tips)
            }

            val patchPayload = JSONObject().apply {
                put("spoken_text", drill.wordOrSentence)
                put("feedback", jsonPayload.toString())
                put("overall_score", 100)
            }

            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.DRILL_SYNC&target_phrase=eq.$encodedDrillId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInFallback = true
                    }
                }
            }

            if (!patched) {
                cleanSyncRows("DRILL_SYNC", drill.id)

                val evalObj = JSONObject().apply {
                    put("student_id", "DRILL_SYNC")
                    put("target_phrase", drill.id)
                    put("spoken_text", drill.wordOrSentence)
                    put("feedback", jsonPayload.toString())
                    put("pronunciation_score", 100)
                    put("fluency_score", 100)
                    put("grammar_score", 100)
                    put("vocabulary_score", 100)
                    put("overall_score", 100)
                }

                val fallbackReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/speaking_evaluations")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(fallbackReq).execute().use { response ->
                    if (response.isSuccessful) savedInFallback = true
                }
            }
        } catch (_: Exception) {}

        savedInPrimary || savedInFallback
    }

    suspend fun deletePronunciationDrill(drillId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false
        val encodedDrillId = try { java.net.URLEncoder.encode(drillId, "UTF-8") } catch (_: Exception) { drillId }

        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/pronunciation_drills?id=eq.$encodedDrillId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("DRILL_SYNC", drillId)
            success = true
        } catch (_: Exception) {}

        success
    }

    // =========================================================================
    // AI SPEAKING PROMPTS SYNC (QUESTIONS & CONVERSATION SENTENCES)
    // =========================================================================

    suspend fun fetchSpeakingPrompts(): List<SpeakingPrompt> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<SpeakingPrompt>()
        val seenIds = mutableSetOf<String>()

        // 1. Try dedicated table
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_prompts?select=*&limit=500")
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
                            val kwArr = obj.optJSONArray("keywords")
                            val kwList = mutableListOf<String>()
                            if (kwArr != null) {
                                for (j in 0 until kwArr.length()) kwList.add(kwArr.getString(j))
                            }
                            list.add(
                                SpeakingPrompt(
                                    id = id,
                                    title = obj.optString("title", ""),
                                    category = obj.optString("category", "General"),
                                    question = obj.optString("question", ""),
                                    followUpTip = obj.optString("follow_up_tip", obj.optString("followUpTip", "")),
                                    recommendedKeywords = kwList,
                                    minSpeakingSeconds = if (obj.has("min_seconds")) obj.optInt("min_seconds", 20) else obj.optInt("minSeconds", 20),
                                    emoji = obj.optString("emoji", "💬"),
                                    difficultyLevel = obj.optString("difficulty_level", obj.optString("difficultyLevel", "Intermediate")),
                                    createdBy = obj.optString("created_by", obj.optString("createdBy", "Faculty")),
                                    milestoneTurns = if (obj.has("milestone_turns")) obj.optInt("milestone_turns", 0) else obj.optInt("milestoneTurns", 0),
                                    weekNumber = (if (obj.has("week_number")) obj.optInt("week_number", 1) else obj.optInt("weekNumber", 1)).coerceAtLeast(1)
                                )
                            )
                            seenIds.add(id)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query speaking_evaluations sync channel
        try {
            val fbReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.PROMPT_SYNC&select=*&limit=500")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(fbReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val row = jsonArray.getJSONObject(i)
                        val targetId = row.optString("target_phrase", "")
                        val feedback = row.optString("feedback", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val id = obj.optString("id", targetId)
                                if (id.isNotBlank()) {
                                    val syncMilestone = obj.optInt("milestoneTurns", obj.optInt("milestone_turns", 0))
                                    val syncWeek = obj.optInt("weekNumber", obj.optInt("week_number", 1)).coerceAtLeast(1)
                                    if (id !in seenIds) {
                                        val kwArr = obj.optJSONArray("keywords")
                                        val kwList = mutableListOf<String>()
                                        if (kwArr != null) {
                                            for (j in 0 until kwArr.length()) kwList.add(kwArr.getString(j))
                                        }
                                        list.add(
                                            SpeakingPrompt(
                                                id = id,
                                                title = obj.optString("title", row.optString("spoken_text", "")),
                                                category = obj.optString("category", "General"),
                                                question = obj.optString("question", ""),
                                                followUpTip = obj.optString("followUpTip", obj.optString("follow_up_tip", "")),
                                                recommendedKeywords = kwList,
                                                minSpeakingSeconds = if (obj.has("minSeconds")) obj.optInt("minSeconds", 20) else obj.optInt("min_seconds", 20),
                                                emoji = obj.optString("emoji", "💬"),
                                                difficultyLevel = obj.optString("difficultyLevel", obj.optString("difficulty_level", "Intermediate")),
                                                createdBy = obj.optString("createdBy", obj.optString("created_by", "Faculty")),
                                                milestoneTurns = syncMilestone,
                                                weekNumber = syncWeek
                                            )
                                        )
                                        seenIds.add(id)
                                    } else {
                                        val idx = list.indexOfFirst { it.id == id }
                                        if (idx >= 0) {
                                            var updatedItem = list[idx]
                                            if (syncMilestone > 0 && updatedItem.milestoneTurns <= 0) {
                                                updatedItem = updatedItem.copy(milestoneTurns = syncMilestone)
                                            }
                                            if (syncWeek > 1 && updatedItem.weekNumber <= 1) {
                                                updatedItem = updatedItem.copy(weekNumber = syncWeek)
                                            }
                                            list[idx] = updatedItem
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun saveSpeakingPrompt(prompt: SpeakingPrompt): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false
        var savedInFallback = false
        val encodedPromptId = try { java.net.URLEncoder.encode(prompt.id, "UTF-8") } catch (_: Exception) { prompt.id }

        val kwArr = JSONArray()
        prompt.recommendedKeywords.forEach { kwArr.put(it) }

        val obj = JSONObject().apply {
            put("id", prompt.id)
            put("title", prompt.title)
            put("category", prompt.category)
            put("question", prompt.question)
            put("follow_up_tip", prompt.followUpTip)
            put("keywords", kwArr)
            put("min_seconds", prompt.minSpeakingSeconds)
            put("emoji", prompt.emoji)
            put("difficulty_level", prompt.difficultyLevel)
            put("created_by", prompt.createdBy)
        }

        // 1. Try dedicated table
        try {
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_prompts?id=eq.$encodedPromptId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(obj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInPrimary = true
                    }
                }
            }

            if (!patched) {
                val request = Request.Builder()
                    .url("$supabaseUrl/rest/v1/speaking_prompts")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(obj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    savedInPrimary = response.isSuccessful
                }
            }
        } catch (_: Exception) {}

        // 2. Synchronize with speaking_evaluations
        try {
            val jsonPayload = JSONObject().apply {
                put("id", prompt.id)
                put("title", prompt.title)
                put("category", prompt.category)
                put("question", prompt.question)
                put("followUpTip", prompt.followUpTip)
                put("follow_up_tip", prompt.followUpTip)
                put("keywords", kwArr)
                put("minSeconds", prompt.minSpeakingSeconds)
                put("min_seconds", prompt.minSpeakingSeconds)
                put("emoji", prompt.emoji)
                put("difficultyLevel", prompt.difficultyLevel)
                put("difficulty_level", prompt.difficultyLevel)
                put("createdBy", prompt.createdBy)
                put("created_by", prompt.createdBy)
                put("milestoneTurns", prompt.milestoneTurns)
                put("milestone_turns", prompt.milestoneTurns)
                put("weekNumber", prompt.weekNumber.coerceAtLeast(1))
                put("week_number", prompt.weekNumber.coerceAtLeast(1))
            }

            val patchPayload = JSONObject().apply {
                put("spoken_text", prompt.title)
                put("feedback", jsonPayload.toString())
                put("overall_score", 100)
            }

            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.PROMPT_SYNC&target_phrase=eq.$encodedPromptId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInFallback = true
                    }
                }
            }

            if (!patched) {
                cleanSyncRows("PROMPT_SYNC", prompt.id)

                val evalObj = JSONObject().apply {
                    put("student_id", "PROMPT_SYNC")
                    put("target_phrase", prompt.id)
                    put("spoken_text", prompt.title)
                    put("feedback", jsonPayload.toString())
                    put("pronunciation_score", 100)
                    put("fluency_score", 100)
                    put("grammar_score", 100)
                    put("vocabulary_score", 100)
                    put("overall_score", 100)
                }

                val fallbackReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/speaking_evaluations")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(fallbackReq).execute().use { response ->
                    if (response.isSuccessful) savedInFallback = true
                }
            }
        } catch (_: Exception) {}

        savedInPrimary || savedInFallback
    }

    suspend fun fetchSpeakingWeeks(): List<SpeakingWeek> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<SpeakingWeek>()
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.WEEK_SYNC&select=*&limit=200")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val arr = JSONArray(body)
                    for (i in 0 until arr.length()) {
                        val row = arr.getJSONObject(i)
                        val feedback = row.optString("feedback", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val num = obj.optInt("weekNumber", row.optInt("overall_score", 1)).coerceAtLeast(1)
                                list.add(
                                    SpeakingWeek(
                                        weekNumber = num,
                                        title = obj.optString("title", row.optString("spoken_text", "Week $num")).ifBlank { "Week $num" },
                                        subtitle = obj.optString("subtitle", "Tap any topic to speak with Nova AI Coach"),
                                        sectionLabel = obj.optString("sectionLabel", "SECTION $num").ifBlank { "SECTION $num" },
                                        createdBy = obj.optString("createdBy", "Faculty")
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        list.distinctBy { it.weekNumber }.sortedBy { it.weekNumber }
    }

    suspend fun saveSpeakingWeek(week: SpeakingWeek): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        val num = week.weekNumber.coerceAtLeast(1)
        val key = "week_$num"
        val encodedKey = try { java.net.URLEncoder.encode(key, "UTF-8") } catch (_: Exception) { key }
        val jsonPayload = JSONObject().apply {
            put("weekNumber", num)
            put("title", week.title.ifBlank { "Week $num" })
            put("subtitle", week.subtitle.ifBlank { "Tap any topic to speak with Nova AI Coach" })
            put("sectionLabel", week.sectionLabel.ifBlank { "SECTION $num" })
            put("createdBy", week.createdBy.ifBlank { "Faculty" })
        }
        try {
            val patchPayload = JSONObject().apply {
                put("spoken_text", week.title.ifBlank { "Week $num" })
                put("feedback", jsonPayload.toString())
                put("overall_score", num)
            }
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.WEEK_SYNC&target_phrase=eq.$encodedKey")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        return@withContext true
                    }
                }
            }

            cleanSyncRows("WEEK_SYNC", key)
            val evalObj = JSONObject().apply {
                put("student_id", "WEEK_SYNC")
                put("target_phrase", key)
                put("spoken_text", week.title.ifBlank { "Week $num" })
                put("feedback", jsonPayload.toString())
                put("pronunciation_score", num)
                put("fluency_score", num)
                put("grammar_score", num)
                put("vocabulary_score", num)
                put("overall_score", num)
            }
            val postReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(postReq).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteSpeakingWeek(weekNumber: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        val key = "week_${weekNumber.coerceAtLeast(1)}"
        cleanSyncRows("WEEK_SYNC", key)
        true
    }

    suspend fun saveNovaMilestoneTarget(target: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        val clean = target.coerceIn(1, 30)
        try {
            val patchPayload = JSONObject().apply {
                put("spoken_text", "Nova Milestone Goal")
                put("feedback", JSONObject().apply { put("milestoneTurns", clean) }.toString())
                put("overall_score", clean)
            }
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.NOVA_CONFIG_SYNC&target_phrase=eq.milestone_turns")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        return@withContext true
                    }
                }
            }

            cleanSyncRows("NOVA_CONFIG_SYNC", "milestone_turns")
            val evalObj = JSONObject().apply {
                put("student_id", "NOVA_CONFIG_SYNC")
                put("target_phrase", "milestone_turns")
                put("spoken_text", "Nova Milestone Goal")
                put("feedback", JSONObject().apply { put("milestoneTurns", clean) }.toString())
                put("pronunciation_score", clean)
                put("fluency_score", clean)
                put("grammar_score", clean)
                put("vocabulary_score", clean)
                put("overall_score", clean)
            }
            val postReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(postReq).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchNovaMilestoneTarget(): Int? = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext null
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.NOVA_CONFIG_SYNC&target_phrase=eq.milestone_turns&select=overall_score,feedback&limit=1")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val arr = JSONArray(body)
                    if (arr.length() > 0) {
                        val row = arr.getJSONObject(0)
                        val scoreVal = row.optInt("overall_score", 0)
                        if (scoreVal in 1..30) return@withContext scoreVal
                        val fb = row.optString("feedback", "")
                        if (fb.isNotBlank()) {
                            val obj = JSONObject(fb)
                            val fbVal = obj.optInt("milestoneTurns", 0)
                            if (fbVal in 1..30) return@withContext fbVal
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun deleteSpeakingPrompt(promptId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false
        val encodedPromptId = try { java.net.URLEncoder.encode(promptId, "UTF-8") } catch (_: Exception) { promptId }

        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_prompts?id=eq.$encodedPromptId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("PROMPT_SYNC", promptId)
            success = true
        } catch (_: Exception) {}

        success
    }

    // =========================================================================
    // MCQ ONLINE EXAMS & QUESTIONS SYNC
    // =========================================================================

    suspend fun saveExam(exam: Exam): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var savedInPrimary = false
        var savedInFallback = false
        val encodedId = try { java.net.URLEncoder.encode(exam.id, "UTF-8") } catch (_: Exception) { exam.id }

        val qArr = JSONArray()
        for (q in exam.questions) {
            val qObj = JSONObject().apply {
                put("id", q.id)
                put("question", q.question)
                val optArr = JSONArray()
                q.options.forEach { optArr.put(it) }
                put("options", optArr)
                put("correctOptionIndex", q.correctOptionIndex)
                put("correct_option_index", q.correctOptionIndex)
                put("marks", q.marks)
                put("explanation", q.explanation)
                put("questionType", q.questionType.name)
                put("question_type", q.questionType.name)
                put("expectedAnswer", q.expectedAnswer)
                put("expected_answer", q.expectedAnswer)
            }
            qArr.put(qObj)
        }

        val examObj = JSONObject().apply {
            put("id", exam.id)
            put("title", exam.title)
            put("description", exam.description)
            put("teacher_name", exam.teacherName)
            put("teacherName", exam.teacherName)
            put("target_class", exam.targetClass)
            put("targetClass", exam.targetClass)
            put("subject_category", exam.subjectCategory)
            put("subjectCategory", exam.subjectCategory)
            put("duration_minutes", exam.durationMinutes)
            put("durationMinutes", exam.durationMinutes)
            put("total_marks", exam.totalMarks)
            put("totalMarks", exam.totalMarks)
            put("passing_percentage", exam.passingPercentage)
            put("passingPercentage", exam.passingPercentage)
            put("created_at", exam.createdAt)
            put("createdAt", exam.createdAt)
            put("due_date", exam.dueDate)
            put("dueDate", exam.dueDate)
            put("is_published", exam.isPublished)
            put("isPublished", exam.isPublished)
            put("questions", qArr)
        }

        // 1. Try primary exams table
        try {
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/exams?id=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(examObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInPrimary = true
                    }
                }
            }

            if (!patched) {
                val postReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/exams")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(examObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(postReq).execute().use { resp ->
                    if (resp.isSuccessful) savedInPrimary = true
                }
            }
        } catch (_: Exception) {}

        val examSyncObj = JSONObject(examObj.toString()).apply {
            put("availableFromMillis", exam.availableFromMillis)
            put("available_from_millis", exam.availableFromMillis)
            put("availableUntilMillis", exam.availableUntilMillis)
            put("available_until_millis", exam.availableUntilMillis)
        }

        // 2. Always sync to speaking_evaluations
        try {
            val patchPayload = JSONObject().apply {
                put("spoken_text", exam.title)
                put("feedback", examSyncObj.toString())
                put("overall_score", exam.passingPercentage)
            }

            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.EXAM_SYNC&target_phrase=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(patchPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        savedInFallback = true
                    }
                }
            }

            if (!patched) {
                cleanSyncRows("EXAM_SYNC", exam.id)

                val evalObj = JSONObject().apply {
                    put("student_id", "EXAM_SYNC")
                    put("target_phrase", exam.id)
                    put("spoken_text", exam.title)
                    put("feedback", examSyncObj.toString())
                    put("pronunciation_score", 100)
                    put("fluency_score", 100)
                    put("grammar_score", 100)
                    put("vocabulary_score", 100)
                    put("overall_score", exam.passingPercentage)
                }

                val postReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/speaking_evaluations")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(postReq).execute().use { resp ->
                    if (resp.isSuccessful) savedInFallback = true
                }
            }
        } catch (_: Exception) {}

        savedInPrimary || savedInFallback
    }

    suspend fun deleteExam(examId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var success = false
        val encodedId = try { java.net.URLEncoder.encode(examId, "UTF-8") } catch (_: Exception) { examId }

        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/exams?id=eq.$encodedId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()
            client.newCall(req).execute().use {
                if (it.isSuccessful) success = true
            }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("EXAM_SYNC", examId)
            success = true
        } catch (_: Exception) {}

        success
    }

    suspend fun fetchExams(): List<Exam> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<Exam>()
        val seenIds = mutableSetOf<String>()

        fun parseExamJson(obj: JSONObject, fallbackId: String = ""): Exam? {
            val id = obj.optString("id", fallbackId).ifBlank { return null }
            val qArr = obj.optJSONArray("questions")
            val questions = mutableListOf<ExamQuestion>()
            if (qArr != null) {
                for (j in 0 until qArr.length()) {
                    val qObj = qArr.getJSONObject(j)
                    val optArr = qObj.optJSONArray("options")
                    val options = mutableListOf<String>()
                    if (optArr != null) {
                        for (k in 0 until optArr.length()) options.add(optArr.getString(k))
                    }
                    val qTypeRaw = qObj.optString("questionType", qObj.optString("question_type", "MCQ")).uppercase()
                    val qType = if (qTypeRaw == ExamQuestionType.LONG_ANSWER.name) {
                        ExamQuestionType.LONG_ANSWER
                    } else {
                        ExamQuestionType.MCQ
                    }
                    questions.add(
                        ExamQuestion(
                            id = qObj.optString("id", "q_$j"),
                            question = qObj.optString("question", ""),
                            options = options,
                            correctOptionIndex = if (qObj.has("correctOptionIndex")) qObj.optInt("correctOptionIndex", 0) else qObj.optInt("correct_option_index", 0),
                            marks = qObj.optInt("marks", 5),
                            explanation = qObj.optString("explanation", ""),
                            questionType = qType,
                            expectedAnswer = qObj.optString("expectedAnswer", qObj.optString("expected_answer", ""))
                        )
                    )
                }
            }
            return Exam(
                id = id,
                title = obj.optString("title", "Exam"),
                description = obj.optString("description", ""),
                teacherName = obj.optString("teacherName", obj.optString("teacher_name", "Faculty")),
                targetClass = obj.optString("targetClass", obj.optString("target_class", AcademicClasses.DEFAULT_CLASS)),
                subjectCategory = obj.optString("subjectCategory", obj.optString("subject_category", "English Grammar")),
                durationMinutes = if (obj.has("durationMinutes")) obj.optInt("durationMinutes", 15) else obj.optInt("duration_minutes", 15),
                totalMarks = if (obj.has("totalMarks")) obj.optInt("totalMarks", 50) else obj.optInt("total_marks", 50),
                passingPercentage = if (obj.has("passingPercentage")) obj.optInt("passingPercentage", 60) else obj.optInt("passing_percentage", 60),
                questions = questions,
                createdAt = obj.optString("createdAt", obj.optString("created_at", "Nov 2026")),
                dueDate = obj.optString("dueDate", obj.optString("due_date", "Tomorrow, 6 PM")),
                isPublished = if (obj.has("isPublished")) obj.optBoolean("isPublished", true) else obj.optBoolean("is_published", true),
                availableFromMillis = if (obj.has("availableFromMillis")) obj.optLong("availableFromMillis", 0L) else obj.optLong("available_from_millis", 0L),
                availableUntilMillis = if (obj.has("availableUntilMillis")) obj.optLong("availableUntilMillis", 0L) else obj.optLong("available_until_millis", 0L)
            )
        }

        // 1. Try primary exams table
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/exams?select=*&limit=500")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val exam = parseExamJson(array.getJSONObject(i))
                        if (exam != null && seenIds.add(exam.id)) {
                            list.add(exam)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query fallback channel in speaking_evaluations (also carries schedule window metadata)
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.EXAM_SYNC&select=*&limit=500")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val row = array.getJSONObject(i)
                        val feedback = row.optString("feedback", "")
                        val targetId = row.optString("target_phrase", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val exam = parseExamJson(obj, targetId)
                                if (exam != null) {
                                    if (seenIds.add(exam.id)) {
                                        list.add(exam)
                                    } else {
                                        val existingIdx = list.indexOfFirst { it.id == exam.id }
                                        if (existingIdx >= 0) {
                                            list[existingIdx] = exam
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun saveExamSubmission(submission: ExamSubmission): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var saved = false
        val encodedSubId = try { java.net.URLEncoder.encode(submission.id, "UTF-8") } catch (_: Exception) { submission.id }

        val answersObj = JSONObject()
        submission.selectedAnswers.forEach { (k, v) -> answersObj.put(k, v) }

        val writtenObj = JSONObject()
        submission.writtenAnswers.forEach { (k, v) -> writtenObj.put(k, v) }

        val longScoresObj = JSONObject()
        submission.longAnswerScores.forEach { (k, v) -> longScoresObj.put(k, v) }

        val subObj = JSONObject().apply {
            put("id", submission.id)
            put("exam_id", submission.examId)
            put("examId", submission.examId)
            put("exam_title", submission.examTitle)
            put("examTitle", submission.examTitle)
            put("student_id", submission.studentId)
            put("studentId", submission.studentId)
            put("student_name", submission.studentName)
            put("studentName", submission.studentName)
            put("student_class", submission.studentClass)
            put("studentClass", submission.studentClass)
            put("student_avatar", submission.studentAvatar)
            put("studentAvatar", submission.studentAvatar)
            put("selected_answers", answersObj)
            put("selectedAnswers", answersObj)
            put("score_obtained", submission.scoreObtained)
            put("scoreObtained", submission.scoreObtained)
            put("total_marks", submission.totalMarks)
            put("totalMarks", submission.totalMarks)
            put("percentage", submission.percentage)
            put("is_passed", submission.isPassed)
            put("isPassed", submission.isPassed)
            put("grade", submission.grade)
            put("performance_title", submission.performanceTitle)
            put("performanceTitle", submission.performanceTitle)
            put("time_taken_seconds", submission.timeTakenSeconds)
            put("timeTakenSeconds", submission.timeTakenSeconds)
            put("completed_at", submission.completedAt)
            put("completedAt", submission.completedAt)
            put("certificate_id", submission.certificateId)
            put("certificateId", submission.certificateId)
        }

        val subSyncObj = JSONObject(subObj.toString()).apply {
            put("writtenAnswers", writtenObj)
            put("written_answers", writtenObj)
            put("longAnswerScores", longScoresObj)
            put("long_answer_scores", longScoresObj)
            put("gradingStatus", submission.gradingStatus)
            put("grading_status", submission.gradingStatus)
            put("teacherFeedback", submission.teacherFeedback)
            put("teacher_feedback", submission.teacherFeedback)
        }

        try {
            val patchReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/exam_submissions?id=eq.$encodedSubId")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Prefer", "return=representation")
                .patch(subObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var patched = false
            client.newCall(patchReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    if (body.trim().startsWith("[") && body.trim() != "[]") {
                        patched = true
                        saved = true
                    }
                }
            }

            if (!patched) {
                val postReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/exam_submissions")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(subObj.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(postReq).execute().use { resp ->
                    if (resp.isSuccessful) saved = true
                }
            }
        } catch (_: Exception) {}

        try {
            cleanSyncRows("SUBMISSION_SYNC", submission.id)
            val evalObj = JSONObject().apply {
                put("student_id", "SUBMISSION_SYNC")
                put("target_phrase", submission.id)
                put("spoken_text", submission.examId)
                put("feedback", subSyncObj.toString())
                put("overall_score", submission.percentage)
            }
            val postReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .post(evalObj.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(postReq).execute().use { resp ->
                if (resp.isSuccessful) saved = true
            }
        } catch (_: Exception) {}

        saved
    }

    suspend fun fetchExamSubmissions(): List<ExamSubmission> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext emptyList()
        val list = mutableListOf<ExamSubmission>()
        val seenIds = mutableSetOf<String>()

        fun parseSubJson(obj: JSONObject, fallbackId: String = ""): ExamSubmission? {
            val id = obj.optString("id", fallbackId).ifBlank { return null }
            val ansObj = obj.optJSONObject("selected_answers") ?: obj.optJSONObject("selectedAnswers")
            val answers = mutableMapOf<String, Int>()
            if (ansObj != null) {
                ansObj.keys().forEach { k -> answers[k] = ansObj.optInt(k, 0) }
            }
            val writtenObj = obj.optJSONObject("written_answers") ?: obj.optJSONObject("writtenAnswers")
            val writtenAnswers = mutableMapOf<String, String>()
            if (writtenObj != null) {
                writtenObj.keys().forEach { k -> writtenAnswers[k] = writtenObj.optString(k, "") }
            }
            val longScoresObj = obj.optJSONObject("long_answer_scores") ?: obj.optJSONObject("longAnswerScores")
            val longAnswerScores = mutableMapOf<String, Int>()
            if (longScoresObj != null) {
                longScoresObj.keys().forEach { k -> longAnswerScores[k] = longScoresObj.optInt(k, 0) }
            }
            return ExamSubmission(
                id = id,
                examId = obj.optString("examId", obj.optString("exam_id", "")),
                examTitle = obj.optString("examTitle", obj.optString("exam_title", "")),
                studentId = obj.optString("studentId", obj.optString("student_id", "")),
                studentName = obj.optString("studentName", obj.optString("student_name", "")),
                studentClass = obj.optString("studentClass", obj.optString("student_class", "Class 10")),
                studentAvatar = obj.optString("studentAvatar", obj.optString("student_avatar", "🎓")),
                selectedAnswers = answers,
                writtenAnswers = writtenAnswers,
                longAnswerScores = longAnswerScores,
                gradingStatus = obj.optString("gradingStatus", obj.optString("grading_status", "AUTO_GRADED")),
                teacherFeedback = obj.optString("teacherFeedback", obj.optString("teacher_feedback", "")),
                scoreObtained = if (obj.has("scoreObtained")) obj.optInt("scoreObtained", 0) else obj.optInt("score_obtained", 0),
                totalMarks = if (obj.has("totalMarks")) obj.optInt("totalMarks", 50) else obj.optInt("total_marks", 50),
                percentage = obj.optInt("percentage", 0),
                isPassed = if (obj.has("isPassed")) obj.optBoolean("isPassed", false) else obj.optBoolean("is_passed", false),
                grade = obj.optString("grade", "B"),
                performanceTitle = obj.optString("performanceTitle", obj.optString("performance_title", "Academic Distinction")),
                timeTakenSeconds = if (obj.has("timeTakenSeconds")) obj.optInt("timeTakenSeconds", 0) else obj.optInt("time_taken_seconds", 0),
                completedAt = obj.optString("completedAt", obj.optString("completed_at", "Today")),
                certificateId = obj.optString("certificateId", obj.optString("certificate_id", "SPK-EXM-0000"))
            )
        }

        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/exam_submissions?select=*&limit=500")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val array = JSONArray(resp.body?.string() ?: "")
                    for (i in 0 until array.length()) {
                        val sub = parseSubJson(array.getJSONObject(i))
                        if (sub != null && seenIds.add(sub.id)) list.add(sub)
                    }
                }
            }
        } catch (_: Exception) {}

        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/speaking_evaluations?student_id=eq.SUBMISSION_SYNC&select=*&limit=500")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val array = JSONArray(resp.body?.string() ?: "")
                    for (i in 0 until array.length()) {
                        val row = array.getJSONObject(i)
                        val feedback = row.optString("feedback", "")
                        val targetId = row.optString("target_phrase", "")
                        if (feedback.isNotBlank()) {
                            try {
                                val obj = JSONObject(feedback)
                                val sub = parseSubJson(obj, targetId)
                                if (sub != null) {
                                    if (seenIds.add(sub.id)) {
                                        list.add(sub)
                                    } else {
                                        val existingIdx = list.indexOfFirst { it.id == sub.id }
                                        if (existingIdx >= 0) {
                                            list[existingIdx] = sub
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        list
    }

    suspend fun deleteStudentExamSubmissions(
        studentId: String,
        studentName: String,
        clearedSubmissionIds: List<String> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        var anyDeleted = false
        try {
            val cleanId = studentId.trim()
            if (cleanId.isNotEmpty()) {
                val encodedId = java.net.URLEncoder.encode(cleanId, "UTF-8")
                val reqById = Request.Builder()
                    .url("$supabaseUrl/rest/v1/exam_submissions?student_id=eq.$encodedId")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .delete()
                    .build()
                client.newCall(reqById).execute().use { resp ->
                    if (resp.isSuccessful) anyDeleted = true
                }
            }
            val cleanName = studentName.trim()
            if (cleanName.isNotEmpty()) {
                val encodedName = java.net.URLEncoder.encode(cleanName, "UTF-8")
                val reqByName = Request.Builder()
                    .url("$supabaseUrl/rest/v1/exam_submissions?student_name=eq.$encodedName")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .delete()
                    .build()
                client.newCall(reqByName).execute().use { resp ->
                    if (resp.isSuccessful) anyDeleted = true
                }
            }
            clearedSubmissionIds.forEach { subId ->
                if (subId.isNotBlank()) {
                    cleanSyncRows("SUBMISSION_SYNC", subId)
                    val encodedSubId = java.net.URLEncoder.encode(subId, "UTF-8")
                    val reqBySubId = Request.Builder()
                        .url("$supabaseUrl/rest/v1/exam_submissions?id=eq.$encodedSubId")
                        .addHeader("apikey", supabaseAnonKey)
                        .addHeader("Authorization", "Bearer $supabaseAnonKey")
                        .delete()
                        .build()
                    client.newCall(reqBySubId).execute().use { resp ->
                        if (resp.isSuccessful) anyDeleted = true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error deleting student exam submissions: ${e.message}")
        }
        anyDeleted
    }

    // =========================================================================
    // ENGLISH ADVENTURE PROGRESSION
    // =========================================================================

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

    companion object {
        val SQL_SCHEMA_BLUEPRINT = """
            -- =========================================================
            -- Speakly Supabase PostgreSQL Schema & RLS Permissions
            -- Run this in your Supabase SQL Editor:
            -- =========================================================

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

            -- 3. Student Progress Table
            CREATE TABLE IF NOT EXISTS student_progress (
                student_id TEXT PRIMARY KEY,
                xp_points INT DEFAULT 0,
                coins INT DEFAULT 0,
                speaking_score INT DEFAULT 0,
                completed_stages INT DEFAULT 0,
                last_active TIMESTAMPTZ DEFAULT NOW()
            );

            -- 4. Speaking Evaluations & Cloud Sync Table
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

            -- 6. English & Assamese Vocabulary Words Table
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

            -- 7. Game Challenges Table (Sentence Builder, Word Match, Missing Letters)
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

            -- 8. Pronunciation Drills Table
            CREATE TABLE IF NOT EXISTS pronunciation_drills (
                id TEXT PRIMARY KEY,
                word_or_sentence TEXT NOT NULL,
                ipa_phonetic TEXT,
                audio_focus TEXT,
                difficulty TEXT DEFAULT 'Medium',
                tips TEXT,
                created_by TEXT DEFAULT 'Faculty',
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 9. AI Speaking Prompts Table
            CREATE TABLE IF NOT EXISTS speaking_prompts (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                category TEXT DEFAULT 'General',
                question TEXT NOT NULL,
                follow_up_tip TEXT,
                keywords JSONB,
                min_seconds INT DEFAULT 20,
                created_by TEXT DEFAULT 'Faculty',
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 10. English Adventure Progress Table
            CREATE TABLE IF NOT EXISTS student_adventure_progress (
                student_id TEXT PRIMARY KEY,
                current_world TEXT,
                completed_levels INT DEFAULT 0,
                total_stars INT DEFAULT 0,
                total_coins INT DEFAULT 0,
                total_xp INT DEFAULT 0,
                last_updated TIMESTAMPTZ DEFAULT NOW()
            );

            -- Enable RLS and grant full permissions to public Anon/Publishable key
            ALTER TABLE students ENABLE ROW LEVEL SECURITY;
            ALTER TABLE teachers ENABLE ROW LEVEL SECURITY;
            ALTER TABLE student_progress ENABLE ROW LEVEL SECURITY;
            ALTER TABLE speaking_evaluations ENABLE ROW LEVEL SECURITY;
            ALTER TABLE assignments ENABLE ROW LEVEL SECURITY;
            ALTER TABLE vocabulary_words ENABLE ROW LEVEL SECURITY;
            ALTER TABLE game_challenges ENABLE ROW LEVEL SECURITY;
            ALTER TABLE pronunciation_drills ENABLE ROW LEVEL SECURITY;
            ALTER TABLE speaking_prompts ENABLE ROW LEVEL SECURITY;
            ALTER TABLE student_adventure_progress ENABLE ROW LEVEL SECURITY;

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

            DROP POLICY IF EXISTS "Allow public vocabulary_words" ON vocabulary_words;
            CREATE POLICY "Allow public vocabulary_words" ON vocabulary_words FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public game_challenges" ON game_challenges;
            CREATE POLICY "Allow public game_challenges" ON game_challenges FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public pronunciation_drills" ON pronunciation_drills;
            CREATE POLICY "Allow public pronunciation_drills" ON pronunciation_drills FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public speaking_prompts" ON speaking_prompts;
            CREATE POLICY "Allow public speaking_prompts" ON speaking_prompts FOR ALL USING (true) WITH CHECK (true);

            DROP POLICY IF EXISTS "Allow public adventure" ON student_adventure_progress;
            CREATE POLICY "Allow public adventure" ON student_adventure_progress FOR ALL USING (true) WITH CHECK (true);
        """.trimIndent()
    }
}
