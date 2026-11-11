package com.example.data.model

enum class AdventureMissionType(val displayName: String, val emoji: String) {
    QUIZ("Interactive Quiz", "❓"),
    WORD_PUZZLE("Word Puzzle", "🧩"),
    SENTENCE_BUILDER("Sentence Builder", "🏗️"),
    LISTENING_CHALLENGE("Listening Audio", "🎧"),
    SPEAKING_CHALLENGE("AI Speaking", "🗣️")
}

data class AdventureMission(
    val id: String,
    val levelId: String,
    val worldId: String,
    val title: String,
    val description: String,
    val type: AdventureMissionType,
    val targetSentence: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val clue: String = "",
    val xp: Int = 40,
    val coins: Int = 20,
    val scrambledWords: List<String> = emptyList(),
    val audioPrompt: String = targetSentence,
    val explanation: String = ""
)

data class AdventureLevel(
    val id: String,
    val worldId: String,
    val levelNumber: Int,
    val title: String,
    val description: String,
    val iconEmoji: String = "⭐",
    val missions: List<AdventureMission> = emptyList(),
    val xpReward: Int = 100,
    val coinsReward: Int = 50,
    val requiredStars: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0 // 0..3
)

data class AdventureWorld(
    val id: String,
    val name: String,
    val order: Int,
    val description: String,
    val tagline: String,
    val emoji: String,
    val themeColorHex: Long,
    val gradientStartHex: Long,
    val gradientEndHex: Long,
    val requiredTotalStars: Int = 0,
    val isUnlocked: Boolean = true,
    val levels: List<AdventureLevel> = emptyList()
)

data class AdventureBadge(
    val id: String,
    val name: String,
    val worldName: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: String = ""
)

data class AdventureProgress(
    val studentId: String = "std_001",
    val completedLevelIds: Set<String> = emptySet(),
    val completedMissionIds: Set<String> = emptySet(),
    val unlockedWorldIds: Set<String> = setOf("world_vocab"),
    val levelStars: Map<String, Int> = emptyMap(), // levelId -> stars 1..3
    val totalStars: Int = 0,
    val totalCoins: Int = 0,
    val totalXp: Int = 0,
    val badges: List<String> = emptyList(),
    val claimedChestWorldIds: Set<String> = emptySet()
)

data class AdventureLeaderboardEntry(
    val rank: Int,
    val studentId: String,
    val studentName: String,
    val avatar: String,
    val className: String,
    val currentWorld: String,
    val totalStars: Int,
    val coins: Int,
    val xp: Int,
    val isCurrentUser: Boolean = false
)

data class StudentAdventureProgressSummary(
    val studentId: String,
    val studentName: String,
    val avatar: String,
    val className: String,
    val currentWorld: String,
    val completedLevelsCount: Int,
    val totalLevelsCount: Int,
    val totalStars: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val lastActiveMission: String,
    val completionPercent: Float
)
