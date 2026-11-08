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

    // Speaking Challenges & Prompts
    private val _speakingPrompts = MutableStateFlow(
        listOf(
            SpeakingPrompt(
                id = "p1",
                title = "Introduce Yourself",
                category = "Daily Life",
                question = "Tell us about yourself, what you do, and why you want to master English.",
                followUpTip = "Use phrases like 'I'm passionate about...', 'Currently, I am working on...'",
                recommendedKeywords = listOf("passionate", "experience", "learning", "goal")
            ),
            SpeakingPrompt(
                id = "p2",
                title = "Describe Your Morning Routine",
                category = "Daily Habits",
                question = "Walk us through what you do from the moment you wake up until you start your day.",
                followUpTip = "Use sequence markers: 'First', 'Next', 'After that', 'Finally'.",
                recommendedKeywords = listOf("wake up", "breakfast", "commute", "routine")
            ),
            SpeakingPrompt(
                id = "p3",
                title = "My Favorite Dish & How It's Made",
                category = "Food & Culture",
                question = "Describe your favorite meal. What ingredients are used, and why is it special to you?",
                followUpTip = "Use sensory adjectives: 'savory', 'crispy', 'aromatic', 'spicy'.",
                recommendedKeywords = listOf("delicious", "flavor", "ingredients", "traditional")
            ),
            SpeakingPrompt(
                id = "p4",
                title = "A Memorable Journey",
                category = "Storytelling",
                question = "Talk about a trip or vacation that left a lasting impression on you.",
                followUpTip = "Describe the setting, the people you met, and how the experience felt.",
                recommendedKeywords = listOf("journey", "scenery", "unforgettable", "discovered")
            )
        )
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

    // Pronunciation Drills
    private val _pronunciationDrills = MutableStateFlow(
        listOf(
            PronunciationDrill("pd1", "Comfortable", "/ˈkʌmftəbl/", "Silent 'or'", "Medium", "Native speakers drop the middle syllables: say 'KUMF-tuh-bl'."),
            PronunciationDrill("pd2", "Specifically", "/spəˈsɪfɪkli/", "Consonant cluster", "Hard", "Start with a crisp 'spuh', stress 'SIF', end quickly with 'ik-lee'."),
            PronunciationDrill("pd3", "Thoughtful", "/θɔːtful/", "Soft 'th' sound", "Medium", "Place tongue gently between teeth with breath for the 'th' sound."),
            PronunciationDrill("pd4", "Phenomenon", "/fəˈnɒmɪnən/", "Vowel reduction", "Hard", "Stress the second syllable: 'fuh-NAH-muh-non'."),
            PronunciationDrill("pd5", "Entrepreneur", "/ˌɒntrəprəˈnɜːr/", "Stress placement", "Hard", "Stress the final syllable: 'on-truh-pruh-NUR'."),
            PronunciationDrill("pd6", "World", "/wɜːld/", "r + l blend", "Hard", "Curl your tongue slightly for 'er', then glide smoothly to 'ld'."),
            PronunciationDrill("pd7", "Schedule", "/ˈskedʒuːl/", "Soft 'dge'", "Easy", "In American English, it starts with 'sked' like 'skeleton'.")
        )
    )
    val pronunciationDrills: StateFlow<List<PronunciationDrill>> = _pronunciationDrills.asStateFlow()

    // Vocabulary Words
    private val _vocabularyWords = MutableStateFlow(
        listOf(
            VocabularyWord("v1", "Eloquence", "/ˈel.ə.kwəns/", "noun", "Fluent or persuasive speaking or writing.", "Her natural eloquence held the entire audience spellbound.", "Communication", listOf("fluency", "articulateness"), isFavorite = true),
            VocabularyWord("v2", "Resilience", "/rɪˈzɪl.jəns/", "noun", "The capacity to recover quickly from difficulties.", "Courage and resilience are key to learning any language.", "Mindset", listOf("tenacity", "strength")),
            VocabularyWord("v3", "Collaborate", "/kəˈlæb.ə.reɪt/", "verb", "To work jointly on an activity or project.", "Students love to collaborate on speaking roleplays.", "Workplace", listOf("cooperate", "team up"), isFavorite = true),
            VocabularyWord("v4", "Pragmatic", "/præɡˈmæt.ɪk/", "adjective", "Dealing with things sensibly and realistically.", "A pragmatic approach to speaking is to prioritize being understood over perfection.", "Academic", listOf("practical", "sensible")),
            VocabularyWord("v5", "Articulate", "/ɑːˈtɪk.jə.lət/", "adjective", "Having or showing the ability to speak fluently and coherently.", "She gave an articulate summary of the proposal.", "Communication", listOf("expressive", "lucid")),
            VocabularyWord("v6", "Fostering", "/ˈfɒs.tər.ɪŋ/", "verb", "Encouraging the development or growth of ideas or skills.", "Daily speaking practice is essential for fostering fluency.", "Growth", listOf("nurturing", "cultivating")),
            VocabularyWord("v7", "Metaphor", "/ˈmet.ə.fɔːr/", "noun", "A figure of speech in which a word or phrase is applied to an object or action not literally applicable.", "Language learning is often described with the metaphor of a mountain climb.", "Literature", listOf("analogy", "symbol")),
            VocabularyWord("v8", "Serendipity", "/ˌser.ənˈdɪp.ə.ti/", "noun", "The occurrence of events by chance in a happy or beneficial way.", "Meeting an English conversational partner at the train station was pure serendipity.", "General", listOf("chance", "fluke"))
        )
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
    private val _assignments = MutableStateFlow(
        listOf(
            Assignment("asg_1", "3-Minute Impromptu Speech: Climate Action", "Prof. Eleanor Vance", "Class 10", "Tomorrow, 6 PM", "Record a 2-3 minute audio response explaining two small eco-habits you practice.", "Speaking", 100, 20, 24),
            Assignment("asg_2", "Business Pitch Presentation Dialogue", "Prof. Eleanor Vance", "All Classes", "Friday, 11:59 PM", "Present your imaginary startup product in clear, persuasive English.", "Presentation", 100, 14, 24),
            Assignment("asg_3", "Pronunciation Drill: Minimal Pairs (P vs B)", "Dr. Marcus Reed", "Class 9", "In 3 Days", "Record 10 sample sentences focusing on explosive /p/ and voiced /b/ sounds.", "Pronunciation", 50, 18, 20),
            Assignment("asg_4", "Everyday English: Ordering at a Restaurant", "Prof. Eleanor Vance", "Class 8", "In 4 Days", "Practice ordering a three-course dinner and asking for the bill politely.", "Speaking", 60, 12, 22)
        )
    )
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
        return newAsg
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
                name = if (draft.isCurrentUser) "${draft.name} (You)" else draft.name,
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
    private val _wordMatchChallenges = MutableStateFlow(
        listOf(
            WordMatchChallenge("wm_1", "Eloquence", "Fluent or persuasive speech", "Communication", "System"),
            WordMatchChallenge("wm_2", "Resilience", "Ability to recover quickly from hardship", "Mindset", "System"),
            WordMatchChallenge("wm_3", "Articulation", "Clear and effective pronunciation", "Communication", "System"),
            WordMatchChallenge("wm_4", "Pragmatic", "Sensible and practical rather than theoretical", "Academic", "System"),
            WordMatchChallenge("wm_5", "Meticulous", "Showing great attention to detail and care", "General", "Prof. Eleanor Vance"),
            WordMatchChallenge("wm_6", "Serendipity", "Finding pleasant things unexpectedly", "General", "Dean Arthur Pendelton")
        )
    )
    val wordMatchChallenges: StateFlow<List<WordMatchChallenge>> = _wordMatchChallenges.asStateFlow()

    private val _missingLettersChallenges = MutableStateFlow(
        listOf(
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
    )
    val missingLettersChallenges: StateFlow<List<MissingLettersChallenge>> = _missingLettersChallenges.asStateFlow()

    private val _missingLettersCustomCategories = MutableStateFlow(listOf("Animals", "Vegetables", "Fruits", "General"))
    val missingLettersCustomCategories: StateFlow<List<String>> = _missingLettersCustomCategories.asStateFlow()

    fun addMissingLettersCategory(category: String): Boolean {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return false
        if (_missingLettersCustomCategories.value.any { it.equals(trimmed, ignoreCase = true) }) return false
        _missingLettersCustomCategories.update { it + trimmed }
        return true
    }

    private val _sentenceBuilderChallenges = MutableStateFlow(
        listOf(
            SentenceBuilderChallenge("sb_1", "She speaks English with natural confidence and clarity", "Daily speaking practice", "তাই স্বাভাৱিক আত্মবিশ্বাস আৰু স্পষ্টতাৰে ইংৰাজী কয়।", "System"),
            SentenceBuilderChallenge("sb_2", "Clear pronunciation helps listeners understand your message easily", "Pronunciation guide", "স্পষ্ট উচ্চাৰণে শ্ৰোতাসকলক আপোনাৰ বাৰ্তা সহজে বুজিবলৈ সহায় কৰে।", "Prof. Eleanor Vance"),
            SentenceBuilderChallenge("sb_3", "We practice speaking English every day to build fluency", "Study habits", "সাবলীলতা গঢ়ি তুলিবলৈ আমি প্ৰতিদিনে ইংৰাজী কোৱা অনুশীলন কৰোঁ।", "System"),
            SentenceBuilderChallenge("sb_4", "Collaborative conversations improve confidence in professional workplace settings", "Workplace English", "পাৰস্পৰিক কথোপকথনে পেছাদাৰী কৰ্মক্ষেত্ৰত আত্মবিশ্বাস বৃদ্ধি কৰে।", "Dean Arthur Pendelton")
        )
    )
    val sentenceBuilderChallenges: StateFlow<List<SentenceBuilderChallenge>> = _sentenceBuilderChallenges.asStateFlow()

    private val _listenChooseChallenges = MutableStateFlow(
        listOf(
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
    )
    val listenChooseChallenges: StateFlow<List<ListenChooseChallenge>> = _listenChooseChallenges.asStateFlow()

    private val _pronunciationBattleChallenges = MutableStateFlow(
        listOf(
            PronunciationBattleChallenge("pb_1", "The courageous astronaut explored uncharted territory", "Consonant clusters 'str', 'ch', 'pl'", "Medium", 80, "System"),
            PronunciationBattleChallenge("pb_2", "Peter Piper picked a peck of pickled peppers", "Crisp explosive /p/ consonant rhythm", "Hard", 85, "System"),
            PronunciationBattleChallenge("pb_3", "Thoughtful philosophers thoroughly examine their theories", "Voiced and voiceless 'th' phonemes", "Hard", 85, "Prof. Eleanor Vance"),
            PronunciationBattleChallenge("pb_4", "She sells seashells by the shimmering seashore", "Sibilants /s/ vs /ʃ/ distinction", "Medium", 80, "System"),
            PronunciationBattleChallenge("pb_5", "World development requires extraordinary perseverance", "Liquid 'r' and 'l' consonant blends", "Hard", 85, "Dean Arthur Pendelton")
        )
    )
    val pronunciationBattleChallenges: StateFlow<List<PronunciationBattleChallenge>> = _pronunciationBattleChallenges.asStateFlow()

    private val _fastVocabularyChallenges = MutableStateFlow(
        listOf(
            FastVocabularyChallenge("fv_1", "Articulate", "Choose the closest synonym for 'Articulate':", listOf("Expressive & clear", "Confused & hesitant", "Quiet & shy", "Angry & rude"), 0, "System"),
            FastVocabularyChallenge("fv_2", "Eloquent", "What does 'Eloquent' describe?", listOf("Persuasive & graceful speaking", "Very fast running", "Complicated math", "Ancient architecture"), 0, "System"),
            FastVocabularyChallenge("fv_3", "Pragmatic", "A 'Pragmatic' solution is one that is:", listOf("Practical & realistic", "Expensive & luxurious", "Theoretical & untested", "Careless & hurried"), 0, "Prof. Eleanor Vance"),
            FastVocabularyChallenge("fv_4", "Resilience", "What is the best definition of 'Resilience'?", listOf("Bouncing back from hardship", "Forgetting the past", "Giving up easily", "Avoiding all conflict"), 0, "System"),
            FastVocabularyChallenge("fv_5", "Collaborate", "When two people 'Collaborate', they:", listOf("Work jointly on a task", "Argue loudly", "Compete against each other", "Work completely alone"), 0, "Dean Arthur Pendelton")
        )
    )
    val fastVocabularyChallenges: StateFlow<List<FastVocabularyChallenge>> = _fastVocabularyChallenges.asStateFlow()

    private val _grammarChallenges = MutableStateFlow(
        listOf(
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
    )
    val grammarChallenges: StateFlow<List<GrammarChallengeQuestion>> = _grammarChallenges.asStateFlow()

    init {
        refreshLeaderboard()
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
        if (newRole == Role.STUDENT) {
            setStudentProfileUserId(updatedUser.id, updatedUser.name)
        } else {
            refreshLeaderboard()
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
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
        // Register immediately into Phonetics and Assamese translation engines
        com.example.service.PhoneticsAiEngine.addCustomPhoneticWord(
            com.example.service.PhoneticSearchResult(
                word = word.word.trim().replaceFirstChar { it.uppercase() },
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
                soundTags = listOf(word.category, "Teacher Added")
            )
        )
    }

    fun deleteVocabularyWord(wordId: String) {
        val wordToDelete = _vocabularyWords.value.find { it.id == wordId }
        _vocabularyWords.update { list -> list.filterNot { it.id == wordId } }
        wordToDelete?.let {
            com.example.service.PhoneticsAiEngine.removeCustomPhoneticWord(it.word)
        }
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
    fun addWordMatchChallenge(word: String, definition: String, category: String = "General", createdBy: String) {
        val id = "wm_${System.currentTimeMillis()}"
        val item = WordMatchChallenge(id, word.trim(), definition.trim(), category.trim(), createdBy)
        _wordMatchChallenges.update { it + item }
    }

    fun deleteWordMatchChallenge(id: String) {
        _wordMatchChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateWordMatchChallenge(updated: WordMatchChallenge) {
        _wordMatchChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addMissingLettersChallenge(
        fullWord: String,
        hint: String,
        category: String = "General",
        createdBy: String
    ) {
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
    }

    fun deleteMissingLettersChallenge(id: String) {
        _missingLettersChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateMissingLettersChallenge(updated: MissingLettersChallenge) {
        _missingLettersChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addSentenceBuilderChallenge(targetSentence: String, hint: String = "Assemble syntax", assameseTranslation: String = "", createdBy: String) {
        val id = "sb_${System.currentTimeMillis()}"
        val item = SentenceBuilderChallenge(id, targetSentence.trim(), hint.trim(), assameseTranslation.trim(), createdBy)
        _sentenceBuilderChallenges.update { it + item }
    }

    fun deleteSentenceBuilderChallenge(id: String) {
        _sentenceBuilderChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateSentenceBuilderChallenge(updated: SentenceBuilderChallenge) {
        _sentenceBuilderChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addListenChooseChallenge(audioScript: String, question: String, options: List<String>, correctIndex: Int, createdBy: String) {
        val id = "lc_${System.currentTimeMillis()}"
        val item = ListenChooseChallenge(id, audioScript.trim(), question.trim(), options.map { it.trim() }, correctIndex, createdBy)
        _listenChooseChallenges.update { it + item }
    }

    fun deleteListenChooseChallenge(id: String) {
        _listenChooseChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateListenChooseChallenge(updated: ListenChooseChallenge) {
        _listenChooseChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addPronunciationBattleChallenge(sentence: String, phonemeFocus: String, difficulty: String = "Medium", passingScore: Int = 80, createdBy: String) {
        val id = "pb_${System.currentTimeMillis()}"
        val item = PronunciationBattleChallenge(id, sentence.trim(), phonemeFocus.trim(), difficulty, passingScore, createdBy)
        _pronunciationBattleChallenges.update { it + item }
    }

    fun deletePronunciationBattleChallenge(id: String) {
        _pronunciationBattleChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updatePronunciationBattleChallenge(updated: PronunciationBattleChallenge) {
        _pronunciationBattleChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addFastVocabularyChallenge(word: String, prompt: String, options: List<String>, correctIndex: Int, createdBy: String) {
        val id = "fv_${System.currentTimeMillis()}"
        val item = FastVocabularyChallenge(id, word.trim(), prompt.trim(), options.map { it.trim() }, correctIndex, createdBy)
        _fastVocabularyChallenges.update { it + item }
    }

    fun deleteFastVocabularyChallenge(id: String) {
        _fastVocabularyChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateFastVocabularyChallenge(updated: FastVocabularyChallenge) {
        _fastVocabularyChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun addGrammarChallenge(question: String, options: List<String>, correctIndex: Int, explanation: String, category: String = "Grammar", createdBy: String) {
        val id = "gc_${System.currentTimeMillis()}"
        val item = GrammarChallengeQuestion(id, question.trim(), options.map { it.trim() }, correctIndex, explanation.trim(), category.trim(), createdBy)
        _grammarChallenges.update { it + item }
    }

    fun deleteGrammarChallenge(id: String) {
        _grammarChallenges.update { it.filterNot { item -> item.id == id } }
    }

    fun updateGrammarChallenge(updated: GrammarChallengeQuestion) {
        _grammarChallenges.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
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
        minSpeakingSeconds: Int = 20
    ): SpeakingPrompt {
        val newPrompt = SpeakingPrompt(
            id = "sp_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category.trim().ifBlank { "General Speaking" },
            question = question.trim(),
            followUpTip = followUpTip.trim(),
            recommendedKeywords = recommendedKeywords.map { it.trim() }.filter { it.isNotBlank() },
            minSpeakingSeconds = minSpeakingSeconds
        )
        _speakingPrompts.update { it + newPrompt }
        return newPrompt
    }

    fun updateSpeakingPrompt(prompt: SpeakingPrompt) {
        _speakingPrompts.update { list ->
            list.map { if (it.id == prompt.id) prompt else it }
        }
    }

    fun deleteSpeakingPrompt(promptId: String) {
        _speakingPrompts.update { list ->
            list.filterNot { it.id == promptId }
        }
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
        return newDrill
    }

    fun updatePronunciationDrill(drill: PronunciationDrill) {
        _pronunciationDrills.update { list ->
            list.map { if (it.id == drill.id) drill else it }
        }
    }

    fun deletePronunciationDrill(drillId: String) {
        _pronunciationDrills.update { list ->
            list.filterNot { it.id == drillId }
        }
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
}

