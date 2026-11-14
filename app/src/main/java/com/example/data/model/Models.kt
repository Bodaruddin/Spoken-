package com.example.data.model

enum class Role {
    STUDENT,
    TEACHER,
    ADMIN
}

enum class LearningLevel(val displayName: String, val cefr: String) {
    BEGINNER("Beginner", "A1"),
    ELEMENTARY("Elementary", "A2"),
    PRE_INTERMEDIATE("Pre-Intermediate", "B1"),
    INTERMEDIATE("Intermediate", "B2"),
    UPPER_INTERMEDIATE("Upper-Intermediate", "C1"),
    ADVANCED("Advanced", "C2")
}

object AcademicClasses {
    val DEFAULT_CLASSES = listOf(
        "Class 6",
        "Class 7",
        "Class 8",
        "Class 9",
        "Class 10",
        "Class 11",
        "Class 12"
    )
    val ALL_CLASSES: List<String> get() = DEFAULT_CLASSES
    val ASSIGNMENT_TARGETS: List<String> get() = listOf("All Classes") + DEFAULT_CLASSES
    const val DEFAULT_CLASS = "Class 10"
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val avatarEmoji: String = "🦉",
    val batch: String = AcademicClasses.DEFAULT_CLASS,
    val isActive: Boolean = true,
    val joinedDate: String = "Nov 2026",
    val securityPin: String = ""
) {
    val studentClass: String get() = batch
}

data class StudentProfile(
    val userId: String,
    val currentLevel: LearningLevel = LearningLevel.BEGINNER,
    val streakDays: Int = 7,
    val xpPoints: Int = 1420,
    val coins: Int = 380,
    val league: String = "Gold League",
    val overallScore: Int = 84, // 0 - 100
    val speakingScore: Int = 82,
    val pronunciationScore: Int = 85,
    val fluencyScore: Int = 78,
    val grammarScore: Int = 88,
    val vocabularyScore: Int = 86,
    val listeningScore: Int = 90,
    val readingScore: Int = 85,
    val completedStagesCount: Int = 14,
    val speakingPracticeMinutes: Int = 165,
    val lastPracticeDate: String = ""
) {
    // Dynamic level (Level 1, 2, 3...) calculated from student activity (XP, stages cleared, practice time)
    val activityLevel: Int
        get() = when {
            xpPoints >= 4000 || completedStagesCount >= 30 -> 6
            xpPoints >= 2500 || completedStagesCount >= 20 -> 5
            xpPoints >= 1500 || completedStagesCount >= 12 -> 4
            xpPoints >= 800 || completedStagesCount >= 7 -> 3
            xpPoints >= 250 || completedStagesCount >= 2 -> 2
            else -> 1
        }

    val activityLevelName: String
        get() = "Level $activityLevel"
}

data class World(
    val id: Int,
    val name: String,
    val description: String,
    val themeColorHex: Long,
    val iconEmoji: String,
    val totalStages: Int,
    val completedStages: Int,
    val isUnlocked: Boolean
)

enum class StageType {
    SPEAKING,
    VOCABULARY,
    GRAMMAR,
    PRONUNCIATION,
    LISTENING,
    STORY,
    CHALLENGE
}

data class LessonStage(
    val id: String,
    val worldId: Int,
    val stageNumber: Int,
    val title: String,
    val topicDescription: String,
    val type: StageType,
    val xpReward: Int = 50,
    val coinReward: Int = 15,
    val stars: Int = 0, // 0 to 3
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)

data class SpeakingEvaluation(
    val spokenText: String,
    val expectedText: String? = null,
    val overallScore: Int,
    val pronunciationScore: Int,
    val fluencyScore: Int,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val feedback: String,
    val suggestedImprovement: String,
    val phonemeAccuracy: List<Pair<String, Boolean>> = emptyList(),
    val isAiEvaluated: Boolean = false,
    val aiModelUsed: String = "",
    val aiCoachResponse: String? = null,
    val cefrLevel: String = "",
    val detectedWeakWords: List<String> = emptyList(),
    val followUpQuestion: String? = null,
    val hint: String? = null,
    val exampleAnswers: List<String> = emptyList()
)

data class NovaChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isNova: Boolean,
    val text: String,
    val audioPrompt: String = text,
    val isQuestion: Boolean = false,
    val questionNumber: Int = 1,
    val evaluation: SpeakingEvaluation? = null,
    val hint: String? = null,
    val exampleAnswers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class DialogueTurn(
    val speaker: String, // "AI" or "USER"
    val text: String,
    val expectedKeywords: List<String> = emptyList(),
    val sampleAudioPrompt: String = text
)

data class RoleplayScenario(
    val id: String,
    val title: String,
    val situation: String,
    val partnerRole: String,
    val userRole: String,
    val difficulty: String,
    val emoji: String,
    val turns: List<DialogueTurn>
)

data class SpeakingPrompt(
    val id: String,
    val title: String,
    val category: String, // Daily, Business, Travel, Story, Hobbies, College, Technology, Culture
    val question: String,
    val followUpTip: String,
    val recommendedKeywords: List<String>,
    val minSpeakingSeconds: Int = 20,
    val emoji: String = "💬",
    val difficultyLevel: String = "Intermediate", // Beginner, Intermediate, Advanced
    val createdBy: String = "Faculty",
    val milestoneTurns: Int = 0 // 0 = follow global teacher milestone target
)

data class PronunciationDrill(
    val id: String,
    val wordOrSentence: String,
    val ipaPhonetic: String,
    val audioFocus: String, // e.g. "th-sound", "silent letters"
    val difficulty: String,
    val tips: String
)

data class VocabularyWord(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val meaning: String,
    val exampleSentence: String,
    val category: String,
    val synonyms: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val assameseMeaning: String = "",
    val assameseTransliteration: String = "",
    val syllables: String = "",
    val coachingTip: String = "",
    val addedByTeacher: String = ""
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class GrammarTopic(
    val id: String,
    val title: String,
    val level: LearningLevel,
    val category: String,
    val explanation: String,
    val rules: List<String>,
    val exampleSentences: List<Pair<String, String>>, // Correct vs Common Mistake
    val questions: List<QuizQuestion>
)

data class Assignment(
    val id: String,
    val title: String,
    val teacherName: String,
    val batch: String = AcademicClasses.DEFAULT_CLASS,
    val dueDate: String,
    val instructions: String,
    val topicType: String,
    val maxPoints: Int = 100,
    val submissionsCount: Int = 18,
    val totalAssigned: Int = 24
) {
    val targetClass: String get() = batch
}

data class StudentSubmission(
    val id: String,
    val assignmentId: String,
    val studentName: String,
    val studentAvatar: String,
    val audioDurationSeconds: Int,
    val studentTranscript: String,
    val submittedDate: String,
    val score: Int? = null,
    val teacherFeedback: String? = null,
    val status: String = "PENDING" // "PENDING" or "GRADED"
)

// --- MCQ Online Examination & Certificate Models ---
data class ExamQuestion(
    val id: String,
    val question: String,
    val options: List<String>, // Exactly 4 options: A, B, C, D
    val correctOptionIndex: Int, // 0 to 3
    val marks: Int = 5,
    val explanation: String = ""
)

data class Exam(
    val id: String,
    val title: String,
    val description: String,
    val teacherName: String,
    val targetClass: String = AcademicClasses.DEFAULT_CLASS, // e.g. "Class 10" or "All Classes"
    val subjectCategory: String = "English Grammar", // "English Grammar", "Phonetics & Pronunciation", "Vocabulary & Idioms", "Spoken English & Fluency"
    val durationMinutes: Int = 15, // Selected exam time limit in minutes
    val totalMarks: Int = 50,
    val passingPercentage: Int = 60, // e.g. 60%
    val questions: List<ExamQuestion> = emptyList(),
    val createdAt: String = "Nov 2026",
    val dueDate: String = "Tomorrow, 6 PM",
    val isPublished: Boolean = true,
    val availableFromMillis: Long = 0L, // 0L = Available immediately
    val availableUntilMillis: Long = 0L // 0L = No deadline (always open)
) {
    val questionsCount: Int get() = questions.size
    val passingMarks: Int get() = (totalMarks * passingPercentage) / 100
    val hasTimeWindow: Boolean get() = availableFromMillis > 0L || availableUntilMillis > 0L

    fun isBeforeStartAt(nowMillis: Long): Boolean =
        availableFromMillis > 0L && nowMillis < availableFromMillis

    fun isExpiredAt(nowMillis: Long): Boolean =
        availableUntilMillis > 0L && nowMillis > availableUntilMillis

    fun isOpenForStudentAt(nowMillis: Long): Boolean =
        isPublished && !isBeforeStartAt(nowMillis) && !isExpiredAt(nowMillis)
}

data class ExamSubmission(
    val id: String,
    val examId: String,
    val examTitle: String,
    val studentId: String,
    val studentName: String,
    val studentClass: String,
    val studentAvatar: String = "🎓",
    val selectedAnswers: Map<String, Int> = emptyMap(), // questionId -> selectedOptionIndex (0..3)
    val scoreObtained: Int,
    val totalMarks: Int,
    val percentage: Int,
    val isPassed: Boolean,
    val grade: String, // "O", "A+", "A", "B+", "B", "C"
    val performanceTitle: String = "Academic Distinction", // "Outstanding Distinction", "Distinction", "Merit", "Satisfactory"
    val timeTakenSeconds: Int = 0,
    val completedAt: String = "Today",
    val certificateId: String = "SPK-EXM-2026-0000"
)


data class LiveClassSession(
    val id: String,
    val title: String,
    val instructorName: String,
    val scheduledTime: String,
    val durationMin: Int,
    val topicDescription: String,
    val status: String, // "LIVE", "UPCOMING", "COMPLETED"
    val participantsCount: Int
)

data class AnnouncementItem(
    val id: String,
    val title: String,
    val message: String,
    val sender: String,
    val timestamp: String,
    val isUrgent: Boolean = false
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val xp: Int,
    val streak: Int,
    val league: String,
    val isCurrentUser: Boolean = false,
    val studentClass: String = AcademicClasses.DEFAULT_CLASS,
    val studentId: String = "",
    val cefrLevel: String = "A2"
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val progress: Float = 1.0f // 0.0 to 1.0
)

// Comprehensive student management profile for Admin & Teacher
data class StudentManagementInfo(
    val id: String,
    val name: String,
    val email: String,
    val avatarEmoji: String = "🎓",
    val batch: String = AcademicClasses.DEFAULT_CLASS,
    val level: LearningLevel = LearningLevel.ELEMENTARY,
    val isActive: Boolean = true,
    val speakingScore: Int = 82,
    val pronunciationScore: Int = 85,
    val fluencyScore: Int = 79,
    val grammarScore: Int = 88,
    val vocabularyScore: Int = 86,
    val listeningScore: Int = 90,
    val streakDays: Int = 7,
    val xpPoints: Int = 1450,
    val joinedDate: String = "Nov 2026",
    val teacherNotes: String = "Dedicated learner. Shows high enthusiasm in live speaking roleplays.",
    val securityPin: String = "1234"
) {
    val studentClass: String get() = batch
}

// Dynamic Game Content Models
enum class VocabularyActivityType(
    val title: String,
    val emoji: String,
    val subtitle: String
) {
    PICTURE_MATCHING("Picture Matching", "🖼️", "Match words with visual scenes & icons"),
    MEANING_MATCHING("Meaning Matching", "📖", "Connect English words to accurate meanings"),
    SYNONYMS("Synonyms", "🔁", "Find words with the same or similar meaning"),
    OPPOSITE_WORDS("Opposite Words", "⚖️", "Match words with their exact antonyms"),
    WORD_SCRAMBLE("Word Scramble", "🔀", "Unscramble letter tiles to form the word");

    companion object {
        fun fromName(raw: String?): VocabularyActivityType =
            values().firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: MEANING_MATCHING
    }
}

data class WordMatchChallenge(
    val id: String,
    val word: String,
    val definition: String,
    val category: String = "General",
    val createdBy: String = "System",
    val activityType: String = VocabularyActivityType.MEANING_MATCHING.name,
    val pictureEmoji: String = "✨",
    val synonym: String = "",
    val antonym: String = "",
    val distractors: List<String> = emptyList(),
    val scrambleHint: String = ""
)

data class VocabularyModeConfig(
    val activityType: VocabularyActivityType,
    val title: String = activityType.title,
    val subtitle: String = activityType.subtitle,
    val xpReward: Int = 50,
    val questionsPerRound: Int = 5,
    val isEnabled: Boolean = true
)

data class VocabularyChallengeProgress(
    val completedModes: Set<String> = emptySet(),
    val totalRoundsPlayed: Int = 0,
    val totalXpEarned: Int = 0,
    val bestAccuracyPercent: Int = 0
)

data class MissingLettersChallenge(
    val id: String,
    val fullWord: String,
    val hint: String,
    val missingIndices: List<Int>,
    val category: String = "General",
    val createdBy: String = "System"
)

data class SentenceBuilderPack(
    val id: String,
    val title: String,
    val description: String = "Build grammatically correct sentences.",
    val difficulty: String = "Easy", // "Easy", "Medium", "Hard"
    val xpReward: Int = 40,
    val createdBy: String = "System"
)

data class SentenceBuilderChallenge(
    val id: String,
    val targetSentence: String,
    val hint: String = "Form a grammatically correct sentence",
    val assameseTranslation: String = "",
    val createdBy: String = "System",
    val packId: String = "sb_pack_easy"
)

data class ListenChooseChallenge(
    val id: String,
    val audioScript: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val createdBy: String = "System"
)

data class PronunciationBattleChallenge(
    val id: String,
    val challengeSentence: String,
    val targetPhonemeFocus: String,
    val difficulty: String = "Medium",
    val passingScore: Int = 80,
    val createdBy: String = "System"
)

data class FastVocabularyPack(
    val id: String,
    val title: String,
    val description: String = "Think fast & pick the right meaning!",
    val themeStyle: String = "Easy", // "Easy", "Medium", "Hard", "Mixed"
    val durationSeconds: Int = 60,
    val xpReward: Int = 40,
    val createdBy: String = "System"
)

data class FastVocabularyChallenge(
    val id: String,
    val word: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val createdBy: String = "System",
    val packId: String = "fv_pack_easy"
)

data class FastVocabularyProgress(
    val playedPackIds: Set<String> = emptySet(),
    val totalXpEarned: Int = 0,
    val bestScorePercent: Int = 0
)

data class GrammarChallengePack(
    val id: String,
    val title: String,
    val description: String = "Test your grammar skills with fun quizzes!",
    val themeStyle: String = "Tenses", // "Tenses", "Narration", "Determiners", "Prepositions"
    val xpReward: Int = 40,
    val createdBy: String = "System"
)

data class GrammarChallengeQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String = "Tenses",
    val createdBy: String = "System",
    val packId: String = "gc_pack_tenses"
)

data class SupabaseGameChallenge(
    val id: String,
    val type: String,
    val titleOrWord: String,
    val hintOrMeaning: String = "",
    val category: String = "General",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val extraData: String = "",
    val createdBy: String = "Faculty"
)

data class PictureVocabChallenge(
    val id: String,
    val emoji: String,
    val word: String,
    val phonetic: String,
    val clue: String,
    val options: List<String>,
    val createdBy: String = "System"
)

