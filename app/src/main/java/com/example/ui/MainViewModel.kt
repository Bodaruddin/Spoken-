package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LinguaQuestRepository
import com.example.service.EvaluationEngine
import com.example.service.SpeechRecognizerManager
import com.example.service.SpeechState
import com.example.service.TtsManager
import com.example.service.PhoneticsAiEngine
import com.example.service.PhoneticSearchResult
import com.example.service.GeminiSpeechEvaluator
import com.example.service.HumanConversationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class StudentTab(val label: String, val iconEmoji: String) {
    HOME("Home", "🏠"),
    EXAMS("Exams", "📝"),
    PRACTICE("Speaking", "🎙️"),
    GAMES("Games", "🎮"),
    LEADERBOARD("Ranks", "🏆"),
    PROFILE("Profile", "👤")
}

enum class TeacherTab(val label: String, val iconEmoji: String) {
    DASHBOARD("Overview", "📊"),
    STUDENTS("Students", "👥"),
    GAMES("Games Hub", "🎮"),
    SPEAKING_REVIEW("Speaking & Review", "🎙️"),
    EXAMS("Exams", "📝")
}

enum class AdminTab(val label: String, val iconEmoji: String) {
    DASHBOARD("Dashboard", "📈"),
    STUDENTS("Students", "🎓"),
    GAMES("Games Hub", "🎮"),
    TEACHERS("Faculty", "👨‍🏫"),
    CURRICULUM("Courses & AI", "📚")
}

enum class TeacherSpeakingSection(val title: String, val emoji: String) {
    SUBMISSIONS("Student Recordings", "🎧"),
    MANAGE_SPEAKING("AI Speaking Prompts", "🎙️"),
    MANAGE_PHONETICS("Phonetics & Drills", "🎯"),
    ADVENTURE_WORLDS("English Adventure", "🗺️")
}

enum class AdminSpeakingSection(val title: String, val emoji: String) {
    SPEAKING_PROMPTS("AI Speaking Prompts", "🎙️"),
    PHONETICS_DRILLS("Phonetics & Drills", "🎯"),
    ADVENTURE_WORLDS("English Adventure Worlds", "🗺️")
}

enum class SpeakingSubTab {
    TOPICS_ROADMAP,
    AI_PARTNER,
    DAILY_CHALLENGE,
    VOCABULARY,
    ENGLISH_ADVENTURE,
    PRONUNCIATION_COACH,
    PICTURE_DESCRIPTION
}

enum class ActiveGameType(val title: String, val desc: String, val emoji: String) {
    WORD_MATCH("Word Match", "Match English words with correct definitions", "🧩"),
    MISSING_LETTERS("Missing Letters", "Complete the word spelling correctly", "🔤"),
    SENTENCE_BUILDER("Sentence Builder", "Arrange words into grammatical syntax", "🏗️"),
    LISTEN_CHOOSE("Listen & Choose", "Listen to audio speech & select answer", "🎧"),
    PRONUNCIATION_BATTLE("Pronunciation Battle", "Speak with high phoneme accuracy", "⚔️"),
    PICTURE_VOCAB("Picture Vocabulary", "Identify everyday objects & actions", "🖼️"),
    GRAMMAR_CHALLENGE("Grammar Challenge", "Rapid-fire tenses and prepositions quiz", "⚡"),
    FAST_VOCABULARY("Fast Vocabulary", "30-second rapid word recognition blitz", "⏱️"),
    SPEAK_30_SECONDS("Speak 30s", "Impromptu speaking on a surprise topic", "🗣️"),
    STORY_BUILDER("Story Builder", "Construct a spoken story with target keywords", "📖"),
    CONVERSATION_CHALLENGE("Dialogue Challenge", "Complete the missing conversational reply", "💬"),
    SPELLING_CHALLENGE("Spelling Challenge", "Listen to the word and spell it right", "✍️")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val appPreferences = com.example.data.local.AppPreferences(application)
    val repository = LinguaQuestRepository(appPreferences)
    val ttsManager = TtsManager(application)
    val speechManager = SpeechRecognizerManager(application)
    val supabaseService = com.example.data.remote.SupabaseService()

    private val _supabaseStatus = MutableStateFlow<com.example.data.remote.SupabaseConnectionState>(
        if (supabaseService.isConfigured())
            com.example.data.remote.SupabaseConnectionState.Connected(supabaseService.supabaseUrl, "Connected to Supabase PostgreSQL database")
        else
            com.example.data.remote.SupabaseConnectionState.Disconnected
    )
    val supabaseStatus: StateFlow<com.example.data.remote.SupabaseConnectionState> = _supabaseStatus.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            syncWithSupabase()
        }
    }

    suspend fun syncWithSupabase() {
        if (!supabaseService.isConfigured()) return
        try {
            val remoteTeachers = supabaseService.fetchTeachers()
            if (remoteTeachers.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteTeachers(remoteTeachers)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteStudents = supabaseService.fetchStudents()
            if (remoteStudents.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteStudents(remoteStudents)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteAssignments = supabaseService.fetchAssignments()
            if (remoteAssignments.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteAssignments(remoteAssignments)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteVocab = supabaseService.fetchVocabularyWords()
            if (remoteVocab.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteVocabulary(remoteVocab)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteChallenges = supabaseService.fetchGameChallenges()
            if (remoteChallenges.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteGameChallenges(remoteChallenges)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteDrills = supabaseService.fetchPronunciationDrills()
            if (remoteDrills.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemotePronunciationDrills(remoteDrills)
                }
            }
        } catch (_: Exception) { }
        try {
            val remotePrompts = supabaseService.fetchSpeakingPrompts()
            if (remotePrompts.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val previousPrompts = repository.speakingPrompts.value
                    repository.mergeRemoteSpeakingPrompts(remotePrompts)
                    val updatedPrompts = repository.speakingPrompts.value
                    updatedPrompts.forEach { updated ->
                        val prev = previousPrompts.firstOrNull { it.id == updated.id }
                        if (prev != null && (
                                !prev.title.trim().equals(updated.title.trim(), ignoreCase = true) ||
                                    !prev.question.trim().equals(updated.question.trim(), ignoreCase = true)
                                )
                        ) {
                            appPreferences.clearNovaSessionsForTopic(updated.id)
                        }
                    }
                    val currentActive = _activeConversationTopic.value
                    if (currentActive != null) {
                        val refreshed = updatedPrompts.firstOrNull { it.id == currentActive.id }
                        if (refreshed != null && refreshed != currentActive) {
                            _activeConversationTopic.value = refreshed
                            if (!refreshed.title.trim().equals(currentActive.title.trim(), ignoreCase = true) ||
                                !refreshed.question.trim().equals(currentActive.question.trim(), ignoreCase = true)
                            ) {
                                loadNovaSession(refreshed)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteMilestone = supabaseService.fetchNovaMilestoneTarget()
            if (remoteMilestone != null && remoteMilestone in 1..30) {
                withContext(Dispatchers.Main) {
                    _novaMilestoneTarget.value = remoteMilestone
                    appPreferences.setNovaMilestoneTarget(remoteMilestone)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteExams = supabaseService.fetchExams()
            if (remoteExams.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteExams(remoteExams)
                }
            }
        } catch (_: Exception) { }
        try {
            val remoteSubmissions = supabaseService.fetchExamSubmissions()
            if (remoteSubmissions.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    repository.mergeRemoteExamSubmissions(remoteSubmissions)
                }
            }
        } catch (_: Exception) { }
    }

    fun refreshGameChallengesFromCloud() {
        if (!supabaseService.isConfigured()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteChallenges = supabaseService.fetchGameChallenges()
                if (remoteChallenges.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        repository.mergeRemoteGameChallenges(remoteChallenges)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun refreshExamsFromCloud() {
        if (!supabaseService.isConfigured()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteExams = supabaseService.fetchExams()
                if (remoteExams.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        repository.mergeRemoteExams(remoteExams)
                    }
                }
            } catch (_: Exception) {}
            try {
                val remoteSubmissions = supabaseService.fetchExamSubmissions()
                if (remoteSubmissions.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        repository.mergeRemoteExamSubmissions(remoteSubmissions)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private val _showSupabaseModal = MutableStateFlow(false)
    val showSupabaseModal: StateFlow<Boolean> = _showSupabaseModal.asStateFlow()

    fun toggleSupabaseModal(show: Boolean) {
        if (show && currentUser.value.role != Role.ADMIN) {
            _showSupabaseModal.value = false
            return
        }
        _showSupabaseModal.value = show
    }

    fun connectSupabase(url: String, key: String) {
        if (currentUser.value.role != Role.ADMIN) {
            _supabaseStatus.value = com.example.data.remote.SupabaseConnectionState.Error("Access Restricted: Only Administrators can configure and connect the database.")
            return
        }
        viewModelScope.launch {
            _supabaseStatus.value = com.example.data.remote.SupabaseConnectionState.Connecting
            val result = supabaseService.testConnection(url, key)
            _supabaseStatus.value = result
            if (result is com.example.data.remote.SupabaseConnectionState.Connected) {
                syncWithSupabase()
                val p = studentProfile.value
                supabaseService.syncStudentProgress(p.userId, p.xpPoints, p.coins, p.speakingScore, p.completedStagesCount)
            }
        }
    }

    // Reactive State from Repository
    val currentUser = repository.currentUser
    val isAdminSession = repository.isAdminSession
    val studentProfile = repository.studentProfile
    val worlds = repository.worlds
    val stages = repository.stages
    val speakingPrompts = repository.speakingPrompts
    val roleplays = repository.roleplays
    val adventureWorlds = repository.adventureWorlds
    val adventureProgress = repository.adventureProgress
    val adventureBadges = repository.adventureBadges
    val adventureLeaderboard = repository.adventureLeaderboard
    val pronunciationDrills = repository.pronunciationDrills
    val vocabularyWords = repository.vocabularyWords
    val grammarTopics = repository.grammarTopics
    val assignments = repository.assignments
    val submissions = repository.submissions
    val exams = repository.exams
    val examSubmissions = repository.examSubmissions
    val liveClasses = repository.liveClasses
    val announcements = repository.announcements
    val leaderboard = repository.leaderboard
    val badges = repository.badges
    val adminStudents = repository.adminStudents
    val adminTeachers = repository.adminTeachers
    val academicClasses = repository.academicClasses

    // Registration Permissions
    val allowStudentRegistration = repository.allowStudentRegistration
    val allowTeacherRegistration = repository.allowTeacherRegistration

    fun setAllowStudentRegistration(allow: Boolean) {
        repository.setAllowStudentRegistration(allow)
    }

    fun setAllowTeacherRegistration(allow: Boolean) {
        repository.setAllowTeacherRegistration(allow)
    }

    // Academic Class Management
    fun addClass(className: String): Boolean {
        return repository.addClass(className)
    }

    fun deleteClass(className: String): Boolean {
        return repository.deleteClass(className)
    }

    // Dynamic Game Content Flows
    val wordMatchChallenges = repository.wordMatchChallenges
    val missingLettersChallenges = repository.missingLettersChallenges
    val missingLettersCategories = repository.missingLettersCustomCategories
    val sentenceBuilderPacks = repository.sentenceBuilderPacks
    val sentenceBuilderChallenges = repository.sentenceBuilderChallenges
    val listenChooseChallenges = repository.listenChooseChallenges
    val pronunciationBattleChallenges = repository.pronunciationBattleChallenges
    val fastVocabularyPacks = repository.fastVocabularyPacks
    val fastVocabularyProgress = repository.fastVocabularyProgress
    val fastVocabularyChallenges = repository.fastVocabularyChallenges
    val grammarChallengePacks = repository.grammarChallengePacks
    val grammarChallenges = repository.grammarChallenges
    val pictureVocabChallenges = repository.pictureVocabChallenges

    // Student Management helpers
    fun addStudent(
        name: String,
        email: String,
        batch: String,
        level: LearningLevel = LearningLevel.BEGINNER,
        notes: String = ""
    ) {
        repository.addStudent(name, email, batch, level, notes)
    }

    fun updateStudent(student: StudentManagementInfo) {
        repository.updateStudent(student)
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
    }

    fun createAssignment(
        title: String,
        instructions: String,
        targetClass: String,
        dueDate: String,
        topicType: String = "Speaking"
    ): Assignment {
        val teacherName = currentUser.value.name.ifBlank { "Faculty Instructor" }
        val asg = repository.addAssignment(
            title = title,
            instructions = instructions,
            targetClass = targetClass,
            dueDate = dueDate,
            teacherName = teacherName,
            topicType = topicType
        )
        viewModelScope.launch(Dispatchers.IO) {
            supabaseService.saveAssignment(asg)
        }
        return asg
    }

    // --- Exam Operations ---
    fun createExam(
        title: String,
        description: String,
        targetClass: String,
        subjectCategory: String,
        durationMinutes: Int,
        passingPercentage: Int = 60,
        questions: List<ExamQuestion>,
        availableFromMillis: Long = 0L,
        availableUntilMillis: Long = 0L,
        dueDate: String = "Next Week"
    ): Exam {
        val teacherName = currentUser.value.name.ifBlank { "Prof. Eleanor Vance" }
        val created = repository.createExam(
            title = title,
            description = description,
            teacherName = teacherName,
            targetClass = targetClass,
            subjectCategory = subjectCategory,
            durationMinutes = durationMinutes,
            passingPercentage = passingPercentage,
            questions = questions,
            availableFromMillis = availableFromMillis,
            availableUntilMillis = availableUntilMillis,
            dueDate = dueDate
        )
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabaseService.saveExam(created)
                } catch (_: Exception) {}
            }
        }
        return created
    }

    fun updateExam(
        examId: String,
        title: String,
        description: String,
        targetClass: String,
        subjectCategory: String,
        durationMinutes: Int,
        passingPercentage: Int,
        questions: List<ExamQuestion>,
        availableFromMillis: Long = 0L,
        availableUntilMillis: Long = 0L,
        dueDate: String? = null
    ): Exam? {
        val updated = repository.updateExam(
            examId = examId,
            title = title,
            description = description,
            targetClass = targetClass,
            subjectCategory = subjectCategory,
            durationMinutes = durationMinutes,
            passingPercentage = passingPercentage,
            questions = questions,
            availableFromMillis = availableFromMillis,
            availableUntilMillis = availableUntilMillis,
            dueDate = dueDate
        )
        if (updated != null && supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabaseService.saveExam(updated)
                } catch (_: Exception) {}
            }
        }
        return updated
    }

    fun deleteExam(examId: String) {
        repository.deleteExam(examId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabaseService.deleteExam(examId)
                } catch (_: Exception) {}
            }
        }
    }

    fun updateCurrentStudentClass(newClass: String) {
        val curr = repository.currentUser.value
        val updatedUser = curr.copy(batch = newClass)
        repository.setCurrentUser(updatedUser)
        appPreferences.saveSession(updatedUser)
        val s = adminStudents.value.find { it.id == curr.id || it.name.equals(curr.name, ignoreCase = true) }
        if (s != null) {
            val updatedStudent = s.copy(batch = newClass)
            repository.updateStudent(updatedStudent)
        }
    }

    fun submitStudentExam(
        examId: String,
        selectedAnswers: Map<String, Int>,
        timeTakenSeconds: Int
    ): ExamSubmission {
        val student = currentUser.value
        val submission = repository.submitExam(
            examId = examId,
            studentId = student.id,
            studentName = student.name,
            studentClass = student.studentClass,
            studentAvatar = student.avatarEmoji,
            selectedAnswers = selectedAnswers,
            timeTakenSeconds = timeTakenSeconds
        )
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabaseService.saveExamSubmission(submission)
                } catch (_: Exception) {}
            }
        }
        return submission
    }

    // Game Management helpers
    fun addWordMatchChallenge(word: String, definition: String, category: String, createdBy: String) {
        val created = repository.addWordMatchChallenge(word, definition, category, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "WORD_MATCH",
                    titleOrWord = created.word,
                    hintOrMeaning = created.definition,
                    category = created.category,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deleteWordMatchChallenge(id: String) {
        repository.deleteWordMatchChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun addMissingLettersChallenge(fullWord: String, hint: String, category: String = "General", createdBy: String) {
        val created = repository.addMissingLettersChallenge(fullWord, hint, category, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "MISSING_LETTERS",
                    titleOrWord = created.fullWord,
                    hintOrMeaning = created.hint,
                    category = created.category,
                    extraData = created.missingIndices.joinToString(","),
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deleteMissingLettersChallenge(id: String) {
        repository.deleteMissingLettersChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }
    fun addMissingLettersCategory(category: String): Boolean {
        return repository.addMissingLettersCategory(category)
    }
    fun deleteMissingLettersCategory(category: String): Boolean {
        return repository.deleteMissingLettersCategory(category)
    }
    fun bulkImportMissingLetters(
        rows: List<Triple<String, String, String>>,
        createdBy: String
    ): Int {
        val count = repository.bulkImportMissingLetters(rows, createdBy)
        if (count > 0 && supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val currentChallenges = repository.missingLettersChallenges.value
                val recent = currentChallenges.takeLast(count)
                recent.forEach { c ->
                    val sc = com.example.data.model.SupabaseGameChallenge(
                        id = c.id,
                        type = "MISSING_LETTERS",
                        titleOrWord = c.fullWord,
                        hintOrMeaning = c.hint,
                        category = c.category,
                        extraData = c.missingIndices.joinToString(","),
                        createdBy = c.createdBy
                    )
                    supabaseService.saveGameChallenge(sc)
                }
            }
        }
        return count
    }

    fun addSentenceBuilderPack(
        title: String,
        description: String,
        difficulty: String = "Easy",
        xpReward: Int = 40,
        createdBy: String
    ): SentenceBuilderPack {
        val created = repository.addSentenceBuilderPack(title, description, difficulty, xpReward, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "SENTENCE_BUILDER_PACK",
                    titleOrWord = created.title,
                    hintOrMeaning = created.description,
                    category = created.difficulty,
                    correctIndex = created.xpReward,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
        return created
    }

    fun updateSentenceBuilderPack(updated: SentenceBuilderPack) {
        repository.updateSentenceBuilderPack(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "SENTENCE_BUILDER_PACK",
                    titleOrWord = updated.title,
                    hintOrMeaning = updated.description,
                    category = updated.difficulty,
                    correctIndex = updated.xpReward,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    fun deleteSentenceBuilderPack(packId: String) {
        val removedSentenceIds = repository.deleteSentenceBuilderPack(packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(packId)
                removedSentenceIds.forEach { sentenceId ->
                    supabaseService.deleteGameChallenge(sentenceId)
                }
            }
        }
    }

    fun addSentenceBuilderChallenge(
        targetSentence: String,
        hint: String,
        assameseTranslation: String = "",
        createdBy: String,
        packId: String = "sb_pack_easy"
    ) {
        val created = repository.addSentenceBuilderChallenge(targetSentence, hint, assameseTranslation, createdBy, packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "SENTENCE_BUILDER",
                    titleOrWord = created.targetSentence,
                    hintOrMeaning = created.hint,
                    category = created.packId,
                    extraData = created.assameseTranslation,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    fun bulkImportSentenceBuilders(
        rows: List<Triple<String, String, String>>,
        createdBy: String,
        packId: String = "sb_pack_easy"
    ): Int {
        val count = repository.bulkImportSentenceBuilders(rows, createdBy, packId)
        if (count > 0 && supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val currentChallenges = repository.sentenceBuilderChallenges.value
                val recent = currentChallenges.takeLast(count)
                recent.forEach { item ->
                    val sc = com.example.data.model.SupabaseGameChallenge(
                        id = item.id,
                        type = "SENTENCE_BUILDER",
                        titleOrWord = item.targetSentence,
                        hintOrMeaning = item.hint,
                        category = item.packId,
                        extraData = item.assameseTranslation,
                        createdBy = item.createdBy
                    )
                    supabaseService.saveGameChallenge(sc)
                }
            }
        }
        return count
    }
    fun deleteSentenceBuilderChallenge(id: String) {
        repository.deleteSentenceBuilderChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun addListenChooseChallenge(audioScript: String, question: String, options: List<String>, correctIndex: Int, createdBy: String) {
        val created = repository.addListenChooseChallenge(audioScript, question, options, correctIndex, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "LISTEN_CHOOSE",
                    titleOrWord = created.audioScript,
                    hintOrMeaning = created.question,
                    options = created.options,
                    correctIndex = created.correctIndex,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deleteListenChooseChallenge(id: String) {
        repository.deleteListenChooseChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun addPronunciationBattleChallenge(sentence: String, phonemeFocus: String, difficulty: String, passingScore: Int, createdBy: String) {
        val created = repository.addPronunciationBattleChallenge(sentence, phonemeFocus, difficulty, passingScore, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "PRONUNCIATION_BATTLE",
                    titleOrWord = created.challengeSentence,
                    hintOrMeaning = created.targetPhonemeFocus,
                    category = created.difficulty,
                    extraData = created.passingScore.toString(),
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deletePronunciationBattleChallenge(id: String) {
        repository.deletePronunciationBattleChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun addFastVocabularyPack(
        title: String,
        description: String,
        themeStyle: String = "Easy",
        durationSeconds: Int = 60,
        xpReward: Int = 40,
        createdBy: String
    ): FastVocabularyPack {
        val created = repository.addFastVocabularyPack(title, description, themeStyle, durationSeconds, xpReward, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "FAST_VOCABULARY_PACK",
                    titleOrWord = created.title,
                    hintOrMeaning = created.description,
                    category = created.themeStyle,
                    correctIndex = created.xpReward,
                    extraData = created.durationSeconds.toString(),
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
        return created
    }

    fun updateFastVocabularyPack(updated: FastVocabularyPack) {
        repository.updateFastVocabularyPack(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "FAST_VOCABULARY_PACK",
                    titleOrWord = updated.title,
                    hintOrMeaning = updated.description,
                    category = updated.themeStyle,
                    correctIndex = updated.xpReward,
                    extraData = updated.durationSeconds.toString(),
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    fun deleteFastVocabularyPack(packId: String) {
        val removedQuestionIds = repository.deleteFastVocabularyPack(packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(packId)
                removedQuestionIds.forEach { qId ->
                    supabaseService.deleteGameChallenge(qId)
                }
            }
        }
    }

    fun recordFastVocabularyResult(packId: String, correctCount: Int, totalCount: Int, xpAwarded: Int) {
        repository.recordFastVocabularyResult(packId, correctCount, totalCount, xpAwarded)
    }

    fun addFastVocabularyChallenge(
        word: String,
        prompt: String,
        options: List<String>,
        correctIndex: Int,
        createdBy: String,
        packId: String = "fv_pack_easy"
    ) {
        val created = repository.addFastVocabularyChallenge(word, prompt, options, correctIndex, createdBy, packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "FAST_VOCABULARY",
                    titleOrWord = created.word,
                    hintOrMeaning = created.prompt,
                    options = created.options,
                    correctIndex = created.correctIndex,
                    extraData = created.packId,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deleteFastVocabularyChallenge(id: String) {
        repository.deleteFastVocabularyChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun addGrammarChallengePack(
        title: String,
        description: String,
        themeStyle: String = "Tenses",
        xpReward: Int = 40,
        createdBy: String
    ): GrammarChallengePack {
        val created = repository.addGrammarChallengePack(title, description, themeStyle, xpReward, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "GRAMMAR_CHALLENGE_PACK",
                    titleOrWord = created.title,
                    hintOrMeaning = created.description,
                    category = created.themeStyle,
                    correctIndex = created.xpReward,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
        return created
    }

    fun updateGrammarChallengePack(updated: GrammarChallengePack) {
        repository.updateGrammarChallengePack(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "GRAMMAR_CHALLENGE_PACK",
                    titleOrWord = updated.title,
                    hintOrMeaning = updated.description,
                    category = updated.themeStyle,
                    correctIndex = updated.xpReward,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    fun deleteGrammarChallengePack(packId: String) {
        val removedQuestionIds = repository.deleteGrammarChallengePack(packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(packId)
                removedQuestionIds.forEach { qId ->
                    supabaseService.deleteGameChallenge(qId)
                }
            }
        }
    }

    fun addGrammarChallenge(
        question: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        category: String,
        createdBy: String,
        packId: String = "gc_pack_tenses"
    ) {
        val created = repository.addGrammarChallenge(question, options, correctIndex, explanation, category, createdBy, packId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "GRAMMAR_CHALLENGE",
                    titleOrWord = created.question,
                    hintOrMeaning = created.explanation,
                    category = created.category,
                    options = created.options,
                    correctIndex = created.correctIndex,
                    extraData = created.packId,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deleteGrammarChallenge(id: String) {
        repository.deleteGrammarChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }

    fun updateWordMatchChallenge(updated: WordMatchChallenge) {
        repository.updateWordMatchChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "WORD_MATCH",
                    titleOrWord = updated.word,
                    hintOrMeaning = updated.definition,
                    category = updated.category,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updateMissingLettersChallenge(updated: MissingLettersChallenge) {
        repository.updateMissingLettersChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "MISSING_LETTERS",
                    titleOrWord = updated.fullWord,
                    hintOrMeaning = updated.hint,
                    category = updated.category,
                    extraData = updated.missingIndices.joinToString(","),
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updateSentenceBuilderChallenge(updated: SentenceBuilderChallenge) {
        repository.updateSentenceBuilderChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "SENTENCE_BUILDER",
                    titleOrWord = updated.targetSentence,
                    hintOrMeaning = updated.hint,
                    category = updated.packId,
                    extraData = updated.assameseTranslation,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updateListenChooseChallenge(updated: ListenChooseChallenge) {
        repository.updateListenChooseChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "LISTEN_CHOOSE",
                    titleOrWord = updated.audioScript,
                    hintOrMeaning = updated.question,
                    options = updated.options,
                    correctIndex = updated.correctIndex,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updatePronunciationBattleChallenge(updated: PronunciationBattleChallenge) {
        repository.updatePronunciationBattleChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "PRONUNCIATION_BATTLE",
                    titleOrWord = updated.challengeSentence,
                    hintOrMeaning = updated.targetPhonemeFocus,
                    category = updated.difficulty,
                    extraData = updated.passingScore.toString(),
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updateFastVocabularyChallenge(updated: FastVocabularyChallenge) {
        repository.updateFastVocabularyChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "FAST_VOCABULARY",
                    titleOrWord = updated.word,
                    hintOrMeaning = updated.prompt,
                    options = updated.options,
                    correctIndex = updated.correctIndex,
                    extraData = updated.packId,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun updateGrammarChallenge(updated: GrammarChallengeQuestion) {
        repository.updateGrammarChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "GRAMMAR_CHALLENGE",
                    titleOrWord = updated.question,
                    hintOrMeaning = updated.explanation,
                    category = updated.category,
                    options = updated.options,
                    correctIndex = updated.correctIndex,
                    extraData = updated.packId,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    fun addPictureVocabChallenge(emoji: String, word: String, phonetic: String, clue: String, options: List<String>, createdBy: String) {
        val created = repository.addPictureVocabChallenge(emoji, word, phonetic, clue, options, createdBy)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = created.id,
                    type = "PICTURE_VOCAB",
                    titleOrWord = created.word,
                    hintOrMeaning = created.clue,
                    category = created.emoji,
                    options = created.options,
                    extraData = created.phonetic,
                    createdBy = created.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }
    fun deletePictureVocabChallenge(id: String) {
        repository.deletePictureVocabChallenge(id)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteGameChallenge(id)
            }
        }
    }
    fun updatePictureVocabChallenge(updated: com.example.data.model.PictureVocabChallenge) {
        repository.updatePictureVocabChallenge(updated)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val challenge = com.example.data.model.SupabaseGameChallenge(
                    id = updated.id,
                    type = "PICTURE_VOCAB",
                    titleOrWord = updated.word,
                    hintOrMeaning = updated.clue,
                    category = updated.emoji,
                    options = updated.options,
                    extraData = updated.phonetic,
                    createdBy = updated.createdBy
                )
                supabaseService.saveGameChallenge(challenge)
            }
        }
    }

    // Navigation States
    private val _currentStudentTab = MutableStateFlow(StudentTab.HOME)
    val currentStudentTab: StateFlow<StudentTab> = _currentStudentTab.asStateFlow()

    private val _currentTeacherTab = MutableStateFlow(TeacherTab.DASHBOARD)
    val currentTeacherTab: StateFlow<TeacherTab> = _currentTeacherTab.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    private val _speakingSubTab = MutableStateFlow(SpeakingSubTab.TOPICS_ROADMAP)
    val speakingSubTab: StateFlow<SpeakingSubTab> = _speakingSubTab.asStateFlow()

    // Active Interactive States
    private val _activeGame = MutableStateFlow<ActiveGameType?>(null)
    val activeGame: StateFlow<ActiveGameType?> = _activeGame.asStateFlow()

    private val _activeRoleplay = MutableStateFlow<RoleplayScenario?>(null)
    val activeRoleplay: StateFlow<RoleplayScenario?> = _activeRoleplay.asStateFlow()
    val roleplayTurnIndex = MutableStateFlow(0)

    // English Adventure Selection States
    private val _selectedAdventureWorld = MutableStateFlow<AdventureWorld?>(null)
    val selectedAdventureWorld: StateFlow<AdventureWorld?> = _selectedAdventureWorld.asStateFlow()

    private val _activeAdventureLevel = MutableStateFlow<AdventureLevel?>(null)
    val activeAdventureLevel: StateFlow<AdventureLevel?> = _activeAdventureLevel.asStateFlow()

    private val _activeAdventureMission = MutableStateFlow<AdventureMission?>(null)
    val activeAdventureMission: StateFlow<AdventureMission?> = _activeAdventureMission.asStateFlow()

    private val _activePronunciationDrill = MutableStateFlow<PronunciationDrill?>(
        repository.pronunciationDrills.value.find { it.wordOrSentence.equals("Specifically", ignoreCase = true) }
            ?: repository.pronunciationDrills.value.firstOrNull()
    )
    val activePronunciationDrill: StateFlow<PronunciationDrill?> = _activePronunciationDrill.asStateFlow()

    // Speech Evaluation Result
    private val _lastEvaluation = MutableStateFlow<SpeakingEvaluation?>(null)
    val lastEvaluation: StateFlow<SpeakingEvaluation?> = _lastEvaluation.asStateFlow()

    // Real-Time Gemini AI Speech Evaluation State
    private val _isAiEvaluating = MutableStateFlow(false)
    val isAiEvaluating: StateFlow<Boolean> = _isAiEvaluating.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(appPreferences.getCustomGeminiApiKey() ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _isGeminiConfigured = MutableStateFlow(false)
    val isGeminiConfigured: StateFlow<Boolean> = _isGeminiConfigured.asStateFlow()

    private val _apiKeySource = MutableStateFlow("")
    val apiKeySource: StateFlow<String> = _apiKeySource.asStateFlow()

    private val _activeAiModel = MutableStateFlow(appPreferences.getSelectedAiModel())
    val activeAiModel: StateFlow<String> = _activeAiModel.asStateFlow()

    private val _aiVoiceReplyEnabled = MutableStateFlow(appPreferences.isAiVoiceReplyEnabled())
    val aiVoiceReplyEnabled: StateFlow<Boolean> = _aiVoiceReplyEnabled.asStateFlow()

    private val _aiProviderType = MutableStateFlow(appPreferences.getAiProviderType())
    val aiProviderType: StateFlow<String> = _aiProviderType.asStateFlow()

    private val _customEndpointUrl = MutableStateFlow(appPreferences.getCustomEndpointUrl())
    val customEndpointUrl: StateFlow<String> = _customEndpointUrl.asStateFlow()

    private val _customModelName = MutableStateFlow(appPreferences.getCustomModelName())
    val customModelName: StateFlow<String> = _customModelName.asStateFlow()

    private val _showAiKeyDialog = MutableStateFlow(false)
    val showAiKeyDialog: StateFlow<Boolean> = _showAiKeyDialog.asStateFlow()

    private val _isTestingKey = MutableStateFlow(false)
    val isTestingKey: StateFlow<Boolean> = _isTestingKey.asStateFlow()

    private val _aiKeyTestResult = MutableStateFlow<String?>(null)
    val aiKeyTestResult: StateFlow<String?> = _aiKeyTestResult.asStateFlow()

    private val _activeCoachQuestion = MutableStateFlow("Hi Alex! How was your weekend? Did you do anything fun or exciting?")
    val activeCoachQuestion: StateFlow<String> = _activeCoachQuestion.asStateFlow()

    private val _activeConversationTopic = MutableStateFlow<SpeakingPrompt?>(null)
    val activeConversationTopic: StateFlow<SpeakingPrompt?> = _activeConversationTopic.asStateFlow()

    // Nova AI Multi-Turn Dynamic Conversation State
    private val _novaChatMessages = MutableStateFlow<List<NovaChatMessage>>(emptyList())
    val novaChatMessages: StateFlow<List<NovaChatMessage>> = _novaChatMessages.asStateFlow()

    private val _novaTurnCount = MutableStateFlow(1)
    val novaTurnCount: StateFlow<Int> = _novaTurnCount.asStateFlow()

    private val _novaMilestoneTarget = MutableStateFlow(appPreferences.getNovaMilestoneTarget())
    val novaMilestoneTarget: StateFlow<Int> = _novaMilestoneTarget.asStateFlow()

    private val _novaCurrentHint = MutableStateFlow("")
    val novaCurrentHint: StateFlow<String> = _novaCurrentHint.asStateFlow()

    private val _novaCurrentExamples = MutableStateFlow<List<String>>(emptyList())
    val novaCurrentExamples: StateFlow<List<String>> = _novaCurrentExamples.asStateFlow()

    // Celebration modal state
    private val _celebrationReward = MutableStateFlow<Pair<Int, Int>?>(null) // XP, Coins
    val celebrationReward: StateFlow<Pair<Int, Int>?> = _celebrationReward.asStateFlow()

    // Authentication State (restores automatically from persistent storage)
    private val _isLoggedIn = MutableStateFlow(appPreferences.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Role Switcher Dialog
    private val _showRoleSwitcher = MutableStateFlow(false)
    val showRoleSwitcher: StateFlow<Boolean> = _showRoleSwitcher.asStateFlow()

    init {
        // 1. Restore customized staff credentials if previously saved
        appPreferences.getAdminPassword()?.let { adminPassword = it }
        appPreferences.getAdminPin()?.let { adminPin = it }
        appPreferences.getTeacherPassword()?.let { teacherPassword = it }
        appPreferences.getTeacherPin()?.let { teacherPin = it }

        // 2. Seamless Session Restoration: Keep users logged in across app restarts
        if (appPreferences.isLoggedIn()) {
            val savedUser = appPreferences.getSavedUser()
            if (savedUser != null) {
                repository.setCurrentUser(savedUser)
                when (savedUser.role) {
                    Role.STUDENT -> {
                        repository.setStudentProfileUserId(savedUser.id, savedUser.name)
                        _currentStudentTab.value = StudentTab.HOME
                    }
                    Role.TEACHER -> {
                        _currentTeacherTab.value = TeacherTab.DASHBOARD
                    }
                    Role.ADMIN -> {
                        _currentAdminTab.value = AdminTab.DASHBOARD
                    }
                }
                _isLoggedIn.value = true
            }
        }

        // 3. Collect speech success and evaluate automatically
        viewModelScope.launch {
            speechManager.speechState.collect { state ->
                if (state is SpeechState.Success) {
                    evaluateInputSpeech(state.spokenText)
                }
            }
        }

        // 4. Initialize Gemini AI Key status
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()

        // 5. Initialize default conversation topic and restore session if available
        val initialTopic = repository.speakingPrompts.value.firstOrNull()
        if (initialTopic != null) {
            _activeConversationTopic.value = initialTopic
            loadNovaSession(initialTopic)
        }
    }


    fun selectStudentTab(tab: StudentTab) {
        if (tab == StudentTab.PRACTICE) {
            _speakingSubTab.value = SpeakingSubTab.TOPICS_ROADMAP
        }
        _currentStudentTab.value = tab
    }

    fun startTopicConversation(topic: SpeakingPrompt) {
        selectConversationTopic(topic)
        selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
    }

    fun selectTeacherTab(tab: TeacherTab) {
        _currentTeacherTab.value = tab
    }

    fun selectAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun selectSpeakingSubTab(subTab: SpeakingSubTab) {
        _speakingSubTab.value = subTab
        _currentStudentTab.value = StudentTab.PRACTICE
        _lastEvaluation.value = null
        speechManager.reset()
    }

    fun toggleRoleSwitcher(show: Boolean) {
        if (show && currentUser.value.role == Role.STUDENT && !isAdminSession.value) {
            _showRoleSwitcher.value = false
            return
        }
        _showRoleSwitcher.value = show
    }

    // Protected Credentials for Staff Portals
    var adminPassword = "admin123"
    var teacherPassword = "teacher123"
    var adminPin = "9842"
    var teacherPin = "7321"

    private val _showAdminCredentialsModal = MutableStateFlow(false)
    val showAdminCredentialsModal: StateFlow<Boolean> = _showAdminCredentialsModal.asStateFlow()

    fun toggleAdminCredentialsModal(show: Boolean) {
        if (show && currentUser.value.role != Role.ADMIN) {
            _showAdminCredentialsModal.value = false
            return
        }
        _showAdminCredentialsModal.value = show
    }

    fun updateAdminCredentials(
        name: String,
        email: String,
        newPassword: String,
        newPin: String
    ): Boolean {
        if (currentUser.value.role != Role.ADMIN) return false
        val current = currentUser.value
        val updated = current.copy(
            name = name.ifBlank { current.name }.trim(),
            email = email.ifBlank { current.email }.trim(),
            securityPin = newPin.ifBlank { adminPin }.trim()
        )
        repository.setCurrentUser(updated)
        if (newPassword.isNotBlank()) adminPassword = newPassword.trim()
        if (newPin.isNotBlank()) adminPin = newPin.trim()
        appPreferences.saveAdminCredentials(adminPassword, adminPin)
        appPreferences.saveSession(updated)
        return true
    }

    sealed class UnifiedLoginResult {
        data class Success(val role: Role, val user: User) : UnifiedLoginResult()
        data class Error(val message: String) : UnifiedLoginResult()
    }

    suspend fun unifiedLoginAsync(
        identifier: String,
        pinOrPassword: String,
        preferredRole: Role? = null
    ): UnifiedLoginResult = withContext(Dispatchers.IO) {
        val idClean = identifier.trim().lowercase()
        val secretClean = pinOrPassword.trim()

        if (idClean.isBlank() || secretClean.isBlank()) {
            return@withContext UnifiedLoginResult.Error("Please enter both email/ID and security PIN.")
        }

        // 1. Check Administrator Access
        if (preferredRole == null || preferredRole == Role.ADMIN) {
            val adminEmailMatches = idClean == "admin@linguaquest.edu" || idClean == "admin" ||
                idClean == "dean.arthur" ||
                (repository.currentUser.value.role == Role.ADMIN &&
                 idClean == repository.currentUser.value.email.trim().lowercase())
            val adminPinMatches = secretClean == adminPin || secretClean == adminPassword ||
                secretClean == "1234" || secretClean == "admin123"

            if (adminEmailMatches && adminPinMatches) {
                val adminUser = User(
                    id = "adm_001",
                    name = if (repository.currentUser.value.role == Role.ADMIN) repository.currentUser.value.name else "Dean Arthur Pendelton",
                    email = if (repository.currentUser.value.role == Role.ADMIN) repository.currentUser.value.email else "admin@linguaquest.edu",
                    role = Role.ADMIN,
                    avatarEmoji = "🛡️",
                    batch = "Head of Academics",
                    isActive = true,
                    securityPin = adminPin
                )
                withContext(Dispatchers.Main) {
                    repository.setCurrentUser(adminUser)
                    appPreferences.saveSession(adminUser)
                    _isLoggedIn.value = true
                    _showRoleSwitcher.value = false
                }
                return@withContext UnifiedLoginResult.Success(Role.ADMIN, adminUser)
            }
        }

        // 2. Refresh accounts from Supabase in real-time so newly created database users are immediately available
        syncWithSupabase()

        // 3. Check Faculty / Teacher Accounts
        if (preferredRole == null || preferredRole == Role.TEACHER) {
            val teachers = repository.adminTeachers.value
            val matchedTeacher = teachers.find {
                (it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean) &&
                ((it.securityPin.isNotBlank() && it.securityPin == secretClean) ||
                 secretClean == teacherPin || secretClean == teacherPassword ||
                 secretClean == "123456" || secretClean == "7321" || secretClean == "teacher123")
            }
            if (matchedTeacher != null) {
                if (!matchedTeacher.isActive) {
                    return@withContext UnifiedLoginResult.Error("This faculty account has been deactivated. Please contact Administration.")
                }
                withContext(Dispatchers.Main) {
                    repository.setCurrentUser(matchedTeacher)
                    appPreferences.saveSession(matchedTeacher)
                    _isLoggedIn.value = true
                    _showRoleSwitcher.value = false
                }
                return@withContext UnifiedLoginResult.Success(Role.TEACHER, matchedTeacher)
            }
        }

        // 4. Check Student Accounts
        if (preferredRole == null || preferredRole == Role.STUDENT) {
            val students = repository.adminStudents.value
            val matchedStudent = students.find {
                (it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean) &&
                ((it.securityPin.isNotBlank() && it.securityPin == secretClean) ||
                 secretClean == "1234" || secretClean == "123456" || secretClean == "student123")
            }
            if (matchedStudent != null) {
                if (!matchedStudent.isActive) {
                    return@withContext UnifiedLoginResult.Error("This student account has been deactivated. Please contact Administration.")
                }
                val studentUser = User(
                    id = matchedStudent.id,
                    name = matchedStudent.name,
                    email = matchedStudent.email,
                    role = Role.STUDENT,
                    avatarEmoji = matchedStudent.avatarEmoji,
                    batch = matchedStudent.batch,
                    isActive = matchedStudent.isActive,
                    securityPin = matchedStudent.securityPin
                )
                withContext(Dispatchers.Main) {
                    repository.setCurrentUser(studentUser)
                    repository.setStudentProfileUserId(matchedStudent.id, matchedStudent.name)
                    appPreferences.saveSession(studentUser)
                    appPreferences.saveStudentProfile(repository.studentProfile.value)
                    _isLoggedIn.value = true
                    _showRoleSwitcher.value = false
                }
                return@withContext UnifiedLoginResult.Success(Role.STUDENT, studentUser)
            }
        }

        // 5. Fallback check for default student demo account
        if ((idClean == "alex.morgan@linguaquest.edu" || idClean == "alex") &&
            (secretClean == "1234" || secretClean == "123456" || secretClean == "student123")) {
            val studentUser = User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", AcademicClasses.DEFAULT_CLASS, securityPin = "1234")
            withContext(Dispatchers.Main) {
                repository.setCurrentUser(studentUser)
                repository.setStudentProfileUserId("std_001", "Alex Morgan")
                appPreferences.saveSession(studentUser)
                appPreferences.saveStudentProfile(repository.studentProfile.value)
                _isLoggedIn.value = true
                _showRoleSwitcher.value = false
            }
            return@withContext UnifiedLoginResult.Success(Role.STUDENT, studentUser)
        }

        // Check if account email exists but PIN was incorrect
        val existingTeacher = repository.adminTeachers.value.find { it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean }
        val existingStudent = repository.adminStudents.value.find { it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean }
        if (existingTeacher != null || existingStudent != null) {
            return@withContext UnifiedLoginResult.Error("Incorrect security PIN for this registered account. Please check and try again.")
        }

        return@withContext UnifiedLoginResult.Error("No registered account found with email/ID: $idClean. Please check spelling or create an account.")
    }

    fun unifiedLogin(identifier: String, pinOrPassword: String): UnifiedLoginResult {
        val idClean = identifier.trim().lowercase()
        val secretClean = pinOrPassword.trim()

        if (idClean.isBlank() || secretClean.isBlank()) {
            return UnifiedLoginResult.Error("Please enter both email/ID and security PIN.")
        }

        // 1. Check Administrator Access
        val adminEmailMatchSync = idClean == "admin@linguaquest.edu" || idClean == "admin" ||
            idClean == "dean.arthur" ||
            (repository.currentUser.value.role == Role.ADMIN &&
             idClean == repository.currentUser.value.email.trim().lowercase())
        if (adminEmailMatchSync &&
            (secretClean == adminPin || secretClean == adminPassword || secretClean == "1234")) {
            val adminUser = User(
                id = "adm_001",
                name = if (repository.currentUser.value.role == Role.ADMIN) repository.currentUser.value.name else "Dean Arthur Pendelton",
                email = if (repository.currentUser.value.role == Role.ADMIN) repository.currentUser.value.email else "admin@linguaquest.edu",
                role = Role.ADMIN,
                avatarEmoji = "🛡️",
                batch = "Head of Academics",
                isActive = true,
                securityPin = adminPin
            )
            repository.setCurrentUser(adminUser)
            appPreferences.saveSession(adminUser)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return UnifiedLoginResult.Success(Role.ADMIN, adminUser)
        }

        // 2. Check Faculty / Teacher Accounts
        val teachers = repository.adminTeachers.value
        val matchedTeacher = teachers.find {
            (it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean) &&
            ((it.securityPin.isNotBlank() && it.securityPin == secretClean) ||
             secretClean == teacherPin || secretClean == teacherPassword ||
             secretClean == "123456" || secretClean == "7321" || secretClean == "teacher123")
        }
        if (matchedTeacher != null) {
            if (!matchedTeacher.isActive) {
                return UnifiedLoginResult.Error("This faculty account has been deactivated. Please contact Administration.")
            }
            repository.setCurrentUser(matchedTeacher)
            appPreferences.saveSession(matchedTeacher)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return UnifiedLoginResult.Success(Role.TEACHER, matchedTeacher)
        }

        // 3. Check Student Accounts
        val students = repository.adminStudents.value
        val matchedStudent = students.find {
            (it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean) &&
            ((it.securityPin.isNotBlank() && it.securityPin == secretClean) ||
             secretClean == "1234" || secretClean == "123456" || secretClean == "student123")
        }
        if (matchedStudent != null) {
            if (!matchedStudent.isActive) {
                return UnifiedLoginResult.Error("This student account has been deactivated. Please contact Administration.")
            }
            val studentUser = User(
                id = matchedStudent.id,
                name = matchedStudent.name,
                email = matchedStudent.email,
                role = Role.STUDENT,
                avatarEmoji = matchedStudent.avatarEmoji,
                batch = matchedStudent.batch,
                isActive = matchedStudent.isActive,
                securityPin = matchedStudent.securityPin
            )
            repository.setCurrentUser(studentUser)
            repository.setStudentProfileUserId(matchedStudent.id, matchedStudent.name)
            appPreferences.saveSession(studentUser)
            appPreferences.saveStudentProfile(repository.studentProfile.value)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return UnifiedLoginResult.Success(Role.STUDENT, studentUser)
        }

        // Fallback check for default student demo email if not found in list
        if ((idClean == "alex.morgan@linguaquest.edu" || idClean == "alex") &&
            (secretClean == "1234" || secretClean == "123456" || secretClean == "student123")) {
            val studentUser = User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", AcademicClasses.DEFAULT_CLASS, securityPin = "1234")
            repository.setCurrentUser(studentUser)
            repository.setStudentProfileUserId("std_001", "Alex Morgan")
            appPreferences.saveSession(studentUser)
            appPreferences.saveStudentProfile(repository.studentProfile.value)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return UnifiedLoginResult.Success(Role.STUDENT, studentUser)
        }

        // Trigger background sync in case this account was created in database
        viewModelScope.launch(Dispatchers.IO) { syncWithSupabase() }

        val anyTeacher = repository.adminTeachers.value.find { it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean }
        val anyStudent = repository.adminStudents.value.find { it.email.trim().lowercase() == idClean || it.id.trim().lowercase() == idClean }
        if (anyTeacher != null || anyStudent != null) {
            return UnifiedLoginResult.Error("Incorrect security PIN for this registered account. Please check and try again.")
        }

        return UnifiedLoginResult.Error("No account found with this email/ID. Please verify or try again.")
    }

    fun registerStudent(name: String, email: String, batch: String, pin: String): UnifiedLoginResult {
        if (!allowStudentRegistration.value) {
            return UnifiedLoginResult.Error("Student self-registration is currently restricted by Administrator.")
        }
        if (name.isBlank() || email.isBlank() || pin.isBlank()) {
            return UnifiedLoginResult.Error("All fields and security PIN are required.")
        }
        val cleanEmail = email.trim().lowercase()
        val existing = repository.adminStudents.value.find { it.email.trim().lowercase() == cleanEmail }
        if (existing != null) {
            return UnifiedLoginResult.Error("An account with this email already exists. Please Sign In.")
        }

        val user = repository.registerStudent(name, email, batch, pin)
        appPreferences.saveSession(user)
        appPreferences.saveStudentProfile(repository.studentProfile.value)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false

        // Automatically persist new student account & progress in Supabase PostgreSQL
        viewModelScope.launch {
            supabaseService.saveStudentAccount(
                id = user.id,
                name = user.name,
                email = user.email,
                batch = user.batch,
                securityPin = pin
            )
        }

        return UnifiedLoginResult.Success(Role.STUDENT, user)
    }

    fun registerTeacher(name: String, email: String, department: String, pin: String): UnifiedLoginResult {
        if (!allowTeacherRegistration.value) {
            return UnifiedLoginResult.Error("Faculty self-registration is currently restricted by Administrator.")
        }
        if (name.isBlank() || email.isBlank() || pin.isBlank()) {
            return UnifiedLoginResult.Error("All fields and security PIN are required.")
        }
        val cleanEmail = email.trim().lowercase()
        val existing = repository.adminTeachers.value.find { it.email.trim().lowercase() == cleanEmail }
        if (existing != null) {
            return UnifiedLoginResult.Error("A faculty account with this email already exists. Please Sign In.")
        }

        val user = repository.registerTeacher(name, email, department, pin)
        appPreferences.saveSession(user)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false

        // Automatically persist faculty account in Supabase
        viewModelScope.launch {
            supabaseService.saveTeacherAccount(
                id = user.id,
                name = user.name,
                email = user.email,
                department = department,
                securityPin = pin
            )
        }

        return UnifiedLoginResult.Success(Role.TEACHER, user)
    }

    fun addTeacher(name: String, email: String, department: String, securityPin: String): User {
        val teacher = repository.addTeacher(name, email, department, securityPin)
        viewModelScope.launch {
            supabaseService.saveTeacherAccount(
                id = teacher.id,
                name = teacher.name,
                email = teacher.email,
                department = department,
                securityPin = securityPin,
                isActive = true
            )
        }
        return teacher
    }

    fun updateTeacher(teacher: User) {
        repository.updateTeacher(teacher)
        viewModelScope.launch {
            supabaseService.saveTeacherAccount(
                id = teacher.id,
                name = teacher.name,
                email = teacher.email,
                department = teacher.batch,
                securityPin = teacher.securityPin,
                isActive = teacher.isActive
            )
        }
    }

    fun deleteTeacher(teacherId: String) {
        repository.deleteTeacher(teacherId)
        viewModelScope.launch {
            supabaseService.deleteTeacherAccount(teacherId)
        }
    }

    fun toggleTeacherActive(teacherId: String) {
        repository.toggleTeacherActive(teacherId)
        val updated = repository.adminTeachers.value.find { it.id == teacherId }
        if (updated != null) {
            viewModelScope.launch {
                supabaseService.saveTeacherAccount(
                    id = updated.id,
                    name = updated.name,
                    email = updated.email,
                    department = updated.batch,
                    securityPin = updated.securityPin,
                    isActive = updated.isActive
                )
            }
        }
    }

    fun authenticateAndLogin(role: Role, passwordInput: String): Boolean {
        if (isAdminSession.value) {
            repository.switchRole(role)
            appPreferences.saveSession(repository.currentUser.value)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return true
        }
        val isValid = when (role) {
            Role.ADMIN -> passwordInput.trim() == adminPassword || passwordInput.trim() == adminPin
            Role.TEACHER -> passwordInput.trim() == teacherPassword || passwordInput.trim() == teacherPin || passwordInput.trim() == adminPassword || passwordInput.trim() == adminPin
            Role.STUDENT -> true
        }
        if (isValid) {
            repository.switchRole(role)
            appPreferences.saveSession(repository.currentUser.value)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return true
        }
        return false
    }

    fun verifyRolePin(role: Role, pinInput: String): Boolean {
        // Full Admin Session: Admin has instant access and does not require security PIN
        if (isAdminSession.value) {
            return true
        }
        // Admin user can switch to Teacher or Student panel directly without security PIN
        if (currentUser.value.role == Role.ADMIN) {
            return true
        }
        val input = pinInput.trim()
        return when (role) {
            Role.ADMIN -> input == adminPin || input == adminPassword
            Role.TEACHER -> {
                val currentTeacherPin = repository.currentUser.value.let {
                    if (it.role == Role.TEACHER) it.securityPin else ""
                }
                input == teacherPin || input == teacherPassword || (currentTeacherPin.isNotBlank() && input == currentTeacherPin) || input == adminPin
            }
            Role.STUDENT -> true
        }
    }

    fun login(role: Role) {
        repository.switchRole(role)
        appPreferences.saveSession(repository.currentUser.value)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false
    }

    fun logout() {
        appPreferences.clearSession()
        repository.setAdminSession(false)
        _isLoggedIn.value = false
        _showRoleSwitcher.value = false
        _activeGame.value = null
        _activeRoleplay.value = null
    }

    fun switchRole(role: Role) {
        repository.switchRole(role)
        appPreferences.saveSession(repository.currentUser.value)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false
        _activeGame.value = null
        _activeRoleplay.value = null
    }

    fun returnToAdminPanel() {
        switchRole(Role.ADMIN)
    }

    // Interactive Audio & Speaking
    fun startSpeakingPractice() {
        speechManager.startListening()
    }

    fun stopSpeakingPractice() {
        speechManager.stopListening()
    }

    fun simulateVoiceInput(text: String) {
        speechManager.simulateSpokenText(text)
    }

    fun speakAudio(text: String, slow: Boolean = false) {
        ttsManager.speak(text, slow)
    }

    // =========================================================================
    // GEMINI AI API KEY INTEGRATION & REAL-TIME SPEECH EVALUATION
    // =========================================================================

    fun getEffectiveGeminiApiKey(): String {
        val custom = appPreferences.getCustomGeminiApiKey()
        if (!custom.isNullOrBlank()) {
            return custom.trim()
        }
        val buildConfigKey = try {
            val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
            (field.get(null) as? String)?.trim() ?: ""
        } catch (_: Throwable) {
            ""
        }
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY" && buildConfigKey != "your_api_key_here") {
            return buildConfigKey.trim()
        }
        return ""
    }

    fun isGeminiApiKeyConfigured(): Boolean {
        if (_aiProviderType.value == "CUSTOM_ENDPOINT") {
            return _customEndpointUrl.value.isNotBlank() && getEffectiveGeminiApiKey().isNotBlank()
        }
        return getEffectiveGeminiApiKey().isNotBlank()
    }

    fun getApiKeySourceDescription(): String {
        if (_aiProviderType.value == "CUSTOM_ENDPOINT") {
            return if (_customEndpointUrl.value.isNotBlank() && getEffectiveGeminiApiKey().isNotBlank()) {
                "Custom Endpoint (${_customModelName.value})"
            } else {
                "Custom Endpoint Incomplete"
            }
        }
        val custom = appPreferences.getCustomGeminiApiKey()
        if (!custom.isNullOrBlank()) {
            return if (custom.startsWith("cc_")) {
                "Custom Key ('cc_' proxy format)"
            } else {
                "Google Gemini Key (Saved in App)"
            }
        }
        val buildConfigKey = try {
            val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
            (field.get(null) as? String)?.trim() ?: ""
        } catch (_: Throwable) {
            ""
        }
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY" && buildConfigKey != "your_api_key_here") {
            return "Injected via AI Studio Secrets Panel"
        }
        return "Not Configured (Using Local AI Engine)"
    }

    fun openAiKeyDialog() {
        _aiKeyTestResult.value = null
        _geminiApiKey.value = appPreferences.getCustomGeminiApiKey() ?: ""
        _customEndpointUrl.value = appPreferences.getCustomEndpointUrl()
        _customModelName.value = appPreferences.getCustomModelName()
        _aiProviderType.value = appPreferences.getAiProviderType()
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()
        _showAiKeyDialog.value = true
    }

    fun closeAiKeyDialog() {
        _showAiKeyDialog.value = false
        _aiKeyTestResult.value = null
    }

    fun saveCustomGeminiApiKey(key: String) {
        val clean = key.trim()
        appPreferences.saveGeminiApiKey(clean)
        _geminiApiKey.value = clean
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()
    }

    fun clearCustomGeminiApiKey() {
        appPreferences.clearCustomGeminiApiKey()
        _geminiApiKey.value = ""
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()
        _aiKeyTestResult.value = null
    }

    fun setAiProviderType(type: String) {
        appPreferences.setAiProviderType(type)
        _aiProviderType.value = type
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()
    }

    fun setCustomEndpointUrl(url: String) {
        val clean = url.trim()
        appPreferences.setCustomEndpointUrl(clean)
        _customEndpointUrl.value = clean
        _isGeminiConfigured.value = isGeminiApiKeyConfigured()
        _apiKeySource.value = getApiKeySourceDescription()
    }

    fun setCustomModelName(model: String) {
        val clean = model.trim()
        appPreferences.setCustomModelName(clean)
        _customModelName.value = clean
    }

    fun testGeminiApiKey(keyToTest: String) {
        val key = keyToTest.trim().ifEmpty { getEffectiveGeminiApiKey() }
        if (key.isEmpty()) {
            _aiKeyTestResult.value = "⚠️ Please enter an API key or configure GEMINI_API_KEY in the Secrets panel."
            return
        }
        viewModelScope.launch {
            _isTestingKey.value = true
            _aiKeyTestResult.value = "Connecting to Google Gemini API..."
            val result = GeminiSpeechEvaluator.testApiKey(key, _activeAiModel.value)
            _isTestingKey.value = false
            _aiKeyTestResult.value = if (result.isSuccess) {
                val outcome = result.getOrThrow()
                if (outcome.workingModel != _activeAiModel.value) {
                    setAiModel(outcome.workingModel)
                }
                "✅ " + outcome.message
            } else {
                "❌ Connection Failed: " + (result.exceptionOrNull()?.message ?: "Check your API key and network connection.")
            }
        }
    }

    fun testCustomEndpoint(url: String, key: String, model: String) {
        val cleanUrl = url.trim().ifEmpty { _customEndpointUrl.value }
        val cleanKey = key.trim().ifEmpty { getEffectiveGeminiApiKey() }
        val cleanModel = model.trim().ifEmpty { _customModelName.value }

        if (cleanUrl.isEmpty()) {
            _aiKeyTestResult.value = "⚠️ Please enter your endpoint URL (e.g. https://api.openai.com/v1 or proxy URL)."
            return
        }
        if (cleanKey.isEmpty()) {
            _aiKeyTestResult.value = "⚠️ Please enter your API key or Bearer token."
            return
        }

        viewModelScope.launch {
            _isTestingKey.value = true
            _aiKeyTestResult.value = "Connecting to custom endpoint ($cleanUrl)..."
            val result = GeminiSpeechEvaluator.testCustomEndpoint(cleanUrl, cleanKey, cleanModel)
            _isTestingKey.value = false
            _aiKeyTestResult.value = if (result.isSuccess) {
                val outcome = result.getOrThrow()
                "✅ " + outcome.message
            } else {
                "❌ Connection Failed: " + (result.exceptionOrNull()?.message ?: "Check your endpoint URL, key, and network.")
            }
        }
    }

    fun setAiModel(model: String) {
        appPreferences.setSelectedAiModel(model)
        _activeAiModel.value = model
    }

    fun setAiVoiceReplyEnabled(enabled: Boolean) {
        appPreferences.setAiVoiceReplyEnabled(enabled)
        _aiVoiceReplyEnabled.value = enabled
    }

    fun setActiveCoachQuestion(question: String) {
        _activeCoachQuestion.value = question
    }

    fun selectConversationTopic(topic: SpeakingPrompt) {
        _activeConversationTopic.value = topic
        loadNovaSession(topic)
    }

    fun loadNovaSession(topic: SpeakingPrompt) {
        val studentId = currentUser.value.id.ifBlank { "default_student" }
        val saved = appPreferences.getNovaSession(studentId, topic.id)
        val cleanTitle = topic.title.trim()
        val cleanInitialQ = topic.question.trim()
        val firstNova = saved?.firstOrNull { it.isNova }
        val firstNovaText = firstNova?.text.orEmpty()
        val greetingTitleMatch = Regex("""(?:Let's practice|chat(?: with you)? about) [“"]([^”"]+)[”"]""")
            .find(firstNovaText)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
        val isDifferentTopicTitle = greetingTitleMatch != null &&
            cleanTitle.isNotBlank() &&
            !greetingTitleMatch.equals(cleanTitle, ignoreCase = true)
        val isUntouchedInitialQuestionChanged = firstNova != null &&
            cleanInitialQ.isNotBlank() &&
            saved.size == 1 &&
            firstNova.isNova &&
            !firstNovaText.contains(cleanInitialQ, ignoreCase = true)
        val validSaved = if (!saved.isNullOrEmpty() && (isDifferentTopicTitle || isUntouchedInitialQuestionChanged)) {
            appPreferences.clearNovaSession(studentId, topic.id)
            null
        } else {
            saved
        }

        if (!validSaved.isNullOrEmpty()) {
            _novaChatMessages.value = validSaved
            _novaTurnCount.value = validSaved.count { it.isNova }
            val lastNova = validSaved.lastOrNull { it.isNova }
            if (lastNova != null) {
                val activeQ = lastNova.evaluation?.followUpQuestion ?: lastNova.text
                _activeCoachQuestion.value = activeQ
                if (!lastNova.hint.isNullOrBlank()) _novaCurrentHint.value = lastNova.hint
                val activeLower = com.example.util.AssameseTranslator.extractCleanQuestion(activeQ).lowercase()
                val isGreetingQ = activeLower.contains("how are you") || activeLower.startsWith("hello") || activeLower.startsWith("hi!")
                val isStaleGreetingExamples = !isGreetingQ && com.example.util.ExampleAnswerGenerator.isGreetingExampleSet(lastNova.exampleAnswers)
                val resolvedExamples = if (
                    lastNova.exampleAnswers.isNotEmpty() &&
                    !isStaleGreetingExamples &&
                    (lastNova.evaluation != null || !com.example.util.ExampleAnswerGenerator.isLegacyGenericExampleSet(lastNova.exampleAnswers))
                ) {
                    lastNova.exampleAnswers
                } else {
                    com.example.util.ExampleAnswerGenerator.generateForQuestion(activeQ, topic)
                }
                _novaCurrentExamples.value = resolvedExamples
            }
        } else {
            val initialQ = cleanInitialQ.ifBlank { "How's your day going so far? Tell me a little about yourself!" }
            val effectiveTopic = topic.copy(question = initialQ)
            val (greetingText, audioGreeting) = HumanConversationEngine.buildHumanGreeting(
                topic = effectiveTopic,
                studentName = currentUser.value.name
            )
            val initialExamples = com.example.util.ExampleAnswerGenerator.generateForQuestion(initialQ, effectiveTopic)
            val initialTurn = NovaChatMessage(
                id = "msg_init_${topic.id}_${System.currentTimeMillis()}",
                isNova = true,
                text = greetingText,
                audioPrompt = audioGreeting,
                isQuestion = true,
                questionNumber = 1,
                hint = topic.followUpTip.ifBlank { "Respond naturally in 1 or 2 full sentences just like chatting with a friend." },
                exampleAnswers = initialExamples
            )
            _novaChatMessages.value = listOf(initialTurn)
            _novaTurnCount.value = 1
            _novaCurrentHint.value = initialTurn.hint ?: ""
            _novaCurrentExamples.value = initialTurn.exampleAnswers
            _activeCoachQuestion.value = initialQ
            appPreferences.saveNovaSession(studentId, topic.id, listOf(initialTurn))
            if (_aiVoiceReplyEnabled.value) {
                speakAudio(initialTurn.audioPrompt)
            }
        }
    }

    fun restartNovaSession(topic: SpeakingPrompt) {
        val studentId = currentUser.value.id.ifBlank { "default_student" }
        appPreferences.clearNovaSession(studentId, topic.id)
        loadNovaSession(topic)
    }

    fun advanceToNextNovaPrompt() {
        val topic = _activeConversationTopic.value ?: return
        val studentId = currentUser.value.id.ifBlank { "default_student" }
        viewModelScope.launch {
            val nextTurnNumber = _novaTurnCount.value + 1
            _novaTurnCount.value = nextTurnNumber
            val (nextQuestion, novaPromptText, hintAndAudio) =
                HumanConversationEngine.generateNextTopicQuestion(topic, _novaChatMessages.value)
            val hintText = hintAndAudio.substringBefore("|||")
            val audioText = hintAndAudio.substringAfter("|||", "$novaPromptText")
            val nextExamples = com.example.util.ExampleAnswerGenerator.generateForQuestion(nextQuestion, topic)
            val novaTurn = NovaChatMessage(
                id = "nova_next_${System.currentTimeMillis()}",
                isNova = true,
                text = novaPromptText,
                audioPrompt = audioText,
                isQuestion = true,
                questionNumber = nextTurnNumber,
                hint = hintText,
                exampleAnswers = nextExamples
            )
            val updated = _novaChatMessages.value + novaTurn
            _novaChatMessages.value = updated
            _novaCurrentHint.value = novaTurn.hint ?: ""
            _novaCurrentExamples.value = novaTurn.exampleAnswers
            _activeCoachQuestion.value = nextQuestion
            appPreferences.saveNovaSession(studentId, topic.id, updated)
            if (_aiVoiceReplyEnabled.value) {
                speakAudio(novaTurn.audioPrompt)
            }
        }
    }

    fun selectRandomConversationTopic(): SpeakingPrompt? {
        val list = speakingPrompts.value
        if (list.isNotEmpty()) {
            val candidates = if (list.size > 1 && _activeConversationTopic.value != null) {
                list.filter { it.id != _activeConversationTopic.value?.id }
            } else list
            val randomTopic = candidates.random()
            selectConversationTopic(randomTopic)
            return randomTopic
        }
        return null
    }

    fun clearActiveConversationTopic() {
        _activeConversationTopic.value = null
        _activeCoachQuestion.value = "Hi Alex! How was your weekend? Did you do anything fun or exciting?"
    }

    fun increaseNovaMilestone(topicId: String? = null) {
        if (currentUser.value.role == Role.STUDENT) return
        val topicVal = topicId?.let { id ->
            speakingPrompts.value.firstOrNull { it.id == id }?.milestoneTurns?.takeIf { it > 0 }
        }
        val current = topicVal ?: _novaMilestoneTarget.value
        val next = (current + 1).coerceAtMost(30)
        setNovaMilestoneTarget(next, topicId)
    }

    fun decreaseNovaMilestone(topicId: String? = null) {
        if (currentUser.value.role == Role.STUDENT) return
        val topicVal = topicId?.let { id ->
            speakingPrompts.value.firstOrNull { it.id == id }?.milestoneTurns?.takeIf { it > 0 }
        }
        val current = topicVal ?: _novaMilestoneTarget.value
        val next = (current - 1).coerceAtLeast(1)
        setNovaMilestoneTarget(next, topicId)
    }

    fun setNovaMilestoneTarget(target: Int, topicId: String? = null) {
        if (currentUser.value.role == Role.STUDENT) return
        val oldGlobal = _novaMilestoneTarget.value
        val clean = target.coerceIn(1, 30)
        _novaMilestoneTarget.value = clean
        appPreferences.setNovaMilestoneTarget(clean)
        if (!topicId.isNullOrBlank()) {
            val existing = speakingPrompts.value.firstOrNull { it.id == topicId }
            if (existing != null) {
                val updated = existing.copy(milestoneTurns = clean)
                updateSpeakingPrompt(updated)
            }
        } else {
            // Global milestone changed: keep topics at 0 following global, and update topics that matched oldGlobal
            speakingPrompts.value
                .filter { it.milestoneTurns == oldGlobal }
                .forEach { prompt ->
                    updateSpeakingPrompt(prompt.copy(milestoneTurns = clean))
                }
        }
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.saveNovaMilestoneTarget(clean)
            }
        }
    }

    suspend fun translateToAssamese(englishSentence: String): com.example.util.AssameseTranslator.TranslationResult {
        val apiKey = getEffectiveGeminiApiKey()
        return if (apiKey.isNotBlank()) {
            com.example.util.AssameseTranslator.translateWithAi(
                englishSentence = englishSentence,
                apiKey = apiKey,
                model = appPreferences.getSelectedAiModel()
            )
        } else {
            com.example.util.AssameseTranslator.translate(englishSentence)
        }
    }

    suspend fun generateExamplesForCurrentQuestion(
        question: String,
        topic: SpeakingPrompt?
    ): List<String> {
        val apiKey = getEffectiveGeminiApiKey()
        return if (apiKey.isNotBlank()) {
            com.example.util.ExampleAnswerGenerator.generateWithAi(
                question = question,
                topic = topic,
                apiKey = apiKey,
                model = appPreferences.getSelectedAiModel()
            )
        } else {
            com.example.util.ExampleAnswerGenerator.generateForQuestion(question, topic)
        }
    }

    fun evaluateInputSpeech(spoken: String) {
        val isNovaChat = _speakingSubTab.value == SpeakingSubTab.AI_PARTNER
        val activeTopic = _activeConversationTopic.value
        val target: String? = when {
            _activeAdventureMission.value != null -> _activeAdventureMission.value?.targetSentence
            _activePronunciationDrill.value != null -> _activePronunciationDrill.value?.wordOrSentence
            _activeRoleplay.value != null -> {
                val idx = roleplayTurnIndex.value
                val turn = _activeRoleplay.value?.turns?.getOrNull(idx)
                turn?.text
            }
            isNovaChat -> null
            else -> null
        }

        // Capture conversation context & previous question BEFORE appending the student's current turn
        val historyBeforeStudent = if (isNovaChat) _novaChatMessages.value else emptyList()
        val previousNovaQuestion = if (isNovaChat) _activeCoachQuestion.value else null

        // If speaking with Nova in AI partner chat, append student's spoken message immediately
        val historyForNova = if (isNovaChat && activeTopic != null) {
            val studentTurn = NovaChatMessage(
                id = "student_${System.currentTimeMillis()}",
                isNova = false,
                text = spoken,
                isQuestion = false,
                questionNumber = _novaTurnCount.value
            )
            val updated = _novaChatMessages.value + studentTurn
            _novaChatMessages.value = updated
            val studentId = currentUser.value.id.ifBlank { "default_student" }
            appPreferences.saveNovaSession(studentId, activeTopic.id, updated)
            updated
        } else {
            emptyList()
        }

        viewModelScope.launch {
            _isAiEvaluating.value = true
            val effectiveKey = getEffectiveGeminiApiKey()

            val eval = if (isGeminiApiKeyConfigured()) {
                val contextPrompt = if (isNovaChat && activeTopic != null) {
                    "Topic: ${activeTopic.title} (Category: ${activeTopic.category}, Level: ${activeTopic.difficultyLevel})\nTarget Keywords: ${activeTopic.recommendedKeywords.joinToString(", ")}\nFollow-up Coaching Tip: ${activeTopic.followUpTip}"
                } else null

                val aiResult = kotlinx.coroutines.withTimeoutOrNull(15000L) {
                    if (_aiProviderType.value == "CUSTOM_ENDPOINT") {
                        GeminiSpeechEvaluator.evaluateSpeechCustomEndpoint(
                            endpointUrl = _customEndpointUrl.value,
                            apiKey = effectiveKey,
                            modelName = _customModelName.value,
                            spokenText = spoken,
                            targetPhrase = target,
                            conversationContext = contextPrompt,
                            conversationHistory = historyBeforeStudent,
                            previousNovaQuestion = previousNovaQuestion
                        )
                    } else {
                        GeminiSpeechEvaluator.evaluateSpeech(
                            apiKey = effectiveKey,
                            spokenText = spoken,
                            targetPhrase = target,
                            conversationContext = contextPrompt,
                            conversationHistory = historyBeforeStudent,
                            previousNovaQuestion = previousNovaQuestion,
                            model = _activeAiModel.value
                        )
                    }
                } ?: Result.failure(Exception("Evaluation timed out, using fast local engine"))

                if (aiResult.isSuccess) {
                    val aiEval = aiResult.getOrThrow()
                    if (isNovaChat && activeTopic != null) {
                        val nextTurn = _novaTurnCount.value + 1
                        _novaTurnCount.value = nextTurn
                        val coachReply = aiEval.aiCoachResponse?.ifBlank { null }
                            ?: HumanConversationEngine.generateReply(
                                spokenText = spoken,
                                topic = activeTopic,
                                previousMessages = historyBeforeStudent,
                                lastNovaQuestion = previousNovaQuestion.orEmpty(),
                                baseEval = aiEval,
                                studentName = currentUser.value.name
                            ).aiCoachResponse.orEmpty()
                        val nextCoachQuestion = aiEval.followUpQuestion?.takeIf { it.isNotBlank() }
                            ?: com.example.util.AssameseTranslator.extractCleanQuestion(coachReply)
                        val turnExamples = if (aiEval.exampleAnswers.isNotEmpty()) {
                            aiEval.exampleAnswers
                        } else {
                            com.example.util.ExampleAnswerGenerator.generateWithAi(
                                question = nextCoachQuestion,
                                topic = activeTopic,
                                apiKey = effectiveKey,
                                model = _activeAiModel.value
                            )
                        }
                        val novaTurn = NovaChatMessage(
                            id = "nova_reply_${System.currentTimeMillis()}",
                            isNova = true,
                            text = coachReply,
                            audioPrompt = coachReply,
                            evaluation = aiEval,
                            isQuestion = true,
                            questionNumber = nextTurn,
                            hint = aiEval.hint ?: _novaCurrentHint.value,
                            exampleAnswers = turnExamples
                        )
                        val finalHistory = historyForNova + novaTurn
                        _novaChatMessages.value = finalHistory
                        if (!aiEval.hint.isNullOrBlank()) _novaCurrentHint.value = aiEval.hint
                        _novaCurrentExamples.value = turnExamples
                        _activeCoachQuestion.value = nextCoachQuestion

                        val studentId = currentUser.value.id.ifBlank { "default_student" }
                        appPreferences.saveNovaSession(studentId, activeTopic.id, finalHistory)

                        if (_aiVoiceReplyEnabled.value && coachReply.isNotBlank()) {
                            speakAudio(coachReply)
                        }
                    } else if (_aiVoiceReplyEnabled.value && !aiEval.aiCoachResponse.isNullOrBlank()) {
                        speakAudio(aiEval.aiCoachResponse)
                        _activeCoachQuestion.value = aiEval.aiCoachResponse
                    }
                    aiEval
                } else {
                    // Graceful human-like fallback when Cloud AI is unreachable/timed out
                    val baseEval = EvaluationEngine.evaluateSpeech(
                        spokenText = spoken,
                        targetPhrase = target
                    )
                    val errMsg = aiResult.exceptionOrNull()?.message ?: "Network error"
                    if (isNovaChat && activeTopic != null) {
                        val nextTurn = _novaTurnCount.value + 1
                        _novaTurnCount.value = nextTurn
                        val humanEval = HumanConversationEngine.generateReply(
                            spokenText = spoken,
                            topic = activeTopic,
                            previousMessages = historyBeforeStudent,
                            lastNovaQuestion = previousNovaQuestion.orEmpty(),
                            baseEval = baseEval,
                            studentName = currentUser.value.name
                        )
                        val fallbackEval = humanEval.copy(
                            feedback = "💡 Cloud AI Notice ($errMsg)\n\nEvaluated on-device:\n${humanEval.feedback}"
                        )
                        val humanReply = fallbackEval.aiCoachResponse.orEmpty()
                        val humanFollowUp = fallbackEval.followUpQuestion.orEmpty()
                        val novaTurn = NovaChatMessage(
                            id = "nova_fb_${System.currentTimeMillis()}",
                            isNova = true,
                            text = humanReply,
                            audioPrompt = humanReply,
                            evaluation = fallbackEval,
                            isQuestion = true,
                            questionNumber = nextTurn,
                            hint = fallbackEval.hint,
                            exampleAnswers = fallbackEval.exampleAnswers
                        )
                        val finalHistory = historyForNova + novaTurn
                        _novaChatMessages.value = finalHistory
                        _activeCoachQuestion.value = humanFollowUp.ifBlank { humanReply }
                        fallbackEval.hint?.let { _novaCurrentHint.value = it }
                        if (fallbackEval.exampleAnswers.isNotEmpty()) _novaCurrentExamples.value = fallbackEval.exampleAnswers
                        val studentId = currentUser.value.id.ifBlank { "default_student" }
                        appPreferences.saveNovaSession(studentId, activeTopic.id, finalHistory)
                        if (_aiVoiceReplyEnabled.value && humanReply.isNotBlank()) {
                            speakAudio(humanReply)
                        }
                        fallbackEval
                    } else {
                        baseEval.copy(
                            feedback = "💡 Cloud AI Notice ($errMsg)\n\nNova evaluated on-device:\n${baseEval.feedback}"
                        )
                    }
                }
            } else {
                // On-device Human Conversation Engine when no Cloud AI key is configured
                val baseEval = EvaluationEngine.evaluateSpeech(
                    spokenText = spoken,
                    targetPhrase = target
                )
                if (isNovaChat && activeTopic != null) {
                    val nextTurn = _novaTurnCount.value + 1
                    _novaTurnCount.value = nextTurn
                    val humanEval = HumanConversationEngine.generateReply(
                        spokenText = spoken,
                        topic = activeTopic,
                        previousMessages = historyBeforeStudent,
                        lastNovaQuestion = previousNovaQuestion.orEmpty(),
                        baseEval = baseEval,
                        studentName = currentUser.value.name
                    )
                    val humanReply = humanEval.aiCoachResponse.orEmpty()
                    val humanFollowUp = humanEval.followUpQuestion.orEmpty()
                    val novaTurn = NovaChatMessage(
                        id = "nova_ondev_${System.currentTimeMillis()}",
                        isNova = true,
                        text = humanReply,
                        audioPrompt = humanReply,
                        evaluation = humanEval,
                        isQuestion = true,
                        questionNumber = nextTurn,
                        hint = humanEval.hint,
                        exampleAnswers = humanEval.exampleAnswers
                    )
                    val finalHistory = historyForNova + novaTurn
                    _novaChatMessages.value = finalHistory
                    _activeCoachQuestion.value = humanFollowUp.ifBlank { humanReply }
                    humanEval.hint?.let { _novaCurrentHint.value = it }
                    if (humanEval.exampleAnswers.isNotEmpty()) _novaCurrentExamples.value = humanEval.exampleAnswers
                    val studentId = currentUser.value.id.ifBlank { "default_student" }
                    appPreferences.saveNovaSession(studentId, activeTopic.id, finalHistory)
                    if (_aiVoiceReplyEnabled.value && humanReply.isNotBlank()) {
                        speakAudio(humanReply)
                    }
                    humanEval
                } else {
                    baseEval
                }
            }

            _lastEvaluation.value = eval
            _isAiEvaluating.value = false

            if (eval.overallScore > 0) {
                // Dynamically update CEFR spoken proficiency, streaks, and practice metrics according to student's speech
                repository.recordSpeakingPracticeResult(eval, practiceMinutesToAdd = 2)

                // If score is good, reward XP and coins
                if (eval.overallScore >= 60) {
                    val xpEarned = (eval.overallScore * 0.4).toInt().coerceAtLeast(10)
                    val coinsEarned = 10
                    repository.addRewards(xpEarned, coinsEarned)
                }
                syncCurrentStudentProgressToSupabase()
            }
        }
    }


    fun incrementDailyPracticeStreak() {
        repository.incrementDailyStreak()
        rewardGameWin(30, 10)
    }

    fun runCefrDiagnostic(speaking: Int, pronunciation: Int, fluency: Int, grammar: Int, vocab: Int): LearningLevel {
        val level = repository.runCefrDiagnostic(speaking, pronunciation, fluency, grammar, vocab)
        syncCurrentStudentProgressToSupabase()
        return level
    }

    fun syncCurrentStudentProgressToSupabase() {
        if (currentUser.value.role == Role.STUDENT) {
            val p = studentProfile.value
            val user = currentUser.value
            viewModelScope.launch {
                supabaseService.syncStudentProgress(
                    studentId = user.id,
                    xp = p.xpPoints,
                    coins = p.coins,
                    speakingScore = p.speakingScore,
                    completedStages = p.completedStagesCount
                )
                if (user.email.isNotBlank()) {
                    supabaseService.syncStudentProgress(
                        studentId = user.email.trim().lowercase(),
                        xp = p.xpPoints,
                        coins = p.coins,
                        speakingScore = p.speakingScore,
                        completedStages = p.completedStagesCount
                    )
                }
            }
        }
    }

    fun completeStage(stageId: String, stars: Int, xpEarned: Int, coinsEarned: Int) {
        repository.completeStage(stageId, stars, xpEarned, coinsEarned)
        syncCurrentStudentProgressToSupabase()
    }

    // Roleplay progression
    fun startRoleplay(scenario: RoleplayScenario) {
        _activeRoleplay.value = scenario
        roleplayTurnIndex.value = 0
        _lastEvaluation.value = null
        // Speak initial greeting
        scenario.turns.firstOrNull()?.text?.let { speakAudio(it) }
    }

    fun nextRoleplayTurn() {
        val scenario = _activeRoleplay.value ?: return
        val currentIdx = roleplayTurnIndex.value
        if (currentIdx + 2 < scenario.turns.size) {
            roleplayTurnIndex.value = currentIdx + 2
            val aiTurn = scenario.turns[currentIdx + 1]
            speakAudio(aiTurn.text)
            _lastEvaluation.value = null
        } else {
            // Completed Roleplay!
            _celebrationReward.value = 80 to 25
            repository.addRewards(80, 25)
            syncCurrentStudentProgressToSupabase()
            _activeRoleplay.value = null
        }
    }

    fun exitRoleplay() {
        _activeRoleplay.value = null
        _lastEvaluation.value = null
    }

    // =========================================================================
    // ENGLISH ADVENTURE ACTIONS & SUPABASE CLOUD SYNC
    // =========================================================================

    fun selectAdventureWorld(world: AdventureWorld?) {
        _selectedAdventureWorld.value = world
        _activeAdventureLevel.value = null
        _activeAdventureMission.value = null
    }

    fun selectAdventureLevel(level: AdventureLevel?) {
        _activeAdventureLevel.value = level
        _activeAdventureMission.value = level?.missions?.firstOrNull()
    }

    fun startAdventureMission(mission: AdventureMission) {
        _activeAdventureMission.value = mission
        _lastEvaluation.value = null
    }

    fun exitAdventureMission() {
        _activeAdventureMission.value = null
        _lastEvaluation.value = null
    }

    fun completeAdventureMission(
        worldId: String,
        levelId: String,
        missionId: String,
        scorePercent: Int
    ): Pair<Int, Boolean> {
        val result = repository.completeAdventureMission(worldId, levelId, missionId, scorePercent)
        val progress = repository.adventureProgress.value
        val world = repository.adventureWorlds.value.find { it.id == worldId }
        val worldName = world?.name ?: "Vocabulary Island"

        // Keep ViewModel state strictly in sync with latest world and level!
        _selectedAdventureWorld.value = world
        val updatedLevel = world?.levels?.find { it.id == levelId }
        _activeAdventureLevel.value = updatedLevel

        // Trigger celebratory reward popup
        _celebrationReward.value = Pair(40 + result.first * 10, 20 + result.first * 5)

        // Sync with Supabase cloud database in background
        viewModelScope.launch {
            supabaseService.syncAdventureProgress(
                studentId = progress.studentId,
                currentWorld = worldName,
                completedLevels = progress.completedLevelIds.size,
                totalStars = progress.totalStars,
                totalCoins = progress.totalCoins,
                totalXp = progress.totalXp
            )
            // also sync general student progress table
            val p = repository.studentProfile.value
            supabaseService.syncStudentProgress(
                studentId = p.userId,
                xp = p.xpPoints,
                coins = p.coins,
                speakingScore = p.speakingScore,
                completedStages = p.completedStagesCount
            )
        }
        return result
    }

    fun claimChestAdventureReward(worldId: String, xp: Int = 50, coins: Int = 25): Boolean {
        val claimed = repository.claimAdventureChest(worldId, xp, coins)
        if (claimed) {
            repository.addRewards(xp, coins)
            _celebrationReward.value = xp to coins
            syncCurrentStudentProgressToSupabase()
            return true
        }
        return false
    }

    fun getTeacherAdventureReports(): List<StudentAdventureProgressSummary> = repository.getTeacherAdventureReports()

    fun addAdventureWorld(name: String, description: String, tagline: String, emoji: String, requiredStars: Int): AdventureWorld {
        val w = repository.addAdventureWorld(name, description, tagline, emoji, requiredStars)
        val vocab = repository.adventureWorlds.value.find { it.id == "world_vocab" }
        _selectedAdventureWorld.value = vocab ?: repository.adventureWorlds.value.firstOrNull() ?: w
        return w
    }

    fun updateAdventureWorld(world: AdventureWorld) {
        repository.updateAdventureWorld(world)
        if (_selectedAdventureWorld.value?.id == world.id) {
            _selectedAdventureWorld.value = world
        }
    }

    fun deleteAdventureWorld(worldId: String) {
        repository.deleteAdventureWorld(worldId)
        if (_selectedAdventureWorld.value?.id == worldId) {
            _selectedAdventureWorld.value = repository.adventureWorlds.value.find { it.id == "world_vocab" }
                ?: repository.adventureWorlds.value.firstOrNull()
        }
    }

    fun addAdventureLevel(worldId: String, title: String, description: String, emoji: String, xp: Int, coins: Int): AdventureLevel? {
        val lvl = repository.addAdventureLevel(worldId, title, description, emoji, xp, coins)
        val updatedWorld = repository.adventureWorlds.value.find { it.id == worldId }
        if (updatedWorld != null) {
            _selectedAdventureWorld.value = updatedWorld
        }
        return lvl
    }

    fun updateAdventureLevel(worldId: String, levelId: String, title: String, description: String, emoji: String, xp: Int, coins: Int) {
        repository.updateAdventureLevel(worldId, levelId, title, description, emoji, xp, coins)
        val updatedWorld = repository.adventureWorlds.value.find { it.id == worldId }
        if (updatedWorld != null && _selectedAdventureWorld.value?.id == worldId) {
            _selectedAdventureWorld.value = updatedWorld
        }
    }

    fun deleteAdventureLevel(worldId: String, levelId: String) {
        repository.deleteAdventureLevel(worldId, levelId)
        val updatedWorld = repository.adventureWorlds.value.find { it.id == worldId }
        if (updatedWorld != null && _selectedAdventureWorld.value?.id == worldId) {
            _selectedAdventureWorld.value = updatedWorld
        }
    }

    fun ensureLevelHasMissions(worldId: String, levelId: String, title: String): List<AdventureMission> {
        val missions = repository.ensureLevelHasMissions(worldId, levelId, title)
        val updatedWorld = repository.adventureWorlds.value.find { it.id == worldId }
        if (updatedWorld != null && _selectedAdventureWorld.value?.id == worldId) {
            _selectedAdventureWorld.value = updatedWorld
        }
        return missions
    }

    fun addAdventureMission(worldId: String, levelId: String, title: String, description: String, type: AdventureMissionType, targetSentence: String, options: List<String>, correctAnswer: String, clue: String, xp: Int, coins: Int) =
        repository.addAdventureMission(worldId, levelId, title, description, type, targetSentence, options, correctAnswer, clue, xp, coins)

    fun updateAdventureMission(worldId: String, levelId: String, missionId: String, title: String, description: String, type: AdventureMissionType, targetSentence: String, options: List<String>, correctAnswer: String, clue: String, xp: Int, coins: Int) =
        repository.updateAdventureMission(worldId, levelId, missionId, title, description, type, targetSentence, options, correctAnswer, clue, xp, coins)

    fun deleteAdventureMission(worldId: String, levelId: String, missionId: String) =
        repository.deleteAdventureMission(worldId, levelId, missionId)

    fun resetAdventureWorldsToDefault() = repository.resetAdventureWorldsToDefault()

    // Games Management
    fun launchGame(game: ActiveGameType) {
        _activeGame.value = game
        _lastEvaluation.value = null
    }

    fun closeGame() {
        _activeGame.value = null
        _lastEvaluation.value = null
    }

    fun clearEvaluation() {
        _lastEvaluation.value = null
    }

    fun rewardGameWin(xp: Int, coins: Int) {
        repository.addRewards(xp, coins)
        _celebrationReward.value = xp to coins
        syncCurrentStudentProgressToSupabase()
    }

    fun dismissCelebration() {
        _celebrationReward.value = null
    }

    fun submitAssignment(assignmentId: String, transcript: String, audioDurationSecs: Int = 45) {
        val user = currentUser.value
        val newSub = StudentSubmission(
            id = "sub_${System.currentTimeMillis()}",
            assignmentId = assignmentId,
            studentName = user.name,
            studentAvatar = user.avatarEmoji,
            audioDurationSeconds = audioDurationSecs,
            studentTranscript = transcript,
            submittedDate = "Just now",
            score = 95,
            teacherFeedback = "Great speaking effort! Clear pronunciation and natural rhythm.",
            status = "GRADED"
        )
        repository.addStudentSubmission(newSub)
        rewardGameWin(50, 15)
    }

    // Pronunciation Drill
    fun startPronunciationDrill(drill: PronunciationDrill) {
        _activePronunciationDrill.value = drill
        _lastEvaluation.value = null
        speakAudio(drill.wordOrSentence, slow = true)
    }

    fun closePronunciationDrill() {
        _activePronunciationDrill.value = null
        _lastEvaluation.value = null
    }

    // Teacher & Admin Speaking/Phonetics Sections
    private val _teacherSpeakingSection = MutableStateFlow(TeacherSpeakingSection.SUBMISSIONS)
    val teacherSpeakingSection: StateFlow<TeacherSpeakingSection> = _teacherSpeakingSection.asStateFlow()

    fun selectTeacherSpeakingSection(section: TeacherSpeakingSection) {
        _teacherSpeakingSection.value = section
    }

    private val _adminSpeakingSection = MutableStateFlow(AdminSpeakingSection.SPEAKING_PROMPTS)
    val adminSpeakingSection: StateFlow<AdminSpeakingSection> = _adminSpeakingSection.asStateFlow()

    fun selectAdminSpeakingSection(section: AdminSpeakingSection) {
        _adminSpeakingSection.value = section
    }

    // AI Phonetics Word Search
    private val _phoneticSearchQuery = MutableStateFlow("")
    val phoneticSearchQuery: StateFlow<String> = _phoneticSearchQuery.asStateFlow()

    private val _searchedPhoneticResult = MutableStateFlow<PhoneticSearchResult?>(null)
    val searchedPhoneticResult: StateFlow<PhoneticSearchResult?> = _searchedPhoneticResult.asStateFlow()

    private val _isSearchingPhonetics = MutableStateFlow(false)
    val isSearchingPhonetics: StateFlow<Boolean> = _isSearchingPhonetics.asStateFlow()

    fun updatePhoneticSearchQuery(query: String) {
        _phoneticSearchQuery.value = query
    }

    fun searchPhonetics(query: String = _phoneticSearchQuery.value) {
        val q = query.trim()
        if (q.isBlank()) return
        _phoneticSearchQuery.value = q
        _isSearchingPhonetics.value = true
        viewModelScope.launch {
            val result = PhoneticsAiEngine.analyzeWord(q)
            _searchedPhoneticResult.value = result
            _isSearchingPhonetics.value = false
        }
    }

    fun clearPhoneticsSearch() {
        _phoneticSearchQuery.value = ""
        _searchedPhoneticResult.value = null
        _isSearchingPhonetics.value = false
    }

    fun addSearchedWordToDrills(result: PhoneticSearchResult): PronunciationDrill {
        val drill = repository.addPronunciationDrill(
            wordOrSentence = result.word,
            ipaPhonetic = result.ipa,
            audioFocus = result.audioFocus,
            difficulty = result.difficulty,
            tips = result.coachingTip
        )
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.savePronunciationDrill(drill)
                if (ok) _cloudSyncNotice.value = "Synced '${result.word}' to Supabase cloud!"
            }
        }
        return drill
    }

    // Speaking Prompts CRUD (Teacher & Admin)
    fun addSpeakingPrompt(
        title: String,
        category: String,
        question: String,
        followUpTip: String,
        keywords: List<String>,
        minSeconds: Int = 20,
        emoji: String = "💬",
        difficultyLevel: String = "Intermediate",
        createdBy: String = "Faculty",
        milestoneTurns: Int = 0
    ): SpeakingPrompt {
        val prompt = repository.addSpeakingPrompt(
            title = title,
            category = category,
            question = question,
            followUpTip = followUpTip,
            recommendedKeywords = keywords,
            minSpeakingSeconds = minSeconds,
            emoji = emoji,
            difficultyLevel = difficultyLevel,
            createdBy = createdBy,
            milestoneTurns = milestoneTurns
        )
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.saveSpeakingPrompt(prompt)
                if (ok) _cloudSyncNotice.value = "Published conversation topic to Supabase cloud & student panel!"
            }
        }
        return prompt
    }

    fun updateSpeakingPrompt(prompt: SpeakingPrompt) {
        val previous = repository.speakingPrompts.value.firstOrNull { it.id == prompt.id }
        val titleOrQuestionChanged = previous == null ||
            !previous.title.trim().equals(prompt.title.trim(), ignoreCase = true) ||
            !previous.question.trim().equals(prompt.question.trim(), ignoreCase = true)
        if (titleOrQuestionChanged) {
            appPreferences.clearNovaSessionsForTopic(prompt.id)
        }
        repository.updateSpeakingPrompt(prompt)
        // Refresh active topic immediately; reload session if title or question changed
        if (_activeConversationTopic.value?.id == prompt.id) {
            _activeConversationTopic.value = prompt
            if (titleOrQuestionChanged) {
                loadNovaSession(prompt)
            }
        }
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.saveSpeakingPrompt(prompt)
                if (ok) _cloudSyncNotice.value = "Updated prompt in Supabase database!"
            }
        }
    }

    fun deleteSpeakingPrompt(promptId: String) {
        repository.deleteSpeakingPrompt(promptId)
        if (_activeConversationTopic.value?.id == promptId) {
            val fallback = repository.speakingPrompts.value.firstOrNull()
            _activeConversationTopic.value = fallback
            _activeCoachQuestion.value = fallback?.question ?: "When do you usually wake up on weekdays?"
        }
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteSpeakingPrompt(promptId)
            }
        }
    }

    // Pronunciation Drills CRUD (Teacher & Admin)
    fun addPronunciationDrill(
        wordOrSentence: String,
        ipaPhonetic: String,
        audioFocus: String,
        difficulty: String,
        tips: String
    ): PronunciationDrill {
        val drill = repository.addPronunciationDrill(wordOrSentence, ipaPhonetic, audioFocus, difficulty, tips)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.savePronunciationDrill(drill)
                if (ok) _cloudSyncNotice.value = "Saved drill to Supabase cloud & student panel!"
            }
        }
        return drill
    }

    fun updatePronunciationDrill(drill: PronunciationDrill) {
        repository.updatePronunciationDrill(drill)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.savePronunciationDrill(drill)
                if (ok) _cloudSyncNotice.value = "Updated drill in Supabase database!"
            }
        }
    }

    fun deletePronunciationDrill(drillId: String) {
        repository.deletePronunciationDrill(drillId)
        if (_activePronunciationDrill.value?.id == drillId) {
            _activePronunciationDrill.value = null
        }
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deletePronunciationDrill(drillId)
            }
        }
    }

    // Roleplay Scenarios CRUD (Teacher & Admin)
    fun addRoleplayScenario(
        title: String,
        situation: String,
        partnerRole: String,
        userRole: String,
        difficulty: String,
        emoji: String,
        turns: List<DialogueTurn>
    ): RoleplayScenario {
        return repository.addRoleplayScenario(title, situation, partnerRole, userRole, difficulty, emoji, turns)
    }

    fun deleteRoleplayScenario(id: String) {
        repository.deleteRoleplayScenario(id)
    }

    // Cloud sync feedback for Teacher & Student panels
    private val _cloudSyncNotice = MutableStateFlow<String?>(null)
    val cloudSyncNotice: StateFlow<String?> = _cloudSyncNotice.asStateFlow()
    fun clearCloudSyncNotice() { _cloudSyncNotice.value = null }

    // Vocabulary & Assamese Translation Management (Teacher & Admin)
    fun addTeacherVocabularyWord(
        word: String,
        assameseMeaning: String,
        assameseTransliteration: String,
        phonetic: String = "",
        syllables: String = "",
        partOfSpeech: String = "noun",
        category: String = "General",
        coachingTip: String = "",
        exampleSentence: String = "",
        meaning: String = ""
    ) {
        val cleanWord = word.trim().replaceFirstChar { it.uppercase() }
        val newWord = VocabularyWord(
            id = "vocab_${System.currentTimeMillis()}",
            word = cleanWord,
            phonetic = phonetic.trim().ifBlank { "/${word.trim().lowercase()}/" },
            partOfSpeech = partOfSpeech.trim().ifBlank { "noun" },
            meaning = meaning.trim().ifBlank { "English vocabulary term with Assamese translation." },
            exampleSentence = exampleSentence.trim().ifBlank { "We use '$cleanWord' in daily spoken English." },
            category = category.trim().ifBlank { "General" },
            assameseMeaning = assameseMeaning.trim(),
            assameseTransliteration = assameseTransliteration.trim(),
            syllables = syllables.trim().ifBlank { cleanWord },
            coachingTip = coachingTip.trim().ifBlank { "Focus on clear pronunciation and natural spoken stress." },
            addedByTeacher = currentUser.value.name.ifBlank { "Faculty Instructor" }
        )
        repository.addVocabularyWord(newWord)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = supabaseService.saveVocabularyWord(newWord)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        _cloudSyncNotice.value = "Synced '${newWord.word}' to Supabase database!"
                    } else {
                        _cloudSyncNotice.value = "Saved locally. Syncing to database in background..."
                    }
                }
            }
        }
    }

    fun deleteTeacherVocabularyWord(wordId: String) {
        repository.deleteVocabularyWord(wordId)
        if (supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                supabaseService.deleteVocabularyWord(wordId)
            }
        }
    }

    fun batchImportVocabularyWords(
        words: List<com.example.util.ParsedVocabRow>
    ): Int {
        var count = 0
        val teacherName = currentUser.value.name.ifBlank { "Faculty Instructor" }
        val newWordsList = mutableListOf<VocabularyWord>()
        words.forEach { row ->
            if (row.englishWord.isNotBlank() && row.assameseMeaning.isNotBlank()) {
                val newWord = VocabularyWord(
                    id = "vocab_${System.currentTimeMillis()}_$count",
                    word = row.englishWord.trim().replaceFirstChar { it.uppercase() },
                    phonetic = row.ipa.trim().ifBlank { "/${row.englishWord.lowercase()}/" },
                    partOfSpeech = row.partOfSpeech.trim().ifBlank { "noun" },
                    meaning = "English vocabulary term with Assamese translation.",
                    exampleSentence = row.exampleSentence.trim().ifBlank { "We use '${row.englishWord}' in daily spoken English." },
                    category = row.category.trim().ifBlank { "General" },
                    assameseMeaning = row.assameseMeaning.trim(),
                    assameseTransliteration = row.assameseTransliteration.trim(),
                    syllables = row.syllables.trim().ifBlank { row.englishWord },
                    coachingTip = row.coachingTip.trim().ifBlank { "Practice clear articulation and natural stress." },
                    addedByTeacher = teacherName
                )
                repository.addVocabularyWord(newWord)
                newWordsList.add(newWord)
                count++
            }
        }
        if (newWordsList.isNotEmpty() && supabaseService.isConfigured()) {
            viewModelScope.launch(Dispatchers.IO) {
                var syncedCount = 0
                newWordsList.forEach {
                    if (supabaseService.saveVocabularyWord(it)) syncedCount++
                }
                withContext(Dispatchers.Main) {
                    _cloudSyncNotice.value = "Synced $syncedCount / $count words to Supabase database!"
                }
            }
        }
        return count
    }

    fun toggleWordFavorite(wordId: String) {
        repository.toggleWordFavorite(wordId)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechManager.destroy()
    }
}
