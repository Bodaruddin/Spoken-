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

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val avatarEmoji: String = "🦉",
    val batch: String = "Morning A",
    val isActive: Boolean = true,
    val joinedDate: String = "Nov 2026"
)

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
    val speakingPracticeMinutes: Int = 165
)

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
    val phonemeAccuracy: List<Pair<String, Boolean>> = emptyList()
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
    val category: String, // Daily, Business, Travel, Story
    val question: String,
    val followUpTip: String,
    val recommendedKeywords: List<String>,
    val minSpeakingSeconds: Int = 20
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
    val isMastered: Boolean = false
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
    val batch: String,
    val dueDate: String,
    val instructions: String,
    val topicType: String,
    val maxPoints: Int = 100,
    val submissionsCount: Int = 18,
    val totalAssigned: Int = 24
)

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
    val isCurrentUser: Boolean = false
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
    val batch: String = "Batch Alpha",
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
    val teacherNotes: String = "Dedicated learner. Shows high enthusiasm in live speaking roleplays."
)

// Dynamic Game Content Models
data class WordMatchChallenge(
    val id: String,
    val word: String,
    val definition: String,
    val category: String = "General",
    val createdBy: String = "System"
)

data class MissingLettersChallenge(
    val id: String,
    val fullWord: String,
    val hint: String,
    val missingIndices: List<Int>,
    val createdBy: String = "System"
)

data class SentenceBuilderChallenge(
    val id: String,
    val targetSentence: String,
    val hint: String = "Form a grammatically correct sentence",
    val assameseTranslation: String = "",
    val createdBy: String = "System"
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

data class FastVocabularyChallenge(
    val id: String,
    val word: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val createdBy: String = "System"
)

data class GrammarChallengeQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String = "Tenses",
    val createdBy: String = "System"
)

