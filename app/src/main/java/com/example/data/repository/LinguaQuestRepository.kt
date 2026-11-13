package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LinguaQuestRepository(private val appPreferences: AppPreferences? = null) {

    // Current logged-in user & role (restores from local preferences if present)
    private val _currentUser = MutableStateFlow(
        appPreferences?.getSavedUser() ?: User(
            id = "std_001",
            name = "Alex Morgan",
            email = "alex.morgan@linguaquest.edu",
            role = Role.STUDENT,
            avatarEmoji = "🦉",
            batch = AcademicClasses.DEFAULT_CLASS,
            securityPin = "1234"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Persistent Admin Session tracker (true if logged in as Admin or unlocked by Admin PIN)
    private val _isAdminSession = MutableStateFlow(
        appPreferences?.isAdminSession() ?: (_currentUser.value.role == Role.ADMIN)
    )
    val isAdminSession: StateFlow<Boolean> = _isAdminSession.asStateFlow()

    fun setAdminSession(isAdmin: Boolean) {
        _isAdminSession.value = isAdmin
        appPreferences?.setAdminSession(isAdmin)
    }

    companion object {
        fun getTodayDateString(): String {
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        }

        fun getYesterdayDateString(): String {
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(System.currentTimeMillis() - 86400000L))
        }

        fun calculateCefrLevel(overallScore: Int): LearningLevel {
            return when {
                overallScore >= 90 -> LearningLevel.ADVANCED          // C2
                overallScore >= 82 -> LearningLevel.UPPER_INTERMEDIATE// C1
                overallScore >= 72 -> LearningLevel.INTERMEDIATE      // B2
                overallScore >= 58 -> LearningLevel.PRE_INTERMEDIATE  // B1
                overallScore >= 45 -> LearningLevel.ELEMENTARY        // A2
                else -> LearningLevel.BEGINNER                         // A1
            }
        }
    }

    // Student Profile
    private val _studentProfile = MutableStateFlow(
        loadInitialProfile()
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private fun loadInitialProfile(): StudentProfile {
        val savedUser = appPreferences?.getSavedUser()
        if (savedUser != null && savedUser.role == Role.STUDENT) {
            val savedProfile = appPreferences.getStudentProfile(savedUser.id)
            if (savedProfile != null) return savedProfile
        }
        return StudentProfile(
            userId = "std_001",
            currentLevel = LearningLevel.INTERMEDIATE,
            streakDays = 7,
            xpPoints = 1450,
            coins = 420,
            league = "Gold League",
            overallScore = 85,
            speakingScore = 82,
            pronunciationScore = 85,
            fluencyScore = 79,
            grammarScore = 88,
            vocabularyScore = 86,
            listeningScore = 90,
            readingScore = 84,
            completedStagesCount = 14,
            speakingPracticeMinutes = 168,
            lastPracticeDate = getTodayDateString()
        )
    }

    // Learning Worlds
    private val _worlds = MutableStateFlow(
        listOf(
            World(1, "English Explorer", "Foundational speaking & daily greetings", 0xFF2563EB, "🧭", 5, 5, true),
            World(2, "Vocabulary Kingdom", "Rich descriptive words & everyday situations", 0xFF8B5CF6, "🏰", 5, 4, true),
            World(3, "Grammar Adventure", "Master past, present & future sentence structures", 0xFF10B981, "🗺️", 5, 3, true),
            World(4, "Pronunciation Planet", "Accent reduction, intonation & phonetics", 0xFFF59E0B, "🪐", 5, 2, true),
            World(5, "Speaking City", "Fluent discussions, debates & professional English", 0xFFFF5733, "🏙️", 5, 0, false),
            World(6, "Fluent Master", "Idioms, nuanced storytelling & native fluency", 0xFF06B6D4, "👑", 5, 0, false)
        )
    )
    val worlds: StateFlow<List<World>> = _worlds.asStateFlow()

    // Stages for World 1 & 2
    private val _stages = MutableStateFlow(
        listOf(
            LessonStage("w1_s1", 1, 1, "Warm-up Greetings", "Say hello & introduce yourself confidently", StageType.SPEAKING, 40, 10, 3, true, true),
            LessonStage("w1_s2", 1, 2, "Essential Words", "Top 20 daily vocabulary & phrases", StageType.VOCABULARY, 40, 10, 3, true, true),
            LessonStage("w1_s3", 1, 3, "Present Simple", "Describe daily habits and routines", StageType.GRAMMAR, 50, 15, 3, true, true),
            LessonStage("w1_s4", 1, 4, "Clear Vowels Drill", "Master short and long vowel sounds", StageType.PRONUNCIATION, 50, 15, 2, true, true),
            LessonStage("w1_s5", 1, 5, "Cafe Roleplay", "Order coffee and ask for the bill", StageType.SPEAKING, 60, 20, 3, true, true),

            LessonStage("w2_s1", 2, 1, "Emotions & Feelings", "Express mood with descriptive adjectives", StageType.VOCABULARY, 50, 15, 3, true, true),
            LessonStage("w2_s2", 2, 2, "Past Experiences", "Tell what you did yesterday", StageType.GRAMMAR, 50, 15, 3, true, true),
            LessonStage("w2_s3", 2, 3, "Th-Sound Mastery", "Conquer 'think', 'this', 'weather'", StageType.PRONUNCIATION, 55, 15, 2, true, true),
            LessonStage("w2_s4", 2, 4, "Airport Check-in", "Ask boarding questions & check luggage", StageType.SPEAKING, 60, 20, 0, true, false),
            LessonStage("w2_s5", 2, 5, "Vocabulary Boss Quiz", "Speed test your acquired vocabulary", StageType.CHALLENGE, 80, 30, 0, false, false),

            LessonStage("w3_s1", 3, 1, "Sentence Connectors", "Use 'although', 'however', 'moreover'", StageType.GRAMMAR, 60, 20, 2, true, true),
            LessonStage("w3_s2", 3, 2, "Story Builder", "Recount an adventure using past tenses", StageType.STORY, 65, 25, 0, true, false)
        )
    )
    val stages: StateFlow<List<LessonStage>> = _stages.asStateFlow()

    // Speaking Challenges & Conversation Topics
    private val defaultSpeakingPrompts = listOf(
        SpeakingPrompt(
            id = "topic_time_words",
            title = "Time Words (In, On, At)",
            category = "Grammar & Speaking",
            question = "When do you usually go to school?",
            followUpTip = "Practice using time prepositions: 'at 8 o'clock', 'in the morning', or 'on weekdays'.",
            recommendedKeywords = listOf("in the morning", "at 8:00 AM", "on weekdays", "usually", "schedule"),
            minSpeakingSeconds = 20,
            emoji = "🕒",
            difficultyLevel = "Beginner",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_hobbies",
            title = "Hobbies & Passions",
            category = "Personal Life",
            question = "Could you tell me about your favorite hobbies and why you enjoy spending time on them?",
            followUpTip = "Talk about how you got started, how often you do it, and how it makes you feel.",
            recommendedKeywords = listOf("relaxing", "creative", "passion", "weekend", "enjoyment"),
            minSpeakingSeconds = 25,
            emoji = "💖",
            difficultyLevel = "Beginner",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_career",
            title = "Career & Ambitions",
            category = "Professional",
            question = "What kind of job or profession do you do, and what do you like most about your work or studies?",
            followUpTip = "Mention your goals, skills you are developing, and your dream project.",
            recommendedKeywords = listOf("profession", "ambition", "collaborate", "milestone", "career"),
            minSpeakingSeconds = 30,
            emoji = "💼",
            difficultyLevel = "Intermediate",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_travel",
            title = "Dream Travel & Vacation",
            category = "Travel & Culture",
            question = "If you could travel anywhere in the world tomorrow with all expenses paid, where would you go and why?",
            followUpTip = "Describe the sights you would visit, local foods you want to try, and who you would take along.",
            recommendedKeywords = listOf("destination", "breathtaking", "culture", "explore", "scenery"),
            minSpeakingSeconds = 25,
            emoji = "✈️",
            difficultyLevel = "Beginner",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_food",
            title = "Favorite Cuisine & Dining",
            category = "Food & Culture",
            question = "What is your all-time favorite meal, and how is it traditionally prepared in your home or culture?",
            followUpTip = "Use descriptive sensory words like savory, aromatic, crispy, flavorful, or seasoned.",
            recommendedKeywords = listOf("ingredients", "aromatic", "flavorful", "traditional", "recipe"),
            minSpeakingSeconds = 25,
            emoji = "🍕",
            difficultyLevel = "Beginner",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_ai_tech",
            title = "AI & Future of Technology",
            category = "Technology",
            question = "How do you think Artificial Intelligence and modern smartphones will change our daily lives in the next five years?",
            followUpTip = "Give specific examples: education, transportation, communication, or creative hobbies.",
            recommendedKeywords = listOf("artificial intelligence", "innovation", "automation", "future", "convenient"),
            minSpeakingSeconds = 35,
            emoji = "🤖",
            difficultyLevel = "Advanced",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_movies",
            title = "Movies & Storytelling",
            category = "Entertainment",
            question = "Tell me about a memorable movie or book that left a strong impression on you. What was the core message?",
            followUpTip = "Explain the plot briefly, highlight the main character's dilemma, and share your takeaway.",
            recommendedKeywords = listOf("character", "inspiring", "plot twist", "cinematography", "emotion"),
            minSpeakingSeconds = 30,
            emoji = "🎬",
            difficultyLevel = "Intermediate",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_routine",
            title = "Daily Routine & Productivity",
            category = "Daily Habits",
            question = "Walk me through your ideal morning and evening routine. How do you stay organized and motivated?",
            followUpTip = "Use time transitions: 'First thing in the morning', 'Following that', 'Towards the evening'.",
            recommendedKeywords = listOf("routine", "habits", "productive", "discipline", "schedule"),
            minSpeakingSeconds = 25,
            emoji = "⏰",
            difficultyLevel = "Beginner",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_festivals",
            title = "Festivals & Celebrations",
            category = "Culture",
            question = "Describe the most colorful holiday or festival celebrated in your region. How do families come together?",
            followUpTip = "Describe the music, traditional clothing, special dishes, and festive decorations.",
            recommendedKeywords = listOf("celebration", "gathering", "tradition", "fireworks", "joyful"),
            minSpeakingSeconds = 30,
            emoji = "🎆",
            difficultyLevel = "Intermediate",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_nature",
            title = "Nature & Environment",
            category = "Environment",
            question = "What simple everyday habits can people adopt to help protect the environment and keep our cities clean?",
            followUpTip = "Discuss recycling, saving electricity, walking more, or reducing plastic waste.",
            recommendedKeywords = listOf("sustainability", "conservation", "eco-friendly", "renewable", "protect"),
            minSpeakingSeconds = 30,
            emoji = "🌿",
            difficultyLevel = "Intermediate",
            createdBy = "Faculty"
        ),
        SpeakingPrompt(
            id = "topic_challenges",
            title = "Overcoming Obstacles",
            category = "Personal Growth",
            question = "Can you share a time when you faced a difficult challenge and how you managed to overcome it?",
            followUpTip = "Reflect on what you learned from the experience and what advice you would give others.",
            recommendedKeywords = listOf("perseverance", "resilience", "overcome", "valuable lesson", "growth"),
            minSpeakingSeconds = 35,
            emoji = "💡",
            difficultyLevel = "Advanced",
            createdBy = "Faculty"
        )
    )
    private val _speakingPrompts = MutableStateFlow(
        run {
            val saved = appPreferences?.getSpeakingPrompts()
            if (!saved.isNullOrEmpty()) {
                if (saved.none { it.id == "topic_time_words" }) {
                    listOf(defaultSpeakingPrompts.first()) + saved
                } else saved
            } else defaultSpeakingPrompts
        }
    )
    val speakingPrompts: StateFlow<List<SpeakingPrompt>> = _speakingPrompts.asStateFlow()

    // Roleplay Scenarios
    private val _roleplays = MutableStateFlow(
        listOf(
            RoleplayScenario(
                id = "rp_cafe",
                title = "Ordering at a Trendy London Café",
                situation = "You just walked into a busy café in Covent Garden.",
                partnerRole = "Barista (Liam)",
                userRole = "Customer",
                difficulty = "Beginner",
                emoji = "☕",
                turns = listOf(
                    DialogueTurn("AI", "Hi there! Welcome to The Daily Roast. What can I get started for you today?"),
                    DialogueTurn("USER", "Hi! I would like a flat white with oat milk, please.", listOf("flat white", "oat milk")),
                    DialogueTurn("AI", "Sure thing! Would you like a small or large, and can I get you any pastries with that?"),
                    DialogueTurn("USER", "A large, please. And could I also have a chocolate croissant?", listOf("large", "croissant")),
                    DialogueTurn("AI", "Perfect! That'll be six pounds fifty. Are you paying by card?"),
                    DialogueTurn("USER", "Yes, contactless card please. Here you go.", listOf("card", "contactless"))
                )
            ),
            RoleplayScenario(
                id = "rp_interview",
                title = "Software Engineer Job Interview",
                situation = "You are interviewing with a senior hiring manager at an international tech firm.",
                partnerRole = "Hiring Director (Sarah)",
                userRole = "Job Candidate",
                difficulty = "Intermediate",
                emoji = "💼",
                turns = listOf(
                    DialogueTurn("AI", "Welcome Alex! Thanks for taking the time today. Could you tell me about your background?"),
                    DialogueTurn("USER", "Thank you Sarah. I have been developing applications for three years, focusing on clean architecture and user experience.", listOf("developing", "architecture")),
                    DialogueTurn("AI", "Impressive. Can you describe a challenging project you solved recently?"),
                    DialogueTurn("USER", "We had a latency issue with our data sync. I redesigned our caching pipeline, which cut load times by 40 percent.", listOf("caching", "pipeline", "latency")),
                    DialogueTurn("AI", "That's fantastic initiative! Where do you see yourself contributing most in our team?"),
                    DialogueTurn("USER", "I want to collaborate closely with product teams to build fast, intuitive mobile experiences.", listOf("collaborate", "intuitive"))
                )
            ),
            RoleplayScenario(
                id = "rp_hotel",
                title = "Checking In at a Boutique Hotel",
                situation = "You have arrived at the reception desk in New York City.",
                partnerRole = "Front Desk Concierge (David)",
                userRole = "Guest",
                difficulty = "Elementary",
                emoji = "🏨",
                turns = listOf(
                    DialogueTurn("AI", "Good afternoon! Welcome to the Grand Central Hotel. Do you have a reservation?"),
                    DialogueTurn("USER", "Good afternoon! Yes, I booked a deluxe room under the name Alex Morgan.", listOf("reservation", "booked")),
                    DialogueTurn("AI", "Found it right here! Room 402 on the fourth floor with a skyline view. Breakfast is served from 7 to 10 AM."),
                    DialogueTurn("USER", "Wonderful, thank you. Could you let me know the Wi-Fi password?", listOf("password", "wifi"))
                )
            )
        )
    )
    val roleplays: StateFlow<List<RoleplayScenario>> = _roleplays.asStateFlow()

    // English Adventure Worlds & Progress
    private val _adventureWorlds = MutableStateFlow(
        run {
            val saved = appPreferences?.getAdventureWorlds()
            val defaults = AdventureDataDefaults.createDefaultWorlds()
            if (!saved.isNullOrEmpty()) {
                val defaultVocab = defaults.find { it.id == "world_vocab" } ?: defaults.first()
                val savedVocab = saved.find { it.id == "world_vocab" } ?: defaultVocab

                // Gather default 5 levels
                val defaultIds = defaultVocab.levels.map { it.id }.toSet()
                val existingSavedVocabLevels = savedVocab.levels

                // Levels in savedVocab that match defaults (preserving completed status)
                val defaultLevelsInVocab = defaultVocab.levels.map { defLvl ->
                    existingSavedVocabLevels.find { it.id == defLvl.id } ?: defLvl
                }

                // Custom teacher-added levels already in savedVocab
                val customLevelsInVocab = existingSavedVocabLevels.filter { it.id !in defaultIds }

                // Any custom levels added in other worlds (e.g. from user testing in other realms)
                val customLevelsFromOtherWorlds = saved.filter { it.id != "world_vocab" }
                    .flatMap { it.levels }
                    .filter { lvl -> lvl.id !in defaultIds && existingSavedVocabLevels.none { it.id == lvl.id } }

                // Combine: Default 1..5 FIRST, followed by all teacher-added games BELOW THEM!
                val allCombinedLevels = (defaultLevelsInVocab + customLevelsInVocab + customLevelsFromOtherWorlds)
                    .mapIndexed { idx, lvl ->
                        lvl.copy(levelNumber = idx + 1, worldId = "world_vocab")
                    }

                val mergedVocabWorld = savedVocab.copy(levels = allCombinedLevels)

                listOf(mergedVocabWorld) + saved.filter { it.id != "world_vocab" }
            } else {
                defaults
            }
        }
    )
    val adventureWorlds: StateFlow<List<AdventureWorld>> = _adventureWorlds.asStateFlow()

    private val _adventureProgress = MutableStateFlow(
        run {
            val saved = appPreferences?.getAdventureProgress()
            saved ?: AdventureProgress(
                studentId = "std_001",
                completedLevelIds = emptySet(),
                completedMissionIds = emptySet(),
                unlockedWorldIds = setOf("world_vocab"),
                levelStars = emptyMap(),
                totalStars = 0,
                totalCoins = 0,
                totalXp = 0
            )
        }
    )
    val adventureProgress: StateFlow<AdventureProgress> = _adventureProgress.asStateFlow()

    private val _adventureBadges = MutableStateFlow(AdventureDataDefaults.createDefaultBadges())
    val adventureBadges: StateFlow<List<AdventureBadge>> = _adventureBadges.asStateFlow()

    private val _adventureLeaderboard = MutableStateFlow(AdventureDataDefaults.createDefaultLeaderboard())
    val adventureLeaderboard: StateFlow<List<AdventureLeaderboardEntry>> = _adventureLeaderboard.asStateFlow()

    // Pronunciation Drills
    private val defaultPronunciationDrills = listOf(
        PronunciationDrill("pd1", "Comfortable", "/ˈkʌmftəbl/", "Silent 'or'", "Medium", "Native speakers drop the middle syllables: say 'KUMF-tuh-bl'."),
        PronunciationDrill("pd2", "Specifically", "/spəˈsɪfɪkli/", "Consonant cluster", "Hard", "Start with a crisp 'spuh', stress 'SIF', end quickly with 'ik-lee'."),
        PronunciationDrill("pd3", "Thoughtful", "/θɔːtful/", "Soft 'th' sound", "Medium", "Place tongue gently between teeth with breath for the 'th' sound."),
        PronunciationDrill("pd4", "Phenomenon", "/fəˈnɒmɪnən/", "Vowel reduction", "Hard", "Stress the second syllable: 'fuh-NAH-muh-non'."),
        PronunciationDrill("pd5", "Entrepreneur", "/ˌɒntrəprəˈnɜːr/", "Stress placement", "Hard", "Stress the final syllable: 'on-truh-pruh-NUR'."),
        PronunciationDrill("pd6", "World", "/wɜːld/", "r + l blend", "Hard", "Curl your tongue slightly for 'er', then glide smoothly to 'ld'."),
        PronunciationDrill("pd7", "Schedule", "/ˈskedʒuːl/", "Soft 'dge'", "Easy", "In American English, it starts with 'sked' like 'skeleton'.")
    )
    private val _pronunciationDrills = MutableStateFlow(
        run {
            val saved = appPreferences?.getPronunciationDrills()
            if (!saved.isNullOrEmpty()) saved else defaultPronunciationDrills
        }
    )
    val pronunciationDrills: StateFlow<List<PronunciationDrill>> = _pronunciationDrills.asStateFlow()

    // Vocabulary Words
    val defaultVocabularyWords = listOf(
        VocabularyWord(
            id = "v1",
            word = "Eloquence",
            phonetic = "/ˈel.ə.kwəns/",
            partOfSpeech = "noun",
            meaning = "Fluent or persuasive speaking or writing.",
            exampleSentence = "Her natural eloquence held the entire audience spellbound.",
            category = "Communication",
            synonyms = listOf("fluency", "articulateness"),
            isFavorite = true,
            assameseMeaning = "বাগ্মীতা / স্পষ্ট আৰু সাৱলীল কথা কোৱাৰ ক্ষমতা",
            assameseTransliteration = "Bagmita / Sabolil kotha kowar khomota",
            syllables = "El • o • quence",
            coachingTip = "Emphasize the first syllable 'EL' with open vocal delivery.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v2",
            word = "Resilience",
            phonetic = "/rɪˈzɪl.jəns/",
            partOfSpeech = "noun",
            meaning = "The capacity to recover quickly from difficulties.",
            exampleSentence = "Courage and resilience are key to learning any language.",
            category = "Mindset",
            synonyms = listOf("tenacity", "strength"),
            assameseMeaning = "স্থিতিস্থাপকতা / টান পৰিস্থিতিত হাৰ নমনা সাহস",
            assameseTransliteration = "Sthitisthapakata / Sahakh",
            syllables = "Re • sil • ience",
            coachingTip = "Stress the second syllable 'ZIL' with a vibrating /z/ sound.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v3",
            word = "Collaborate",
            phonetic = "/kəˈlæb.ə.reɪt/",
            partOfSpeech = "verb",
            meaning = "To work jointly on an activity or project.",
            exampleSentence = "Students love to collaborate on speaking roleplays.",
            category = "Workplace",
            synonyms = listOf("cooperate", "team up"),
            isFavorite = true,
            assameseMeaning = "সহযোগিতা কৰা / একেলগে কাম কৰা",
            assameseTransliteration = "Hohujogita kora / Ekeloge kam kora",
            syllables = "Col • lab • o • rate",
            coachingTip = "Link 'col' and 'lab' smoothly without pausing in between.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v4",
            word = "Pragmatic",
            phonetic = "/præɡˈmæt.ɪk/",
            partOfSpeech = "adjective",
            meaning = "Dealing with things sensibly and realistically.",
            exampleSentence = "A pragmatic approach to speaking is to prioritize being understood over perfection.",
            category = "Academic",
            synonyms = listOf("practical", "sensible"),
            assameseMeaning = "বাস্তৱবাদী / ব্যৱহাৰিক",
            assameseTransliteration = "Bastob-badi / Byoboharik",
            syllables = "Prag • mat • ic",
            coachingTip = "Pronounce the hard /g/ clearly before transitioning to /m/.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v5",
            word = "Articulate",
            phonetic = "/ɑːˈtɪk.jə.lət/",
            partOfSpeech = "adjective",
            meaning = "Having or showing the ability to speak fluently and coherently.",
            exampleSentence = "She gave an articulate summary of the proposal.",
            category = "Communication",
            synonyms = listOf("expressive", "lucid"),
            assameseMeaning = "স্পষ্টভাষী / সাৱলীলভাৱে প্ৰকাশ কৰিব পৰা",
            assameseTransliteration = "Spashtobhasi / Sabolil prokash",
            syllables = "Ar • tic • u • late",
            coachingTip = "Crisp consonants at syllable borders give this word its punch.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v6",
            word = "Fostering",
            phonetic = "/ˈfɒs.tər.ɪŋ/",
            partOfSpeech = "verb",
            meaning = "Encouraging the development or growth of ideas or skills.",
            exampleSentence = "Daily speaking practice is essential for fostering fluency.",
            category = "Growth",
            synonyms = listOf("nurturing", "cultivating"),
            assameseMeaning = "উৎসাদন / বিকাশ বা বৃদ্ধিত সহায় কৰা",
            assameseTransliteration = "Bikash ba bridhit xohai kora",
            syllables = "Fos • ter • ing",
            coachingTip = "Keep the vowel in 'Fos' short and open.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v7",
            word = "Metaphor",
            phonetic = "/ˈmet.ə.fɔːr/",
            partOfSpeech = "noun",
            meaning = "A figure of speech in which a word or phrase is applied to an object or action.",
            exampleSentence = "Language learning is often described with the metaphor of a mountain climb.",
            category = "Literature",
            synonyms = listOf("analogy", "symbol"),
            assameseMeaning = "ৰূপক / উপমা",
            assameseTransliteration = "Rupok / Upoma",
            syllables = "Met • a • phor",
            coachingTip = "Pronounce 'ph' as a soft /f/ sound, ending in /ɔːr/.",
            addedByTeacher = "Faculty Instructor"
        ),
        VocabularyWord(
            id = "v8",
            word = "Serendipity",
            phonetic = "/ˌser.ənˈdɪp.ə.ti/",
            partOfSpeech = "noun",
            meaning = "The occurrence of events by chance in a happy or beneficial way.",
            exampleSentence = "Meeting an English conversational partner at the train station was pure serendipity.",
            category = "General",
            synonyms = listOf("chance", "fluke"),
            assameseMeaning = "আকস্মিক সৌভাগ্য / হঠাৎ ঘটা সুখকৰ ঘটনা",
            assameseTransliteration = "Akosmik soubhagya / Hothat ghota xukh",
            syllables = "Ser • en • dip • i • ty",
            coachingTip = "Primary accent on 'DIP'. Rhythm: ser-en-DIP-i-ty.",
            addedByTeacher = "Faculty Instructor"
        )
    )

    private val _vocabularyWords = MutableStateFlow<List<VocabularyWord>>(
        run {
            val saved = appPreferences?.getVocabularyWords()
            if (saved != null && saved.isNotEmpty()) {
                saved
            } else {
                appPreferences?.saveVocabularyWords(defaultVocabularyWords)
                defaultVocabularyWords
            }
        }
    )
    val vocabularyWords: StateFlow<List<VocabularyWord>> = _vocabularyWords.asStateFlow()

    // Grammar Topics
    private val _grammarTopics = MutableStateFlow(
        listOf(
            GrammarTopic(
                id = "gt1",
                title = "Present Simple vs Continuous",
                level = LearningLevel.BEGINNER,
                category = "Tenses",
                explanation = "Use Present Simple for general facts and daily habits. Use Present Continuous for actions happening right at this very moment or temporary situations.",
                rules = listOf(
                    "Habits: I drink tea every morning (Simple)",
                    "Now: I am drinking tea right now (Continuous)",
                    "Stative verbs (like 'know', 'believe', 'want') rarely take continuous form: say 'I understand', not 'I am understanding'."
                ),
                exampleSentences = listOf(
                    "I live in Boston." to "I am living in Boston for two months.",
                    "He speaks three languages." to "He is speaking on the phone right now."
                ),
                questions = listOf(
                    QuizQuestion("q1", "Listen! The birds ___ outside in the garden.", listOf("sing", "are singing", "is singing", "sang"), 1, "Because it is happening right now ('Listen!')."),
                    QuizQuestion("q2", "My brother usually ___ to work by bicycle.", listOf("goes", "is going", "go", "gone"), 0, "Use present simple for habitual actions ('usually').")
                )
            ),
            GrammarTopic(
                id = "gt2",
                title = "Past Simple vs Present Perfect",
                level = LearningLevel.PRE_INTERMEDIATE,
                category = "Tenses",
                explanation = "Past Simple refers to actions finished at a specific stated past time. Present Perfect connects past actions to the present, or mentions unspecified past experiences.",
                rules = listOf(
                    "Specific past time: 'I visited Paris in 2022' (Past Simple)",
                    "Life experience (any time until now): 'I have visited Paris three times' (Present Perfect)",
                    "Never use Present Perfect with specific past time indicators like 'yesterday', 'last night', 'in 1999'."
                ),
                exampleSentences = listOf(
                    "I saw that movie yesterday." to "I have already seen that movie.",
                    "She graduated two years ago." to "She has recently graduated."
                ),
                questions = listOf(
                    QuizQuestion("q3", "I ___ my keys! I cannot find them anywhere right now.", listOf("lost", "have lost", "was losing", "had lost"), 1, "The result affects the present moment, so use Present Perfect."),
                    QuizQuestion("q4", "We ___ our final exams last Friday.", listOf("finished", "have finished", "are finishing", "finish"), 0, "Because of the specific past time 'last Friday'.")
                )
            ),
            GrammarTopic(
                id = "gt3",
                title = "Prepositions of Time & Place",
                level = LearningLevel.ELEMENTARY,
                category = "Prepositions",
                explanation = "Master IN, ON, and AT for both temporal and spatial clarity.",
                rules = listOf(
                    "AT: specific points in time or location (at 5 PM, at the station)",
                    "ON: days, dates, and surfaces (on Monday, on May 4th, on the table)",
                    "IN: months, years, seasons, enclosed spaces (in July, in 2026, in the room)"
                ),
                exampleSentences = listOf(
                    "Our speaking session begins at 3 PM." to "Don't say 'in 3 PM'.",
                    "We meet on Mondays." to "Don't say 'at Mondays'."
                ),
                questions = listOf(
                    QuizQuestion("q5", "The annual English conference starts ___ October 12th.", listOf("at", "on", "in", "by"), 1, "Use 'on' for specific calendar dates.")
                )
            )
        )
    )
    val grammarTopics: StateFlow<List<GrammarTopic>> = _grammarTopics.asStateFlow()

    // Teacher Assignments
    private val defaultAssignments = listOf(
        Assignment("asg_1", "3-Minute Impromptu Speech: Climate Action", "Prof. Eleanor Vance", "Class 10", "Tomorrow, 6 PM", "Record a 2-3 minute audio response explaining two small eco-habits you practice.", "Speaking", 100, 20, 24),
        Assignment("asg_2", "Business Pitch Presentation Dialogue", "Prof. Eleanor Vance", "All Classes", "Friday, 11:59 PM", "Present your imaginary startup product in clear, persuasive English.", "Presentation", 100, 14, 24),
        Assignment("asg_3", "Pronunciation Drill: Minimal Pairs (P vs B)", "Dr. Marcus Reed", "Class 9", "In 3 Days", "Record 10 sample sentences focusing on explosive /p/ and voiced /b/ sounds.", "Pronunciation", 50, 18, 20),
        Assignment("asg_4", "Everyday English: Ordering at a Restaurant", "Prof. Eleanor Vance", "Class 8", "In 4 Days", "Practice ordering a three-course dinner and asking for the bill politely.", "Speaking", 60, 12, 22)
    )

    private fun loadInitialAssignments(): List<Assignment> {
        val saved = appPreferences?.getAssignments()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultAssignments.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveAssignments(defaultAssignments)
        return defaultAssignments
    }

    private val _assignments = MutableStateFlow(loadInitialAssignments())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    fun addAssignment(
        title: String,
        instructions: String,
        targetClass: String,
        dueDate: String,
        teacherName: String,
        topicType: String = "Speaking"
    ): Assignment {
        val newAsg = Assignment(
            id = "asg_${System.currentTimeMillis()}",
            title = title.trim(),
            teacherName = teacherName.ifBlank { "Faculty Instructor" },
            batch = targetClass.trim().ifBlank { AcademicClasses.DEFAULT_CLASS },
            dueDate = dueDate.ifBlank { "In 2 Days" },
            instructions = instructions.trim(),
            topicType = topicType,
            maxPoints = 100,
            submissionsCount = 0,
            totalAssigned = 25
        )
        _assignments.value = listOf(newAsg) + _assignments.value
        appPreferences?.saveAssignments(_assignments.value)
        return newAsg
    }

    // --- MCQ ONLINE EXAMS & CERTIFICATES ---
    private val defaultExams = listOf(
            Exam(
                id = "exm_grammar_8",
                title = "English Grammar Test",
                description = "Official Spoken English Learning & Academic Portal comprehensive grammar examination covering tenses, voice, speech, vocabulary, and syntax.",
                teacherName = "Prof. Eleanor Vance",
                targetClass = "Class 8",
                subjectCategory = "English",
                durationMinutes = 20,
                totalMarks = 100,
                passingPercentage = 60,
                createdAt = "27 Nov 2026",
                dueDate = "Completed",
                isPublished = true,
                questions = listOf(
                    ExamQuestion(
                        id = "qg8_1",
                        question = "Choose the correct sentence in Present Perfect Continuous Tense:",
                        options = listOf(
                            "She is reading a novel since morning.",
                            "She has been reading a novel for three hours.",
                            "She had been reading the novel yesterday.",
                            "She will be reading a novel."
                        ),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "Present perfect continuous tense uses 'has/have been + verb-ing' to express continuous action up to present."
                    ),
                    ExamQuestion(
                        id = "qg8_2",
                        question = "Which word correctly completes: 'The principal congratulated Fariha ______ her exceptional speech.'?",
                        options = listOf("for", "on", "at", "with"),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "The standard preposition with congratulate is 'on'."
                    ),
                    ExamQuestion(
                        id = "qg8_3",
                        question = "Convert to Passive: 'The teacher praised the students for their high score.'",
                        options = listOf(
                            "The students were praised by the teacher for their high score.",
                            "The students had been praised by the teacher.",
                            "The teacher was praising the students.",
                            "The students are praised by teacher."
                        ),
                        correctOptionIndex = 0,
                        marks = 10,
                        explanation = "Past simple active becomes 'were praised' in passive voice."
                    ),
                    ExamQuestion(
                        id = "qg8_4",
                        question = "Select the Complex sentence:",
                        options = listOf(
                            "The bell rang and students entered.",
                            "Although it rained heavily, she achieved first place in the debate.",
                            "He likes literature, but his sister prefers math.",
                            "The morning sunlight was warm and pleasant."
                        ),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "Subordinating conjunction 'Although' introduces a dependent clause."
                    ),
                    ExamQuestion(
                        id = "qg8_5",
                        question = "Choose the correct conditional sentence (Type 2):",
                        options = listOf(
                            "If I have time, I would travel.",
                            "If I had more time, I would travel across the world.",
                            "If I would have time, I will travel.",
                            "If I had time, I travel."
                        ),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "Type 2 conditional takes past simple in if-clause and would + base verb in main clause."
                    ),
                    ExamQuestion(
                        id = "qg8_6",
                        question = "Identify the antonym of 'Meticulous':",
                        options = listOf("Careful", "Careless", "Thorough", "Precise"),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "'Careless' is the direct opposite of 'Meticulous'."
                    ),
                    ExamQuestion(
                        id = "qg8_7",
                        question = "Choose the correct indirect speech for: He said, 'I am studying English.'",
                        options = listOf(
                            "He said that he was studying English.",
                            "He said that he is studying English.",
                            "He said that I was studying English.",
                            "He told that he studies English."
                        ),
                        correctOptionIndex = 0,
                        marks = 10,
                        explanation = "Present continuous shifts back to past continuous in indirect reported speech."
                    ),
                    ExamQuestion(
                        id = "qg8_8",
                        question = "Which word is an Adverb in: 'She spoke quite eloquently at the academic seminar.'?",
                        options = listOf("spoke", "quite", "eloquently", "Both 'quite' and 'eloquently'"),
                        correctOptionIndex = 3,
                        marks = 10,
                        explanation = "'Eloquently' is an adverb of manner and 'quite' is an adverb of degree modifying it."
                    ),
                    ExamQuestion(
                        id = "qg8_9",
                        question = "Select the sentence with correct subject-verb agreement:",
                        options = listOf(
                            "Neither the teacher nor the students was present.",
                            "Neither the teacher nor the students were present.",
                            "Either the book or the pens is on table.",
                            "The group of students are waiting."
                        ),
                        correctOptionIndex = 1,
                        marks = 10,
                        explanation = "With 'neither... nor', the verb agrees with the closer subject ('students')."
                    ),
                    ExamQuestion(
                        id = "qg8_10",
                        question = "Identify the idiom meaning 'to face a difficult situation with courage':",
                        options = listOf("Bite the bullet", "Break the ice", "Spill the beans", "Burn the bridge"),
                        correctOptionIndex = 0,
                        marks = 10,
                        explanation = "'Bite the bullet' means to bravely endure a difficult situation."
                    )
                )
            ),
            Exam(
                id = "exm_1",
                title = "Class 10 Comprehensive English Grammar & Tenses Test",
                description = "Assessment covering Present & Past tenses, active/passive voice, complex clauses, prepositions, and reported speech.",
                teacherName = "Prof. Eleanor Vance",
                targetClass = "Class 10",
                subjectCategory = "English Grammar",
                durationMinutes = 15,
                totalMarks = 50,
                passingPercentage = 60,
                createdAt = "Nov 2026",
                dueDate = "Tomorrow, 6 PM",
                isPublished = true,
                questions = listOf(
                    ExamQuestion(
                        id = "q1_1",
                        question = "Identify the sentence written in the Present Perfect Continuous Tense:",
                        options = listOf(
                            "She is reading a novel since early morning.",
                            "She has been reading a novel for three hours.",
                            "She had been reading the novel yesterday.",
                            "She will have read the novel by tomorrow."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "Present perfect continuous tense uses 'has/have + been + verb-ing' to denote an action starting in the past and continuing into the present."
                    ),
                    ExamQuestion(
                        id = "q1_2",
                        question = "Choose the appropriate preposition: 'The principal congratulated Maya ______ winning the public speaking trophy.'",
                        options = listOf("for", "at", "on", "about"),
                        correctOptionIndex = 2,
                        marks = 5,
                        explanation = "The verb 'congratulate' takes the preposition 'on' (congratulate someone ON an achievement)."
                    ),
                    ExamQuestion(
                        id = "q1_3",
                        question = "Which of the following sentences is a Complex Sentence?",
                        options = listOf(
                            "The bell rang and the students entered the classroom.",
                            "Although it was pouring rain, the debate team arrived on schedule.",
                            "He likes reading classic literature, but his brother prefers poetry.",
                            "The early morning breeze felt refreshing along the riverbank."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "A complex sentence contains an independent clause and at least one dependent clause introduced by a subordinating conjunction like 'Although'."
                    ),
                    ExamQuestion(
                        id = "q1_4",
                        question = "Transform to Passive Voice: 'The headmaster delivered an inspiring lecture on integrity.'",
                        options = listOf(
                            "An inspiring lecture on integrity was delivered by the headmaster.",
                            "An inspiring lecture on integrity had been delivered by the headmaster.",
                            "The headmaster was delivering an inspiring lecture.",
                            "An inspiring lecture had delivered by the headmaster."
                        ),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "Past simple active ('delivered') becomes 'was delivered' in passive voice with past participle."
                    ),
                    ExamQuestion(
                        id = "q1_5",
                        question = "Choose the grammatically sound Type 2 conditional sentence:",
                        options = listOf(
                            "If I would have more time, I will travel across Europe.",
                            "If I had more time, I would travel across Europe.",
                            "If I have more time, I would have traveled across Europe.",
                            "If I will have time, I would travel across Europe."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "Second conditional (hypothetical present) follows the pattern: 'If + past simple, ... would + base verb'."
                    ),
                    ExamQuestion(
                        id = "q1_6",
                        question = "Select the antonym for the vocabulary word 'METICULOUS':",
                        options = listOf("Scrupulous", "Careless", "Diligent", "Exacting"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "Meticulous means showing great attention to detail. Its opposite is 'Careless'."
                    ),
                    ExamQuestion(
                        id = "q1_7",
                        question = "Select the correct modal: 'You ______ talk while the examination is in progress.'",
                        options = listOf("might not", "must not", "needn't", "would not"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Must not' expresses explicit institutional prohibition and strict rule obligation."
                    ),
                    ExamQuestion(
                        id = "q1_8",
                        question = "Identify the correctly punctuated sentence:",
                        options = listOf(
                            "Its time to submit you're completed assignment.",
                            "It's time to submit your completed assignment.",
                            "Its time to submit your completed assignment.",
                            "It's time to submit you're completed assignment."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'It's' is the contraction for 'It is', and 'your' is the possessive adjective."
                    ),
                    ExamQuestion(
                        id = "q1_9",
                        question = "Identify the figure of speech: 'The ancient oak tree stood like a silent guardian of the village.'",
                        options = listOf("Metaphor", "Simile", "Personification", "Hyperbole"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "A comparison between two different objects using the comparative word 'like' or 'as' is a Simile."
                    ),
                    ExamQuestion(
                        id = "q1_10",
                        question = "Change to Indirect Speech: He said, 'I have lived in London for five years.'",
                        options = listOf(
                            "He said that he has lived in London for five years.",
                            "He said that he had lived in London for five years.",
                            "He said that he lived in London for five years.",
                            "He asked if he had lived in London for five years."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "Present perfect ('have lived') shifts back to past perfect ('had lived') in indirect speech."
                    )
                )
            ),
            Exam(
                id = "exm_2",
                title = "Phonetics & Pronunciation IPA Symbol Mastery",
                description = "Interactive test on English phonemes, silent letters, diphthongs, and syllable stress rules.",
                teacherName = "Dr. Marcus Reed",
                targetClass = "All Classes",
                subjectCategory = "Phonetics & Pronunciation",
                durationMinutes = 10,
                totalMarks = 40,
                passingPercentage = 60,
                createdAt = "Nov 2026",
                dueDate = "Friday, 11:59 PM",
                isPublished = true,
                questions = listOf(
                    ExamQuestion(
                        id = "q2_1",
                        question = "Which sound does the IPA symbol /θ/ represent?",
                        options = listOf(
                            "The voiced 'th' in 'this' and 'feather'",
                            "The voiceless 'th' in 'think' and 'theater'",
                            "The 'sh' sound in 'ship'",
                            "The 'ch' sound in 'chair'"
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "/θ/ (theta) is the voiceless dental fricative found in words like 'think', 'bath', and 'theater'."
                    ),
                    ExamQuestion(
                        id = "q2_2",
                        question = "Which of the following words contains a silent letter 'b'?",
                        options = listOf("Marble", "Doubt", "Cabbage", "Timber"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "In 'Doubt' (and 'debt', 'subtle'), the letter 'b' is completely silent."
                    ),
                    ExamQuestion(
                        id = "q2_3",
                        question = "On which syllable does primary stress fall in the noun 'PHOTOGRAPH'?",
                        options = listOf("First syllable (PHO-to-graph)", "Second syllable (pho-TO-graph)", "Third syllable (pho-to-GRAPH)", "Equal stress"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "The noun 'photograph' has primary stress on the first syllable (/ˈfoʊ.tə.ɡræf/)."
                    ),
                    ExamQuestion(
                        id = "q2_4",
                        question = "Which word contains the diphthong /aɪ/?",
                        options = listOf("Day", "Sky", "Boy", "Now"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Sky' /skaɪ/ contains the closing diphthong /aɪ/."
                    ),
                    ExamQuestion(
                        id = "q2_5",
                        question = "What is the schwa sound /ə/ primarily known for in English?",
                        options = listOf(
                            "The most common unstressed, neutral vowel in English",
                            "A sharp explosive consonant",
                            "A long stressed back vowel",
                            "A nasal consonant"
                        ),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "Schwa /ə/ is the most frequently spoken vowel in English, appearing only in unstressed syllables (e.g. 'about', 'banana')."
                    ),
                    ExamQuestion(
                        id = "q2_6",
                        question = "Which word has a voiced 's' pronounced as /z/?",
                        options = listOf("Cats", "Music", "Bus", "Pass"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "In 'Music' (/ˈmjuː.zɪk/), the 's' is voiced as a /z/ sound."
                    ),
                    ExamQuestion(
                        id = "q2_7",
                        question = "In the word 'KNIGHT', which letters are silent?",
                        options = listOf("k only", "k and gh", "gh only", "t only"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "In 'knight' (/naɪt/), the initial 'k' and the digraph 'gh' are both silent."
                    ),
                    ExamQuestion(
                        id = "q2_8",
                        question = "Which minimal pair contrasts the short /ɪ/ and long /iː/ vowel sounds?",
                        options = listOf("Ship vs Sheep", "Bat vs Bet", "Cat vs Cut", "Hot vs Hat"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "'Ship' contains short lax /ɪ/ while 'Sheep' contains long tense /iː/."
                    )
                )
            ),
            Exam(
                id = "exm_3",
                title = "Advanced English Vocabulary, Idioms & Collocations",
                description = "Test your grasp on high-frequency idioms, academic vocabulary, and natural collocations.",
                teacherName = "Prof. Eleanor Vance",
                targetClass = "Class 10",
                subjectCategory = "Vocabulary & Idioms",
                durationMinutes = 20,
                totalMarks = 50,
                passingPercentage = 60,
                createdAt = "Nov 2026",
                dueDate = "In 3 Days",
                isPublished = true,
                questions = listOf(
                    ExamQuestion(
                        id = "q3_1",
                        question = "What is the meaning of the idiom 'To bite the bullet'?",
                        options = listOf(
                            "To engage in a physical fight",
                            "To face a difficult or unpleasant situation with courage",
                            "To make a hasty, impulsive decision",
                            "To waste valuable time on trivial matters"
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'To bite the bullet' means to bravely accept an unpleasant or unavoidable grim reality."
                    ),
                    ExamQuestion(
                        id = "q3_2",
                        question = "Select the natural English collocation: 'The scientist made a ______ discovery.'",
                        options = listOf("groundbreaking", "earthfalling", "soilbreaking", "landcracking"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "'Groundbreaking discovery' is the standard idiomatic collocation in academic and professional English."
                    ),
                    ExamQuestion(
                        id = "q3_3",
                        question = "Choose the word closest in meaning to 'UBIQUITOUS':",
                        options = listOf("Omnipresent", "Scarce", "Fictional", "Temporary"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "Ubiquitous means found or existing everywhere at the same time (omnipresent)."
                    ),
                    ExamQuestion(
                        id = "q3_4",
                        question = "What does the expression 'Once in a blue moon' signify?",
                        options = listOf("Frequently", "Every month", "Very rarely", "During clear nights"),
                        correctOptionIndex = 2,
                        marks = 5,
                        explanation = "'Once in a blue moon' denotes an event that happens extremely rarely."
                    ),
                    ExamQuestion(
                        id = "q3_5",
                        question = "Which word correctly completes: 'She gave a very ______ and convincing argument.'",
                        options = listOf("eloquent", "clumsy", "murky", "hesitant"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "Eloquent describes fluent, expressive, and persuasive speech or writing."
                    ),
                    ExamQuestion(
                        id = "q3_6",
                        question = "What is the meaning of the idiom 'Burn the midnight oil'?",
                        options = listOf("Cause a fire hazard", "Work or study late into the night", "Waste electric energy", "Cook an elaborate meal"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Burn the midnight oil' refers to working or studying late into the night."
                    ),
                    ExamQuestion(
                        id = "q3_7",
                        question = "Select the appropriate phrasal verb: 'We decided to ______ the meeting until next Tuesday.'",
                        options = listOf("call out", "put off", "take off", "look up"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Put off' means to postpone or delay an event."
                    ),
                    ExamQuestion(
                        id = "q3_8",
                        question = "What does the idiom 'See eye to eye' mean?",
                        options = listOf("To have strong eye contact", "To agree with someone completely", "To watch someone closely", "To challenge a rival"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'To see eye to eye' means to have the same viewpoint or agree fully with someone."
                    ),
                    ExamQuestion(
                        id = "q3_9",
                        question = "Select the correct adjective for someone who recovers quickly from difficulties:",
                        options = listOf("Vulnerable", "Resilient", "Fragile", "Stagnant"),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Resilient' means able to withstand or recover quickly from difficult conditions."
                    ),
                    ExamQuestion(
                        id = "q3_10",
                        question = "Fill in the correct collocation: 'He paid me a sincere ______ on my presentation.'",
                        options = listOf("compliment", "complement", "agreement", "reprimand"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "To 'pay a compliment' is the standard polite English collocation."
                    )
                )
            ),
            Exam(
                id = "exm_4",
                title = "Class 9 Conversational English & Sentence Structure Quiz",
                description = "Fundamental speaking etiquette, greeting responses, polite requests, and question tags.",
                teacherName = "Prof. Eleanor Vance",
                targetClass = "Class 9",
                subjectCategory = "Spoken English & Fluency",
                durationMinutes = 12,
                totalMarks = 30,
                passingPercentage = 60,
                createdAt = "Nov 2026",
                dueDate = "In 4 Days",
                isPublished = true,
                questions = listOf(
                    ExamQuestion(
                        id = "q4_1",
                        question = "What is the most polite way to ask for assistance in a formal setting?",
                        options = listOf(
                            "Help me now, please.",
                            "Could you possibly assist me with this document?",
                            "I want your help immediately.",
                            "Give me a hand here."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "'Could you possibly...' is an established polite indirect request formula in English."
                    ),
                    ExamQuestion(
                        id = "q4_2",
                        question = "Select the correct question tag: 'You haven't seen my notebook, ______?'",
                        options = listOf("did you", "haven't you", "have you", "do you"),
                        correctOptionIndex = 2,
                        marks = 5,
                        explanation = "A negative main clause takes a positive question tag with the same auxiliary verb ('haven't' -> 'have you?')."
                    ),
                    ExamQuestion(
                        id = "q4_3",
                        question = "What is the natural reply to: 'How do you do?'",
                        options = listOf(
                            "I am doing my homework.",
                            "How do you do?",
                            "Yes, I do.",
                            "I am 16 years old."
                        ),
                        correctOptionIndex = 1,
                        marks = 5,
                        explanation = "In traditional formal English etiquette, the standard response to 'How do you do?' is 'How do you do?'."
                    ),
                    ExamQuestion(
                        id = "q4_4",
                        question = "Choose the correct connector: 'She was exhausted; ______, she finished the project.'",
                        options = listOf("nevertheless", "because", "so", "since"),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "'Nevertheless' signifies contrast with the preceding statement."
                    ),
                    ExamQuestion(
                        id = "q4_5",
                        question = "Select the appropriate response when someone says 'Thank you so much!':",
                        options = listOf(
                            "You're very welcome!",
                            "Why are you saying that?",
                            "No problem, you must pay.",
                            "Yes, I know."
                        ),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "'You're very welcome!' is the polite standard response to expressions of gratitude."
                    ),
                    ExamQuestion(
                        id = "q4_6",
                        question = "Choose the correct phrase for agreeing with a negative statement: 'I don't like bitter coffee.' - '______'",
                        options = listOf("Neither do I.", "So do I.", "Me too neither.", "Nor I am."),
                        correctOptionIndex = 0,
                        marks = 5,
                        explanation = "'Neither do I' is used to agree with a negative statement."
                    )
                )
            )
        )

    private fun loadInitialExams(): List<Exam> {
        val saved = appPreferences?.getExams()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultExams.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveExams(defaultExams)
        return defaultExams
    }

    private val _exams = MutableStateFlow(loadInitialExams())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    // Submissions and Completed Certificates
    private val defaultSubmissions = listOf(
            ExamSubmission(
                id = "sub_fariha_1",
                examId = "exm_grammar_8",
                examTitle = "English Grammar Test",
                studentId = "stu_fariha",
                studentName = "Fariha Yesmin",
                studentClass = "VIII",
                studentAvatar = "🎓",
                selectedAnswers = mapOf(
                    "qg8_1" to 1, "qg8_2" to 1, "qg8_3" to 0, "qg8_4" to 1,
                    "qg8_5" to 1, "qg8_6" to 1, "qg8_7" to 0, "qg8_8" to 3,
                    "qg8_9" to 1
                ),
                scoreObtained = 88,
                totalMarks = 100,
                percentage = 88,
                isPassed = true,
                grade = "A",
                performanceTitle = "Excellent Performance",
                timeTakenSeconds = 810,
                completedAt = "27 Nov 2026",
                certificateId = "SPK-2026-000368"
            ),
            ExamSubmission(
                id = "sub_exm_1",
                examId = "exm_1",
                examTitle = "Class 10 Comprehensive English Grammar & Tenses Test",
                studentId = "stu_alex_1",
                studentName = "Alex Morgan",
                studentClass = "Class 10",
                studentAvatar = "🦉",
                selectedAnswers = mapOf(
                    "q1_1" to 1, "q1_2" to 2, "q1_3" to 1, "q1_4" to 0, "q1_5" to 1,
                    "q1_6" to 1, "q1_7" to 1, "q1_8" to 1, "q1_9" to 1, "q1_10" to 0 // 9/10 correct = 45 marks
                ),
                scoreObtained = 45,
                totalMarks = 50,
                percentage = 90,
                isPassed = true,
                grade = "O",
                performanceTitle = "Outstanding Distinction",
                timeTakenSeconds = 540,
                completedAt = "Today, 10:15 AM",
                certificateId = "SPK-EXM-2026-7841"
            ),
            ExamSubmission(
                id = "sub_exm_2",
                examId = "exm_1",
                examTitle = "Class 10 Comprehensive English Grammar & Tenses Test",
                studentId = "stu_sofia_2",
                studentName = "Sofia Martinez",
                studentClass = "Class 10",
                studentAvatar = "🦊",
                selectedAnswers = mapOf(
                    "q1_1" to 1, "q1_2" to 2, "q1_3" to 1, "q1_4" to 0, "q1_5" to 1,
                    "q1_6" to 1, "q1_7" to 1, "q1_8" to 1, "q1_9" to 0, "q1_10" to 1 // 40 marks
                ),
                scoreObtained = 40,
                totalMarks = 50,
                percentage = 80,
                isPassed = true,
                grade = "A+",
                performanceTitle = "Distinction with Honors",
                timeTakenSeconds = 612,
                completedAt = "Yesterday, 3:30 PM",
                certificateId = "SPK-EXM-2026-6429"
            ),
            ExamSubmission(
                id = "sub_exm_3",
                examId = "exm_1",
                examTitle = "Class 10 Comprehensive English Grammar & Tenses Test",
                studentId = "stu_kenji_3",
                studentName = "Kenji Sato",
                studentClass = "Class 10",
                studentAvatar = "🦁",
                selectedAnswers = emptyMap(),
                scoreObtained = 35,
                totalMarks = 50,
                percentage = 70,
                isPassed = true,
                grade = "A",
                performanceTitle = "First Class Distinction",
                timeTakenSeconds = 720,
                completedAt = "Yesterday, 5:45 PM",
                certificateId = "SPK-EXM-2026-5120"
            )
        )

    private fun loadInitialSubmissions(): List<ExamSubmission> {
        val saved = appPreferences?.getExamSubmissions()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultSubmissions.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveExamSubmissions(defaultSubmissions)
        return defaultSubmissions
    }

    private val _examSubmissions = MutableStateFlow(loadInitialSubmissions())
    val examSubmissions: StateFlow<List<ExamSubmission>> = _examSubmissions.asStateFlow()

    fun createExam(
        title: String,
        description: String,
        teacherName: String,
        targetClass: String,
        subjectCategory: String,
        durationMinutes: Int,
        passingPercentage: Int = 60,
        questions: List<ExamQuestion>,
        availableFromMillis: Long = 0L,
        availableUntilMillis: Long = 0L,
        dueDate: String = "Next Week"
    ): Exam {
        val totalMarks = questions.sumOf { it.marks }.let { if (it > 0) it else 50 }
        val newExam = Exam(
            id = "exm_${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim(),
            teacherName = teacherName.ifBlank { "Prof. Eleanor Vance" },
            targetClass = targetClass.trim().ifBlank { AcademicClasses.DEFAULT_CLASS },
            subjectCategory = subjectCategory.ifBlank { "English Grammar" },
            durationMinutes = if (durationMinutes > 0) durationMinutes else 15,
            totalMarks = totalMarks,
            passingPercentage = passingPercentage,
            questions = questions,
            createdAt = "Nov 2026",
            dueDate = dueDate,
            isPublished = true,
            availableFromMillis = availableFromMillis,
            availableUntilMillis = availableUntilMillis
        )
        _exams.value = listOf(newExam) + _exams.value
        appPreferences?.saveExams(_exams.value)
        return newExam
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
        val current = _exams.value.toMutableList()
        val idx = current.indexOfFirst { it.id == examId }
        if (idx < 0) return null
        val existing = current[idx]
        val totalMarks = questions.sumOf { it.marks }.let { if (it > 0) it else existing.totalMarks }
        val updated = existing.copy(
            title = title.trim(),
            description = description.trim(),
            targetClass = targetClass.trim().ifBlank { AcademicClasses.DEFAULT_CLASS },
            subjectCategory = subjectCategory.ifBlank { existing.subjectCategory },
            durationMinutes = if (durationMinutes > 0) durationMinutes else existing.durationMinutes,
            totalMarks = totalMarks,
            passingPercentage = passingPercentage,
            questions = questions,
            dueDate = dueDate ?: existing.dueDate,
            availableFromMillis = availableFromMillis,
            availableUntilMillis = availableUntilMillis
        )
        current[idx] = updated
        _exams.value = current
        appPreferences?.saveExams(current)
        return updated
    }

    fun deleteExam(examId: String) {
        _exams.value = _exams.value.filter { it.id != examId }
        _examSubmissions.value = _examSubmissions.value.filter { it.examId != examId }
        appPreferences?.saveExams(_exams.value)
        appPreferences?.saveExamSubmissions(_examSubmissions.value)
    }

    fun submitExam(
        examId: String,
        studentId: String,
        studentName: String,
        studentClass: String,
        studentAvatar: String,
        selectedAnswers: Map<String, Int>,
        timeTakenSeconds: Int
    ): ExamSubmission {
        val exam = _exams.value.find { it.id == examId }
            ?: _exams.value.first()

        var calculatedScore = 0
        var totalMarksPossible = 0

        exam.questions.forEach { q ->
            totalMarksPossible += q.marks
            val selected = selectedAnswers[q.id]
            if (selected != null && selected == q.correctOptionIndex) {
                calculatedScore += q.marks
            }
        }
        if (totalMarksPossible <= 0) totalMarksPossible = exam.totalMarks

        val percentage = if (totalMarksPossible > 0) (calculatedScore * 100) / totalMarksPossible else 0
        val isPassed = percentage >= exam.passingPercentage

        val grade = when {
            percentage >= 90 -> "O"
            percentage >= 80 -> "A+"
            percentage >= 70 -> "A"
            percentage >= 60 -> "B+"
            percentage >= 50 -> "B"
            percentage >= 40 -> "C"
            else -> "F"
        }

        val performanceTitle = when {
            percentage >= 90 -> "Outstanding Performance"
            percentage >= 80 -> "Excellent Performance"
            percentage >= 70 -> "First Class Distinction"
            percentage >= 60 -> "Higher Merit"
            percentage >= 50 -> "Merit Pass"
            percentage >= 40 -> "Passing Grade"
            else -> "Course Completion"
        }

        val randomCertNum = String.format("%06d", (100..999999).random())
        val certificateId = "SPK-2026-$randomCertNum"

        val submission = ExamSubmission(
            id = "sub_exm_${System.currentTimeMillis()}",
            examId = exam.id,
            examTitle = exam.title,
            studentId = studentId,
            studentName = studentName,
            studentClass = studentClass,
            studentAvatar = studentAvatar,
            selectedAnswers = selectedAnswers,
            scoreObtained = calculatedScore,
            totalMarks = totalMarksPossible,
            percentage = percentage,
            isPassed = isPassed,
            grade = grade,
            performanceTitle = performanceTitle,
            timeTakenSeconds = timeTakenSeconds,
            completedAt = "Today",
            certificateId = certificateId
        )

        // Replace any existing submission for this student on this exam, or prepend
        _examSubmissions.value = listOf(submission) + _examSubmissions.value.filterNot {
            it.examId == examId && (it.studentId == studentId || it.studentName.equals(studentName, ignoreCase = true))
        }
        appPreferences?.saveExamSubmissions(_examSubmissions.value)

        return submission
    }


    // Submissions for Teacher Review
    private val _submissions = MutableStateFlow(
        listOf(
            StudentSubmission(
                id = "sub_1",
                assignmentId = "asg_1",
                studentName = "Alex Morgan",
                studentAvatar = "🦉",
                audioDurationSeconds = 142,
                studentTranscript = "Hello Professor, today I want to share my thoughts on reducing single-use plastics. In my household, we replaced all plastic grocery bags with canvas totes and started carrying reusable stainless steel water bottles.",
                submittedDate = "Today, 10:24 AM",
                score = null,
                teacherFeedback = null,
                status = "PENDING"
            ),
            StudentSubmission(
                id = "sub_2",
                assignmentId = "asg_1",
                studentName = "Sofia Martinez",
                studentAvatar = "🦊",
                audioDurationSeconds = 160,
                studentTranscript = "Good morning everyone. Climate change requires collective commitment. In our neighborhood, we organized a weekend community composting drive.",
                submittedDate = "Yesterday, 4:15 PM",
                score = 92,
                teacherFeedback = "Superb diction, Sofia! Your pacing was very natural and expressive.",
                status = "GRADED"
            ),
            StudentSubmission(
                id = "sub_3",
                assignmentId = "asg_1",
                studentName = "Kenji Sato",
                studentAvatar = "🐼",
                audioDurationSeconds = 110,
                studentTranscript = "I believe we can save energy by walking or taking the electric tram instead of driving private cars every morning.",
                submittedDate = "Yesterday, 2:30 PM",
                score = 85,
                teacherFeedback = "Good vocabulary! Watch the rhythm on multi-syllable adjectives like 'electric'.",
                status = "GRADED"
            )
        )
    )
    val submissions: StateFlow<List<StudentSubmission>> = _submissions.asStateFlow()

    // Live Classes
    private val _liveClasses = MutableStateFlow(
        listOf(
            LiveClassSession("lc_1", "Live Conversational Debate: Remote Work vs Office", "Prof. Eleanor Vance", "Today at 7:00 PM", 45, "Interactive group discussion focusing on argumentation and polite disagreement phrases.", "UPCOMING", 18),
            LiveClassSession("lc_2", "Mastering British vs American Intonation", "Dr. Marcus Reed", "Tomorrow at 4:30 PM", 60, "Live ear-training and speech resonance workshop with real-time student participation.", "UPCOMING", 26),
            LiveClassSession("lc_3", "Idiomatic English in Real-World Situations", "Prof. Chloe Bennett", "Yesterday", 45, "Review of 20 modern colloquial expressions used by native speakers.", "COMPLETED", 32)
        )
    )
    val liveClasses: StateFlow<List<LiveClassSession>> = _liveClasses.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow(
        listOf(
            AnnouncementItem("an_1", "Weekly Spoken English Tournament Begins!", "Compete in the 30-Second Speaking Challenge to win 500 bonus XP and the Diamond League crown.", "Admin", "2 hours ago", isUrgent = true),
            AnnouncementItem("an_2", "New Roleplay Scenarios Added", "Check out the new Hotel Check-in and Job Interview dialogues in your Speaking Hub.", "Teacher Team", "1 day ago"),
            AnnouncementItem("an_3", "System Maintenance Notice", "Server maintenance scheduled on Sunday 2:00 AM UTC for 15 minutes. Offline mode will remain fully accessible.", "System Admin", "3 days ago")
        )
    )
    val announcements: StateFlow<List<AnnouncementItem>> = _announcements.asStateFlow()

    // Leaderboard state
    private val _leaderboard = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardUser>> = _leaderboard.asStateFlow()

    // Achievement Badges
    private val _badges = MutableStateFlow(
        listOf(
            AchievementBadge("b1", "7-Day Streak Warrior", "Practiced spoken English 7 days in a row", "🔥", true, 1.0f),
            AchievementBadge("b2", "Speech Virtuoso", "Scored over 90% in 5 AI speaking conversations", "🎙️", true, 1.0f),
            AchievementBadge("b3", "Word Wizard", "Mastered 50 new vocabulary items", "📚", true, 1.0f),
            AchievementBadge("b4", "Roleplay Champion", "Completed all dialogues in 3 different scenarios", "🎭", true, 1.0f),
            AchievementBadge("b5", "Pronunciation Ace", "Achieved 95% accuracy in a phoneme challenge", "🎯", false, 0.75f),
            AchievementBadge("b6", "Classroom Star", "Attended 5 live interactive workshops", "🌟", false, 0.60f)
        )
    )
    val badges: StateFlow<List<AchievementBadge>> = _badges.asStateFlow()

    private val defaultStudents = listOf(
        StudentManagementInfo("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", "🦉", "Class 10", LearningLevel.INTERMEDIATE, true, 82, 85, 79, 88, 86, 90, 7, 1450, "Nov 2026", "Consistently practices speaking. Excellent rhythm and confidence.", "1234"),
        StudentManagementInfo("std_002", "Elena Rostova", "elena.r@linguaquest.edu", "🦄", "Class 10", LearningLevel.ADVANCED, true, 94, 96, 92, 90, 95, 96, 19, 2850, "Oct 2026", "Top class fluency. Demonstrates near-native sentence intonation.", "1234"),
        StudentManagementInfo("std_003", "David Chen", "david.c@linguaquest.edu", "🐯", "Class 9", LearningLevel.UPPER_INTERMEDIATE, true, 88, 89, 85, 91, 87, 92, 14, 2420, "Oct 2026", "Strong analytical vocabulary. Recommended for advanced debate roleplays.", "1234"),
        StudentManagementInfo("std_004", "Amira Al-Mansoor", "amira.m@linguaquest.edu", "🦁", "Class 10", LearningLevel.INTERMEDIATE, true, 78, 80, 74, 82, 80, 85, 8, 1390, "Nov 2026", "Needs extra practice on past tense irregular verbs and voiced /th/ sounds.", "1234"),
        StudentManagementInfo("std_005", "Lucas Silva", "lucas.s@linguaquest.edu", "🐺", "Class 8", LearningLevel.PRE_INTERMEDIATE, true, 71, 72, 68, 75, 74, 80, 5, 1210, "Nov 2026", "Improving quickly. Benefit from daily sentence builder and word match games.", "1234"),
        StudentManagementInfo("std_006", "Hana Tanaka", "hana.t@linguaquest.edu", "🌸", "Class 7", LearningLevel.PRE_INTERMEDIATE, false, 65, 68, 62, 70, 71, 76, 3, 980, "Nov 2026", "Temporarily paused. Needs supportive speaking drills on basic greetings.", "1234")
    )

    private data class LeaderboardDraft(
        val id: String,
        val name: String,
        val avatarEmoji: String,
        val xp: Int,
        val streak: Int,
        val league: String,
        val isCurrentUser: Boolean,
        val studentClass: String,
        val cefrLevel: String
    )

    private fun generateLeaderboard(
        students: List<StudentManagementInfo>,
        activeProfile: StudentProfile,
        curUser: User
    ): List<LeaderboardUser> {
        val activeStudentId = if (curUser.role == Role.STUDENT) curUser.id else activeProfile.userId

        val activeList = students.filter { it.isActive }
        val allStudents = if (curUser.role == Role.STUDENT && activeList.none { it.id == curUser.id || it.email.equals(curUser.email, ignoreCase = true) }) {
            activeList + StudentManagementInfo(
                id = curUser.id,
                name = curUser.name,
                email = curUser.email,
                avatarEmoji = curUser.avatarEmoji,
                batch = curUser.batch,
                level = activeProfile.currentLevel,
                isActive = true,
                speakingScore = activeProfile.speakingScore,
                pronunciationScore = activeProfile.pronunciationScore,
                fluencyScore = activeProfile.fluencyScore,
                grammarScore = activeProfile.grammarScore,
                vocabularyScore = activeProfile.vocabularyScore,
                listeningScore = activeProfile.listeningScore,
                streakDays = activeProfile.streakDays,
                xpPoints = activeProfile.xpPoints,
                joinedDate = curUser.joinedDate
            )
        } else {
            activeList
        }

        val drafts = allStudents.map { s ->
            val isCur = (s.id == activeStudentId || s.email.equals(curUser.email, ignoreCase = true) || (curUser.role == Role.STUDENT && s.id == curUser.id))
            val xp = if (isCur && curUser.role == Role.STUDENT) activeProfile.xpPoints else s.xpPoints
            val streak = if (isCur && curUser.role == Role.STUDENT) activeProfile.streakDays else s.streakDays
            val league = when {
                xp >= 2500 -> "Diamond League"
                xp >= 1800 -> "Platinum League"
                xp >= 1200 -> "Gold League"
                xp >= 700 -> "Silver League"
                else -> "Bronze League"
            }
            val cefr = if (isCur && curUser.role == Role.STUDENT) activeProfile.currentLevel.cefr else s.level.cefr
            LeaderboardDraft(
                id = s.id,
                name = s.name,
                avatarEmoji = s.avatarEmoji,
                xp = xp,
                streak = streak,
                league = league,
                isCurrentUser = isCur,
                studentClass = s.batch,
                cefrLevel = cefr
            )
        }

        val sorted = drafts.sortedWith(
            compareByDescending<LeaderboardDraft> { it.xp }
                .thenByDescending { it.streak }
        )

        return sorted.mapIndexed { index, draft ->
            LeaderboardUser(
                rank = index + 1,
                name = draft.name,
                avatarEmoji = draft.avatarEmoji,
                xp = draft.xp,
                streak = draft.streak,
                league = draft.league,
                isCurrentUser = draft.isCurrentUser,
                studentClass = draft.studentClass,
                studentId = draft.id,
                cefrLevel = draft.cefrLevel
            )
        }
    }

    fun refreshLeaderboard() {
        _leaderboard.value = generateLeaderboard(_adminStudents.value, _studentProfile.value, _currentUser.value)
    }

    private val defaultTeachers = listOf(
        User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Class 10 & 9", true, securityPin = "7321"),
        User("tch_002", "Dr. Marcus Reed", "m.reed@linguaquest.edu", Role.TEACHER, "👨‍🏫", "Class 8 & 7", true, securityPin = "7321"),
        User("tch_003", "Prof. Chloe Bennett", "c.bennett@linguaquest.edu", Role.TEACHER, "👩‍💼", "Class 11 & 12", true, securityPin = "7321")
    )

    private fun loadInitialStudents(): List<StudentManagementInfo> {
        val saved = appPreferences?.getStudents()
        if (!saved.isNullOrEmpty()) {
            val existingIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultStudents.filter { it.id !in existingIds }
            val merged = saved + missingDefaults
            return merged
        }
        appPreferences?.saveStudents(defaultStudents)
        return defaultStudents
    }

    private fun loadInitialTeachers(): List<User> {
        val saved = appPreferences?.getTeachers()
        if (!saved.isNullOrEmpty()) {
            val existingIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultTeachers.filter { it.id !in existingIds }
            val merged = saved + missingDefaults
            return merged
        }
        appPreferences?.saveTeachers(defaultTeachers)
        return defaultTeachers
    }

    // Admin & Teacher Managed Students
    private val _adminStudents = MutableStateFlow(loadInitialStudents())
    val adminStudents: StateFlow<List<StudentManagementInfo>> = _adminStudents.asStateFlow()

    // Admin Managed Teachers
    private val _adminTeachers = MutableStateFlow(loadInitialTeachers())
    val adminTeachers: StateFlow<List<User>> = _adminTeachers.asStateFlow()

    // Admin & Teacher Managed Classes
    private val _academicClasses = MutableStateFlow(
        appPreferences?.getClasses() ?: AcademicClasses.DEFAULT_CLASSES
    )
    val academicClasses: StateFlow<List<String>> = _academicClasses.asStateFlow()

    // Self-registration permissions (Admin controllable)
    private val _allowStudentRegistration = MutableStateFlow(true)
    val allowStudentRegistration: StateFlow<Boolean> = _allowStudentRegistration.asStateFlow()

    private val _allowTeacherRegistration = MutableStateFlow(true)
    val allowTeacherRegistration: StateFlow<Boolean> = _allowTeacherRegistration.asStateFlow()

    fun setAllowStudentRegistration(allow: Boolean) {
        _allowStudentRegistration.value = allow
    }

    fun setAllowTeacherRegistration(allow: Boolean) {
        _allowTeacherRegistration.value = allow
    }

    // Dynamic Game Content Collections
    private val defaultWordMatchChallenges = listOf(
        WordMatchChallenge("wm_1", "Eloquence", "Fluent or persuasive speech", "Communication", "System"),
        WordMatchChallenge("wm_2", "Resilience", "Ability to recover quickly from hardship", "Mindset", "System"),
        WordMatchChallenge("wm_3", "Articulation", "Clear and effective pronunciation", "Communication", "System"),
        WordMatchChallenge("wm_4", "Pragmatic", "Sensible and practical rather than theoretical", "Academic", "System"),
        WordMatchChallenge("wm_5", "Meticulous", "Showing great attention to detail and care", "General", "Prof. Eleanor Vance"),
        WordMatchChallenge("wm_6", "Serendipity", "Finding pleasant things unexpectedly", "General", "Dean Arthur Pendelton")
    )

    private fun loadInitialWordMatches(): List<WordMatchChallenge> {
        val saved = appPreferences?.getWordMatchChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultWordMatchChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveWordMatchChallenges(defaultWordMatchChallenges)
        return defaultWordMatchChallenges
    }

    private val _wordMatchChallenges = MutableStateFlow(loadInitialWordMatches())
    val wordMatchChallenges: StateFlow<List<WordMatchChallenge>> = _wordMatchChallenges.asStateFlow()

    private val defaultMissingLettersChallenges = listOf(
        // Animals Category
        MissingLettersChallenge("ml_a1", "ELEPHANT", "The largest living land animal with large ears and a trunk", listOf(1, 4, 6), "Animals", "System"),
        MissingLettersChallenge("ml_a2", "GIRAFFE", "Tallest living terrestrial animal with an extremely long neck", listOf(2, 4), "Animals", "System"),
        MissingLettersChallenge("ml_a3", "KANGAROO", "Large Australian marsupial that hops on powerful hind legs", listOf(2, 5), "Animals", "Prof. Eleanor Vance"),
        MissingLettersChallenge("ml_a4", "DOLPHIN", "Highly intelligent aquatic marine mammal known for playful acrobatics", listOf(1, 4), "Animals", "Dr. Marcus Reed"),
        MissingLettersChallenge("ml_a5", "LEOPARD", "Graceful solitary big cat with a spotted coat and stealthy hunter", listOf(2, 5), "Animals", "System"),

        // Vegetables Category
        MissingLettersChallenge("ml_v1", "BROCCOLI", "Edible green plant in the cabbage family with large flowering head", listOf(2, 5), "Vegetables", "System"),
        MissingLettersChallenge("ml_v2", "CUCUMBER", "Widely cultivated creeping vine plant bearing cylindrical green fruit", listOf(1, 4, 6), "Vegetables", "System"),
        MissingLettersChallenge("ml_v3", "SPINACH", "Nutritious leafy green flowering plant rich in iron and vitamins", listOf(2, 5), "Vegetables", "Prof. Eleanor Vance"),
        MissingLettersChallenge("ml_v4", "CABBAGE", "Leafy green or purple biennial plant grown as an annual vegetable", listOf(1, 4), "Vegetables", "Dean Arthur Pendelton"),
        MissingLettersChallenge("ml_v5", "CARROT", "Crisp root vegetable usually orange in color with sweet earthy taste", listOf(1, 3), "Vegetables", "Dr. Marcus Reed"),

        // Fruits Category
        MissingLettersChallenge("ml_f1", "PINEAPPLE", "Tropical plant with edible fruit comprising coalesced berries", listOf(2, 6), "Fruits", "System"),
        MissingLettersChallenge("ml_f2", "STRAWBERRY", "Sweet, soft red fruit with small seeds on its surface", listOf(3, 7), "Fruits", "Prof. Eleanor Vance"),
        MissingLettersChallenge("ml_f3", "WATERMELON", "Large sweet fruit with smooth green rind and juicy red flesh", listOf(2, 5, 8), "Fruits", "System"),

        // General Vocabulary Category
        MissingLettersChallenge("ml_1", "CONVERSATION", "An informal spoken exchange between people", listOf(2, 5, 8), "General", "System"),
        MissingLettersChallenge("ml_2", "PRONUNCIATION", "The correct way in which a word or language is spoken", listOf(2, 6, 9), "General", "System"),
        MissingLettersChallenge("ml_3", "FLUENCY", "Ability to speak smoothly, easily, and naturally", listOf(1, 4), "General", "Prof. Eleanor Vance"),
        MissingLettersChallenge("ml_4", "VOCABULARY", "The body of words used in a particular language", listOf(1, 5, 8), "General", "System"),
        MissingLettersChallenge("ml_5", "INTELLIGENT", "Having or showing mental alertness and capacity", listOf(3, 7), "General", "Dr. Marcus Reed")
    )

    private fun loadInitialMissingLetters(): List<MissingLettersChallenge> {
        val saved = appPreferences?.getMissingLettersChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultMissingLettersChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveMissingLettersChallenges(defaultMissingLettersChallenges)
        return defaultMissingLettersChallenges
    }

    private val _missingLettersChallenges = MutableStateFlow(loadInitialMissingLetters())
    val missingLettersChallenges: StateFlow<List<MissingLettersChallenge>> = _missingLettersChallenges.asStateFlow()

    private val _missingLettersCustomCategories = MutableStateFlow(
        appPreferences?.getMissingLettersCategories() ?: listOf("Animals", "Vegetables", "Fruits", "General")
    )
    val missingLettersCustomCategories: StateFlow<List<String>> = _missingLettersCustomCategories.asStateFlow()

    fun addMissingLettersCategory(category: String): Boolean {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return false
        if (_missingLettersCustomCategories.value.any { it.equals(trimmed, ignoreCase = true) }) return false
        _missingLettersCustomCategories.update { it + trimmed }
        appPreferences?.saveMissingLettersCategories(_missingLettersCustomCategories.value)
        return true
    }

    fun deleteMissingLettersCategory(category: String): Boolean {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return false
        if (trimmed.equals("General", ignoreCase = true)) return false // Keep General as system default
        val exists = _missingLettersCustomCategories.value.any { it.equals(trimmed, ignoreCase = true) }
        if (!exists) return false
        _missingLettersCustomCategories.update { list ->
            val updated = list.filterNot { it.equals(trimmed, ignoreCase = true) }
            if (updated.isEmpty()) listOf("General") else updated
        }
        // Reassign challenges belonging to deleted category to "General"
        _missingLettersChallenges.update { list ->
            list.map { item ->
                if (item.category.equals(trimmed, ignoreCase = true)) item.copy(category = "General")
                else item
            }
        }
        appPreferences?.saveMissingLettersCategories(_missingLettersCustomCategories.value)
        return true
    }

    private val defaultSentenceBuilderPacks = listOf(
        SentenceBuilderPack(
            id = "sb_pack_easy",
            title = "Easy Challenge",
            description = "Build simple and short sentences.",
            difficulty = "Easy",
            xpReward = 40,
            createdBy = "System"
        ),
        SentenceBuilderPack(
            id = "sb_pack_medium",
            title = "Medium Challenge",
            description = "Build longer and more meaningful sentences.",
            difficulty = "Medium",
            xpReward = 60,
            createdBy = "System"
        ),
        SentenceBuilderPack(
            id = "sb_pack_hard",
            title = "Hard Challenge",
            description = "Build complex sentences with proper grammar.",
            difficulty = "Hard",
            xpReward = 100,
            createdBy = "System"
        )
    )

    private fun loadInitialSentenceBuilderPacks(): List<SentenceBuilderPack> {
        val saved = appPreferences?.getSentenceBuilderPacks()
        if (!saved.isNullOrEmpty()) {
            return saved
        }
        appPreferences?.saveSentenceBuilderPacks(defaultSentenceBuilderPacks)
        return defaultSentenceBuilderPacks
    }

    private val _sentenceBuilderPacks = MutableStateFlow(loadInitialSentenceBuilderPacks())
    val sentenceBuilderPacks: StateFlow<List<SentenceBuilderPack>> = _sentenceBuilderPacks.asStateFlow()

    private val defaultSentenceBuilderChallenges = listOf(
        SentenceBuilderChallenge("sb_1", "She speaks English with natural confidence and clarity", "Daily speaking practice", "তাই স্বাভাৱিক আত্মবিশ্বাস আৰু স্পষ্টতাৰে ইংৰাজী কয়।", "System", "sb_pack_easy"),
        SentenceBuilderChallenge("sb_5", "We drink warm tea every morning", "Present simple routine", "আমি প্ৰতিদিনে ৰাতিপুৱা গৰম চাহ খাওঁ।", "System", "sb_pack_easy"),
        SentenceBuilderChallenge("sb_2", "Clear pronunciation helps listeners understand your message easily", "Pronunciation guide", "স্পষ্ট উচ্চাৰণে শ্ৰোতাসকলক আপোনাৰ বাৰ্তা সহজে বুজিবলৈ সহায় কৰে।", "Prof. Eleanor Vance", "sb_pack_medium"),
        SentenceBuilderChallenge("sb_3", "We practice speaking English every day to build fluency", "Study habits", "সাবলীলতা গঢ়ি তুলিবলৈ আমি প্ৰতিদিনে ইংৰাজী কোৱা অনুশীলন কৰোঁ।", "System", "sb_pack_medium"),
        SentenceBuilderChallenge("sb_7", "Speaking clearly every day builds strong confidence", "Daily fluency habit", "প্ৰতিদিনে স্পষ্টকৈ ক'লে শক্তিশালী আত্মবিশ্বাস গঢ়ি উঠে।", "System", "sb_pack_medium"),
        SentenceBuilderChallenge("sb_4", "Collaborative conversations improve confidence in professional workplace settings", "Workplace English", "পাৰস্পৰিক কথোপকথনে পেছাদাৰী কৰ্মক্ষেত্ৰত আত্মবিশ্বাস বৃদ্ধি কৰে।", "Dean Arthur Pendelton", "sb_pack_hard"),
        SentenceBuilderChallenge("sb_6", "Although the weather was stormy we arrived on time", "Complex clause", "বতৰ ধুমুহাময় আছিল যদিও আমি সময়মতে উপস্থিত হ'লোঁ।", "System", "sb_pack_hard")
    )

    private fun loadInitialSentenceBuilders(): List<SentenceBuilderChallenge> {
        val saved = appPreferences?.getSentenceBuilderChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultSentenceBuilderChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveSentenceBuilderChallenges(defaultSentenceBuilderChallenges)
        return defaultSentenceBuilderChallenges
    }

    private val _sentenceBuilderChallenges = MutableStateFlow(loadInitialSentenceBuilders())
    val sentenceBuilderChallenges: StateFlow<List<SentenceBuilderChallenge>> = _sentenceBuilderChallenges.asStateFlow()

    private val defaultListenChooseChallenges = listOf(
        ListenChooseChallenge(
            id = "lc_1",
            audioScript = "Could you please tell me how to get to the nearest subway station?",
            question = "What is the speaker asking for directions to?",
            options = listOf("The airport terminal", "The nearest subway station", "A nearby coffee shop", "The central library"),
            correctIndex = 1,
            createdBy = "System"
        ),
        ListenChooseChallenge(
            id = "lc_2",
            audioScript = "I would like a large cappuccino with oat milk and cinnamon sprinkle, please.",
            question = "What coffee beverage did the customer request?",
            options = listOf("Iced mocha with cream", "Hot black Americano", "Large cappuccino with oat milk", "Espresso double shot"),
            correctIndex = 2,
            createdBy = "System"
        ),
        ListenChooseChallenge(
            id = "lc_3",
            audioScript = "The flight to Chicago will be boarding through gate 24 in fifteen minutes.",
            question = "What gate is the flight boarding through?",
            options = listOf("Gate 14", "Gate 24", "Gate 42", "Gate 34"),
            correctIndex = 1,
            createdBy = "Prof. Eleanor Vance"
        ),
        ListenChooseChallenge(
            id = "lc_4",
            audioScript = "Our quarterly academic review will be held next Tuesday morning at nine sharp.",
            question = "When is the team academic review scheduled?",
            options = listOf("Next Tuesday at 9:00 AM", "This Friday at 3:00 PM", "Tomorrow at noon", "Next Monday at 10:00 AM"),
            correctIndex = 0,
            createdBy = "Dean Arthur Pendelton"
        )
    )

    private fun loadInitialListenChooses(): List<ListenChooseChallenge> {
        val saved = appPreferences?.getListenChooseChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultListenChooseChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveListenChooseChallenges(defaultListenChooseChallenges)
        return defaultListenChooseChallenges
    }

    private val _listenChooseChallenges = MutableStateFlow(loadInitialListenChooses())
    val listenChooseChallenges: StateFlow<List<ListenChooseChallenge>> = _listenChooseChallenges.asStateFlow()

    private val defaultPronunciationBattleChallenges = listOf(
        PronunciationBattleChallenge("pb_1", "The courageous astronaut explored uncharted territory", "Consonant clusters 'str', 'ch', 'pl'", "Medium", 80, "System"),
        PronunciationBattleChallenge("pb_2", "Peter Piper picked a peck of pickled peppers", "Crisp explosive /p/ consonant rhythm", "Hard", 85, "System"),
        PronunciationBattleChallenge("pb_3", "Thoughtful philosophers thoroughly examine their theories", "Voiced and voiceless 'th' phonemes", "Hard", 85, "Prof. Eleanor Vance"),
        PronunciationBattleChallenge("pb_4", "She sells seashells by the shimmering seashore", "Sibilants /s/ vs /ʃ/ distinction", "Medium", 80, "System"),
        PronunciationBattleChallenge("pb_5", "World development requires extraordinary perseverance", "Liquid 'r' and 'l' consonant blends", "Hard", 85, "Dean Arthur Pendelton")
    )

    private fun loadInitialPronunciationBattles(): List<PronunciationBattleChallenge> {
        val saved = appPreferences?.getPronunciationBattleChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultPronunciationBattleChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.savePronunciationBattleChallenges(defaultPronunciationBattleChallenges)
        return defaultPronunciationBattleChallenges
    }

    private val _pronunciationBattleChallenges = MutableStateFlow(loadInitialPronunciationBattles())
    val pronunciationBattleChallenges: StateFlow<List<PronunciationBattleChallenge>> = _pronunciationBattleChallenges.asStateFlow()

    private val defaultFastVocabularyChallenges = listOf(
        FastVocabularyChallenge("fv_1", "Articulate", "Choose the closest synonym for 'Articulate':", listOf("Expressive & clear", "Confused & hesitant", "Quiet & shy", "Angry & rude"), 0, "System"),
        FastVocabularyChallenge("fv_2", "Eloquent", "What does 'Eloquent' describe?", listOf("Persuasive & graceful speaking", "Very fast running", "Complicated math", "Ancient architecture"), 0, "System"),
        FastVocabularyChallenge("fv_3", "Pragmatic", "A 'Pragmatic' solution is one that is:", listOf("Practical & realistic", "Expensive & luxurious", "Theoretical & untested", "Careless & hurried"), 0, "Prof. Eleanor Vance"),
        FastVocabularyChallenge("fv_4", "Resilience", "What is the best definition of 'Resilience'?", listOf("Bouncing back from hardship", "Forgetting the past", "Giving up easily", "Avoiding all conflict"), 0, "System"),
        FastVocabularyChallenge("fv_5", "Collaborate", "When two people 'Collaborate', they:", listOf("Work jointly on a task", "Argue loudly", "Compete against each other", "Work completely alone"), 0, "Dean Arthur Pendelton")
    )

    private fun loadInitialFastVocabulary(): List<FastVocabularyChallenge> {
        val saved = appPreferences?.getFastVocabularyChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultFastVocabularyChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveFastVocabularyChallenges(defaultFastVocabularyChallenges)
        return defaultFastVocabularyChallenges
    }

    private val _fastVocabularyChallenges = MutableStateFlow(loadInitialFastVocabulary())
    val fastVocabularyChallenges: StateFlow<List<FastVocabularyChallenge>> = _fastVocabularyChallenges.asStateFlow()

    private val defaultGrammarChallenges = listOf(
        GrammarChallengeQuestion(
            id = "gc_1",
            question = "If I ___ more time, I would practice conversational English every evening.",
            options = listOf("have", "had", "will have", "would have"),
            correctIndex = 1,
            explanation = "Second conditional uses past simple ('had') in the 'if' clause for hypothetical situations.",
            category = "Conditionals",
            createdBy = "System"
        ),
        GrammarChallengeQuestion(
            id = "gc_2",
            question = "She has been living in London ___ over three years.",
            options = listOf("since", "for", "during", "in"),
            correctIndex = 1,
            explanation = "Use 'for' to express a duration of time ('over three years').",
            category = "Prepositions",
            createdBy = "System"
        ),
        GrammarChallengeQuestion(
            id = "gc_3",
            question = "Listen! The professor ___ the key grammar points on the board.",
            options = listOf("explains", "is explaining", "explained", "has explained"),
            correctIndex = 1,
            explanation = "Present continuous is used for actions taking place right at this moment ('Listen!').",
            category = "Tenses",
            createdBy = "Prof. Eleanor Vance"
        ),
        GrammarChallengeQuestion(
            id = "gc_4",
            question = "Neither the teacher nor the students ___ present at yesterday's rehearsal.",
            options = listOf("was", "were", "is", "are"),
            correctIndex = 1,
            explanation = "With 'neither... nor', the verb agrees with the closer subject ('the students' -> 'were').",
            category = "Subject-Verb Agreement",
            createdBy = "Dean Arthur Pendelton"
        )
    )

    private fun loadInitialGrammarChallenges(): List<GrammarChallengeQuestion> {
        val saved = appPreferences?.getGrammarChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultGrammarChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.saveGrammarChallenges(defaultGrammarChallenges)
        return defaultGrammarChallenges
    }

    private val _grammarChallenges = MutableStateFlow(loadInitialGrammarChallenges())
    val grammarChallenges: StateFlow<List<GrammarChallengeQuestion>> = _grammarChallenges.asStateFlow()

    private val defaultPictureVocabChallenges = listOf(
        PictureVocabChallenge("pv_1", "🩺", "Stethoscope", "/ˈsteθ.ə.skəʊp/", "An instrument used by medical doctors to listen to heart and lungs", listOf("Stethoscope", "Microscope", "Thermometer", "Telescope"), "System"),
        PictureVocabChallenge("pv_2", "✈️", "Airplane", "/ˈeə.pleɪn/", "A powered flying vehicle with fixed wings", listOf("Airplane", "Helicopter", "Submarine", "Spaceship"), "System"),
        PictureVocabChallenge("pv_3", "📚", "Library", "/ˈlaɪ.brər.i/", "A quiet room or building containing books for reading or study", listOf("Library", "Bakery", "Auditorium", "Laboratory"), "System"),
        PictureVocabChallenge("pv_4", "🍳", "Cooking", "/ˈkʊk.ɪŋ/", "The practice or skill of preparing food by combining ingredients", listOf("Cooking", "Gardening", "Sewing", "Painting"), "System"),
        PictureVocabChallenge("pv_5", "🎸", "Guitar", "/ɡɪˈtɑːr/", "A musical instrument having six strings played with fingers or pick", listOf("Guitar", "Violin", "Drum", "Saxophone"), "System"),
        PictureVocabChallenge("pv_6", "🏃", "Sprinting", "/ˈsprɪn.tɪŋ/", "Running at full speed over a short distance", listOf("Sprinting", "Swimming", "Jumping", "Climbing"), "System")
    )

    private fun loadInitialPictureVocabChallenges(): List<PictureVocabChallenge> {
        val saved = appPreferences?.getPictureVocabChallenges()
        if (!saved.isNullOrEmpty()) {
            val savedIds = saved.map { it.id }.toSet()
            val missingDefaults = defaultPictureVocabChallenges.filter { it.id !in savedIds }
            return saved + missingDefaults
        }
        appPreferences?.savePictureVocabChallenges(defaultPictureVocabChallenges)
        return defaultPictureVocabChallenges
    }

    private val _pictureVocabChallenges = MutableStateFlow(loadInitialPictureVocabChallenges())
    val pictureVocabChallenges: StateFlow<List<PictureVocabChallenge>> = _pictureVocabChallenges.asStateFlow()

    init {
        refreshLeaderboard()
        _vocabularyWords.value.forEach { w ->
            registerWordInEngines(w)
        }
    }

    fun switchRole(newRole: Role) {
        val current = _currentUser.value
        val updatedUser = when (newRole) {
            Role.STUDENT -> User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", AcademicClasses.DEFAULT_CLASS, securityPin = "1234")
            Role.TEACHER -> _adminTeachers.value.firstOrNull { it.isActive }
                ?: _adminTeachers.value.firstOrNull()
                ?: User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Lead Instructor", securityPin = "7321")
            Role.ADMIN -> User("adm_001", "Dean Arthur Pendelton", "admin@linguaquest.edu", Role.ADMIN, "🛡️", "Head of Academics", securityPin = "9842")
        }
        _currentUser.value = updatedUser
        if (newRole == Role.ADMIN) {
            setAdminSession(true)
        }
        if (newRole == Role.STUDENT) {
            setStudentProfileUserId(updatedUser.id, updatedUser.name)
        } else {
            refreshLeaderboard()
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        if (user.role == Role.ADMIN) {
            setAdminSession(true)
        }
        refreshLeaderboard()
    }

    fun setStudentProfile(profile: StudentProfile) {
        _studentProfile.value = profile
        appPreferences?.saveStudentProfile(profile)
        refreshLeaderboard()
    }

    fun setStudentProfileUserId(studentId: String, studentName: String) {
        val info = _adminStudents.value.find { it.id == studentId || it.email.equals(studentId, ignoreCase = true) || it.name.equals(studentName, ignoreCase = true) }
        val saved = appPreferences?.getStudentProfile(studentId)
        val initialOrSaved = if (saved != null) {
            if (info != null) {
                val overall = (info.speakingScore + info.pronunciationScore + info.fluencyScore + info.grammarScore + info.vocabularyScore + info.listeningScore) / 6
                saved.copy(
                    currentLevel = info.level,
                    streakDays = maxOf(saved.streakDays, info.streakDays),
                    xpPoints = maxOf(saved.xpPoints, info.xpPoints),
                    overallScore = if (saved.overallScore > 0) saved.overallScore else overall,
                    speakingScore = if (saved.speakingScore > 0) saved.speakingScore else info.speakingScore,
                    pronunciationScore = if (saved.pronunciationScore > 0) saved.pronunciationScore else info.pronunciationScore,
                    fluencyScore = if (saved.fluencyScore > 0) saved.fluencyScore else info.fluencyScore,
                    grammarScore = if (saved.grammarScore > 0) saved.grammarScore else info.grammarScore,
                    vocabularyScore = if (saved.vocabularyScore > 0) saved.vocabularyScore else info.vocabularyScore,
                    listeningScore = if (saved.listeningScore > 0) saved.listeningScore else info.listeningScore
                )
            } else saved
        } else {
            if (info != null) {
                val overall = (info.speakingScore + info.pronunciationScore + info.fluencyScore + info.grammarScore + info.vocabularyScore + info.listeningScore) / 6
                StudentProfile(
                    userId = studentId,
                    currentLevel = info.level,
                    streakDays = maxOf(info.streakDays, 1),
                    xpPoints = info.xpPoints,
                    coins = (info.xpPoints / 4).coerceAtLeast(100),
                    league = when {
                        info.xpPoints >= 2500 -> "Diamond League"
                        info.xpPoints >= 1800 -> "Platinum League"
                        info.xpPoints >= 1200 -> "Gold League"
                        info.xpPoints >= 700 -> "Silver League"
                        else -> "Bronze League"
                    },
                    overallScore = overall,
                    speakingScore = info.speakingScore,
                    pronunciationScore = info.pronunciationScore,
                    fluencyScore = info.fluencyScore,
                    grammarScore = info.grammarScore,
                    vocabularyScore = info.vocabularyScore,
                    listeningScore = info.listeningScore,
                    readingScore = 80,
                    completedStagesCount = if (info.xpPoints > 1500) 14 else 5,
                    speakingPracticeMinutes = (info.xpPoints / 10).coerceAtLeast(30),
                    lastPracticeDate = getTodayDateString()
                )
            } else {
                StudentProfile(
                    userId = studentId,
                    currentLevel = LearningLevel.BEGINNER,
                    streakDays = 1,
                    xpPoints = 100,
                    coins = 50,
                    league = "Bronze League",
                    overallScore = 75,
                    speakingScore = 75,
                    pronunciationScore = 78,
                    fluencyScore = 72,
                    grammarScore = 80,
                    vocabularyScore = 76,
                    listeningScore = 82,
                    readingScore = 75,
                    completedStagesCount = 0,
                    speakingPracticeMinutes = 0,
                    lastPracticeDate = getTodayDateString()
                )
            }
        }
        _studentProfile.value = initialOrSaved
        appPreferences?.saveStudentProfile(initialOrSaved)
        refreshLeaderboard()
    }

    // Dynamic Speaking Practice Evaluation Recording & CEFR Progression
    fun recordSpeakingPracticeResult(
        eval: com.example.data.model.SpeakingEvaluation,
        practiceMinutesToAdd: Int = 2
    ) {
        if (eval.overallScore <= 0) return

        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        _studentProfile.update { current ->
            // Smooth dynamic rolling average based on student's actual performance
            val newSpeaking = ((current.speakingScore * 2 + eval.overallScore) / 3).coerceIn(40, 99)
            val newPronunciation = ((current.pronunciationScore * 2 + eval.pronunciationScore) / 3).coerceIn(40, 99)
            val newFluency = ((current.fluencyScore * 2 + eval.fluencyScore) / 3).coerceIn(40, 99)
            val newGrammar = if (eval.grammarScore > 0) ((current.grammarScore * 2 + eval.grammarScore) / 3).coerceIn(40, 99) else current.grammarScore
            val newVocab = if (eval.vocabularyScore > 0) ((current.vocabularyScore * 2 + eval.vocabularyScore) / 3).coerceIn(40, 99) else current.vocabularyScore
            val newListening = current.listeningScore
            val newOverall = (newSpeaking + newPronunciation + newFluency + newGrammar + newVocab + newListening) / 6

            // Dynamic CEFR Level calculation according to international CEFR standard
            val newCefrLevel = calculateCefrLevel(newOverall)

            // Daily practice streak logic: increment once per day, or maintain if already practiced today
            val newStreak = when (current.lastPracticeDate) {
                today -> current.streakDays
                yesterday -> current.streakDays + 1
                else -> if (current.lastPracticeDate.isBlank()) maxOf(current.streakDays, 1) else current.streakDays + 1
            }

            val newMinutes = current.speakingPracticeMinutes + practiceMinutesToAdd

            current.copy(
                speakingScore = newSpeaking,
                pronunciationScore = newPronunciation,
                fluencyScore = newFluency,
                grammarScore = newGrammar,
                vocabularyScore = newVocab,
                overallScore = newOverall,
                currentLevel = newCefrLevel,
                streakDays = newStreak,
                speakingPracticeMinutes = newMinutes,
                lastPracticeDate = today
            )
        }

        val updatedProfile = _studentProfile.value
        appPreferences?.saveStudentProfile(updatedProfile)

        // Sync with admin/teacher student roster so teachers & admins see the student's updated streak, scores, and CEFR level!
        val curId = _currentUser.value.id
        _adminStudents.update { list ->
            list.map {
                if (it.id == curId || it.email.equals(_currentUser.value.email, ignoreCase = true)) {
                    it.copy(
                        speakingScore = updatedProfile.speakingScore,
                        pronunciationScore = updatedProfile.pronunciationScore,
                        fluencyScore = updatedProfile.fluencyScore,
                        grammarScore = updatedProfile.grammarScore,
                        vocabularyScore = updatedProfile.vocabularyScore,
                        listeningScore = updatedProfile.listeningScore,
                        level = updatedProfile.currentLevel,
                        streakDays = updatedProfile.streakDays,
                        xpPoints = updatedProfile.xpPoints
                    )
                } else it
            }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    fun incrementDailyStreak() {
        val today = getTodayDateString()
        _studentProfile.update { current ->
            val updatedStreak = if (current.lastPracticeDate == today) current.streakDays else current.streakDays + 1
            current.copy(
                streakDays = updatedStreak,
                lastPracticeDate = today
            )
        }
        val updatedProf = _studentProfile.value
        appPreferences?.saveStudentProfile(updatedProf)
        val curId = _currentUser.value.id
        _adminStudents.update { list ->
            list.map {
                if (it.id == curId || it.email.equals(_currentUser.value.email, ignoreCase = true)) {
                    it.copy(streakDays = updatedProf.streakDays)
                } else it
            }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    // Gamification Updates: Add XP and Coins
    fun addRewards(xp: Int, coins: Int) {
        _studentProfile.update {
            val newXp = it.xpPoints + xp
            val newLeague = when {
                newXp >= 2500 -> "Diamond League"
                newXp >= 1800 -> "Platinum League"
                newXp >= 1200 -> "Gold League"
                newXp >= 700 -> "Silver League"
                else -> "Bronze League"
            }
            it.copy(
                xpPoints = newXp,
                coins = it.coins + coins,
                league = newLeague
            )
        }
        appPreferences?.saveStudentProfile(_studentProfile.value)
        val curId = _currentUser.value.id
        _adminStudents.update { list ->
            list.map {
                if (it.id == curId || it.email.equals(_currentUser.value.email, ignoreCase = true)) {
                    it.copy(xpPoints = _studentProfile.value.xpPoints)
                } else it
            }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    // Official CEFR Diagnostic assessment
    fun runCefrDiagnostic(
        speaking: Int,
        pronunciation: Int,
        fluency: Int,
        grammar: Int,
        vocab: Int,
        listening: Int = _studentProfile.value.listeningScore
    ): LearningLevel {
        val overall = (speaking + pronunciation + fluency + grammar + vocab + listening) / 6
        val newLevel = calculateCefrLevel(overall)
        _studentProfile.update { current ->
            current.copy(
                speakingScore = speaking,
                pronunciationScore = pronunciation,
                fluencyScore = fluency,
                grammarScore = grammar,
                vocabularyScore = vocab,
                listeningScore = listening,
                overallScore = overall,
                currentLevel = newLevel
            )
        }
        val updated = _studentProfile.value
        appPreferences?.saveStudentProfile(updated)
        val curId = _currentUser.value.id
        _adminStudents.update { list ->
            list.map {
                if (it.id == curId || it.email.equals(_currentUser.value.email, ignoreCase = true)) {
                    it.copy(
                        speakingScore = speaking,
                        pronunciationScore = pronunciation,
                        fluencyScore = fluency,
                        grammarScore = grammar,
                        vocabularyScore = vocab,
                        listeningScore = listening,
                        level = newLevel
                    )
                } else it
            }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
        return newLevel
    }

    // Complete a stage
    fun completeStage(stageId: String, stars: Int, xpEarned: Int, coinsEarned: Int) {
        _stages.update { list ->
            list.map { stage ->
                if (stage.id == stageId) {
                    stage.copy(
                        isCompleted = true,
                        stars = maxOf(stage.stars, stars)
                    )
                } else stage
            }
        }
        addRewards(xpEarned, coinsEarned)
        _studentProfile.update {
            it.copy(
                completedStagesCount = it.completedStagesCount + 1,
                streakDays = it.streakDays + 1
            )
        }
        appPreferences?.saveStudentProfile(_studentProfile.value)
    }

    // Toggle Vocabulary Favorite
    fun toggleWordFavorite(wordId: String) {
        _vocabularyWords.update { words ->
            words.map {
                if (it.id == wordId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
        appPreferences?.saveVocabularyWords(_vocabularyWords.value)
    }

    // Teacher & Faculty vocabulary word addition with Assamese meaning
    fun addVocabularyWord(word: VocabularyWord) {
        _vocabularyWords.update { currentList ->
            val index = currentList.indexOfFirst { it.id == word.id || it.word.equals(word.word, ignoreCase = true) }
            if (index >= 0) {
                currentList.toMutableList().apply { set(index, word) }
            } else {
                listOf(word) + currentList
            }
        }
        appPreferences?.saveVocabularyWords(_vocabularyWords.value)
        registerWordInEngines(word)
    }

    fun deleteVocabularyWord(wordId: String) {
        val wordToDelete = _vocabularyWords.value.find { it.id == wordId }
        _vocabularyWords.update { list -> list.filterNot { it.id == wordId } }
        appPreferences?.saveVocabularyWords(_vocabularyWords.value)
        wordToDelete?.let {
            com.example.service.PhoneticsAiEngine.removeCustomPhoneticWord(it.word)
            com.example.util.AssameseTranslator.removeCustomWord(it.word)
        }
    }

    fun registerWordInEngines(word: VocabularyWord) {
        val cleanWord = word.word.trim().replaceFirstChar { it.uppercase() }
        com.example.service.PhoneticsAiEngine.addCustomPhoneticWord(
            com.example.service.PhoneticSearchResult(
                word = cleanWord,
                ipa = word.phonetic.ifBlank { "/${word.word.lowercase()}/" },
                syllables = word.syllables.ifBlank { word.word },
                audioFocus = "Vocabulary Word",
                difficulty = "Medium",
                phoneticBreakdown = "${word.partOfSpeech.replaceFirstChar { it.uppercase() }} • Added by ${word.addedByTeacher.ifBlank { "Faculty Instructor" }}",
                coachingTip = word.coachingTip.ifBlank { "Practice clear articulation and use it in your daily English speaking practice." },
                exampleSentence = word.exampleSentence,
                meaning = word.meaning,
                assameseMeaning = word.assameseMeaning,
                assameseTransliteration = word.assameseTransliteration,
                soundTags = listOf(word.category, if (word.addedByTeacher.isNotBlank()) "Teacher Added" else "Core Lexicon")
            )
        )
        if (word.assameseMeaning.isNotBlank()) {
            com.example.util.AssameseTranslator.addCustomWord(cleanWord, word.assameseMeaning, word.assameseTransliteration)
        }
    }

    fun mergeRemoteVocabulary(remoteWords: List<VocabularyWord>) {
        if (remoteWords.isEmpty()) return
        _vocabularyWords.update { currentList ->
            val idMap = currentList.associateBy { it.id }.toMutableMap()
            val textMap = currentList.associateBy { it.word.lowercase().trim() }.toMutableMap()
            remoteWords.forEach { rw ->
                val existing = idMap[rw.id] ?: textMap[rw.word.lowercase().trim()]
                if (existing != null) {
                    idMap[existing.id] = rw
                } else {
                    idMap[rw.id] = rw
                }
            }
            idMap.values.toList()
        }
        appPreferences?.saveVocabularyWords(_vocabularyWords.value)
        _vocabularyWords.value.forEach { registerWordInEngines(it) }
    }

    // Teacher grades a submission
    fun gradeSubmission(submissionId: String, score: Int, feedback: String) {
        _submissions.update { list ->
            list.map {
                if (it.id == submissionId) {
                    it.copy(
                        score = score,
                        teacherFeedback = feedback,
                        status = "GRADED"
                    )
                } else it
            }
        }
    }

    fun addStudentSubmission(submission: StudentSubmission) {
        _submissions.update { list -> listOf(submission) + list }
    }

    // --- STUDENT MANAGEMENT METHODS ---
    fun addStudent(name: String, email: String, batch: String, level: LearningLevel = LearningLevel.BEGINNER, notes: String = "") {
        val newId = "std_${System.currentTimeMillis() % 10000}"
        val newStudent = StudentManagementInfo(
            id = newId,
            name = name,
            email = email,
            avatarEmoji = listOf("🎓", "🌟", "🚀", "💡", "🌿", "🦋").random(),
            batch = batch,
            level = level,
            isActive = true,
            speakingScore = 75,
            pronunciationScore = 78,
            fluencyScore = 72,
            grammarScore = 80,
            vocabularyScore = 76,
            listeningScore = 82,
            streakDays = 1,
            xpPoints = 100,
            joinedDate = "Nov 2026",
            teacherNotes = notes.ifBlank { "Newly enrolled student. Initial speaking diagnostic scheduled." }
        )
        _adminStudents.update { it + newStudent }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    fun updateStudent(updated: StudentManagementInfo) {
        _adminStudents.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        if (_studentProfile.value.userId == updated.id) {
            _studentProfile.update {
                it.copy(
                    currentLevel = updated.level,
                    streakDays = updated.streakDays,
                    speakingScore = updated.speakingScore,
                    pronunciationScore = updated.pronunciationScore,
                    fluencyScore = updated.fluencyScore,
                    grammarScore = updated.grammarScore,
                    vocabularyScore = updated.vocabularyScore,
                    listeningScore = updated.listeningScore,
                    overallScore = (updated.speakingScore + updated.pronunciationScore + updated.fluencyScore + updated.grammarScore + updated.vocabularyScore + updated.listeningScore) / 6
                )
            }
            appPreferences?.saveStudentProfile(_studentProfile.value)
        }
        refreshLeaderboard()
    }

    fun deleteStudent(studentId: String) {
        _adminStudents.update { list ->
            list.filterNot { it.id == studentId }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    fun toggleStudentActive(studentId: String) {
        _adminStudents.update { list ->
            list.map {
                if (it.id == studentId) it.copy(isActive = !it.isActive) else it
            }
        }
        appPreferences?.saveStudents(_adminStudents.value)
        refreshLeaderboard()
    }

    fun bulkImportStudents(count: Int) {
        val sampleNames = listOf("Liam O'Connor", "Zoe Kravitz", "Carlos Mendez", "Fatima Zahra", "Min-jun Park", "Aisha Diallo")
        val classes = AcademicClasses.ALL_CLASSES
        val levels = listOf(LearningLevel.BEGINNER, LearningLevel.ELEMENTARY, LearningLevel.PRE_INTERMEDIATE)
        val newStudents = sampleNames.take(count).mapIndexed { i, name ->
            val id = "std_${System.currentTimeMillis() % 1000 + i}"
            StudentManagementInfo(
                id = id,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@linguaquest.edu",
                avatarEmoji = listOf("🦄", "🦁", "🦊", "🐬", "🌸").random(),
                batch = classes[i % classes.size],
                level = levels[i % levels.size],
                isActive = true,
                speakingScore = (70..90).random(),
                pronunciationScore = (72..92).random(),
                fluencyScore = (68..88).random(),
                grammarScore = (75..95).random(),
                vocabularyScore = (70..90).random(),
                listeningScore = (75..95).random(),
                streakDays = (1..10).random(),
                xpPoints = (500..2000).random(),
                joinedDate = "Nov 2026",
                teacherNotes = "Enrolled via batch CSV import. Standard diagnostic profile created."
            )
        }
        _adminStudents.update { it + newStudents }
        appPreferences?.saveStudents(_adminStudents.value)
    }

    // --- ACADEMIC CLASS MANAGEMENT METHODS ---
    fun addClass(className: String): Boolean {
        val clean = className.trim()
        if (clean.isBlank()) return false
        if (_academicClasses.value.any { it.equals(clean, ignoreCase = true) }) return false
        _academicClasses.update { it + clean }
        appPreferences?.saveClasses(_academicClasses.value)
        return true
    }

    fun deleteClass(className: String): Boolean {
        val clean = className.trim()
        if (_academicClasses.value.none { it.equals(clean, ignoreCase = true) }) return false
        _academicClasses.update { list -> list.filterNot { it.equals(clean, ignoreCase = true) } }
        appPreferences?.saveClasses(_academicClasses.value)
        return true
    }

    // --- GAME ADDITION & MANAGEMENT METHODS ---
    fun addWordMatchChallenge(word: String, definition: String, category: String = "General", createdBy: String): WordMatchChallenge {
        val id = "wm_${System.currentTimeMillis()}"
        val item = WordMatchChallenge(id, word.trim(), definition.trim(), category.trim(), createdBy)
        _wordMatchChallenges.update { it + item }
        appPreferences?.saveWordMatchChallenges(_wordMatchChallenges.value)
        return item
    }

    fun deleteWordMatchChallenge(id: String) {
        _wordMatchChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveWordMatchChallenges(_wordMatchChallenges.value)
    }

    fun updateWordMatchChallenge(updated: WordMatchChallenge) {
        _wordMatchChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveWordMatchChallenges(_wordMatchChallenges.value)
    }

    fun addMissingLettersChallenge(
        fullWord: String,
        hint: String,
        category: String = "General",
        createdBy: String
    ): MissingLettersChallenge {
        val id = "ml_${System.currentTimeMillis()}"
        val cleaned = fullWord.trim().uppercase()
        // Automatically choose 2-3 vowel or interior indices to mask if length permits
        val indices = if (cleaned.length >= 6) {
            listOf(1, cleaned.length / 2, cleaned.length - 2)
        } else if (cleaned.length >= 4) {
            listOf(1, cleaned.length - 2)
        } else {
            listOf(1)
        }
        val targetCat = category.trim().ifBlank { "General" }
        addMissingLettersCategory(targetCat)
        val item = MissingLettersChallenge(id, cleaned, hint.trim(), indices, targetCat, createdBy)
        _missingLettersChallenges.update { it + item }
        appPreferences?.saveMissingLettersChallenges(_missingLettersChallenges.value)
        return item
    }

    fun bulkImportMissingLetters(
        rows: List<Triple<String, String, String>>,
        createdBy: String
    ): Int {
        if (rows.isEmpty()) return 0
        val timestamp = System.currentTimeMillis()
        var count = 0
        val newChallenges = mutableListOf<MissingLettersChallenge>()

        rows.forEachIndexed { index, (word, hint, cat) ->
            val cleaned = word.trim().uppercase()
            if (cleaned.isNotBlank()) {
                val indices = if (cleaned.length >= 6) {
                    listOf(1, cleaned.length / 2, cleaned.length - 2)
                } else if (cleaned.length >= 4) {
                    listOf(1, cleaned.length - 2)
                } else {
                    listOf(1)
                }
                val targetCat = cat.trim().ifBlank { "General" }
                addMissingLettersCategory(targetCat)
                val finalHint = hint.trim().ifBlank { "English Vocabulary Word" }
                val id = "ml_${timestamp}_$index"
                newChallenges.add(MissingLettersChallenge(id, cleaned, finalHint, indices, targetCat, createdBy))
                count++
            }
        }

        if (newChallenges.isNotEmpty()) {
            _missingLettersChallenges.update { it + newChallenges }
            appPreferences?.saveMissingLettersChallenges(_missingLettersChallenges.value)
        }
        return count
    }

    fun deleteMissingLettersChallenge(id: String) {
        _missingLettersChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveMissingLettersChallenges(_missingLettersChallenges.value)
    }

    fun updateMissingLettersChallenge(updated: MissingLettersChallenge) {
        _missingLettersChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveMissingLettersChallenges(_missingLettersChallenges.value)
    }

    fun addSentenceBuilderPack(
        title: String,
        description: String,
        difficulty: String = "Easy",
        xpReward: Int = 40,
        createdBy: String
    ): SentenceBuilderPack {
        val id = "sb_pack_${System.currentTimeMillis()}"
        val pack = SentenceBuilderPack(
            id = id,
            title = title.trim().ifBlank { "Custom Challenge" },
            description = description.trim().ifBlank { "Arrange words into grammatical sentences." },
            difficulty = difficulty.trim().ifBlank { "Easy" },
            xpReward = xpReward.coerceIn(10, 500),
            createdBy = createdBy
        )
        _sentenceBuilderPacks.update { it + pack }
        appPreferences?.saveSentenceBuilderPacks(_sentenceBuilderPacks.value)
        return pack
    }

    fun updateSentenceBuilderPack(updated: SentenceBuilderPack) {
        _sentenceBuilderPacks.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveSentenceBuilderPacks(_sentenceBuilderPacks.value)
    }

    fun deleteSentenceBuilderPack(packId: String): List<String> {
        val removedSentenceIds = _sentenceBuilderChallenges.value
            .filter { it.packId == packId }
            .map { it.id }
        _sentenceBuilderPacks.update { it.filterNot { pack -> pack.id == packId } }
        appPreferences?.saveSentenceBuilderPacks(_sentenceBuilderPacks.value)

        if (removedSentenceIds.isNotEmpty()) {
            _sentenceBuilderChallenges.update { it.filterNot { item -> item.packId == packId } }
            appPreferences?.saveSentenceBuilderChallenges(_sentenceBuilderChallenges.value)
        }
        return removedSentenceIds
    }

    fun addSentenceBuilderChallenge(
        targetSentence: String,
        hint: String = "Assemble syntax",
        assameseTranslation: String = "",
        createdBy: String,
        packId: String = "sb_pack_easy"
    ): SentenceBuilderChallenge {
        val id = "sb_${System.currentTimeMillis()}"
        val resolvedPackId = packId.ifBlank { "sb_pack_easy" }
        val item = SentenceBuilderChallenge(
            id = id,
            targetSentence = targetSentence.trim(),
            hint = hint.trim(),
            assameseTranslation = assameseTranslation.trim(),
            createdBy = createdBy,
            packId = resolvedPackId
        )
        _sentenceBuilderChallenges.update { it + item }
        appPreferences?.saveSentenceBuilderChallenges(_sentenceBuilderChallenges.value)
        return item
    }

    fun bulkImportSentenceBuilders(
        rows: List<Triple<String, String, String>>,
        createdBy: String,
        packId: String = "sb_pack_easy"
    ): Int {
        if (rows.isEmpty()) return 0
        val timestamp = System.currentTimeMillis()
        val resolvedPackId = packId.ifBlank { "sb_pack_easy" }
        var count = 0
        val newChallenges = mutableListOf<SentenceBuilderChallenge>()

        rows.forEachIndexed { index, (sentence, hint, assamese) ->
            val cleaned = sentence.trim()
            if (cleaned.isNotBlank()) {
                val finalHint = hint.trim().ifBlank { "Assemble syntax" }
                val finalAssamese = assamese.trim()
                val id = "sb_${timestamp}_$index"
                newChallenges.add(
                    SentenceBuilderChallenge(
                        id = id,
                        targetSentence = cleaned,
                        hint = finalHint,
                        assameseTranslation = finalAssamese,
                        createdBy = createdBy,
                        packId = resolvedPackId
                    )
                )
                count++
            }
        }

        if (newChallenges.isNotEmpty()) {
            _sentenceBuilderChallenges.update { it + newChallenges }
            appPreferences?.saveSentenceBuilderChallenges(_sentenceBuilderChallenges.value)
        }
        return count
    }

    fun deleteSentenceBuilderChallenge(id: String) {
        _sentenceBuilderChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveSentenceBuilderChallenges(_sentenceBuilderChallenges.value)
    }

    fun updateSentenceBuilderChallenge(updated: SentenceBuilderChallenge) {
        _sentenceBuilderChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveSentenceBuilderChallenges(_sentenceBuilderChallenges.value)
    }

    fun addListenChooseChallenge(audioScript: String, question: String, options: List<String>, correctIndex: Int, createdBy: String): ListenChooseChallenge {
        val id = "lc_${System.currentTimeMillis()}"
        val item = ListenChooseChallenge(id, audioScript.trim(), question.trim(), options.map { it.trim() }, correctIndex, createdBy)
        _listenChooseChallenges.update { it + item }
        appPreferences?.saveListenChooseChallenges(_listenChooseChallenges.value)
        return item
    }

    fun deleteListenChooseChallenge(id: String) {
        _listenChooseChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveListenChooseChallenges(_listenChooseChallenges.value)
    }

    fun updateListenChooseChallenge(updated: ListenChooseChallenge) {
        _listenChooseChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveListenChooseChallenges(_listenChooseChallenges.value)
    }

    fun addPronunciationBattleChallenge(sentence: String, phonemeFocus: String, difficulty: String = "Medium", passingScore: Int = 80, createdBy: String): PronunciationBattleChallenge {
        val id = "pb_${System.currentTimeMillis()}"
        val item = PronunciationBattleChallenge(id, sentence.trim(), phonemeFocus.trim(), difficulty, passingScore, createdBy)
        _pronunciationBattleChallenges.update { it + item }
        appPreferences?.savePronunciationBattleChallenges(_pronunciationBattleChallenges.value)
        return item
    }

    fun deletePronunciationBattleChallenge(id: String) {
        _pronunciationBattleChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.savePronunciationBattleChallenges(_pronunciationBattleChallenges.value)
    }

    fun updatePronunciationBattleChallenge(updated: PronunciationBattleChallenge) {
        _pronunciationBattleChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.savePronunciationBattleChallenges(_pronunciationBattleChallenges.value)
    }

    fun addFastVocabularyChallenge(word: String, prompt: String, options: List<String>, correctIndex: Int, createdBy: String): FastVocabularyChallenge {
        val id = "fv_${System.currentTimeMillis()}"
        val item = FastVocabularyChallenge(id, word.trim(), prompt.trim(), options.map { it.trim() }, correctIndex, createdBy)
        _fastVocabularyChallenges.update { it + item }
        appPreferences?.saveFastVocabularyChallenges(_fastVocabularyChallenges.value)
        return item
    }

    fun deleteFastVocabularyChallenge(id: String) {
        _fastVocabularyChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveFastVocabularyChallenges(_fastVocabularyChallenges.value)
    }

    fun updateFastVocabularyChallenge(updated: FastVocabularyChallenge) {
        _fastVocabularyChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveFastVocabularyChallenges(_fastVocabularyChallenges.value)
    }

    fun addGrammarChallenge(question: String, options: List<String>, correctIndex: Int, explanation: String, category: String = "Grammar", createdBy: String): GrammarChallengeQuestion {
        val id = "gc_${System.currentTimeMillis()}"
        val item = GrammarChallengeQuestion(id, question.trim(), options.map { it.trim() }, correctIndex, explanation.trim(), category.trim(), createdBy)
        _grammarChallenges.update { it + item }
        appPreferences?.saveGrammarChallenges(_grammarChallenges.value)
        return item
    }

    fun deleteGrammarChallenge(id: String) {
        _grammarChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.saveGrammarChallenges(_grammarChallenges.value)
    }

    fun updateGrammarChallenge(updated: GrammarChallengeQuestion) {
        _grammarChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.saveGrammarChallenges(_grammarChallenges.value)
    }

    fun addPictureVocabChallenge(
        emoji: String,
        word: String,
        phonetic: String,
        clue: String,
        options: List<String>,
        createdBy: String
    ): PictureVocabChallenge {
        val id = "pv_${System.currentTimeMillis()}"
        val item = PictureVocabChallenge(
            id = id,
            emoji = emoji.trim().ifBlank { "🖼️" },
            word = word.trim(),
            phonetic = phonetic.trim().ifBlank { "/${word.trim().lowercase()}/" },
            clue = clue.trim(),
            options = options.map { it.trim() }.filter { it.isNotBlank() },
            createdBy = createdBy
        )
        _pictureVocabChallenges.update { it + item }
        appPreferences?.savePictureVocabChallenges(_pictureVocabChallenges.value)
        return item
    }

    fun deletePictureVocabChallenge(id: String) {
        _pictureVocabChallenges.update { it.filterNot { item -> item.id == id } }
        appPreferences?.savePictureVocabChallenges(_pictureVocabChallenges.value)
    }

    fun updatePictureVocabChallenge(updated: PictureVocabChallenge) {
        _pictureVocabChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        appPreferences?.savePictureVocabChallenges(_pictureVocabChallenges.value)
    }

    // Admin: Add Teacher with Security PIN
    fun addTeacher(name: String, email: String, department: String, securityPin: String = "7321"): User {
        val newId = "tch_${System.currentTimeMillis() % 10000}"
        val pin = if (securityPin.isNotBlank()) securityPin.trim() else "7321"
        val newTeacher = User(
            id = newId,
            name = name.trim(),
            email = email.trim(),
            role = Role.TEACHER,
            avatarEmoji = listOf("👩‍🏫", "👨‍🏫", "👩‍💼", "🧑‍🏫").random(),
            batch = department.trim().ifBlank { "Spoken English Dept" },
            isActive = true,
            securityPin = pin
        )
        _adminTeachers.update { it + newTeacher }
        appPreferences?.saveTeachers(_adminTeachers.value)
        return newTeacher
    }

    // Admin: Update Teacher
    fun updateTeacher(updated: User) {
        _adminTeachers.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        if (_currentUser.value.id == updated.id) {
            _currentUser.value = updated
        }
        appPreferences?.saveTeachers(_adminTeachers.value)
    }

    // Admin: Delete Teacher
    fun deleteTeacher(teacherId: String) {
        _adminTeachers.update { list ->
            list.filterNot { it.id == teacherId }
        }
        if (_currentUser.value.id == teacherId) {
            _currentUser.value = _adminTeachers.value.firstOrNull { it.isActive }
                ?: User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Classes 9 & 10", true, securityPin = "7321")
        }
        appPreferences?.saveTeachers(_adminTeachers.value)
    }

    // Admin: Toggle Teacher Active Status
    fun toggleTeacherActive(teacherId: String) {
        _adminTeachers.update { list ->
            list.map { if (it.id == teacherId) it.copy(isActive = !it.isActive) else it }
        }
        if (_currentUser.value.id == teacherId) {
            _currentUser.value = _currentUser.value.copy(isActive = !_currentUser.value.isActive)
        }
        appPreferences?.saveTeachers(_adminTeachers.value)
    }

    // Self-registration for Teacher
    fun registerTeacher(name: String, email: String, department: String, securityPin: String): User {
        val newId = "tch_${System.currentTimeMillis() % 10000}"
        val pin = if (securityPin.isNotBlank()) securityPin.trim() else "7321"
        val newTeacher = User(
            id = newId,
            name = name.trim(),
            email = email.trim(),
            role = Role.TEACHER,
            avatarEmoji = listOf("👩‍🏫", "👨‍🏫", "👩‍💼", "🧑‍🏫").random(),
            batch = department.trim().ifBlank { "Spoken English Dept" },
            isActive = true,
            securityPin = pin
        )
        _adminTeachers.update { it + newTeacher }
        _currentUser.value = newTeacher
        appPreferences?.saveTeachers(_adminTeachers.value)
        return newTeacher
    }

    // Self-registration for Student
    fun registerStudent(name: String, email: String, batch: String, securityPin: String): User {
        val newId = "std_${System.currentTimeMillis() % 10000}"
        val pin = if (securityPin.isNotBlank()) securityPin.trim() else "1234"
        val chosenBatch = batch.trim().ifBlank { AcademicClasses.DEFAULT_CLASS }
        val newStudent = StudentManagementInfo(
            id = newId,
            name = name.trim(),
            email = email.trim(),
            avatarEmoji = listOf("🎓", "🌟", "🚀", "💡", "🌿", "🦋").random(),
            batch = chosenBatch,
            level = LearningLevel.BEGINNER,
            isActive = true,
            speakingScore = 75,
            pronunciationScore = 78,
            fluencyScore = 72,
            grammarScore = 80,
            vocabularyScore = 76,
            listeningScore = 82,
            streakDays = 1,
            xpPoints = 100,
            joinedDate = "Nov 2026",
            teacherNotes = "New student registered via portal.",
            securityPin = pin
        )
        _adminStudents.update { it + newStudent }
        appPreferences?.saveStudents(_adminStudents.value)

        val studentUser = User(
            id = newId,
            name = newStudent.name,
            email = newStudent.email,
            role = Role.STUDENT,
            avatarEmoji = newStudent.avatarEmoji,
            batch = newStudent.batch,
            isActive = true,
            securityPin = pin
        )
        _currentUser.value = studentUser

        val newProfile = StudentProfile(
            userId = newId,
            currentLevel = LearningLevel.BEGINNER,
            streakDays = 1,
            xpPoints = 100,
            coins = 50,
            league = "Bronze League",
            overallScore = 75,
            speakingScore = 75,
            pronunciationScore = 78,
            fluencyScore = 72,
            grammarScore = 80,
            vocabularyScore = 76,
            listeningScore = 82,
            readingScore = 75,
            completedStagesCount = 0,
            speakingPracticeMinutes = 0,
            lastPracticeDate = getTodayDateString()
        )
        _studentProfile.value = newProfile
        appPreferences?.saveStudentProfile(newProfile)
        refreshLeaderboard()

        return studentUser
    }

    // -----------------------------------------------------------------
    // Admin & Teacher: AI Speaking & Phonetics Management
    // -----------------------------------------------------------------

    fun addSpeakingPrompt(
        title: String,
        category: String,
        question: String,
        followUpTip: String,
        recommendedKeywords: List<String>,
        minSpeakingSeconds: Int = 20,
        emoji: String = "💬",
        difficultyLevel: String = "Intermediate",
        createdBy: String = "Faculty",
        milestoneTurns: Int = 0
    ): SpeakingPrompt {
        val newPrompt = SpeakingPrompt(
            id = "sp_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category.trim().ifBlank { "General Speaking" },
            question = question.trim(),
            followUpTip = followUpTip.trim(),
            recommendedKeywords = recommendedKeywords.map { it.trim() }.filter { it.isNotBlank() },
            minSpeakingSeconds = minSpeakingSeconds,
            emoji = emoji.trim().ifBlank { "💬" },
            difficultyLevel = difficultyLevel.trim().ifBlank { "Intermediate" },
            createdBy = createdBy.trim().ifBlank { "Faculty" },
            milestoneTurns = if (milestoneTurns > 0) milestoneTurns.coerceIn(1, 30) else 0
        )
        _speakingPrompts.update { it + newPrompt }
        appPreferences?.saveSpeakingPrompts(_speakingPrompts.value)
        return newPrompt
    }

    fun updateSpeakingPrompt(prompt: SpeakingPrompt) {
        _speakingPrompts.update { list ->
            list.map { if (it.id == prompt.id) prompt else it }
        }
        appPreferences?.saveSpeakingPrompts(_speakingPrompts.value)
    }

    fun deleteSpeakingPrompt(promptId: String) {
        _speakingPrompts.update { list ->
            list.filterNot { it.id == promptId }
        }
        appPreferences?.saveSpeakingPrompts(_speakingPrompts.value)
    }

    fun addPronunciationDrill(
        wordOrSentence: String,
        ipaPhonetic: String,
        audioFocus: String,
        difficulty: String,
        tips: String
    ): PronunciationDrill {
        val newDrill = PronunciationDrill(
            id = "pd_${System.currentTimeMillis()}",
            wordOrSentence = wordOrSentence.trim(),
            ipaPhonetic = ipaPhonetic.trim().ifBlank { "/.../" },
            audioFocus = audioFocus.trim().ifBlank { "Standard Pronunciation" },
            difficulty = difficulty.trim().ifBlank { "Medium" },
            tips = tips.trim()
        )
        _pronunciationDrills.update { it + newDrill }
        appPreferences?.savePronunciationDrills(_pronunciationDrills.value)
        return newDrill
    }

    fun updatePronunciationDrill(drill: PronunciationDrill) {
        _pronunciationDrills.update { list ->
            list.map { if (it.id == drill.id) drill else it }
        }
        appPreferences?.savePronunciationDrills(_pronunciationDrills.value)
    }

    fun deletePronunciationDrill(drillId: String) {
        _pronunciationDrills.update { list ->
            list.filterNot { it.id == drillId }
        }
        appPreferences?.savePronunciationDrills(_pronunciationDrills.value)
    }

    fun addRoleplayScenario(
        title: String,
        situation: String,
        partnerRole: String,
        userRole: String,
        difficulty: String,
        emoji: String,
        turns: List<DialogueTurn>
    ): RoleplayScenario {
        val newScenario = RoleplayScenario(
            id = "rp_${System.currentTimeMillis()}",
            title = title.trim(),
            situation = situation.trim(),
            partnerRole = partnerRole.trim(),
            userRole = userRole.trim(),
            difficulty = difficulty.trim().ifBlank { "Intermediate" },
            emoji = emoji.ifBlank { "🎭" },
            turns = turns
        )
        _roleplays.update { it + newScenario }
        return newScenario
    }

    fun deleteRoleplayScenario(scenarioId: String) {
        _roleplays.update { list ->
            list.filterNot { it.id == scenarioId }
        }
    }

    // =========================================================================
    // REMOTE SUPABASE CLOUD SYNC MERGERS
    // =========================================================================

    fun mergeRemoteStudents(remoteStudents: List<StudentManagementInfo>) {
        if (remoteStudents.isEmpty()) return
        val current = _adminStudents.value.toMutableList()
        var changed = false
        for (remote in remoteStudents) {
            val existingIndex = current.indexOfFirst {
                it.id == remote.id || (it.email.isNotBlank() && it.email.equals(remote.email, ignoreCase = true))
            }
            if (existingIndex >= 0) {
                // Keep local high score / progress if higher
                val existing = current[existingIndex]
                current[existingIndex] = remote.copy(
                    xpPoints = maxOf(existing.xpPoints, remote.xpPoints),
                    streakDays = maxOf(existing.streakDays, remote.streakDays),
                    securityPin = if (remote.securityPin.isNotBlank()) remote.securityPin else existing.securityPin,
                    batch = if (remote.batch.isNotBlank() && !remote.batch.equals("Batch Alpha", ignoreCase = true)) remote.batch else existing.batch
                )
                changed = true
            } else {
                current.add(remote)
                changed = true
            }
        }
        if (changed) {
            _adminStudents.value = current
            appPreferences?.saveStudents(current)
            refreshLeaderboard()
        }
    }

    fun mergeRemoteTeachers(remoteTeachers: List<User>) {
        if (remoteTeachers.isEmpty()) return
        val current = _adminTeachers.value.toMutableList()
        var changed = false
        for (remote in remoteTeachers) {
            val existingIndex = current.indexOfFirst {
                it.id == remote.id || (it.email.isNotBlank() && it.email.equals(remote.email, ignoreCase = true))
            }
            if (existingIndex >= 0) {
                val existing = current[existingIndex]
                current[existingIndex] = remote.copy(
                    securityPin = if (remote.securityPin.isNotBlank()) remote.securityPin else existing.securityPin
                )
                changed = true
            } else {
                current.add(remote)
                changed = true
            }
        }
        if (changed) {
            _adminTeachers.value = current
            appPreferences?.saveTeachers(current)
        }
    }

    fun mergeRemoteAssignments(remoteAssignments: List<Assignment>) {
        if (remoteAssignments.isEmpty()) return
        val current = _assignments.value.toMutableList()
        var changed = false
        for (remote in remoteAssignments) {
            val existingIndex = current.indexOfFirst { it.id == remote.id }
            if (existingIndex >= 0) {
                current[existingIndex] = remote
                changed = true
            } else {
                current.add(0, remote)
                changed = true
            }
        }
        if (changed) {
            _assignments.value = current
            appPreferences?.saveAssignments(current)
        }
    }

    fun mergeRemoteGameChallenges(remoteChallenges: List<SupabaseGameChallenge>) {
        if (remoteChallenges.isEmpty()) return
        var sbPackChanged = false
        var sbChanged = false
        var wmChanged = false
        var mlChanged = false
        var lcChanged = false
        var pbChanged = false
        var fvChanged = false
        var gcChanged = false
        var pvChanged = false

        val currentSbPacks = _sentenceBuilderPacks.value.toMutableList()
        val currentSb = _sentenceBuilderChallenges.value.toMutableList()
        val currentWm = _wordMatchChallenges.value.toMutableList()
        val currentMl = _missingLettersChallenges.value.toMutableList()
        val currentLc = _listenChooseChallenges.value.toMutableList()
        val currentPb = _pronunciationBattleChallenges.value.toMutableList()
        val currentFv = _fastVocabularyChallenges.value.toMutableList()
        val currentGc = _grammarChallenges.value.toMutableList()
        val currentPv = _pictureVocabChallenges.value.toMutableList()

        for (rc in remoteChallenges) {
            when (rc.type.uppercase()) {
                "SENTENCE_BUILDER_PACK" -> {
                    val idx = currentSbPacks.indexOfFirst { it.id == rc.id }
                    val pack = SentenceBuilderPack(
                        id = rc.id,
                        title = rc.titleOrWord.ifBlank { "Sentence Challenge" },
                        description = rc.hintOrMeaning.ifBlank { "Build grammatically correct sentences." },
                        difficulty = rc.category.ifBlank { "Easy" },
                        xpReward = if (rc.correctIndex > 0) rc.correctIndex else 40,
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentSbPacks[idx] = pack
                    } else {
                        currentSbPacks.add(pack)
                    }
                    sbPackChanged = true
                }
                "SENTENCE_BUILDER" -> {
                    val resolvedPackId = if (
                        rc.category.isBlank() ||
                        rc.category.equals("Syntax", ignoreCase = true) ||
                        rc.category.equals("General", ignoreCase = true)
                    ) {
                        "sb_pack_easy"
                    } else {
                        rc.category
                    }
                    val idx = currentSb.indexOfFirst { it.id == rc.id || (it.targetSentence.isNotBlank() && it.targetSentence.equals(rc.titleOrWord, ignoreCase = true) && it.packId == resolvedPackId) }
                    val item = SentenceBuilderChallenge(
                        id = rc.id,
                        targetSentence = rc.titleOrWord,
                        hint = rc.hintOrMeaning.ifBlank { "Form a grammatically correct sentence" },
                        assameseTranslation = rc.extraData,
                        createdBy = rc.createdBy,
                        packId = resolvedPackId
                    )
                    if (idx >= 0) {
                        currentSb[idx] = item
                    } else {
                        currentSb.add(item)
                    }
                    sbChanged = true
                }
                "WORD_MATCH" -> {
                    val idx = currentWm.indexOfFirst { it.id == rc.id || (it.word.isNotBlank() && it.word.equals(rc.titleOrWord, ignoreCase = true)) }
                    val item = WordMatchChallenge(
                        id = rc.id,
                        word = rc.titleOrWord,
                        definition = rc.hintOrMeaning,
                        category = rc.category,
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentWm[idx] = item
                    } else {
                        currentWm.add(item)
                    }
                    wmChanged = true
                }
                "MISSING_LETTERS" -> {
                    val idx = currentMl.indexOfFirst { it.id == rc.id || (it.fullWord.isNotBlank() && it.fullWord.equals(rc.titleOrWord, ignoreCase = true) && it.category.equals(rc.category, ignoreCase = true)) }
                    val parsedIndices = rc.extraData.split(",").mapNotNull { it.trim().toIntOrNull() }
                    val wordLen = rc.titleOrWord.length
                    val indices = if (parsedIndices.isNotEmpty()) {
                        parsedIndices.filter { it in 0 until wordLen }.ifEmpty { if (wordLen > 1) listOf(1) else listOf(0) }
                    } else {
                        if (wordLen > 3) listOf(1, wordLen - 2) else if (wordLen > 1) listOf(1) else listOf(0)
                    }
                    val item = MissingLettersChallenge(
                        id = rc.id,
                        fullWord = rc.titleOrWord,
                        hint = rc.hintOrMeaning,
                        missingIndices = indices,
                        category = rc.category.ifBlank { "General" },
                        createdBy = rc.createdBy
                    )
                    if (rc.category.isNotBlank()) {
                        addMissingLettersCategory(rc.category)
                    }
                    if (idx >= 0) {
                        currentMl[idx] = item
                    } else {
                        currentMl.add(item)
                    }
                    mlChanged = true
                }
                "LISTEN_CHOOSE" -> {
                    val idx = currentLc.indexOfFirst { it.id == rc.id }
                    val item = ListenChooseChallenge(
                        id = rc.id,
                        audioScript = rc.titleOrWord,
                        question = rc.hintOrMeaning,
                        options = rc.options,
                        correctIndex = rc.correctIndex,
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentLc[idx] = item
                    } else {
                        currentLc.add(item)
                    }
                    lcChanged = true
                }
                "PRONUNCIATION_BATTLE" -> {
                    val idx = currentPb.indexOfFirst { it.id == rc.id }
                    val passingScore = rc.extraData.toIntOrNull() ?: 80
                    val item = PronunciationBattleChallenge(
                        id = rc.id,
                        challengeSentence = rc.titleOrWord,
                        targetPhonemeFocus = rc.hintOrMeaning,
                        difficulty = rc.category.ifBlank { "Medium" },
                        passingScore = passingScore,
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentPb[idx] = item
                    } else {
                        currentPb.add(item)
                    }
                    pbChanged = true
                }
                "FAST_VOCABULARY" -> {
                    val idx = currentFv.indexOfFirst { it.id == rc.id }
                    val item = FastVocabularyChallenge(
                        id = rc.id,
                        word = rc.titleOrWord,
                        prompt = rc.hintOrMeaning,
                        options = rc.options,
                        correctIndex = rc.correctIndex,
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentFv[idx] = item
                    } else {
                        currentFv.add(item)
                    }
                    fvChanged = true
                }
                "GRAMMAR_CHALLENGE" -> {
                    val idx = currentGc.indexOfFirst { it.id == rc.id }
                    val item = GrammarChallengeQuestion(
                        id = rc.id,
                        question = rc.titleOrWord,
                        options = rc.options,
                        correctIndex = rc.correctIndex,
                        explanation = rc.hintOrMeaning,
                        category = rc.category.ifBlank { "Grammar" },
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentGc[idx] = item
                    } else {
                        currentGc.add(item)
                    }
                    gcChanged = true
                }
                "PICTURE_VOCAB" -> {
                    val idx = currentPv.indexOfFirst { it.id == rc.id || (it.word.isNotBlank() && it.word.equals(rc.titleOrWord, ignoreCase = true)) }
                    val item = PictureVocabChallenge(
                        id = rc.id,
                        emoji = if (rc.category.isNotBlank() && rc.category != "General") rc.category else "🖼️",
                        word = rc.titleOrWord,
                        phonetic = rc.extraData.ifBlank { "/${rc.titleOrWord.lowercase()}/" },
                        clue = rc.hintOrMeaning,
                        options = if (rc.options.isNotEmpty()) rc.options else listOf(rc.titleOrWord),
                        createdBy = rc.createdBy
                    )
                    if (idx >= 0) {
                        currentPv[idx] = item
                    } else {
                        currentPv.add(item)
                    }
                    pvChanged = true
                }
            }
        }

        if (sbPackChanged) {
            _sentenceBuilderPacks.value = currentSbPacks
            appPreferences?.saveSentenceBuilderPacks(currentSbPacks)
        }
        if (sbChanged) {
            _sentenceBuilderChallenges.value = currentSb
            appPreferences?.saveSentenceBuilderChallenges(currentSb)
        }
        if (wmChanged) {
            _wordMatchChallenges.value = currentWm
            appPreferences?.saveWordMatchChallenges(currentWm)
        }
        if (mlChanged) {
            _missingLettersChallenges.value = currentMl
            appPreferences?.saveMissingLettersChallenges(currentMl)
        }
        if (lcChanged) {
            _listenChooseChallenges.value = currentLc
            appPreferences?.saveListenChooseChallenges(currentLc)
        }
        if (pbChanged) {
            _pronunciationBattleChallenges.value = currentPb
            appPreferences?.savePronunciationBattleChallenges(currentPb)
        }
        if (fvChanged) {
            _fastVocabularyChallenges.value = currentFv
            appPreferences?.saveFastVocabularyChallenges(currentFv)
        }
        if (gcChanged) {
            _grammarChallenges.value = currentGc
            appPreferences?.saveGrammarChallenges(currentGc)
        }
        if (pvChanged) {
            _pictureVocabChallenges.value = currentPv
            appPreferences?.savePictureVocabChallenges(currentPv)
        }
    }

    fun mergeRemotePronunciationDrills(remoteDrills: List<PronunciationDrill>) {
        if (remoteDrills.isEmpty()) return
        val current = _pronunciationDrills.value.toMutableList()
        var changed = false
        for (item in remoteDrills) {
            val idx = current.indexOfFirst { it.id == item.id || it.wordOrSentence.equals(item.wordOrSentence, ignoreCase = true) }
            if (idx >= 0) {
                current[idx] = item
                changed = true
            } else {
                current.add(item)
                changed = true
            }
        }
        if (changed) {
            _pronunciationDrills.value = current
            appPreferences?.savePronunciationDrills(current)
        }
    }

    fun mergeRemoteSpeakingPrompts(remotePrompts: List<SpeakingPrompt>) {
        if (remotePrompts.isEmpty()) return
        val current = _speakingPrompts.value.toMutableList()
        var changed = false
        for (item in remotePrompts) {
            val idx = current.indexOfFirst { it.id == item.id || it.title.equals(item.title, ignoreCase = true) }
            if (idx >= 0) {
                current[idx] = item
                changed = true
            } else {
                current.add(item)
                changed = true
            }
        }
        if (changed) {
            _speakingPrompts.value = current
            appPreferences?.saveSpeakingPrompts(current)
        }
    }

    fun mergeRemoteExams(remoteExams: List<Exam>) {
        if (remoteExams.isEmpty()) return
        val current = _exams.value.toMutableList()
        var changed = false
        for (item in remoteExams) {
            val idx = current.indexOfFirst { it.id == item.id || it.title.equals(item.title, ignoreCase = true) }
            if (idx >= 0) {
                current[idx] = item
                changed = true
            } else {
                current.add(item)
                changed = true
            }
        }
        if (changed) {
            _exams.value = current
            appPreferences?.saveExams(current)
        }
    }

    fun mergeRemoteExamSubmissions(remoteSubmissions: List<ExamSubmission>) {
        if (remoteSubmissions.isEmpty()) return
        val current = _examSubmissions.value.toMutableList()
        var changed = false
        for (item in remoteSubmissions) {
            val idx = current.indexOfFirst { it.id == item.id }
            if (idx >= 0) {
                current[idx] = item
                changed = true
            } else {
                current.add(item)
                changed = true
            }
        }
        if (changed) {
            _examSubmissions.value = current
            appPreferences?.saveExamSubmissions(current)
        }
    }

    // =========================================================================
    // ENGLISH ADVENTURE PROGRESSION, REWARDS & ADMIN/TEACHER OPERATIONS
    // =========================================================================

    fun completeAdventureMission(
        worldId: String,
        levelId: String,
        missionId: String,
        scorePercent: Int
    ): Pair<Int, Boolean> { // returns Pair(starsEarned, isLevelCompleted)
        val stars = when {
            scorePercent >= 85 -> 3
            scorePercent >= 65 -> 2
            else -> 1
        }

        val currentProgress = _adventureProgress.value
        val newCompletedMissions = currentProgress.completedMissionIds + missionId

        // Find world and level
        var levelJustCompleted = false
        val worlds = _adventureWorlds.value.map { world ->
            if (world.id == worldId) {
                val updatedLevels = world.levels.mapIndexed { idx, lvl ->
                    if (lvl.id == levelId) {
                        val allMissionsDone = lvl.missions.all { it.id in newCompletedMissions }
                        val prevStars = currentProgress.levelStars[levelId] ?: 0
                        val finalStars = maxOf(prevStars, stars)
                        if (allMissionsDone) levelJustCompleted = true
                        lvl.copy(
                            isCompleted = allMissionsDone || lvl.isCompleted,
                            starsEarned = finalStars
                        )
                    } else {
                        lvl
                    }
                }

                // If level is completed, unlock next level in this world
                val finalizedLevels = updatedLevels.mapIndexed { idx, lvl ->
                    if (idx > 0 && updatedLevels[idx - 1].isCompleted) {
                        lvl.copy(isUnlocked = true)
                    } else {
                        lvl
                    }
                }
                world.copy(levels = finalizedLevels)
            } else {
                world
            }
        }

        // Calculate new stars map
        val currentStarsMap = currentProgress.levelStars.toMutableMap()
        val prevStarsForLevel = currentStarsMap[levelId] ?: 0
        currentStarsMap[levelId] = maxOf(prevStarsForLevel, stars)
        val newTotalStars = currentStarsMap.values.sum()

        // Unlock next worlds based on required total stars
        val unlockedWorlds = worlds.map { world ->
            if (newTotalStars >= world.requiredTotalStars) {
                val levelsWithFirstUnlocked = world.levels.mapIndexed { idx, lvl ->
                    if (idx == 0) lvl.copy(isUnlocked = true) else lvl
                }
                world.copy(isUnlocked = true, levels = levelsWithFirstUnlocked)
            } else {
                world
            }
        }
        _adventureWorlds.value = unlockedWorlds

        // Credit XP and Coins
        val earnedXp = 40 + (stars * 10)
        val earnedCoins = 20 + (stars * 5)
        addRewards(earnedXp, earnedCoins)

        val completedLevelIds = currentProgress.completedLevelIds.toMutableSet()
        if (levelJustCompleted) {
            completedLevelIds.add(levelId)
        }

        val unlockedWorldIds = unlockedWorlds.filter { it.isUnlocked }.map { it.id }.toSet()

        // Unlock Badges
        val updatedBadges = _adventureBadges.value.map { badge ->
            when (badge.id) {
                "b_island" -> badge.copy(isUnlocked = newTotalStars >= 3)
                "b_knight" -> badge.copy(isUnlocked = newTotalStars >= 8)
                "b_listener" -> badge.copy(isUnlocked = newTotalStars >= 14)
                "b_orator" -> badge.copy(isUnlocked = newTotalStars >= 20)
                "b_monarch" -> badge.copy(isUnlocked = newTotalStars >= 25)
                else -> badge
            }
        }
        _adventureBadges.value = updatedBadges

        // Update progress state
        val updatedProgress = currentProgress.copy(
            completedMissionIds = newCompletedMissions,
            completedLevelIds = completedLevelIds,
            unlockedWorldIds = unlockedWorldIds,
            levelStars = currentStarsMap,
            totalStars = newTotalStars,
            totalCoins = currentProgress.totalCoins + earnedCoins,
            totalXp = currentProgress.totalXp + earnedXp
        )
        _adventureProgress.value = updatedProgress
        saveAdventureStateToPreferences()

        // Update current user in leaderboard
        _adventureLeaderboard.update { list ->
            list.map { entry ->
                if (entry.isCurrentUser) {
                    val highestWorld = unlockedWorlds.lastOrNull { it.isUnlocked }?.name ?: "Vocabulary Island"
                    entry.copy(
                        totalStars = newTotalStars,
                        coins = entry.coins + earnedCoins,
                        xp = entry.xp + earnedXp,
                        currentWorld = highestWorld
                    )
                } else {
                    entry
                }
            }.sortedByDescending { it.totalStars }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        }

        return Pair(stars, levelJustCompleted)
    }

    fun saveAdventureStateToPreferences() {
        appPreferences?.saveAdventureProgress(_adventureProgress.value)
        appPreferences?.saveAdventureWorlds(_adventureWorlds.value)
    }

    fun addAdventureRewards(xp: Int, coins: Int) {
        _adventureProgress.update { curr ->
            curr.copy(
                totalCoins = curr.totalCoins + coins,
                totalXp = curr.totalXp + xp
            )
        }
        saveAdventureStateToPreferences()
    }

    fun claimAdventureChest(worldId: String, xp: Int, coins: Int): Boolean {
        val curr = _adventureProgress.value
        if (curr.claimedChestWorldIds.contains(worldId)) {
            return false // Already claimed for this world
        }
        _adventureProgress.update { current ->
            if (current.claimedChestWorldIds.contains(worldId)) {
                current
            } else {
                current.copy(
                    claimedChestWorldIds = current.claimedChestWorldIds + worldId,
                    totalCoins = current.totalCoins + coins,
                    totalXp = current.totalXp + xp
                )
            }
        }
        saveAdventureStateToPreferences()
        return true
    }

    // Admin World Management
    fun addAdventureWorld(
        name: String,
        description: String,
        tagline: String,
        emoji: String,
        requiredStars: Int
    ): AdventureWorld {
        val newWorld = AdventureWorld(
            id = "world_${System.currentTimeMillis()}",
            name = name.trim(),
            order = _adventureWorlds.value.size + 1,
            description = description.trim(),
            tagline = tagline.trim(),
            emoji = emoji.ifBlank { "🗺️" },
            themeColorHex = 0xFF7C3AED,
            gradientStartHex = 0xFF6366F1,
            gradientEndHex = 0xFF9333EA,
            requiredTotalStars = requiredStars,
            isUnlocked = requiredStars == 0,
            levels = emptyList()
        )
        _adventureWorlds.update { it + newWorld }
        saveAdventureStateToPreferences()
        return newWorld
    }

    fun updateAdventureWorld(updated: AdventureWorld) {
        _adventureWorlds.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        saveAdventureStateToPreferences()
    }

    fun deleteAdventureWorld(worldId: String) {
        _adventureWorlds.update { list ->
            list.filterNot { it.id == worldId }
        }
        saveAdventureStateToPreferences()
    }

    // Admin Level Management
    fun addAdventureLevel(
        worldId: String,
        title: String,
        description: String,
        emoji: String,
        xpReward: Int,
        coinsReward: Int
    ): AdventureLevel? {
        val world = _adventureWorlds.value.find { it.id == worldId } ?: return null
        val levelNum = world.levels.size + 1
        val levelId = "lvl_${System.currentTimeMillis()}"

        val defaultMissions = listOf(
            AdventureMission(
                id = "m_${levelId}_1",
                levelId = levelId,
                worldId = worldId,
                title = "$title Speaking Quest",
                description = "Speak this English sentence clearly to master $title.",
                type = AdventureMissionType.SPEAKING_CHALLENGE,
                targetSentence = "I am excited to explore $title and master English speaking.",
                audioPrompt = "I am excited to explore $title and master English speaking.",
                correctAnswer = "I am excited to explore $title and master English speaking.",
                xp = (xpReward / 2).coerceAtLeast(20),
                coins = (coinsReward / 2).coerceAtLeast(10)
            ),
            AdventureMission(
                id = "m_${levelId}_2",
                levelId = levelId,
                worldId = worldId,
                title = "$title Vocabulary Quiz",
                description = "Choose the best synonym or meaning for $title.",
                type = AdventureMissionType.QUIZ,
                options = listOf("Fluent and confident", "Hesitant", "Silent", "Unprepared"),
                correctAnswer = "Fluent and confident",
                explanation = "Practicing spoken English adventure quests helps you speak fluently and confidently.",
                xp = (xpReward / 2).coerceAtLeast(20),
                coins = (coinsReward / 2).coerceAtLeast(10)
            )
        )

        val newLevel = AdventureLevel(
            id = levelId,
            worldId = worldId,
            levelNumber = levelNum,
            title = title.trim(),
            description = description.trim(),
            iconEmoji = emoji.ifBlank { "⭐" },
            xpReward = xpReward,
            coinsReward = coinsReward,
            isUnlocked = world.levels.isEmpty() || world.levels.all { it.isCompleted },
            missions = defaultMissions
        )
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(levels = w.levels + newLevel)
                } else w
            }
        }
        saveAdventureStateToPreferences()
        return newLevel
    }

    fun ensureLevelHasMissions(worldId: String, levelId: String, title: String): List<AdventureMission> {
        val world = _adventureWorlds.value.find { it.id == worldId }
        val lvl = world?.levels?.find { it.id == levelId }
        if (lvl != null && lvl.missions.isNotEmpty()) return lvl.missions

        val defaultMissions = listOf(
            AdventureMission(
                id = "m_${levelId}_1",
                levelId = levelId,
                worldId = worldId,
                title = "$title Speaking Quest",
                description = "Speak this English sentence clearly to master $title.",
                type = AdventureMissionType.SPEAKING_CHALLENGE,
                targetSentence = "I am excited to explore $title and master English speaking.",
                audioPrompt = "I am excited to explore $title and master English speaking.",
                correctAnswer = "I am excited to explore $title and master English speaking.",
                xp = 35,
                coins = 15
            ),
            AdventureMission(
                id = "m_${levelId}_2",
                levelId = levelId,
                worldId = worldId,
                title = "$title Vocabulary Quiz",
                description = "Choose the best synonym or meaning for $title.",
                type = AdventureMissionType.QUIZ,
                options = listOf("Fluent and confident", "Hesitant", "Silent", "Unprepared"),
                correctAnswer = "Fluent and confident",
                explanation = "Practicing spoken English adventure quests helps you speak fluently and confidently.",
                xp = 35,
                coins = 15
            )
        )
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(levels = w.levels.map { l ->
                        if (l.id == levelId) l.copy(missions = defaultMissions) else l
                    })
                } else w
            }
        }
        saveAdventureStateToPreferences()
        return defaultMissions
    }

    fun deleteAdventureLevel(worldId: String, levelId: String) {
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(levels = w.levels.filterNot { it.id == levelId })
                } else w
            }
        }
        saveAdventureStateToPreferences()
    }

    // Admin Mission Management
    fun addAdventureMission(
        worldId: String,
        levelId: String,
        title: String,
        description: String,
        type: AdventureMissionType,
        targetSentence: String,
        options: List<String>,
        correctAnswer: String,
        clue: String,
        xp: Int,
        coins: Int
    ): AdventureMission? {
        val newMission = AdventureMission(
            id = "m_${System.currentTimeMillis()}",
            levelId = levelId,
            worldId = worldId,
            title = title.trim(),
            description = description.trim(),
            type = type,
            targetSentence = targetSentence.trim(),
            options = options.filter { it.isNotBlank() },
            correctAnswer = correctAnswer.trim(),
            clue = clue.trim(),
            xp = xp,
            coins = coins,
            scrambledWords = if (type == AdventureMissionType.SENTENCE_BUILDER) {
                targetSentence.split(" ").filter { it.isNotBlank() }.shuffled()
            } else if (type == AdventureMissionType.WORD_PUZZLE) {
                correctAnswer.map { it.toString() }.shuffled()
            } else emptyList()
        )

        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(
                        levels = w.levels.map { lvl ->
                            if (lvl.id == levelId) {
                                lvl.copy(missions = lvl.missions + newMission)
                            } else lvl
                        }
                    )
                } else w
            }
        }
        saveAdventureStateToPreferences()
        return newMission
    }

    fun deleteAdventureMission(worldId: String, levelId: String, missionId: String) {
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(
                        levels = w.levels.map { lvl ->
                            if (lvl.id == levelId) {
                                lvl.copy(missions = lvl.missions.filterNot { it.id == missionId })
                            } else lvl
                        }
                    )
                } else w
            }
        }
        saveAdventureStateToPreferences()
    }

    fun updateAdventureLevel(
        worldId: String,
        levelId: String,
        title: String,
        description: String,
        emoji: String,
        xpReward: Int,
        coinsReward: Int
    ) {
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(
                        levels = w.levels.map { lvl ->
                            if (lvl.id == levelId) {
                                lvl.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    iconEmoji = emoji.ifBlank { lvl.iconEmoji },
                                    xpReward = xpReward,
                                    coinsReward = coinsReward
                                )
                            } else lvl
                        }
                    )
                } else w
            }
        }
        saveAdventureStateToPreferences()
    }

    fun updateAdventureMission(
        worldId: String,
        levelId: String,
        missionId: String,
        title: String,
        description: String,
        type: AdventureMissionType,
        targetSentence: String,
        options: List<String>,
        correctAnswer: String,
        clue: String,
        xp: Int,
        coins: Int
    ) {
        _adventureWorlds.update { list ->
            list.map { w ->
                if (w.id == worldId) {
                    w.copy(
                        levels = w.levels.map { lvl ->
                            if (lvl.id == levelId) {
                                lvl.copy(
                                    missions = lvl.missions.map { m ->
                                        if (m.id == missionId) {
                                            m.copy(
                                                title = title.trim(),
                                                description = description.trim(),
                                                type = type,
                                                targetSentence = targetSentence.trim(),
                                                options = options.filter { it.isNotBlank() },
                                                correctAnswer = correctAnswer.trim(),
                                                clue = clue.trim(),
                                                xp = xp,
                                                coins = coins,
                                                scrambledWords = if (type == AdventureMissionType.SENTENCE_BUILDER) {
                                                    targetSentence.split(" ").filter { it.isNotBlank() }.shuffled()
                                                } else if (type == AdventureMissionType.WORD_PUZZLE) {
                                                    correctAnswer.map { it.toString() }.shuffled()
                                                } else emptyList()
                                            )
                                        } else m
                                    }
                                )
                            } else lvl
                        }
                    )
                } else w
            }
        }
        saveAdventureStateToPreferences()
    }

    fun resetAdventureWorldsToDefault() {
        _adventureWorlds.value = AdventureDataDefaults.createDefaultWorlds()
        saveAdventureStateToPreferences()
    }

    // Teacher Student Adventure Monitoring Reports
    fun getTeacherAdventureReports(): List<StudentAdventureProgressSummary> {
        val students = _adminStudents.value
        val worlds = _adventureWorlds.value
        val totalLevels = worlds.sumOf { it.levels.size }

        return students.mapIndexed { idx, s ->
            val compLevels = when (idx) {
                0 -> 3 // Alex Morgan
                1 -> 2
                2 -> 2
                3 -> 1
                else -> 0
            }
            val stars = when (idx) {
                0 -> _adventureProgress.value.totalStars.coerceAtLeast(8)
                1 -> 5
                2 -> 6
                3 -> 3
                else -> 1
            }
            val worldName = when (idx) {
                0 -> "Speaking City"
                1 -> "Grammar Kingdom"
                2 -> "Listening Forest"
                3 -> "Vocabulary Island"
                else -> "Vocabulary Island"
            }
            StudentAdventureProgressSummary(
                studentId = s.id,
                studentName = s.name,
                avatar = s.avatarEmoji,
                className = s.batch,
                currentWorld = worldName,
                completedLevelsCount = compLevels,
                totalLevelsCount = totalLevels,
                totalStars = stars,
                coinsEarned = s.xpPoints / 4,
                xpEarned = s.xpPoints,
                lastActiveMission = if (compLevels > 0) "Level $compLevels Completed" else "Just Began",
                completionPercent = if (totalLevels > 0) compLevels.toFloat() / totalLevels else 0f
            )
        }
    }
}


