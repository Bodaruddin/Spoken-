package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "speakly_local_db"

        // Session Keys
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_SESSION_USER = "key_session_user"
        private const val KEY_SESSION_ROLE = "key_session_role"

        // Account Collections Keys
        private const val KEY_STUDENTS_LIST = "key_students_list"
        private const val KEY_TEACHERS_LIST = "key_teachers_list"
        private const val KEY_ACADEMIC_CLASSES = "key_academic_classes"

        // Staff Credentials Keys
        private const val KEY_ADMIN_PASSWORD = "key_admin_password"
        private const val KEY_ADMIN_PIN = "key_admin_pin"
        private const val KEY_TEACHER_PASSWORD = "key_teacher_password"
        private const val KEY_TEACHER_PIN = "key_teacher_pin"

        // Per-user profile prefix
        private const val PREFIX_STUDENT_PROFILE = "key_student_profile_"
    }

    // =========================================================================
    // SESSION PERSISTENCE (Admin, Teacher, Student)
    // =========================================================================

    fun saveSession(user: User) {
        val userJson = userToJson(user).toString()
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_SESSION_USER, userJson)
            .putString(KEY_SESSION_ROLE, user.role.name)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_SESSION_USER)
            .remove(KEY_SESSION_ROLE)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSavedUser(): User? {
        val userJson = prefs.getString(KEY_SESSION_USER, null) ?: return null
        return try {
            jsonToUser(JSONObject(userJson))
        } catch (_: Exception) {
            null
        }
    }

    fun getSavedRole(): Role? {
        val roleStr = prefs.getString(KEY_SESSION_ROLE, null) ?: return null
        return try {
            Role.valueOf(roleStr)
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // STUDENT ACCOUNTS PERSISTENCE
    // =========================================================================

    fun saveStudents(students: List<StudentManagementInfo>) {
        try {
            val jsonArray = JSONArray()
            for (student in students) {
                jsonArray.put(studentToJson(student))
            }
            prefs.edit().putString(KEY_STUDENTS_LIST, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun getStudents(): List<StudentManagementInfo>? {
        val raw = prefs.getString(KEY_STUDENTS_LIST, null) ?: return null
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<StudentManagementInfo>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(jsonToStudent(obj))
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveStudentProfile(profile: StudentProfile) {
        try {
            val json = profileToJson(profile).toString()
            prefs.edit().putString(PREFIX_STUDENT_PROFILE + profile.userId, json).apply()
        } catch (_: Exception) {
        }
    }

    fun getStudentProfile(userId: String): StudentProfile? {
        val raw = prefs.getString(PREFIX_STUDENT_PROFILE + userId, null) ?: return null
        return try {
            jsonToProfile(JSONObject(raw))
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // TEACHER ACCOUNTS PERSISTENCE
    // =========================================================================

    fun saveTeachers(teachers: List<User>) {
        try {
            val jsonArray = JSONArray()
            for (teacher in teachers) {
                jsonArray.put(userToJson(teacher))
            }
            prefs.edit().putString(KEY_TEACHERS_LIST, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun getTeachers(): List<User>? {
        val raw = prefs.getString(KEY_TEACHERS_LIST, null) ?: return null
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<User>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(jsonToUser(obj))
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // ACADEMIC CLASSES PERSISTENCE
    // =========================================================================

    fun saveClasses(classes: List<String>) {
        try {
            val jsonArray = JSONArray()
            for (cls in classes) {
                jsonArray.put(cls)
            }
            prefs.edit().putString(KEY_ACADEMIC_CLASSES, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun getClasses(): List<String>? {
        val raw = prefs.getString(KEY_ACADEMIC_CLASSES, null) ?: return null
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            if (list.isNotEmpty()) list else null
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // STAFF CREDENTIALS PERSISTENCE
    // =========================================================================

    fun saveAdminCredentials(password: String, pin: String) {
        prefs.edit()
            .putString(KEY_ADMIN_PASSWORD, password)
            .putString(KEY_ADMIN_PIN, pin)
            .apply()
    }

    fun getAdminPassword(): String? {
        return prefs.getString(KEY_ADMIN_PASSWORD, null)
    }

    fun getAdminPin(): String? {
        return prefs.getString(KEY_ADMIN_PIN, null)
    }

    fun saveTeacherCredentials(password: String, pin: String) {
        prefs.edit()
            .putString(KEY_TEACHER_PASSWORD, password)
            .putString(KEY_TEACHER_PIN, pin)
            .apply()
    }

    fun getTeacherPassword(): String? {
        return prefs.getString(KEY_TEACHER_PASSWORD, null)
    }

    fun getTeacherPin(): String? {
        return prefs.getString(KEY_TEACHER_PIN, null)
    }

    // =========================================================================
    // JSON SERIALIZERS / DESERIALIZERS
    // =========================================================================

    private fun userToJson(u: User): JSONObject {
        val obj = JSONObject()
        obj.put("id", u.id)
        obj.put("name", u.name)
        obj.put("email", u.email)
        obj.put("role", u.role.name)
        obj.put("avatarEmoji", u.avatarEmoji)
        obj.put("batch", u.batch)
        obj.put("isActive", u.isActive)
        obj.put("joinedDate", u.joinedDate)
        obj.put("securityPin", u.securityPin)
        return obj
    }

    private fun jsonToUser(obj: JSONObject): User {
        val roleStr = obj.optString("role", Role.STUDENT.name)
        val role = try { Role.valueOf(roleStr) } catch (_: Exception) { Role.STUDENT }
        return User(
            id = obj.optString("id", ""),
            name = obj.optString("name", ""),
            email = obj.optString("email", ""),
            role = role,
            avatarEmoji = obj.optString("avatarEmoji", "🦉"),
            batch = obj.optString("batch", AcademicClasses.DEFAULT_CLASS),
            isActive = obj.optBoolean("isActive", true),
            joinedDate = obj.optString("joinedDate", "Nov 2026"),
            securityPin = obj.optString("securityPin", "")
        )
    }

    private fun studentToJson(s: StudentManagementInfo): JSONObject {
        val obj = JSONObject()
        obj.put("id", s.id)
        obj.put("name", s.name)
        obj.put("email", s.email)
        obj.put("avatarEmoji", s.avatarEmoji)
        obj.put("batch", s.batch)
        obj.put("level", s.level.name)
        obj.put("isActive", s.isActive)
        obj.put("speakingScore", s.speakingScore)
        obj.put("pronunciationScore", s.pronunciationScore)
        obj.put("fluencyScore", s.fluencyScore)
        obj.put("grammarScore", s.grammarScore)
        obj.put("vocabularyScore", s.vocabularyScore)
        obj.put("listeningScore", s.listeningScore)
        obj.put("streakDays", s.streakDays)
        obj.put("xpPoints", s.xpPoints)
        obj.put("joinedDate", s.joinedDate)
        obj.put("teacherNotes", s.teacherNotes)
        obj.put("securityPin", s.securityPin)
        return obj
    }

    private fun jsonToStudent(obj: JSONObject): StudentManagementInfo {
        val levelStr = obj.optString("level", LearningLevel.BEGINNER.name)
        val level = try { LearningLevel.valueOf(levelStr) } catch (_: Exception) { LearningLevel.BEGINNER }
        return StudentManagementInfo(
            id = obj.optString("id", ""),
            name = obj.optString("name", ""),
            email = obj.optString("email", ""),
            avatarEmoji = obj.optString("avatarEmoji", "🎓"),
            batch = obj.optString("batch", AcademicClasses.DEFAULT_CLASS),
            level = level,
            isActive = obj.optBoolean("isActive", true),
            speakingScore = obj.optInt("speakingScore", 75),
            pronunciationScore = obj.optInt("pronunciationScore", 75),
            fluencyScore = obj.optInt("fluencyScore", 75),
            grammarScore = obj.optInt("grammarScore", 75),
            vocabularyScore = obj.optInt("vocabularyScore", 75),
            listeningScore = obj.optInt("listeningScore", 75),
            streakDays = obj.optInt("streakDays", 1),
            xpPoints = obj.optInt("xpPoints", 100),
            joinedDate = obj.optString("joinedDate", "Nov 2026"),
            teacherNotes = obj.optString("teacherNotes", ""),
            securityPin = obj.optString("securityPin", "1234")
        )
    }

    private fun profileToJson(p: StudentProfile): JSONObject {
        val obj = JSONObject()
        obj.put("userId", p.userId)
        obj.put("currentLevel", p.currentLevel.name)
        obj.put("streakDays", p.streakDays)
        obj.put("xpPoints", p.xpPoints)
        obj.put("coins", p.coins)
        obj.put("league", p.league)
        obj.put("overallScore", p.overallScore)
        obj.put("speakingScore", p.speakingScore)
        obj.put("pronunciationScore", p.pronunciationScore)
        obj.put("fluencyScore", p.fluencyScore)
        obj.put("grammarScore", p.grammarScore)
        obj.put("vocabularyScore", p.vocabularyScore)
        obj.put("listeningScore", p.listeningScore)
        obj.put("readingScore", p.readingScore)
        obj.put("completedStagesCount", p.completedStagesCount)
        obj.put("speakingPracticeMinutes", p.speakingPracticeMinutes)
        obj.put("lastPracticeDate", p.lastPracticeDate)
        return obj
    }

    private fun jsonToProfile(obj: JSONObject): StudentProfile {
        val levelStr = obj.optString("currentLevel", LearningLevel.BEGINNER.name)
        val level = try { LearningLevel.valueOf(levelStr) } catch (_: Exception) { LearningLevel.BEGINNER }
        return StudentProfile(
            userId = obj.optString("userId", ""),
            currentLevel = level,
            streakDays = obj.optInt("streakDays", 1),
            xpPoints = obj.optInt("xpPoints", 100),
            coins = obj.optInt("coins", 50),
            league = obj.optString("league", "Bronze League"),
            overallScore = obj.optInt("overallScore", 75),
            speakingScore = obj.optInt("speakingScore", 75),
            pronunciationScore = obj.optInt("pronunciationScore", 75),
            fluencyScore = obj.optInt("fluencyScore", 75),
            grammarScore = obj.optInt("grammarScore", 75),
            vocabularyScore = obj.optInt("vocabularyScore", 75),
            listeningScore = obj.optInt("listeningScore", 75),
            readingScore = obj.optInt("readingScore", 75),
            completedStagesCount = obj.optInt("completedStagesCount", 0),
            speakingPracticeMinutes = obj.optInt("speakingPracticeMinutes", 0),
            lastPracticeDate = obj.optString("lastPracticeDate", "")
        )
    }
}
