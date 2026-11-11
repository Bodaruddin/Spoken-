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
        private const val KEY_IS_ADMIN_SESSION = "key_is_admin_session"

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

        // Missing Letters Categories
        private const val KEY_MISSING_LETTERS_CATEGORIES = "key_missing_letters_categories"

        // Teacher Uploaded & Created Content Keys
        private const val KEY_EXAMS_LIST = "key_exams_list"
        private const val KEY_EXAM_SUBMISSIONS = "key_exam_submissions"
        private const val KEY_ASSIGNMENTS_LIST = "key_assignments_list"
        private const val KEY_WORD_MATCH_LIST = "key_word_match_list"
        private const val KEY_MISSING_LETTERS_LIST = "key_missing_letters_list"
        private const val KEY_SENTENCE_BUILDER_LIST = "key_sentence_builder_list"
        private const val KEY_LISTEN_CHOOSE_LIST = "key_listen_choose_list"
        private const val KEY_PRONUNCIATION_BATTLE_LIST = "key_pronunciation_battle_list"
        private const val KEY_FAST_VOCABULARY_LIST = "key_fast_vocabulary_list"
        private const val KEY_GRAMMAR_CHALLENGE_LIST = "key_grammar_challenge_list"
        private const val KEY_VOCABULARY_WORDS_LIST = "key_vocabulary_words_list"
    }

    fun saveMissingLettersCategories(categories: List<String>) {
        val jsonArray = JSONArray()
        categories.forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_MISSING_LETTERS_CATEGORIES, jsonArray.toString()).apply()
    }

    fun getMissingLettersCategories(): List<String>? {
        val raw = prefs.getString(KEY_MISSING_LETTERS_CATEGORIES, null) ?: return null
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
    // SESSION PERSISTENCE (Admin, Teacher, Student)
    // =========================================================================

    fun saveSession(user: User) {
        val userJson = userToJson(user).toString()
        val editor = prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_SESSION_USER, userJson)
            .putString(KEY_SESSION_ROLE, user.role.name)
        if (user.role == Role.ADMIN) {
            editor.putBoolean(KEY_IS_ADMIN_SESSION, true)
        }
        editor.apply()
    }

    fun setAdminSession(isAdmin: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ADMIN_SESSION, isAdmin).apply()
    }

    fun isAdminSession(): Boolean {
        return prefs.getBoolean(KEY_IS_ADMIN_SESSION, false)
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_SESSION_USER)
            .remove(KEY_SESSION_ROLE)
            .remove(KEY_IS_ADMIN_SESSION)
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
    // TEACHER UPLOADED EXAMS PERSISTENCE
    // =========================================================================

    fun saveExams(exams: List<Exam>) {
        try {
            val array = JSONArray()
            for (exam in exams) {
                array.put(examToJson(exam))
            }
            prefs.edit().putString(KEY_EXAMS_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getExams(): List<Exam>? {
        val raw = prefs.getString(KEY_EXAMS_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Exam>()
            for (i in 0 until array.length()) {
                list.add(jsonToExam(array.getJSONObject(i)))
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveExamSubmissions(submissions: List<ExamSubmission>) {
        try {
            val array = JSONArray()
            for (sub in submissions) {
                array.put(submissionToJson(sub))
            }
            prefs.edit().putString(KEY_EXAM_SUBMISSIONS, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getExamSubmissions(): List<ExamSubmission>? {
        val raw = prefs.getString(KEY_EXAM_SUBMISSIONS, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ExamSubmission>()
            for (i in 0 until array.length()) {
                list.add(jsonToSubmission(array.getJSONObject(i)))
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // ASSIGNMENTS PERSISTENCE
    // =========================================================================

    fun saveAssignments(assignments: List<Assignment>) {
        try {
            val array = JSONArray()
            for (asg in assignments) {
                val obj = JSONObject()
                obj.put("id", asg.id)
                obj.put("title", asg.title)
                obj.put("teacherName", asg.teacherName)
                obj.put("batch", asg.batch)
                obj.put("dueDate", asg.dueDate)
                obj.put("instructions", asg.instructions)
                obj.put("topicType", asg.topicType)
                obj.put("maxPoints", asg.maxPoints)
                obj.put("submissionsCount", asg.submissionsCount)
                obj.put("totalAssigned", asg.totalAssigned)
                array.put(obj)
            }
            prefs.edit().putString(KEY_ASSIGNMENTS_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getAssignments(): List<Assignment>? {
        val raw = prefs.getString(KEY_ASSIGNMENTS_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Assignment>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Assignment(
                        id = obj.optString("id", "asg_${System.currentTimeMillis()}"),
                        title = obj.optString("title", ""),
                        teacherName = obj.optString("teacherName", "Faculty"),
                        batch = obj.optString("batch", AcademicClasses.DEFAULT_CLASS),
                        dueDate = obj.optString("dueDate", "In 3 Days"),
                        instructions = obj.optString("instructions", ""),
                        topicType = obj.optString("topicType", "Speaking"),
                        maxPoints = obj.optInt("maxPoints", 100),
                        submissionsCount = obj.optInt("submissionsCount", 0),
                        totalAssigned = obj.optInt("totalAssigned", 25)
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    // =========================================================================
    // GAME CHALLENGES PERSISTENCE
    // =========================================================================

    fun saveMissingLettersChallenges(challenges: List<MissingLettersChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("fullWord", c.fullWord)
                obj.put("hint", c.hint)
                val idxArr = JSONArray()
                c.missingIndices.forEach { idxArr.put(it) }
                obj.put("missingIndices", idxArr)
                obj.put("category", c.category)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_MISSING_LETTERS_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getMissingLettersChallenges(): List<MissingLettersChallenge>? {
        val raw = prefs.getString(KEY_MISSING_LETTERS_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<MissingLettersChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val idxArr = obj.optJSONArray("missingIndices")
                val indices = mutableListOf<Int>()
                if (idxArr != null) {
                    for (j in 0 until idxArr.length()) indices.add(idxArr.getInt(j))
                }
                list.add(
                    MissingLettersChallenge(
                        id = obj.optString("id", ""),
                        fullWord = obj.optString("fullWord", ""),
                        hint = obj.optString("hint", ""),
                        missingIndices = indices,
                        category = obj.optString("category", "General"),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveWordMatchChallenges(challenges: List<WordMatchChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("word", c.word)
                obj.put("definition", c.definition)
                obj.put("category", c.category)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_WORD_MATCH_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getWordMatchChallenges(): List<WordMatchChallenge>? {
        val raw = prefs.getString(KEY_WORD_MATCH_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<WordMatchChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WordMatchChallenge(
                        id = obj.optString("id", ""),
                        word = obj.optString("word", ""),
                        definition = obj.optString("definition", ""),
                        category = obj.optString("category", "General"),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveSentenceBuilderChallenges(challenges: List<SentenceBuilderChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("targetSentence", c.targetSentence)
                obj.put("hint", c.hint)
                obj.put("assameseTranslation", c.assameseTranslation)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_SENTENCE_BUILDER_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getSentenceBuilderChallenges(): List<SentenceBuilderChallenge>? {
        val raw = prefs.getString(KEY_SENTENCE_BUILDER_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<SentenceBuilderChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SentenceBuilderChallenge(
                        id = obj.optString("id", ""),
                        targetSentence = obj.optString("targetSentence", ""),
                        hint = obj.optString("hint", "Form a grammatically correct sentence"),
                        assameseTranslation = obj.optString("assameseTranslation", ""),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveListenChooseChallenges(challenges: List<ListenChooseChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("audioScript", c.audioScript)
                obj.put("question", c.question)
                val optArr = JSONArray()
                c.options.forEach { optArr.put(it) }
                obj.put("options", optArr)
                obj.put("correctIndex", c.correctIndex)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_LISTEN_CHOOSE_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getListenChooseChallenges(): List<ListenChooseChallenge>? {
        val raw = prefs.getString(KEY_LISTEN_CHOOSE_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ListenChooseChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optArr = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optArr != null) {
                    for (j in 0 until optArr.length()) options.add(optArr.getString(j))
                }
                list.add(
                    ListenChooseChallenge(
                        id = obj.optString("id", ""),
                        audioScript = obj.optString("audioScript", ""),
                        question = obj.optString("question", ""),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun savePronunciationBattleChallenges(challenges: List<PronunciationBattleChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("challengeSentence", c.challengeSentence)
                obj.put("targetPhonemeFocus", c.targetPhonemeFocus)
                obj.put("difficulty", c.difficulty)
                obj.put("passingScore", c.passingScore)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_PRONUNCIATION_BATTLE_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getPronunciationBattleChallenges(): List<PronunciationBattleChallenge>? {
        val raw = prefs.getString(KEY_PRONUNCIATION_BATTLE_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<PronunciationBattleChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PronunciationBattleChallenge(
                        id = obj.optString("id", ""),
                        challengeSentence = obj.optString("challengeSentence", ""),
                        targetPhonemeFocus = obj.optString("targetPhonemeFocus", ""),
                        difficulty = obj.optString("difficulty", "Medium"),
                        passingScore = obj.optInt("passingScore", 80),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveFastVocabularyChallenges(challenges: List<FastVocabularyChallenge>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("word", c.word)
                obj.put("prompt", c.prompt)
                val optArr = JSONArray()
                c.options.forEach { optArr.put(it) }
                obj.put("options", optArr)
                obj.put("correctIndex", c.correctIndex)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_FAST_VOCABULARY_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getFastVocabularyChallenges(): List<FastVocabularyChallenge>? {
        val raw = prefs.getString(KEY_FAST_VOCABULARY_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<FastVocabularyChallenge>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optArr = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optArr != null) {
                    for (j in 0 until optArr.length()) options.add(optArr.getString(j))
                }
                list.add(
                    FastVocabularyChallenge(
                        id = obj.optString("id", ""),
                        word = obj.optString("word", ""),
                        prompt = obj.optString("prompt", ""),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveGrammarChallenges(challenges: List<GrammarChallengeQuestion>) {
        try {
            val array = JSONArray()
            for (c in challenges) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("question", c.question)
                val optArr = JSONArray()
                c.options.forEach { optArr.put(it) }
                obj.put("options", optArr)
                obj.put("correctIndex", c.correctIndex)
                obj.put("explanation", c.explanation)
                obj.put("category", c.category)
                obj.put("createdBy", c.createdBy)
                array.put(obj)
            }
            prefs.edit().putString(KEY_GRAMMAR_CHALLENGE_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getGrammarChallenges(): List<GrammarChallengeQuestion>? {
        val raw = prefs.getString(KEY_GRAMMAR_CHALLENGE_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<GrammarChallengeQuestion>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optArr = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optArr != null) {
                    for (j in 0 until optArr.length()) options.add(optArr.getString(j))
                }
                list.add(
                    GrammarChallengeQuestion(
                        id = obj.optString("id", ""),
                        question = obj.optString("question", ""),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", ""),
                        category = obj.optString("category", "Tenses"),
                        createdBy = obj.optString("createdBy", "System")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
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

    private fun examToJson(exam: Exam): JSONObject {
        val obj = JSONObject()
        obj.put("id", exam.id)
        obj.put("title", exam.title)
        obj.put("description", exam.description)
        obj.put("teacherName", exam.teacherName)
        obj.put("targetClass", exam.targetClass)
        obj.put("subjectCategory", exam.subjectCategory)
        obj.put("durationMinutes", exam.durationMinutes)
        obj.put("totalMarks", exam.totalMarks)
        obj.put("passingPercentage", exam.passingPercentage)
        obj.put("createdAt", exam.createdAt)
        obj.put("dueDate", exam.dueDate)
        obj.put("isPublished", exam.isPublished)

        val qArr = JSONArray()
        for (q in exam.questions) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("question", q.question)
            val optArr = JSONArray()
            q.options.forEach { optArr.put(it) }
            qObj.put("options", optArr)
            qObj.put("correctOptionIndex", q.correctOptionIndex)
            qObj.put("marks", q.marks)
            qObj.put("explanation", q.explanation)
            qArr.put(qObj)
        }
        obj.put("questions", qArr)
        return obj
    }

    private fun jsonToExam(obj: JSONObject): Exam {
        val qArr = obj.optJSONArray("questions")
        val questions = mutableListOf<ExamQuestion>()
        if (qArr != null) {
            for (i in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(i)
                val optArr = qObj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optArr != null) {
                    for (j in 0 until optArr.length()) options.add(optArr.getString(j))
                }
                questions.add(
                    ExamQuestion(
                        id = qObj.optString("id", "q_$i"),
                        question = qObj.optString("question", ""),
                        options = options,
                        correctOptionIndex = qObj.optInt("correctOptionIndex", 0),
                        marks = qObj.optInt("marks", 5),
                        explanation = qObj.optString("explanation", "")
                    )
                )
            }
        }
        return Exam(
            id = obj.optString("id", "exm_${System.currentTimeMillis()}"),
            title = obj.optString("title", "Exam"),
            description = obj.optString("description", ""),
            teacherName = obj.optString("teacherName", "Faculty"),
            targetClass = obj.optString("targetClass", AcademicClasses.DEFAULT_CLASS),
            subjectCategory = obj.optString("subjectCategory", "English Grammar"),
            durationMinutes = obj.optInt("durationMinutes", 15),
            totalMarks = obj.optInt("totalMarks", 50),
            passingPercentage = obj.optInt("passingPercentage", 60),
            questions = questions,
            createdAt = obj.optString("createdAt", "Nov 2026"),
            dueDate = obj.optString("dueDate", "Upcoming"),
            isPublished = obj.optBoolean("isPublished", true)
        )
    }

    private fun submissionToJson(sub: ExamSubmission): JSONObject {
        val obj = JSONObject()
        obj.put("id", sub.id)
        obj.put("examId", sub.examId)
        obj.put("examTitle", sub.examTitle)
        obj.put("studentId", sub.studentId)
        obj.put("studentName", sub.studentName)
        obj.put("studentClass", sub.studentClass)
        obj.put("studentAvatar", sub.studentAvatar)
        obj.put("scoreObtained", sub.scoreObtained)
        obj.put("totalMarks", sub.totalMarks)
        obj.put("percentage", sub.percentage)
        obj.put("isPassed", sub.isPassed)
        obj.put("grade", sub.grade)
        obj.put("performanceTitle", sub.performanceTitle)
        obj.put("timeTakenSeconds", sub.timeTakenSeconds)
        obj.put("completedAt", sub.completedAt)
        obj.put("certificateId", sub.certificateId)

        val answersObj = JSONObject()
        sub.selectedAnswers.forEach { (k, v) -> answersObj.put(k, v) }
        obj.put("selectedAnswers", answersObj)
        return obj
    }

    private fun jsonToSubmission(obj: JSONObject): ExamSubmission {
        val answersObj = obj.optJSONObject("selectedAnswers")
        val selectedAnswers = mutableMapOf<String, Int>()
        if (answersObj != null) {
            val keys = answersObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                selectedAnswers[k] = answersObj.optInt(k, 0)
            }
        }
        return ExamSubmission(
            id = obj.optString("id", ""),
            examId = obj.optString("examId", ""),
            examTitle = obj.optString("examTitle", ""),
            studentId = obj.optString("studentId", ""),
            studentName = obj.optString("studentName", ""),
            studentClass = obj.optString("studentClass", ""),
            studentAvatar = obj.optString("studentAvatar", "🎓"),
            selectedAnswers = selectedAnswers,
            scoreObtained = obj.optInt("scoreObtained", 0),
            totalMarks = obj.optInt("totalMarks", 100),
            percentage = obj.optInt("percentage", 0),
            isPassed = obj.optBoolean("isPassed", false),
            grade = obj.optString("grade", "C"),
            performanceTitle = obj.optString("performanceTitle", "Satisfactory"),
            timeTakenSeconds = obj.optInt("timeTakenSeconds", 0),
            completedAt = obj.optString("completedAt", "Today"),
            certificateId = obj.optString("certificateId", "SPK-EXM-2026-0000")
        )
    }

    // =========================================================================
    // ENGLISH & ASSAMESE VOCABULARY WORDS PERSISTENCE
    // =========================================================================

    fun saveVocabularyWords(words: List<VocabularyWord>) {
        try {
            val array = JSONArray()
            for (w in words) {
                val obj = JSONObject()
                obj.put("id", w.id)
                obj.put("word", w.word)
                obj.put("phonetic", w.phonetic)
                obj.put("partOfSpeech", w.partOfSpeech)
                obj.put("meaning", w.meaning)
                obj.put("exampleSentence", w.exampleSentence)
                obj.put("category", w.category)
                obj.put("isFavorite", w.isFavorite)
                obj.put("isMastered", w.isMastered)
                obj.put("assameseMeaning", w.assameseMeaning)
                obj.put("assameseTransliteration", w.assameseTransliteration)
                obj.put("syllables", w.syllables)
                obj.put("coachingTip", w.coachingTip)
                obj.put("addedByTeacher", w.addedByTeacher)
                val synArray = JSONArray()
                w.synonyms.forEach { synArray.put(it) }
                obj.put("synonyms", synArray)
                array.put(obj)
            }
            prefs.edit().putString(KEY_VOCABULARY_WORDS_LIST, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getVocabularyWords(): List<VocabularyWord>? {
        val raw = prefs.getString(KEY_VOCABULARY_WORDS_LIST, null) ?: return null
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<VocabularyWord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val synList = mutableListOf<String>()
                val synArray = obj.optJSONArray("synonyms")
                if (synArray != null) {
                    for (j in 0 until synArray.length()) {
                        synList.add(synArray.getString(j))
                    }
                }
                list.add(
                    VocabularyWord(
                        id = obj.optString("id", "vocab_${System.currentTimeMillis()}_$i"),
                        word = obj.optString("word", ""),
                        phonetic = obj.optString("phonetic", ""),
                        partOfSpeech = obj.optString("partOfSpeech", "noun"),
                        meaning = obj.optString("meaning", ""),
                        exampleSentence = obj.optString("exampleSentence", ""),
                        category = obj.optString("category", "General"),
                        synonyms = synList,
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isMastered = obj.optBoolean("isMastered", false),
                        assameseMeaning = obj.optString("assameseMeaning", ""),
                        assameseTransliteration = obj.optString("assameseTransliteration", ""),
                        syllables = obj.optString("syllables", ""),
                        coachingTip = obj.optString("coachingTip", ""),
                        addedByTeacher = obj.optString("addedByTeacher", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }
}
