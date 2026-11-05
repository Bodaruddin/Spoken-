package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LinguaQuestRepository {

    // Current logged-in user & role
    private val _currentUser = MutableStateFlow(
        User(
            id = "std_001",
            name = "Alex Morgan",
            email = "alex.morgan@linguaquest.edu",
            role = Role.STUDENT,
            avatarEmoji = "🦉",
            batch = "Batch Alpha"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Student Profile
    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            userId = "std_001",
            currentLevel = LearningLevel.ELEMENTARY,
            streakDays = 7,
            xpPoints = 1450,
            coins = 420,
            league = "Gold League",
            overallScore = 84,
            speakingScore = 82,
            pronunciationScore = 85,
            fluencyScore = 79,
            grammarScore = 88,
            vocabularyScore = 86,
            listeningScore = 90,
            readingScore = 84,
            completedStagesCount = 14,
            speakingPracticeMinutes = 168
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

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
            PronunciationDrill("pd3", "Thoughtful", "/ˈθɔːtfəl/", "Soft 'th' sound", "Medium", "Place tongue gently between teeth with breath for the 'th' sound."),
            PronunciationDrill("pd4", "World", "/wɜːld/", "r + l blend", "Hard", "Curl your tongue slightly for 'er', then glide smoothly to 'ld'."),
            PronunciationDrill("pd5", "Entrepreneur", "/ˌɒntrəprəˈnɜːr/", "Stress placement", "Hard", "Stress the final syllable: 'on-truh-pruh-NUR'."),
            PronunciationDrill("pd6", "Schedule", "/ˈskedʒuːl/", "Soft 'dge'", "Easy", "In American English, it starts with 'sked' like 'skeleton'.")
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
            Assignment("asg_1", "3-Minute Impromptu Speech: Climate Action", "Prof. Eleanor Vance", "Batch Alpha", "Tomorrow, 6 PM", "Record a 2-3 minute audio response explaining two small eco-habits you practice.", "Speaking", 100, 20, 24),
            Assignment("asg_2", "Business Pitch Presentation Dialogue", "Prof. Eleanor Vance", "Batch Alpha", "Friday, 11:59 PM", "Present your imaginary startup product in clear, persuasive English.", "Presentation", 100, 14, 24),
            Assignment("asg_3", "Pronunciation Drill: Minimal Pairs (P vs B)", "Dr. Marcus Reed", "Batch Beta", "In 3 Days", "Record 10 sample sentences focusing on explosive /p/ and voiced /b/ sounds.", "Pronunciation", 50, 18, 20)
        )
    )
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

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

    // Leaderboard
    private val _leaderboard = MutableStateFlow(
        listOf(
            LeaderboardUser(1, "Elena Rostova", "🦄", 2850, 19, "Diamond League"),
            LeaderboardUser(2, "David Chen", "🐯", 2420, 14, "Diamond League"),
            LeaderboardUser(3, "Alex Morgan (You)", "🦉", 1450, 7, "Gold League", isCurrentUser = true),
            LeaderboardUser(4, "Amira Al-Mansoor", "🦁", 1390, 8, "Gold League"),
            LeaderboardUser(5, "Lucas Silva", "🐺", 1210, 5, "Gold League"),
            LeaderboardUser(6, "Hana Tanaka", "🌸", 1150, 6, "Silver League"),
            LeaderboardUser(7, "Mateo Gomez", "🚀", 980, 4, "Silver League")
        )
    )
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

    // Admin & Teacher Managed Students
    private val _adminStudents = MutableStateFlow(
        listOf(
            StudentManagementInfo("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", "🦉", "Batch Alpha", LearningLevel.ELEMENTARY, true, 82, 85, 79, 88, 86, 90, 7, 1450, "Nov 2026", "Consistently practices speaking. Excellent rhythm and confidence."),
            StudentManagementInfo("std_002", "Elena Rostova", "elena.r@linguaquest.edu", "🦄", "Batch Alpha", LearningLevel.INTERMEDIATE, true, 94, 96, 92, 90, 95, 96, 19, 2850, "Oct 2026", "Top cohort fluency. Demonstrates near-native sentence intonation."),
            StudentManagementInfo("std_003", "David Chen", "david.c@linguaquest.edu", "🐯", "Batch Beta", LearningLevel.INTERMEDIATE, true, 88, 89, 85, 91, 87, 92, 14, 2420, "Oct 2026", "Strong analytical vocabulary. Recommended for advanced debate roleplays."),
            StudentManagementInfo("std_004", "Amira Al-Mansoor", "amira.m@linguaquest.edu", "🦁", "Batch Alpha", LearningLevel.ELEMENTARY, true, 78, 80, 74, 82, 80, 85, 8, 1390, "Nov 2026", "Needs extra practice on past tense irregular verbs and voiced /th/ sounds."),
            StudentManagementInfo("std_005", "Lucas Silva", "lucas.s@linguaquest.edu", "🐺", "Batch Beta", LearningLevel.BEGINNER, true, 71, 72, 68, 75, 74, 80, 5, 1210, "Nov 2026", "Improving quickly. Benefit from daily sentence builder and word match games."),
            StudentManagementInfo("std_006", "Hana Tanaka", "hana.t@linguaquest.edu", "🌸", "Batch Gamma", LearningLevel.BEGINNER, false, 65, 68, 62, 70, 71, 76, 3, 980, "Nov 2026", "Temporarily paused. Needs supportive speaking drills on basic greetings.")
        )
    )
    val adminStudents: StateFlow<List<StudentManagementInfo>> = _adminStudents.asStateFlow()

    // Admin Managed Teachers
    private val _adminTeachers = MutableStateFlow(
        listOf(
            User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Batch Alpha & Beta", true),
            User("tch_002", "Dr. Marcus Reed", "m.reed@linguaquest.edu", Role.TEACHER, "👨‍🏫", "Batch Beta & Gamma", true),
            User("tch_003", "Prof. Chloe Bennett", "c.bennett@linguaquest.edu", Role.TEACHER, "👩‍💼", "Advanced Fluency", true)
        )
    )
    val adminTeachers: StateFlow<List<User>> = _adminTeachers.asStateFlow()

    // Dynamic Game Content Collections
    private val _wordMatchChallenges = MutableStateFlow(
        listOf(
            WordMatchChallenge("wm_1", "Eloquence", "Fluent or persuasive speech", "Communication", "System"),
            WordMatchChallenge("wm_2", "Resilience", "Ability to recover quickly from hardship", "Mindset", "System"),
            WordMatchChallenge("wm_3", "Articulate", "Expressing ideas clearly and coherently", "Communication", "System"),
            WordMatchChallenge("wm_4", "Pragmatic", "Sensible and practical rather than theoretical", "Academic", "System"),
            WordMatchChallenge("wm_5", "Meticulous", "Showing great attention to detail and care", "General", "Prof. Eleanor Vance"),
            WordMatchChallenge("wm_6", "Serendipity", "Finding pleasant things unexpectedly", "General", "Dean Arthur Pendelton")
        )
    )
    val wordMatchChallenges: StateFlow<List<WordMatchChallenge>> = _wordMatchChallenges.asStateFlow()

    private val _missingLettersChallenges = MutableStateFlow(
        listOf(
            MissingLettersChallenge("ml_1", "CONVERSATION", "An informal spoken exchange between people", listOf(2, 5, 8), "System"),
            MissingLettersChallenge("ml_2", "PRONUNCIATION", "The correct way in which a word or language is spoken", listOf(2, 6, 9), "System"),
            MissingLettersChallenge("ml_3", "FLUENCY", "Ability to speak smoothly, easily, and naturally", listOf(1, 4), "Prof. Eleanor Vance"),
            MissingLettersChallenge("ml_4", "VOCABULARY", "The body of words used in a particular language", listOf(1, 5, 8), "System"),
            MissingLettersChallenge("ml_5", "INTELLIGENT", "Having or showing mental alertness and capacity", listOf(3, 7), "Dr. Marcus Reed")
        )
    )
    val missingLettersChallenges: StateFlow<List<MissingLettersChallenge>> = _missingLettersChallenges.asStateFlow()

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

    // Switch Role
    fun switchRole(newRole: Role) {
        val current = _currentUser.value
        val updatedUser = when (newRole) {
            Role.STUDENT -> User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", "Batch Alpha")
            Role.TEACHER -> User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Lead Instructor")
            Role.ADMIN -> User("adm_001", "Dean Arthur Pendelton", "admin@linguaquest.edu", Role.ADMIN, "🛡️", "Head of Academics")
        }
        _currentUser.value = updatedUser
    }

    // Gamification Updates: Add XP and Coins
    fun addRewards(xp: Int, coins: Int) {
        _studentProfile.update {
            it.copy(
                xpPoints = it.xpPoints + xp,
                coins = it.coins + coins
            )
        }
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
            it.copy(completedStagesCount = it.completedStagesCount + 1)
        }
    }

    // Toggle Vocabulary Favorite
    fun toggleWordFavorite(wordId: String) {
        _vocabularyWords.update { words ->
            words.map {
                if (it.id == wordId) it.copy(isFavorite = !it.isFavorite) else it
            }
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
    }

    fun updateStudent(updated: StudentManagementInfo) {
        _adminStudents.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteStudent(studentId: String) {
        _adminStudents.update { list ->
            list.filterNot { it.id == studentId }
        }
    }

    fun toggleStudentActive(studentId: String) {
        _adminStudents.update { list ->
            list.map {
                if (it.id == studentId) it.copy(isActive = !it.isActive) else it
            }
        }
    }

    fun bulkImportStudents(count: Int) {
        val sampleNames = listOf("Liam O'Connor", "Zoe Kravitz", "Carlos Mendez", "Fatima Zahra", "Min-jun Park", "Aisha Diallo")
        val batches = listOf("Batch Alpha", "Batch Beta", "Batch Gamma")
        val levels = listOf(LearningLevel.BEGINNER, LearningLevel.ELEMENTARY, LearningLevel.PRE_INTERMEDIATE)
        val newStudents = sampleNames.take(count).mapIndexed { i, name ->
            val id = "std_${System.currentTimeMillis() % 1000 + i}"
            StudentManagementInfo(
                id = id,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@linguaquest.edu",
                avatarEmoji = listOf("🦄", "🦁", "🦊", "🐬", "🌸").random(),
                batch = batches[i % batches.size],
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

    fun addMissingLettersChallenge(fullWord: String, hint: String, createdBy: String) {
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
        val item = MissingLettersChallenge(id, cleaned, hint.trim(), indices, createdBy)
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

    // Admin: Add Teacher
    fun addTeacher(name: String, email: String, department: String) {
        val newId = "tch_${System.currentTimeMillis() % 10000}"
        val newTeacher = User(newId, name, email, Role.TEACHER, "👨‍🏫", department, true)
        _adminTeachers.update { it + newTeacher }
    }
}

