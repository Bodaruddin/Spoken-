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

    // Admin Managed Students
    private val _adminStudents = MutableStateFlow(
        listOf(
            User("std_001", "Alex Morgan", "alex.morgan@linguaquest.edu", Role.STUDENT, "🦉", "Batch Alpha", true),
            User("std_002", "Elena Rostova", "elena.r@linguaquest.edu", Role.STUDENT, "🦄", "Batch Alpha", true),
            User("std_003", "David Chen", "david.c@linguaquest.edu", Role.STUDENT, "🐯", "Batch Beta", true),
            User("std_004", "Amira Al-Mansoor", "amira.m@linguaquest.edu", Role.STUDENT, "🦁", "Batch Alpha", true),
            User("std_005", "Lucas Silva", "lucas.s@linguaquest.edu", Role.STUDENT, "🐺", "Batch Beta", true),
            User("std_006", "Hana Tanaka", "hana.t@linguaquest.edu", Role.STUDENT, "🌸", "Batch Gamma", false)
        )
    )
    val adminStudents: StateFlow<List<User>> = _adminStudents.asStateFlow()

    // Admin Managed Teachers
    private val _adminTeachers = MutableStateFlow(
        listOf(
            User("tch_001", "Prof. Eleanor Vance", "e.vance@linguaquest.edu", Role.TEACHER, "👩‍🏫", "Batch Alpha & Beta", true),
            User("tch_002", "Dr. Marcus Reed", "m.reed@linguaquest.edu", Role.TEACHER, "👨‍🏫", "Batch Beta & Gamma", true),
            User("tch_003", "Prof. Chloe Bennett", "c.bennett@linguaquest.edu", Role.TEACHER, "👩‍💼", "Advanced Fluency", true)
        )
    )
    val adminTeachers: StateFlow<List<User>> = _adminTeachers.asStateFlow()

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

    // Admin: Add Student
    fun addStudent(name: String, email: String, batch: String) {
        val newId = "std_${System.currentTimeMillis() % 10000}"
        val newStudent = User(newId, name, email, Role.STUDENT, "🎓", batch, true)
        _adminStudents.update { it + newStudent }
    }

    // Admin: Bulk Import Simulation
    fun bulkImportStudents(count: Int) {
        val sampleNames = listOf("Liam O'Connor", "Zoe Kravitz", "Carlos Mendez", "Fatima Zahra", "Min-jun Park")
        val newStudents = sampleNames.take(count).mapIndexed { i, name ->
            val id = "std_${System.currentTimeMillis() % 1000 + i}"
            User(id, name, "${name.lowercase().replace(" ", ".")}@linguaquest.edu", Role.STUDENT, "✨", "Batch Alpha", true)
        }
        _adminStudents.update { it + newStudents }
    }

    // Admin: Toggle Student Active
    fun toggleStudentActive(studentId: String) {
        _adminStudents.update { list ->
            list.map {
                if (it.id == studentId) it.copy(isActive = !it.isActive) else it
            }
        }
    }

    // Admin: Add Teacher
    fun addTeacher(name: String, email: String, department: String) {
        val newId = "tch_${System.currentTimeMillis() % 10000}"
        val newTeacher = User(newId, name, email, Role.TEACHER, "👨‍🏫", department, true)
        _adminTeachers.update { it + newTeacher }
    }
}
