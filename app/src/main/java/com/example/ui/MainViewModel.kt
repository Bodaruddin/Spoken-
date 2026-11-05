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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StudentTab(val label: String, val iconEmoji: String) {
    HOME("Home", "🏠"),
    LEARN("Journey", "🗺️"),
    PRACTICE("Speaking", "🎙️"),
    GAMES("Games", "🎮"),
    PROFILE("Profile", "👤")
}

enum class TeacherTab(val label: String, val iconEmoji: String) {
    DASHBOARD("Overview", "📊"),
    STUDENTS("Students", "👥"),
    SPEAKING_REVIEW("Assessments", "🎧"),
    ASSIGNMENTS("Assignments", "📝"),
    LIVE_CLASSES("Classes", "🎥")
}

enum class AdminTab(val label: String, val iconEmoji: String) {
    DASHBOARD("Dashboard", "📈"),
    STUDENTS("Students", "🎓"),
    TEACHERS("Faculty", "👨‍🏫"),
    CURRICULUM("Courses", "📚"),
    ANALYTICS("Reports", "📋"),
    SETTINGS("Settings", "⚙️")
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

    val repository = LinguaQuestRepository()
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
        _showSupabaseModal.value = show
    }

    fun connectSupabase(url: String, key: String) {
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

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Role Switcher Dialog
    private val _showRoleSwitcher = MutableStateFlow(false)
    val showRoleSwitcher: StateFlow<Boolean> = _showRoleSwitcher.asStateFlow()

    init {
        // Collect speech success and evaluate automatically
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
        _showRoleSwitcher.value = show
    }

    fun login(role: Role) {
        repository.switchRole(role)
        _isLoggedIn.value = true
        _showRoleSwitcher.value = false
    }

    fun logout() {
        _isLoggedIn.value = false
        _showRoleSwitcher.value = false
        _activeGame.value = null
        _activeRoleplay.value = null
    }

    fun switchRole(role: Role) {
        repository.switchRole(role)
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
        }
    }

    fun completeStage(stageId: String, stars: Int, xpEarned: Int, coinsEarned: Int) {
        repository.completeStage(stageId, stars, xpEarned, coinsEarned)
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

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechManager.destroy()
    }
}
