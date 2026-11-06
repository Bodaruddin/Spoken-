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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StudentTab(val label: String, val iconEmoji: String) {
    HOME("Home", "🏠"),
    LEARN("Journey", "🗺️"),
    PRACTICE("Speaking", "🎙️"),
    GAMES("Games", "🎮"),
    LEADERBOARD("Leaderboard", "🏆"),
    PROFILE("Profile", "👤")
}

enum class TeacherTab(val label: String, val iconEmoji: String) {
    DASHBOARD("Overview", "📊"),
    STUDENTS("Students", "👥"),
    GAMES("Games Hub", "🎮"),
    SPEAKING_REVIEW("Speaking & Review", "🎙️"),
    ASSIGNMENTS("Tasks", "📝")
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
    MANAGE_PHONETICS("Phonetics & Drills", "🎯")
}

enum class AdminSpeakingSection(val title: String, val emoji: String) {
    SPEAKING_PROMPTS("AI Speaking Prompts", "🎙️"),
    PHONETICS_DRILLS("Phonetics & Drills", "🎯"),
    ROLEPLAY_SCENARIOS("Roleplay Scenarios", "🎭")
}

enum class SpeakingSubTab {
    AI_PARTNER,
    DAILY_CHALLENGE,
    ROLEPLAY,
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
            com.example.data.remote.SupabaseConnectionState.Connected(supabaseService.supabaseUrl, "Connected via configured secret")
        else
            com.example.data.remote.SupabaseConnectionState.Disconnected
    )
    val supabaseStatus: StateFlow<com.example.data.remote.SupabaseConnectionState> = _supabaseStatus.asStateFlow()

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
                val p = studentProfile.value
                supabaseService.syncStudentProgress(p.userId, p.xpPoints, p.coins, p.speakingScore, p.completedStagesCount)
            }
        }
    }

    // Reactive State from Repository
    val currentUser = repository.currentUser
    val studentProfile = repository.studentProfile
    val worlds = repository.worlds
    val stages = repository.stages
    val speakingPrompts = repository.speakingPrompts
    val roleplays = repository.roleplays
    val pronunciationDrills = repository.pronunciationDrills
    val vocabularyWords = repository.vocabularyWords
    val grammarTopics = repository.grammarTopics
    val assignments = repository.assignments
    val submissions = repository.submissions
    val liveClasses = repository.liveClasses
    val announcements = repository.announcements
    val leaderboard = repository.leaderboard
    val badges = repository.badges
    val adminStudents = repository.adminStudents
    val adminTeachers = repository.adminTeachers

    // Registration Permissions
    val allowStudentRegistration = repository.allowStudentRegistration
    val allowTeacherRegistration = repository.allowTeacherRegistration

    fun setAllowStudentRegistration(allow: Boolean) {
        repository.setAllowStudentRegistration(allow)
    }

    fun setAllowTeacherRegistration(allow: Boolean) {
        repository.setAllowTeacherRegistration(allow)
    }

    // Dynamic Game Content Flows
    val wordMatchChallenges = repository.wordMatchChallenges
    val missingLettersChallenges = repository.missingLettersChallenges
    val sentenceBuilderChallenges = repository.sentenceBuilderChallenges
    val listenChooseChallenges = repository.listenChooseChallenges
    val pronunciationBattleChallenges = repository.pronunciationBattleChallenges
    val fastVocabularyChallenges = repository.fastVocabularyChallenges
    val grammarChallenges = repository.grammarChallenges

    // Student Management helpers
    fun updateStudent(student: StudentManagementInfo) {
        repository.updateStudent(student)
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
    }

    // Game Management helpers
    fun addWordMatchChallenge(word: String, definition: String, category: String, createdBy: String) {
        repository.addWordMatchChallenge(word, definition, category, createdBy)
    }
    fun deleteWordMatchChallenge(id: String) {
        repository.deleteWordMatchChallenge(id)
    }

    fun addMissingLettersChallenge(fullWord: String, hint: String, createdBy: String) {
        repository.addMissingLettersChallenge(fullWord, hint, createdBy)
    }
    fun deleteMissingLettersChallenge(id: String) {
        repository.deleteMissingLettersChallenge(id)
    }

    fun addSentenceBuilderChallenge(targetSentence: String, hint: String, assameseTranslation: String = "", createdBy: String) {
        repository.addSentenceBuilderChallenge(targetSentence, hint, assameseTranslation, createdBy)
    }
    fun deleteSentenceBuilderChallenge(id: String) {
        repository.deleteSentenceBuilderChallenge(id)
    }

    fun addListenChooseChallenge(audioScript: String, question: String, options: List<String>, correctIndex: Int, createdBy: String) {
        repository.addListenChooseChallenge(audioScript, question, options, correctIndex, createdBy)
    }
    fun deleteListenChooseChallenge(id: String) {
        repository.deleteListenChooseChallenge(id)
    }

    fun addPronunciationBattleChallenge(sentence: String, phonemeFocus: String, difficulty: String, passingScore: Int, createdBy: String) {
        repository.addPronunciationBattleChallenge(sentence, phonemeFocus, difficulty, passingScore, createdBy)
    }
    fun deletePronunciationBattleChallenge(id: String) {
        repository.deletePronunciationBattleChallenge(id)
    }

    fun addFastVocabularyChallenge(word: String, prompt: String, options: List<String>, correctIndex: Int, createdBy: String) {
        repository.addFastVocabularyChallenge(word, prompt, options, correctIndex, createdBy)
    }
    fun deleteFastVocabularyChallenge(id: String) {
        repository.deleteFastVocabularyChallenge(id)
    }

    fun addGrammarChallenge(question: String, options: List<String>, correctIndex: Int, explanation: String, category: String, createdBy: String) {
        repository.addGrammarChallenge(question, options, correctIndex, explanation, category, createdBy)
    }
    fun deleteGrammarChallenge(id: String) {
        repository.deleteGrammarChallenge(id)
    }

    fun updateWordMatchChallenge(updated: WordMatchChallenge) {
        repository.updateWordMatchChallenge(updated)
    }
    fun updateMissingLettersChallenge(updated: MissingLettersChallenge) {
        repository.updateMissingLettersChallenge(updated)
    }
    fun updateSentenceBuilderChallenge(updated: SentenceBuilderChallenge) {
        repository.updateSentenceBuilderChallenge(updated)
    }
    fun updateListenChooseChallenge(updated: ListenChooseChallenge) {
        repository.updateListenChooseChallenge(updated)
    }
    fun updatePronunciationBattleChallenge(updated: PronunciationBattleChallenge) {
        repository.updatePronunciationBattleChallenge(updated)
    }
    fun updateFastVocabularyChallenge(updated: FastVocabularyChallenge) {
        repository.updateFastVocabularyChallenge(updated)
    }
    fun updateGrammarChallenge(updated: GrammarChallengeQuestion) {
        repository.updateGrammarChallenge(updated)
    }

    // Navigation States
    private val _currentStudentTab = MutableStateFlow(StudentTab.HOME)
    val currentStudentTab: StateFlow<StudentTab> = _currentStudentTab.asStateFlow()

    private val _currentTeacherTab = MutableStateFlow(TeacherTab.DASHBOARD)
    val currentTeacherTab: StateFlow<TeacherTab> = _currentTeacherTab.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    private val _speakingSubTab = MutableStateFlow(SpeakingSubTab.AI_PARTNER)
    val speakingSubTab: StateFlow<SpeakingSubTab> = _speakingSubTab.asStateFlow()

    // Active Interactive States
    private val _activeGame = MutableStateFlow<ActiveGameType?>(null)
    val activeGame: StateFlow<ActiveGameType?> = _activeGame.asStateFlow()

    private val _activeRoleplay = MutableStateFlow<RoleplayScenario?>(null)
    val activeRoleplay: StateFlow<RoleplayScenario?> = _activeRoleplay.asStateFlow()
    val roleplayTurnIndex = MutableStateFlow(0)

    private val _activePronunciationDrill = MutableStateFlow<PronunciationDrill?>(null)
    val activePronunciationDrill: StateFlow<PronunciationDrill?> = _activePronunciationDrill.asStateFlow()

    // Speech Evaluation Result
    private val _lastEvaluation = MutableStateFlow<SpeakingEvaluation?>(null)
    val lastEvaluation: StateFlow<SpeakingEvaluation?> = _lastEvaluation.asStateFlow()

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
    }

    fun selectStudentTab(tab: StudentTab) {
        _currentStudentTab.value = tab
    }

    fun selectTeacherTab(tab: TeacherTab) {
        _currentTeacherTab.value = tab
    }

    fun selectAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun selectSpeakingSubTab(subTab: SpeakingSubTab) {
        _speakingSubTab.value = subTab
        _lastEvaluation.value = null
        speechManager.reset()
    }

    fun toggleRoleSwitcher(show: Boolean) {
        if (show && currentUser.value.role == Role.STUDENT) {
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

    fun unifiedLogin(identifier: String, pinOrPassword: String): UnifiedLoginResult {
        val idClean = identifier.trim().lowercase()
        val secretClean = pinOrPassword.trim()

        if (idClean.isBlank() || secretClean.isBlank()) {
            return UnifiedLoginResult.Error("Please enter both email/ID and security PIN.")
        }

        // 1. Check Administrator Access
        if ((idClean == "admin@linguaquest.edu" || idClean == "admin" || idClean == "dean.arthur" || idClean == repository.currentUser.value.email.trim().lowercase()) &&
            (secretClean == adminPin || secretClean == adminPassword)) {
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
            it.email.trim().lowercase() == idClean &&
            ((it.securityPin.isNotBlank() && it.securityPin == secretClean) || secretClean == teacherPin || secretClean == teacherPassword)
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
            it.email.trim().lowercase() == idClean &&
            ((it.securityPin.isNotBlank() && it.securityPin == secretClean) || secretClean == "1234" || secretClean == "student123")
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
        if (idClean == "alex.morgan@linguaquest.edu" && (secretClean == "1234" || secretClean == "student123")) {
            val studentUser = User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", "Batch Alpha", securityPin = "1234")
            repository.setCurrentUser(studentUser)
            repository.setStudentProfileUserId("std_001", "Alex Morgan")
            appPreferences.saveSession(studentUser)
            appPreferences.saveStudentProfile(repository.studentProfile.value)
            _isLoggedIn.value = true
            _showRoleSwitcher.value = false
            return UnifiedLoginResult.Success(Role.STUDENT, studentUser)
        }

        return UnifiedLoginResult.Error("Invalid email or security PIN. Please check your credentials.")
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
        // Students cannot elevate role
        if (currentUser.value.role == Role.STUDENT) {
            return false
        }
        val isValid = when (role) {
            Role.ADMIN -> passwordInput.trim() == adminPassword || passwordInput.trim() == adminPin
            Role.TEACHER -> passwordInput.trim() == teacherPassword || passwordInput.trim() == teacherPin || passwordInput.trim() == adminPassword || passwordInput.trim() == adminPin
            Role.STUDENT -> false
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
        // Strict Security: Students cannot verify staff PINs or switch to staff portals
        if (currentUser.value.role == Role.STUDENT) {
            return false
        }
        // Admin can switch to Teacher panel directly without security PIN
        if (currentUser.value.role == Role.ADMIN && role == Role.TEACHER) {
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
        if (currentUser.value.role == Role.STUDENT && role != Role.STUDENT) {
            return
        }
        repository.switchRole(role)
        appPreferences.saveSession(repository.currentUser.value)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false
    }

    fun logout() {
        appPreferences.clearSession()
        _isLoggedIn.value = false
        _showRoleSwitcher.value = false
        _activeGame.value = null
        _activeRoleplay.value = null
    }

    fun switchRole(role: Role) {
        // Strict Security: Logged-in students are strictly forbidden from switching to TEACHER or ADMIN
        if (currentUser.value.role == Role.STUDENT && role != Role.STUDENT) {
            return
        }
        repository.switchRole(role)
        appPreferences.saveSession(repository.currentUser.value)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false
        _activeGame.value = null
        _activeRoleplay.value = null
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

    fun evaluateInputSpeech(spoken: String) {
        val target: String? = when {
            _activePronunciationDrill.value != null -> _activePronunciationDrill.value?.wordOrSentence
            _activeRoleplay.value != null -> {
                val idx = roleplayTurnIndex.value
                val turn = _activeRoleplay.value?.turns?.getOrNull(idx)
                turn?.text
            }
            _speakingSubTab.value == SpeakingSubTab.AI_PARTNER -> "I would like to order a flat white coffee with oat milk please"
            else -> null
        }

        val eval = EvaluationEngine.evaluateSpeech(
            spokenText = spoken,
            targetPhrase = target
        )
        _lastEvaluation.value = eval

        // If score is good, reward XP and coins
        if (eval.overallScore >= 70) {
            val xpEarned = (eval.overallScore * 0.4).toInt()
            val coinsEarned = 10
            repository.addRewards(xpEarned, coinsEarned)
            syncCurrentStudentProgressToSupabase()
        }
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

    // Games Management
    fun launchGame(game: ActiveGameType) {
        _activeGame.value = game
        _lastEvaluation.value = null
    }

    fun closeGame() {
        _activeGame.value = null
    }

    fun rewardGameWin(xp: Int, coins: Int) {
        repository.addRewards(xp, coins)
        _celebrationReward.value = xp to coins
        syncCurrentStudentProgressToSupabase()
    }

    fun dismissCelebration() {
        _celebrationReward.value = null
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
        return drill
    }

    // Speaking Prompts CRUD (Teacher & Admin)
    fun addSpeakingPrompt(
        title: String,
        category: String,
        question: String,
        followUpTip: String,
        keywords: List<String>,
        minSeconds: Int = 20
    ): SpeakingPrompt {
        return repository.addSpeakingPrompt(title, category, question, followUpTip, keywords, minSeconds)
    }

    fun updateSpeakingPrompt(prompt: SpeakingPrompt) {
        repository.updateSpeakingPrompt(prompt)
    }

    fun deleteSpeakingPrompt(promptId: String) {
        repository.deleteSpeakingPrompt(promptId)
    }

    // Pronunciation Drills CRUD (Teacher & Admin)
    fun addPronunciationDrill(
        wordOrSentence: String,
        ipaPhonetic: String,
        audioFocus: String,
        difficulty: String,
        tips: String
    ): PronunciationDrill {
        return repository.addPronunciationDrill(wordOrSentence, ipaPhonetic, audioFocus, difficulty, tips)
    }

    fun updatePronunciationDrill(drill: PronunciationDrill) {
        repository.updatePronunciationDrill(drill)
    }

    fun deletePronunciationDrill(drillId: String) {
        repository.deletePronunciationDrill(drillId)
        if (_activePronunciationDrill.value?.id == drillId) {
            _activePronunciationDrill.value = null
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
    }

    fun deleteTeacherVocabularyWord(wordId: String) {
        repository.deleteVocabularyWord(wordId)
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
