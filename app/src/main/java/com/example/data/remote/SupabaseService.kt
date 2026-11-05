package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class SupabaseConnectionState {
    object Disconnected : SupabaseConnectionState()
    object Connecting : SupabaseConnectionState()
    data class Connected(val url: String, val message: String) : SupabaseConnectionState()
    data class Error(val message: String) : SupabaseConnectionState()
}

class SupabaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Configurable endpoint & key (defaults to BuildConfig or user input)
    var supabaseUrl: String = try {
        BuildConfig.SUPABASE_URL.takeIf { it.isNotBlank() && !it.contains("your-project") } ?: ""
    } catch (e: Exception) { "" }

    var supabaseAnonKey: String = try {
        BuildConfig.SUPABASE_ANON_KEY.takeIf { it.isNotBlank() && !it.contains("your-supabase") } ?: ""
    } catch (e: Exception) { "" }

    fun isConfigured(): Boolean {
        return supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()
    }

    suspend fun testConnection(url: String = supabaseUrl, key: String = supabaseAnonKey): SupabaseConnectionState = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim().removeSuffix("/")
        if (cleanUrl.isBlank() || key.isBlank()) {
            return@withContext SupabaseConnectionState.Error("Please provide both Supabase URL and Anon Key.")
        }

        try {
            val request = Request.Builder()
                .url("$cleanUrl/rest/v1/")
                .addHeader("apikey", key.trim())
                .addHeader("Authorization", "Bearer ${key.trim()}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 404 || response.code == 200) {
                    supabaseUrl = cleanUrl
                    supabaseAnonKey = key.trim()
                    SupabaseConnectionState.Connected(cleanUrl, "Connected successfully to Supabase PostgreSQL backend (HTTP ${response.code})")
                } else {
                    SupabaseConnectionState.Error("Supabase returned status code ${response.code}: ${response.message}")
                }
            }
        } catch (e: Exception) {
            SupabaseConnectionState.Error("Network error connecting to Supabase: ${e.localizedMessage}")
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

    companion object {
        val SQL_SCHEMA_BLUEPRINT = """
            -- Speakly Supabase PostgreSQL Schema
            -- Run this in your Supabase SQL Editor:

            CREATE TABLE IF NOT EXISTS student_progress (
                student_id TEXT PRIMARY KEY,
                xp_points INT DEFAULT 0,
                coins INT DEFAULT 0,
                speaking_score INT DEFAULT 0,
                completed_stages INT DEFAULT 0,
                last_active TIMESTAMPTZ DEFAULT NOW()
            );

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

            CREATE TABLE IF NOT EXISTS assignments (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                teacher_name TEXT,
                batch TEXT,
                due_date TEXT,
                instructions TEXT,
                max_points INT DEFAULT 100
            );

            -- Enable Row Level Security (RLS)
            ALTER TABLE student_progress ENABLE ROW LEVEL SECURITY;
            ALTER TABLE speaking_evaluations ENABLE ROW LEVEL SECURITY;

            -- Allow anonymous / authenticated read and write for rapid integration
            CREATE POLICY "Allow public read" ON student_progress FOR SELECT USING (true);
            CREATE POLICY "Allow public insert/update" ON student_progress FOR ALL USING (true);
            CREATE POLICY "Allow public evals" ON speaking_evaluations FOR ALL USING (true);
        """.trimIndent()
    }
}
