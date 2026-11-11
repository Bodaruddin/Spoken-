package com.example.data.repository

import com.example.data.model.*

object AdventureDataDefaults {

    fun createDefaultWorlds(): List<AdventureWorld> {
        return listOf(
            // -------------------------------------------------------------
            // 1. VOCABULARY ISLAND (🏝️)
            // -------------------------------------------------------------
            AdventureWorld(
                id = "world_vocab",
                name = "Vocabulary Island",
                order = 1,
                description = "Navigate lush tropical bays, uncover lost pirate treasure words, and master rich everyday English vocabulary.",
                tagline = "Island of Lexical Discovery",
                emoji = "🏝️",
                themeColorHex = 0xFF10B981, // Emerald Green
                gradientStartHex = 0xFF059669,
                gradientEndHex = 0xFF06B6D4,
                requiredTotalStars = 0,
                isUnlocked = true,
                levels = listOf(
                    AdventureLevel(
                        id = "lvl_vocab_1",
                        worldId = "world_vocab",
                        levelNumber = 1,
                        title = "Tropical Bay Market",
                        description = "Identify exotic island goods, spices, and vibrant descriptive adjectives.",
                        iconEmoji = "🥥",
                        xpReward = 60,
                        coinsReward = 25,
                        requiredStars = 0,
                        isUnlocked = true,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_v1_1",
                                levelId = "lvl_vocab_1",
                                worldId = "world_vocab",
                                title = "Island Produce Quiz",
                                description = "Choose the best synonym for 'abundant' in the island market.",
                                type = AdventureMissionType.QUIZ,
                                options = listOf("Plentiful", "Scarce", "Dry", "Tiny"),
                                correctAnswer = "Plentiful",
                                explanation = "'Abundant' means existing in large quantities, synonymous with 'plentiful'.",
                                xp = 20,
                                coins = 10
                            ),
                            AdventureMission(
                                id = "m_v1_2",
                                levelId = "lvl_vocab_1",
                                worldId = "world_vocab",
                                title = "Unscramble the Gem",
                                description = "Unscramble the letters to reveal a precious stone.",
                                type = AdventureMissionType.WORD_PUZZLE,
                                targetSentence = "EMERALD",
                                correctAnswer = "EMERALD",
                                scrambledWords = listOf("E", "M", "R", "A", "L", "E", "D"),
                                clue = "A bright green precious gemstone found in deep caves.",
                                xp = 20,
                                coins = 10
                            ),
                            AdventureMission(
                                id = "m_v1_3",
                                levelId = "lvl_vocab_1",
                                worldId = "world_vocab",
                                title = "Merchant's Greeting",
                                description = "Speak this greeting clearly to unlock the merchant's spices.",
                                type = AdventureMissionType.SPEAKING_CHALLENGE,
                                targetSentence = "Good morning, I would like two pounds of fresh ripe mangos.",
                                audioPrompt = "Good morning, I would like two pounds of fresh ripe mangos.",
                                xp = 25,
                                coins = 15
                            )
                        )
                    ),
                    AdventureLevel(
                        id = "lvl_vocab_2",
                        worldId = "world_vocab",
                        levelNumber = 2,
                        title = "Coral Reef Sanctuary",
                        description = "Explore maritime vocabulary, sea creatures, and navigation phrases.",
                        iconEmoji = "🐬",
                        xpReward = 80,
                        coinsReward = 35,
                        requiredStars = 2,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_v2_1",
                                levelId = "lvl_vocab_2",
                                worldId = "world_vocab",
                                title = "Reef Navigation Order",
                                description = "Assemble the sailor's navigational instruction.",
                                type = AdventureMissionType.SENTENCE_BUILDER,
                                targetSentence = "The submarine sailed carefully beneath the glowing coral reef",
                                correctAnswer = "The submarine sailed carefully beneath the glowing coral reef",
                                scrambledWords = listOf("carefully", "coral", "The", "submarine", "beneath", "sailed", "glowing", "reef", "the"),
                                xp = 30,
                                coins = 15
                            ),
                            AdventureMission(
                                id = "m_v2_2",
                                levelId = "lvl_vocab_2",
                                worldId = "world_vocab",
                                title = "Dolphin Echoes",
                                description = "Listen to the marine biologist's report and choose the topic.",
                                type = AdventureMissionType.LISTENING_CHALLENGE,
                                audioPrompt = "Dolphins use echolocation to communicate across long ocean distances.",
                                options = listOf("Echolocation communication", "Deep-sea volcanism", "Fishing boats", "Weather satellites"),
                                correctAnswer = "Echolocation communication",
                                xp = 30,
                                coins = 15
                            )
                        )
                    ),
                    AdventureLevel(
                        id = "lvl_vocab_3",
                        worldId = "world_vocab",
                        levelNumber = 3,
                        title = "The Pirate Captain's Vault",
                        description = "Boss Level: Unlock the legendary vocabulary chest on Volcano Peak.",
                        iconEmoji = "🏴‍☠️",
                        xpReward = 120,
                        coinsReward = 60,
                        requiredStars = 5,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_v3_1",
                                levelId = "lvl_vocab_3",
                                worldId = "world_vocab",
                                title = "Island Boss Speech",
                                description = "Declare the explorer's victory oath to claim the island badge!",
                                type = AdventureMissionType.SPEAKING_CHALLENGE,
                                targetSentence = "We sailed across uncharted waters and unlocked the secret treasure.",
                                audioPrompt = "We sailed across uncharted waters and unlocked the secret treasure.",
                                xp = 50,
                                coins = 25
                            )
                        )
                    )
                )
            ),

            // -------------------------------------------------------------
            // 2. GRAMMAR KINGDOM (👑)
            // -------------------------------------------------------------
            AdventureWorld(
                id = "world_grammar",
                name = "Grammar Kingdom",
                order = 2,
                description = "Enter the majestic realm of royal tenses, preposition drawbridges, and structural elegance.",
                tagline = "Realm of Structural Mastery",
                emoji = "👑",
                themeColorHex = 0xFF8B5CF6, // Royal Purple
                gradientStartHex = 0xFF6D28D9,
                gradientEndHex = 0xFF3B82F6,
                requiredTotalStars = 6,
                isUnlocked = false,
                levels = listOf(
                    AdventureLevel(
                        id = "lvl_gram_1",
                        worldId = "world_grammar",
                        levelNumber = 1,
                        title = "The Knights of Tenses",
                        description = "Master present perfect, past continuous, and future predictions.",
                        iconEmoji = "⚔️",
                        xpReward = 70,
                        coinsReward = 30,
                        requiredStars = 6,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_g1_1",
                                levelId = "lvl_gram_1",
                                worldId = "world_grammar",
                                title = "Tense Precision Duel",
                                description = "Select the sentence with the grammatically correct present perfect form.",
                                type = AdventureMissionType.QUIZ,
                                options = listOf(
                                    "She has visited five countries this year.",
                                    "She have visited five countries this year.",
                                    "She has visit five countries this year.",
                                    "She was visited five countries this year."
                                ),
                                correctAnswer = "She has visited five countries this year.",
                                explanation = "Third person singular 'she' takes 'has' + past participle 'visited'.",
                                xp = 25,
                                coins = 10
                            ),
                            AdventureMission(
                                id = "m_g1_2",
                                levelId = "lvl_gram_1",
                                worldId = "world_grammar",
                                title = "Royal Clause Construction",
                                description = "Rebuild the royal law in proper conditional syntax.",
                                type = AdventureMissionType.SENTENCE_BUILDER,
                                targetSentence = "If the knights train diligently every day they will win the tournament",
                                correctAnswer = "If the knights train diligently every day they will win the tournament",
                                scrambledWords = listOf("diligently", "they", "If", "knights", "train", "will", "the", "every", "tournament", "win", "the", "day"),
                                xp = 35,
                                coins = 15
                            )
                        )
                    ),
                    AdventureLevel(
                        id = "lvl_gram_2",
                        worldId = "world_grammar",
                        levelNumber = 2,
                        title = "Preposition Drawbridge",
                        description = "Cross the castle moat by positioning spatial and temporal prepositions.",
                        iconEmoji = "🏰",
                        xpReward = 90,
                        coinsReward = 40,
                        requiredStars = 8,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_g2_1",
                                levelId = "lvl_gram_2",
                                worldId = "world_grammar",
                                title = "Preposition Trial",
                                description = "Which preposition correctly completes: 'The banquet begins ___ eight in the evening'?",
                                type = AdventureMissionType.QUIZ,
                                options = listOf("at", "on", "in", "during"),
                                correctAnswer = "at",
                                explanation = "Use 'at' with specific clock times ('at eight').",
                                xp = 30,
                                coins = 15
                            ),
                            AdventureMission(
                                id = "m_g2_2",
                                levelId = "lvl_gram_2",
                                worldId = "world_grammar",
                                title = "Chancellor's Pronunciation Oath",
                                description = "Recite the royal decree with flawless articulation.",
                                type = AdventureMissionType.SPEAKING_CHALLENGE,
                                targetSentence = "Throughout history, discipline and perseverance have conquered all obstacles.",
                                audioPrompt = "Throughout history, discipline and perseverance have conquered all obstacles.",
                                xp = 40,
                                coins = 20
                            )
                        )
                    )
                )
            ),

            // -------------------------------------------------------------
            // 3. LISTENING FOREST (🌲)
            // -------------------------------------------------------------
            AdventureWorld(
                id = "world_forest",
                name = "Listening Forest",
                order = 3,
                description = "Wander beneath bioluminescent trees, decipher whispering winds, and hone acute auditory perception.",
                tagline = "Canopy of Auditory Clarity",
                emoji = "🌲",
                themeColorHex = 0xFF059669, // Forest Emerald
                gradientStartHex = 0xFF047857,
                gradientEndHex = 0xFF14B8A6,
                requiredTotalStars = 12,
                isUnlocked = false,
                levels = listOf(
                    AdventureLevel(
                        id = "lvl_for_1",
                        worldId = "world_forest",
                        levelNumber = 1,
                        title = "Whispering Willows",
                        description = "Listen closely to native accents and subtle phonetic nuances.",
                        iconEmoji = "🍃",
                        xpReward = 75,
                        coinsReward = 35,
                        requiredStars = 12,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_f1_1",
                                levelId = "lvl_for_1",
                                worldId = "world_forest",
                                title = "Echo in the Canopy",
                                description = "Listen to the forest ranger's advice and select the primary recommendation.",
                                type = AdventureMissionType.LISTENING_CHALLENGE,
                                audioPrompt = "Always carry a compass and stay on the designated trail after dusk.",
                                options = listOf(
                                    "Carry a compass and stay on the marked path",
                                    "Climb the tallest willow tree",
                                    "Light a campfire near dry leaves",
                                    "Rest near the rushing waterfall"
                                ),
                                correctAnswer = "Carry a compass and stay on the marked path",
                                xp = 35,
                                coins = 15
                            ),
                            AdventureMission(
                                id = "m_f1_2",
                                levelId = "lvl_for_1",
                                worldId = "world_forest",
                                title = "Solve the Forest Riddle",
                                description = "Unscramble the forest dweller.",
                                type = AdventureMissionType.WORD_PUZZLE,
                                targetSentence = "NIGHTINGALE",
                                correctAnswer = "NIGHTINGALE",
                                scrambledWords = listOf("G", "A", "N", "T", "I", "L", "E", "I", "H", "N", "G"),
                                clue = "A famous woodland songbird celebrated for its melodic night melodies.",
                                xp = 30,
                                coins = 15
                            )
                        )
                    )
                )
            ),

            // -------------------------------------------------------------
            // 4. SPEAKING CITY (🏙️)
            // -------------------------------------------------------------
            AdventureWorld(
                id = "world_city",
                name = "Speaking City",
                order = 4,
                description = "Step into a bustling neon metropolis where confidence, impromptu speaking, and conversation reign supreme.",
                tagline = "Metropolis of Vocal Fluency",
                emoji = "🏙️",
                themeColorHex = 0xFF0284C7, // Electric Cyan-Blue
                gradientStartHex = 0xFF2563EB,
                gradientEndHex = 0xFF7C3AED,
                requiredTotalStars = 18,
                isUnlocked = false,
                levels = listOf(
                    AdventureLevel(
                        id = "lvl_city_1",
                        worldId = "world_city",
                        levelNumber = 1,
                        title = "Times Square Interview",
                        description = "Speak confidently under city neon lights and test your speech speed.",
                        iconEmoji = "🎙️",
                        xpReward = 90,
                        coinsReward = 45,
                        requiredStars = 18,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_c1_1",
                                levelId = "lvl_city_1",
                                worldId = "world_city",
                                title = "Metropolitan Broadcast",
                                description = "Deliver the live weather and commuter update to the city listeners.",
                                type = AdventureMissionType.SPEAKING_CHALLENGE,
                                targetSentence = "Welcome back listeners, traffic across the suspension bridge is moving smoothly this morning.",
                                audioPrompt = "Welcome back listeners, traffic across the suspension bridge is moving smoothly this morning.",
                                xp = 45,
                                coins = 20
                            ),
                            AdventureMission(
                                id = "m_c1_2",
                                levelId = "lvl_city_1",
                                worldId = "world_city",
                                title = "Urban Elevator Pitch",
                                description = "Arrange words into a crisp business introduction.",
                                type = AdventureMissionType.SENTENCE_BUILDER,
                                targetSentence = "Our innovative mobile platform empowers students to speak English with confidence",
                                correctAnswer = "Our innovative mobile platform empowers students to speak English with confidence",
                                scrambledWords = listOf("platform", "empowers", "English", "Our", "innovative", "confidence", "mobile", "students", "speak", "to", "with"),
                                xp = 40,
                                coins = 20
                            )
                        )
                    )
                )
            ),

            // -------------------------------------------------------------
            // 5. FLUENT ENGLISH CASTLE (🏰)
            // -------------------------------------------------------------
            AdventureWorld(
                id = "world_castle",
                name = "Fluent English Castle",
                order = 5,
                description = "The pinnacle citadel! Master advanced idioms, philosophical debate, and graduate as a crowned Master of English.",
                tagline = "Citadel of Grand Mastery",
                emoji = "🏰",
                themeColorHex = 0xFFF59E0B, // Golden Amber
                gradientStartHex = 0xFFD97706,
                gradientEndHex = 0xFFEC4899,
                requiredTotalStars = 25,
                isUnlocked = false,
                levels = listOf(
                    AdventureLevel(
                        id = "lvl_cas_1",
                        worldId = "world_castle",
                        levelNumber = 1,
                        title = "Coronation of Fluency",
                        description = "The ultimate test: deliver the sovereign keynote address before the Grand Academy.",
                        iconEmoji = "🌟",
                        xpReward = 150,
                        coinsReward = 80,
                        requiredStars = 25,
                        isUnlocked = false,
                        isCompleted = false,
                        starsEarned = 0,
                        missions = listOf(
                            AdventureMission(
                                id = "m_cas_1",
                                levelId = "lvl_cas_1",
                                worldId = "world_castle",
                                title = "Mastery Idiom Trial",
                                description = "What does the idiom 'to burn the midnight oil' mean?",
                                type = AdventureMissionType.QUIZ,
                                options = listOf(
                                    "To work or study late into the night",
                                    "To waste fuel needlessly",
                                    "To sleep under the stars",
                                    "To celebrate a royal festival"
                                ),
                                correctAnswer = "To work or study late into the night",
                                explanation = "'Burn the midnight oil' means studying or working hard until late hours.",
                                xp = 50,
                                coins = 25
                            ),
                            AdventureMission(
                                id = "m_cas_2",
                                levelId = "lvl_cas_1",
                                worldId = "world_castle",
                                title = "Grand Sovereign Address",
                                description = "Speak this triumphant address with authority and clear articulation.",
                                type = AdventureMissionType.SPEAKING_CHALLENGE,
                                targetSentence = "Through dedication, curiosity, and practice, we have unlocked the gateway to fluent expression.",
                                audioPrompt = "Through dedication, curiosity, and practice, we have unlocked the gateway to fluent expression.",
                                xp = 70,
                                coins = 40
                            )
                        )
                    )
                )
            )
        )
    }

    fun createDefaultBadges(): List<AdventureBadge> {
        return listOf(
            AdventureBadge("b_island", "Island Navigator", "Vocabulary Island", "Explored all market coves and collected 6 stars", "🏝️", true, "Unlocked"),
            AdventureBadge("b_knight", "Grammar Knight", "Grammar Kingdom", "Defeated the irregular tenses and breached the drawbridge", "⚔️", false),
            AdventureBadge("b_listener", "Forest Listener", "Listening Forest", "Deciphered all whispering tree audio riddles", "🌲", false),
            AdventureBadge("b_orator", "City Orator", "Speaking City", "Conquered the broadcast tower with >85% speech fluency", "🏙️", false),
            AdventureBadge("b_monarch", "Fluent Monarch", "Fluent English Castle", "Ascended the Golden Throne as a certified English speaker", "👑", false)
        )
    }

    fun createDefaultLeaderboard(): List<AdventureLeaderboardEntry> {
        return listOf(
            AdventureLeaderboardEntry(1, "std_005", "Emma Watson", "🦊", "Class 10", "Fluent English Castle", 28, 540, 1920),
            AdventureLeaderboardEntry(2, "std_001", "Alex Morgan", "🦉", "Class 10", "Speaking City", 22, 410, 1540, isCurrentUser = true),
            AdventureLeaderboardEntry(3, "std_003", "David Chen", "🐯", "Class 9", "Listening Forest", 18, 320, 1280),
            AdventureLeaderboardEntry(4, "std_002", "Priya Sharma", "🌸", "Class 10", "Grammar Kingdom", 14, 260, 980),
            AdventureLeaderboardEntry(5, "std_004", "Fatima Noor", "🐼", "Class 10", "Vocabulary Island", 9, 150, 620)
        )
    }
}
