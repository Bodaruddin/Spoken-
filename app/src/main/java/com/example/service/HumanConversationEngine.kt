package com.example.service

import com.example.data.model.NovaChatMessage
import com.example.data.model.SpeakingEvaluation
import com.example.data.model.SpeakingPrompt
import com.example.util.AssameseTranslator
import com.example.util.ExampleAnswerGenerator

/**
 * Generates natural, human-like greetings, context-aware conversational replies,
 * and dynamic follow-up questions directly based on the student's actual spoken
 * or typed answer and conversation history.
 */
object HumanConversationEngine {

    private val genericNames = setOf(
        "", "student", "guest", "user", "admin", "teacher", "faculty", "learner", "default_student"
    )

    private val nonNameAdjectivesAndNouns = setOf(
        "fine", "good", "great", "well", "happy", "okay", "ok", "tired", "busy", "sad",
        "excited", "ready", "hungry", "sleepy", "nervous", "bored", "sorry", "sure",
        "student", "teacher", "engineer", "doctor", "boy", "girl", "man", "woman",
        "here", "home", "from", "going", "doing", "trying", "learning", "practicing",
        "speaking", "living", "working", "studying", "indian", "assamese", "very", "really",
        "pretty", "quite", "not", "just", "feeling", "also", "still", "always", "never"
    )

    private val stopWords = setOf(
        "i", "me", "my", "myself", "we", "our", "you", "your", "he", "she", "it", "they", "them",
        "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do",
        "does", "did", "will", "would", "shall", "should", "can", "could", "may", "might",
        "a", "an", "the", "and", "but", "or", "if", "because", "as", "of", "at", "by", "for",
        "with", "about", "into", "through", "to", "from", "in", "out", "on", "off", "over",
        "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both",
        "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only",
        "own", "same", "so", "than", "too", "very", "just", "really", "actually", "basically",
        "literally", "today", "tomorrow", "thing", "things", "something", "anything", "nothing",
        "that", "this", "these", "those", "what", "which", "who", "whom", "im", "i'm", "ive",
        "i've", "dont", "don't", "didnt", "didn't", "cant", "can't", "like", "love", "enjoy",
        "want", "going", "think", "feel", "know", "say", "said", "tell", "told", "get", "got",
        "make", "made", "much", "many", "lot", "lots", "well", "good", "fine", "great", "nice"
    )

    /**
     * Builds a warm, natural opening message (display text + clean TTS prompt) so Nova
     * starts the conversation like a real human partner rather than a worksheet quiz.
     */
    fun buildHumanGreeting(
        topic: SpeakingPrompt,
        studentName: String? = null
    ): Pair<String, String> {
        val displayTitle = topic.title.trim().ifBlank { "English Conversation" }
        val rawQ = topic.question.trim().ifBlank {
            "How's your day going so far? Tell me a little about yourself!"
        }
        val firstName = extractCleanFirstName(studentName)
        val greetingWord = if (firstName != null) "Hey $firstName!" else "Hey there!"

        val openerLine = when {
            displayTitle.contains("Introduction", ignoreCase = true) ||
                rawQ.contains("How are you", ignoreCase = true) ->
                "$greetingWord I'm Nova 😊 Really glad we get to chat about “$displayTitle” today."
            else ->
                "$greetingWord I'm Nova 😊 I'd love to have a real chat with you about “$displayTitle” today."
        }

        val displayText = "$openerLine\n\n$rawQ"
        val cleanAudioTitle = displayTitle.replace("(", "").replace(")", "")
        val audioPrompt = "$greetingWord I'm Nova. Really glad we get to chat about $cleanAudioTitle today. $rawQ"
        return displayText to audioPrompt
    }

    /**
     * Generates a human-like conversational response and follow-up question tailored directly
     * to what the student just answered, avoiding previously asked questions.
     */
    fun generateReply(
        spokenText: String,
        topic: SpeakingPrompt,
        previousMessages: List<NovaChatMessage>,
        lastNovaQuestion: String,
        baseEval: SpeakingEvaluation,
        studentName: String? = null
    ): SpeakingEvaluation {
        val cleanSpoken = spokenText.trim()
        val lower = cleanSpoken.lowercase()
        val words = cleanSpoken.split(Regex("\\s+")).filter { it.isNotBlank() }

        val previouslyAsked = collectAskedQuestions(previousMessages, lastNovaQuestion)
        val grammarCorrectionTip = detectFriendlyGrammarTip(cleanSpoken)
        val studentAskedQuestion = detectAndAnswerStudentQuestion(cleanSpoken, lower)

        val (reaction, candidates, hintOverride) = buildContextReactionAndQuestions(
            cleanSpoken = cleanSpoken,
            lower = lower,
            words = words,
            topic = topic,
            lastNovaQuestion = lastNovaQuestion,
            previousMessages = previousMessages,
            studentName = studentName
        )

        val chosenQuestion = pickFreshQuestion(candidates, previouslyAsked, topic, cleanSpoken)

        // Assemble a warm, concise, human-sounding reply (avoid generic robotic phrases)
        val responseParts = mutableListOf<String>()
        if (studentAskedQuestion != null) {
            responseParts.add(studentAskedQuestion)
            // Only add reaction if student also shared substantive info beyond just the question
            if (words.size > 5 && reaction.isNotBlank()) {
                responseParts.add(reaction)
            }
        } else if (reaction.isNotBlank()) {
            responseParts.add(reaction)
        }

        // Light, non-intrusive phrasing nudge only when there's a clear slip and student isn't just greeting
        if (grammarCorrectionTip != null && words.size >= 4) {
            responseParts.add(grammarCorrectionTip)
        }

        responseParts.add(chosenQuestion)
        val fullReply = responseParts.joinToString(" ").replace(Regex("\\s+"), " ").trim()

        val computedCefr = baseEval.cefrLevel.ifBlank { computeCefrFromScore(baseEval.overallScore) }
        val resolvedHint = hintOverride ?: buildHintForQuestion(chosenQuestion)
        val resolvedExamples = ExampleAnswerGenerator.generateForQuestion(chosenQuestion, topic)
        val improvedTip = grammarCorrectionTip ?: baseEval.suggestedImprovement

        return baseEval.copy(
            cefrLevel = computedCefr,
            aiCoachResponse = fullReply,
            followUpQuestion = chosenQuestion,
            suggestedImprovement = improvedTip,
            hint = resolvedHint,
            exampleAnswers = resolvedExamples
        )
    }

    /**
     * Generates a fresh, natural human topic question when the student taps "Next".
     */
    fun generateNextTopicQuestion(
        topic: SpeakingPrompt,
        previousMessages: List<NovaChatMessage>
    ): Triple<String, String, String> {
        val previouslyAsked = collectAskedQuestions(previousMessages, "")
        val topicLower = "${topic.title} ${topic.category}".lowercase()

        val pool = when {
            topicLower.contains("intro") || topicLower.contains("greeting") -> listOf(
                "By the way, what do you usually enjoy doing when you have some free time?" to "Mention one or two hobbies and why you enjoy them.",
                "What's your favorite meal or comfort food after a long day?" to "Name a dish you love and describe how it tastes.",
                "If you had a completely free Sunday, how would you spend it?" to "Talk about people you'd meet or places you'd visit.",
                "What motivated you to practice speaking English these days?" to "Share a personal goal for your school, work, or travels."
            )
            topicLower.contains("time") || topicLower.contains("routine") || topicLower.contains("habit") -> listOf(
                "What time do you usually wake up on weekdays compared to weekends?" to "Use 'at' for exact times and 'on' for days of the week.",
                "What's your favorite part of the day — morning, afternoon, or evening — and why?" to "Use 'in the morning/afternoon/evening' with a reason.",
                "How do you usually spend your Sunday evenings before the week starts?" to "Mention one relaxing habit you do on Sundays.",
                "When do you feel most focused during the day?" to "Share the time of day and what you usually work on then."
            )
            else -> {
                val label = topic.title.trim().ifBlank { "this" }
                listOf(
                    "What's something interesting about $label that most people might not expect?" to "Share a real example or observation from your experience.",
                    "How has your view on $label changed over the past year?" to "Compare what you thought before with how you see it now.",
                    "If you were talking to a friend about $label, what's the first story you'd tell them?" to "Describe a short memory using past tense verbs.",
                    "What's one thing you'd love to try or improve next around $label?" to "Use 'I want to...' or 'I hope to...' to describe your plan."
                )
            }
        }

        val fresh = pool.firstOrNull { (q, _) -> !isAlreadyAsked(q, previouslyAsked) } ?: pool.first()
        val transitions = listOf(
            "Ooh, let's switch gears for a second!",
            "Here's something else I was curious to ask you:",
            "Let's chat about another fun angle on this!"
        )
        val transition = transitions[(previousMessages.size) % transitions.size]
        val fullText = "$transition\n\n${fresh.first}"
        val audioText = "$transition ${fresh.first}"
        return Triple(fresh.first, fullText, "${fresh.second}|||$audioText")
    }

    private fun extractCleanFirstName(name: String?): String? {
        val clean = name?.trim()?.substringBefore(" ")?.replace(Regex("[^A-Za-z]"), "").orEmpty()
        if (clean.length < 2) return null
        if (clean.lowercase() in genericNames) return null
        return clean.replaceFirstChar { it.uppercaseChar() }
    }

    private fun computeCefrFromScore(score: Int): String = when {
        score >= 90 -> "C1"
        score >= 80 -> "B2"
        score >= 70 -> "B1"
        score >= 55 -> "A2"
        else -> "A1"
    }

    private fun collectAskedQuestions(
        previousMessages: List<NovaChatMessage>,
        lastNovaQuestion: String
    ): Set<String> {
        val set = mutableSetOf<String>()
        fun addNormalized(raw: String?) {
            if (raw.isNullOrBlank()) return
            val extracted = AssameseTranslator.extractCleanQuestion(raw)
            set.add(normalizeQ(extracted))
            set.add(normalizeQ(raw))
        }
        addNormalized(lastNovaQuestion)
        previousMessages.filter { it.isNova }.forEach { msg ->
            addNormalized(msg.evaluation?.followUpQuestion)
            addNormalized(msg.text)
            // Also extract any sentence ending with '?'
            Regex("[^.!?\\n]+\\?").findAll(msg.text).forEach { match ->
                addNormalized(match.value)
            }
        }
        return set
    }

    private fun normalizeQ(q: String): String =
        q.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

    private fun isAlreadyAsked(candidate: String, previouslyAsked: Set<String>): Boolean {
        val norm = normalizeQ(candidate)
        if (norm in previouslyAsked) return true
        return previouslyAsked.any { prev ->
            prev.length > 15 && (prev.contains(norm) || norm.contains(prev))
        }
    }

    private fun pickFreshQuestion(
        candidates: List<String>,
        previouslyAsked: Set<String>,
        topic: SpeakingPrompt,
        cleanSpoken: String
    ): String {
        for (q in candidates) {
            if (!isAlreadyAsked(q, previouslyAsked)) {
                return q
            }
        }
        // Final fallback still grounded in the student's actual words rather than generic topic boilerplate
        val focus = extractFocusPhrase(cleanSpoken)
        val extraPool = if (focus != null) {
            listOf(
                "How did you first get interested in $focus?",
                "What's your favorite memory involving $focus?",
                "Who do you usually share $focus with?"
            )
        } else {
            listOf(
                "What's been the best part of your week so far?",
                "What's something fun you're looking forward to soon?",
                "Tell me more — how do you usually feel when doing that?"
            )
        }
        return extraPool.firstOrNull { !isAlreadyAsked(it, previouslyAsked) } ?: candidates.first()
    }

    private fun detectAndAnswerStudentQuestion(cleanSpoken: String, lower: String): String? {
        return when {
            lower.contains("how about you") ||
                lower.contains("what about you") ||
                lower.contains("and you") ||
                lower.contains("how are you") ||
                lower.contains("how r u") ->
                "I'm doing great, thanks so much for asking! Chatting with you always makes my day brighter."

            lower.contains("who are you") || lower.contains("what is your name") || lower.contains("what's your name") ->
                "I'm Nova, your speaking partner! I love helping people feel comfortable chatting in English."

            lower.contains("where are you from") || lower.contains("where do you live") ->
                "I'm right here online whenever you want to chat — though I hear so many cool stories from learners in Assam and across India!"

            lower.contains("do you like ") -> {
                val likedThing = cleanSpoken.substringAfter("like ", "")
                    .replace("?", "")
                    .trim()
                    .take(35)
                if (likedThing.isNotBlank()) {
                    "Oh, I definitely think $likedThing is awesome! There's so much to love about it."
                } else {
                    "Yes, I think that sounds really cool!"
                }
            }

            else -> null
        }
    }

    private fun buildContextReactionAndQuestions(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        topic: SpeakingPrompt,
        lastNovaQuestion: String,
        previousMessages: List<NovaChatMessage>,
        studentName: String?
    ): Triple<String, List<String>, String?> {

        // 1. Check if the student introduced their name (e.g., "My name is Rahat", "I'm Priya", "I am Bodaruddin")
        val extractedName = extractSpokenPersonName(cleanSpoken)
        // 2. Check hometown / place
        val extractedPlace = extractPlace(cleanSpoken, lower)
        // 3. Check hobby / sport / activity
        val hobbyMatch = findHobbyMatch(lower)
        // 4. Check food / drink
        val foodMatch = findFoodMatch(lower)
        // 5. Check time / routine expression
        val timeMatch = extractTimeMention(cleanSpoken)
        // 6. Check school / study / job
        val studyOrWorkMatch = findStudyOrWorkMatch(lower)
        // 7. Check family / friends
        val personMention = findPersonMention(lower)
        // 8. Check mood / greeting
        val positiveMood = hasAnyWord(lower, listOf("fine", "good", "great", "awesome", "wonderful", "fantastic", "happy", "amazing", "excited", "well", "blessed")) ||
            lower.contains("not bad") || lower.contains("pretty good") || lower.contains("doing well")
        val tiredOrNegativeMood = hasAnyWord(lower, listOf("tired", "sleepy", "exhausted", "busy", "stressed", "sad", "bored", "sick", "bad", "rough", "hard"))

        // Check if student's name is already known in history or profile
        val knownName = extractedName ?: extractCleanFirstName(studentName)
        val hasNameAlreadyBeenDiscussed = previousMessages.any { msg ->
            !msg.isNova && extractSpokenPersonName(msg.text) != null
        } || lastNovaQuestion.contains("what's your name", ignoreCase = true)

        // Priority 1: Student just said their name!
        if (extractedName != null) {
            val placeTail = if (extractedPlace != null) " And $extractedPlace sounds like a really nice place!" else ""
            val hobbyTail = if (hobbyMatch != null) " Plus, ${hobbyMatch.display} is awesome!" else ""
            val reaction = "Oh, nice to meet you, $extractedName!$placeTail$hobbyTail"
            val questions = when {
                hobbyMatch != null -> hobbyMatch.questions
                extractedPlace != null -> listOf(
                    "What do you enjoy most about living in $extractedPlace, $extractedName?",
                    "What do you usually like doing for fun around $extractedPlace?"
                )
                studyOrWorkMatch != null -> studyOrWorkMatch.questions
                else -> listOf(
                    "Where are you joining me from today, $extractedName?",
                    "So tell me, $extractedName, what do you usually enjoy doing in your free time?",
                    "Are you focusing on school or work right now, $extractedName?"
                )
            }
            return Triple(reaction, questions, "Respond naturally in 1 or 2 full sentences—share real details just like chatting with a friend.")
        }

        // Priority 2: Pure short greeting ("Hi", "Hello", "Hey Nova")
        val pureGreeting = words.size <= 3 && hasAnyWord(lower, listOf("hi", "hello", "hey", "namaskar", "good morning", "good evening", "good afternoon"))
        if (pureGreeting) {
            val nameBit = if (knownName != null) " $knownName" else ""
            return Triple(
                "Hey$nameBit! So glad you jumped on to chat with me.",
                listOf(
                    "How has your day been treating you so far?",
                    "What's something interesting you did or saw today?"
                ),
                "Tell Nova how your day is going and add one small detail about what you did today."
            )
        }

        // Priority 3: Student shared how they feel / mood ("I am fine", "Doing great", "A bit tired today")
        val lastAskedHowAreYou = lastNovaQuestion.contains("how are you", ignoreCase = true) ||
            lastNovaQuestion.contains("how has your day", ignoreCase = true) ||
            lastNovaQuestion.contains("how's your day", ignoreCase = true)
        if ((positiveMood || tiredOrNegativeMood) && words.size <= 14 && hobbyMatch == null && foodMatch == null && extractedPlace == null && studyOrWorkMatch == null) {
            if (tiredOrNegativeMood) {
                return Triple(
                    "Aw, I hear you — those tiring days definitely catch up with us.",
                    listOf(
                        "What's been keeping you so busy or worn out lately?",
                        "When you finally get a break, what helps you relax the most?",
                        "Did you at least get a chance to grab something good to eat today?"
                    ),
                    "Share what made today tiring or what helps you recharge afterward."
                )
            }
            if (positiveMood) {
                val reaction = if (lastAskedHowAreYou) {
                    "I'm really glad to hear you're doing well!"
                } else {
                    "Love that positive energy!"
                }
                val qs = if (topic.title.contains("Introduction", ignoreCase = true) && !hasNameAlreadyBeenDiscussed && knownName == null) {
                    listOf(
                        "Since we're just getting to know each other, what's your name and where are you from?",
                        "What's been the highlight of your day so far?",
                        "What do you usually enjoy doing when you get some free time?"
                    )
                } else {
                    listOf(
                        "What's been the best part of your day so far?",
                        "What do you enjoy doing most in your free time lately?",
                        "Have you got any fun plans coming up this week?"
                    )
                }
                return Triple(reaction, qs, "Share a specific detail about your day or one thing you're looking forward to.")
            }
        }

        // Priority 4: Student mentioned a specific hobby / sport / activity
        if (hobbyMatch != null) {
            return Triple(
                hobbyMatch.reaction,
                hobbyMatch.questions,
                hobbyMatch.hint
            )
        }

        // Priority 5: Student mentioned a hometown / city / country / travel spot
        if (extractedPlace != null) {
            val isTravelWant = lower.contains("visit") || lower.contains("travel") || lower.contains("go to") || lower.contains("dream")
            val reaction = if (isTravelWant) {
                "Ooh, $extractedPlace would be such an exciting place to visit!"
            } else {
                "Oh nice, $extractedPlace sounds like a really warm place to live!"
            }
            val qs = if (isTravelWant) {
                listOf(
                    "What's the very first thing you'd want to see or try in $extractedPlace?",
                    "Who would you love to take along with you to $extractedPlace?"
                )
            } else {
                listOf(
                    "What's your favorite spot or thing to do around $extractedPlace?",
                    "What kind of local food is $extractedPlace best known for?",
                    "Have you lived in $extractedPlace for a long time?"
                )
            }
            return Triple(reaction, qs, "Describe a specific sight, food, or place you love about $extractedPlace.")
        }

        // Priority 6: Student talked about Food or Drink
        if (foodMatch != null) {
            return Triple(
                "Ooh, $foodMatch sounds delicious — you're making me hungry!",
                listOf(
                    "Do you prefer eating $foodMatch homemade, or is there a favorite spot you get it from?",
                    "How often do you usually have $foodMatch, and who do you share it with?",
                    "What's another dish you could never get tired of eating?"
                ),
                "Describe the taste, how it's cooked, or who makes it best."
            )
        }

        // Priority 7: Student talked about School, Studies, Subject, or Work
        if (studyOrWorkMatch != null) {
            return Triple(
                studyOrWorkMatch.reaction,
                studyOrWorkMatch.questions,
                studyOrWorkMatch.hint
            )
        }

        // Priority 8: Student mentioned specific clock times or daily routine moments
        if (timeMatch != null) {
            return Triple(
                "Got it — around $timeMatch! That sounds like a solid rhythm.",
                listOf(
                    "What's the very first thing you do right after $timeMatch?",
                    "Do you keep that same schedule on weekends too, or sleep in a bit?",
                    "And how do you usually spend the rest of your afternoon?"
                ),
                "Try using natural time phrases like 'right after that', 'usually around', or 'on weekends'."
            )
        }

        // Priority 9: Student mentioned family or friends
        if (personMention != null) {
            return Triple(
                "Aww, it's always special spending time with your $personMention!",
                listOf(
                    "What's something fun you and your $personMention did together recently?",
                    "What do you guys usually talk about or enjoy doing together?"
                ),
                "Tell a quick story using past tense like 'we went', 'we talked', or 'we laughed'."
            )
        }

        // Priority 10: Student used "because" or shared a personal goal / reason / past story
        if (lower.contains("because ")) {
            val reasonPart = cleanSpoken.substringAfter("because ", "").trim().take(50).trimEnd('.', '!')
            val reaction = if (reasonPart.length >= 6) {
                "That makes total sense — saying \"$reasonPart\" really puts it into perspective."
            } else {
                "I totally get why you feel that way!"
            }
            return Triple(
                reaction,
                listOf(
                    "Have you always felt that way, or did an experience change how you see it?",
                    "How does that affect what you do day to day?"
                ),
                "Explain with a quick example from your own life."
            )
        }

        if (hasAnyWord(lower, listOf("yesterday", "last week", "last night", "last month", "last year", "weekend", "went", "visited", "watched", "played", "bought"))) {
            val focus = extractFocusPhrase(cleanSpoken)
            val reaction = if (focus != null) {
                "Oh nice, sounds like $focus made things interesting!"
            } else {
                "Sounds like you had quite an eventful time!"
            }
            return Triple(
                reaction,
                listOf(
                    "What was the best or funniest part of that?",
                    "Who was there with you, and would you do it again?"
                ),
                "Practice past tense verbs like 'we saw', 'I felt', or 'it was'."
            )
        }

        // Priority 11: Very short reply ("yes", "no", "maybe", 1-2 words)
        if (words.size <= 2) {
            return when {
                lower in setOf("yes", "yeah", "yep", "yup", "sure", "of course", "definitely") -> Triple(
                    "Nice! I'd love to hear a bit more than just a quick yes though.",
                    listOf(
                        "What makes you say yes — can you tell me in a full sentence?",
                        "Tell me a quick example behind that!"
                    ),
                    "Expand your 'yes' into a full sentence using 'Yes, because...'."
                )
                lower in setOf("no", "nope", "not really", "nah", "never") -> Triple(
                    "Fair enough, nothing wrong with that!",
                    listOf(
                        "What would you rather spend time on instead?",
                        "Tell me something you really do enjoy doing these days!"
                    ),
                    "Share what you prefer instead using 'I prefer...' or 'I actually like...'."
                )
                lower in setOf("nothing", "idk", "don't know", "not sure", "maybe") -> Triple(
                    "Haha, totally fine — no rush at all!",
                    listOf(
                        "Let's keep it easy: what's one thing that made you smile this week?",
                        "What's your favorite way to relax after a long day?"
                    ),
                    "Pick any small everyday moment and describe it in one or two sentences."
                )
                else -> Triple(
                    "Ooh, \"$cleanSpoken\" — now you've got me curious!",
                    listOf(
                        "Tell me more about $cleanSpoken — what makes it stand out to you?",
                        "How does $cleanSpoken fit into your everyday life?"
                    ),
                    "Try putting '$cleanSpoken' inside a complete sentence with why you mentioned it."
                )
            }
        }

        // Priority 12: General rich sentence -> Mirror their key focus phrase directly like an attentive listener!
        val focus = extractFocusPhrase(cleanSpoken)
        return if (focus != null) {
            Triple(
                "Oh, interesting that you brought up $focus!",
                listOf(
                    "What do you enjoy or notice most about $focus?",
                    "Have you been into $focus for a while, or is it something new for you?",
                    "How does $focus usually play into your day?"
                ),
                "Build directly on '$focus' using words like 'actually', 'usually', or 'especially'."
            )
        } else {
            Triple(
                "I really appreciate how clearly you shared that!",
                listOf(
                    "What made you think of that just now?",
                    "Tell me a bit more — what usually happens next after that?"
                ),
                "Add one extra detail or reason using 'because' or 'for example'."
            )
        }
    }

    private fun extractSpokenPersonName(text: String): String? {
        val patterns = listOf(
            Regex("""\bmy name is\s+([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE),
            Regex("""\bcall me\s+([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE),
            Regex("""\bi am\s+([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE),
            Regex("""\bi['’]m\s+([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE)
        )
        for (regex in patterns) {
            val match = regex.find(text) ?: continue
            val candidate = match.groupValues[1].trim()
            val candidateLower = candidate.lowercase()
            if (candidateLower !in nonNameAdjectivesAndNouns && candidateLower !in stopWords) {
                return candidate.replaceFirstChar { it.uppercaseChar() }
            }
        }
        return null
    }

    private fun extractPlace(text: String, lower: String): String? {
        val knownPlaces = listOf(
            "Assam", "Guwahati", "Dibrugarh", "Jorhat", "Silchar", "Tezpur", "Nagaon", "Tinsukia",
            "Sivasagar", "Barpeta", "India", "Delhi", "New Delhi", "Mumbai", "Kolkata", "Bangalore",
            "Bengaluru", "Chennai", "Hyderabad", "Pune", "Jaipur", "London", "Japan", "Tokyo",
            "Switzerland", "Paris", "France", "America", "USA", "Canada", "Australia", "Dubai",
            "Singapore", "Kerala", "Goa", "Shillong", "Meghalaya"
        )
        knownPlaces.firstOrNull { Regex("""\b${Regex.escape(it.lowercase())}\b""").containsMatchIn(lower) }
            ?.let { return it }

        val placePatterns = listOf(
            Regex("""\b(?:live in|stay in|located in|am from|i'm from|come from|hometown is)\s+([A-Za-z][A-Za-z ]{1,24}?)(?=[.,!?]|\band\b|\bbut\b|\bwith\b|\bwhere\b|$)""", RegexOption.IGNORE_CASE),
            Regex("""\b(?:visit|travel to|go to)\s+([A-Z][a-z]{2,20}(?:\s+[A-Z][a-z]{2,20})?)""")
        )
        for (p in placePatterns) {
            val raw = p.find(text)?.groupValues?.getOrNull(1)?.trim() ?: continue
            val firstWordLower = raw.lowercase().split(" ").firstOrNull().orEmpty()
            if (raw.length >= 3 && firstWordLower !in setOf("a", "an", "the", "my", "school", "home", "bed", "work", "class")) {
                return raw.split(" ").joinToString(" ") { w ->
                    w.replaceFirstChar { it.uppercaseChar() }
                }
            }
        }
        return null
    }

    private data class ContextualMatch(
        val display: String,
        val reaction: String,
        val questions: List<String>,
        val hint: String
    )

    private fun findHobbyMatch(lower: String): ContextualMatch? {
        return when {
            hasAnyWord(lower, listOf("cricket", "football", "soccer", "badminton", "basketball", "tennis", "volleyball", "kabaddi", "chess")) -> {
                val sport = listOf("cricket", "football", "soccer", "badminton", "basketball", "tennis", "volleyball", "kabaddi", "chess")
                    .first { Regex("""\b$it\b""").containsMatchIn(lower) }
                ContextualMatch(
                    display = sport,
                    reaction = "Oh nice, $sport is so much fun!",
                    questions = listOf(
                        "Do you usually play $sport with your friends, or enjoy watching big matches more?",
                        "Who's your favorite $sport player or team to cheer for?",
                        "How long have you been into $sport?"
                    ),
                    hint = "Talk about who you play with, how often, or your favorite player/team."
                )
            }
            hasAnyWord(lower, listOf("reading", "read", "book", "books", "novel", "novels", "stories")) -> ContextualMatch(
                display = "reading books",
                reaction = "I love that you enjoy reading — there's nothing like getting lost in a good story!",
                questions = listOf(
                    "What kind of books or stories pull you in the most?",
                    "What's the best book you've read recently, and what was it about?",
                    "Do you usually prefer reading in the morning or right before bed?"
                ),
                hint = "Share a book genre (fiction, biography, mystery) or a title you liked."
            )
            hasAnyWord(lower, listOf("music", "song", "songs", "sing", "singing", "guitar", "piano", "listening")) -> ContextualMatch(
                display = "music",
                reaction = "Music is such a great mood booster!",
                questions = listOf(
                    "What kind of songs or singers have you been listening to lately?",
                    "Do you like singing along or playing any instrument yourself?",
                    "When do you listen to music the most — while relaxing, traveling, or studying?"
                ),
                hint = "Name a music style, artist, or when you love putting headphones on."
            )
            hasAnyWord(lower, listOf("movie", "movies", "film", "films", "cinema", "anime", "series", "show")) -> ContextualMatch(
                display = "movies and shows",
                reaction = "Ooh, I love a great movie or series too!",
                questions = listOf(
                    "What's a movie or show you watched recently that really stayed with you?",
                    "Do you enjoy action, comedy, or mystery stories the most?",
                    "Do you prefer watching at the theater or cozying up at home?"
                ),
                hint = "Briefly mention the story or why a favorite movie stood out to you."
            )
            hasAnyWord(lower, listOf("drawing", "painting", "sketch", "sketching", "art", "craft")) -> ContextualMatch(
                display = "art and drawing",
                reaction = "That's awesome — being creative with art is so relaxing!",
                questions = listOf(
                    "What do you love drawing or painting the most — nature, portraits, or designs?",
                    "How did you first get interested in art?"
                ),
                hint = "Describe what you enjoy creating and what inspires your sketches."
            )
            hasAnyWord(lower, listOf("cooking", "cook", "baking", "bake")) -> ContextualMatch(
                display = "cooking",
                reaction = "Cooking is such an awesome skill to have!",
                questions = listOf(
                    "What's your signature dish that you love cooking?",
                    "Who taught you how to cook, or did you learn by experimenting?"
                ),
                hint = "Mention the dish, its ingredients, and who enjoys eating it."
            )
            hasAnyWord(lower, listOf("game", "games", "gaming", "pubg", "free fire", "minecraft")) -> ContextualMatch(
                display = "gaming",
                reaction = "Haha, gaming can be super fun when you're unwinding!",
                questions = listOf(
                    "Which game have you been playing the most lately, and do you team up with friends?",
                    "What do you like best about playing games — the teamwork or the challenge?"
                ),
                hint = "Talk about your favorite game and whether you play solo or with friends."
            )
            hasAnyWord(lower, listOf("travel", "traveling", "travelling", "trip", "explore", "mountains", "beach")) -> ContextualMatch(
                display = "traveling",
                reaction = "Traveling opens up so many cool memories!",
                questions = listOf(
                    "What's the most memorable trip you've ever taken so far?",
                    "Are you more of a mountain person or a beach person?"
                ),
                hint = "Share a place you visited or would love to explore soon."
            )
            hasAnyWord(lower, listOf("coding", "programming", "software", "app", "website", "tech")) -> ContextualMatch(
                display = "coding and tech",
                reaction = "Oh cool! Building things with code is super rewarding.",
                questions = listOf(
                    "What kind of apps or projects are you most excited to build?",
                    "How did you first get into programming?"
                ),
                hint = "Describe a project or skill you're practicing right now."
            )
            hasAnyWord(lower, listOf("cycling", "swimming", "running", "gym", "workout", "yoga", "walking")) -> {
                val act = listOf("cycling", "swimming", "running", "gym", "workout", "yoga", "walking")
                    .first { Regex("""\b$it\b""").containsMatchIn(lower) }
                ContextualMatch(
                    display = act,
                    reaction = "Staying active with $act is awesome for energy!",
                    questions = listOf(
                        "Do you usually do $act early in the morning or in the evening?",
                        "Do you feel a big difference in your energy on days you do $act?"
                    ),
                    hint = "Mention what time of day you do it and how it makes you feel."
                )
            }
            else -> null
        }
    }

    private fun findFoodMatch(lower: String): String? {
        val foods = listOf(
            "biryani", "fish curry", "rice", "curry", "chicken", "mutton", "pitha", "momo",
            "noodles", "pizza", "burger", "pasta", "sandwich", "tea", "chai", "coffee",
            "chocolate", "ice cream", "mango", "paneer", "samosa", "dosa", "dal"
        )
        return foods.firstOrNull { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(lower) }
    }

    private fun extractTimeMention(text: String): String? {
        Regex("""\b(\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.|o'clock))\b""", RegexOption.IGNORE_CASE)
            .find(text)
            ?.let { return it.groupValues[1].trim() }

        val timePhrases = listOf(
            "in the morning", "in the afternoon", "in the evening", "at night", "at midnight",
            "at noon", "on weekdays", "on weekends", "on sundays", "on sunday", "every morning"
        )
        return timePhrases.firstOrNull { text.lowercase().contains(it) }
    }

    private fun findStudyOrWorkMatch(lower: String): ContextualMatch? {
        val subjects = listOf("english", "math", "mathematics", "science", "physics", "chemistry", "biology", "history", "geography", "computer science", "computer")
        val matchedSub = subjects.firstOrNull { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(lower) }
        if (matchedSub != null) {
            val pretty = matchedSub.replaceFirstChar { it.uppercaseChar() }
            if (matchedSub == "english") {
                return ContextualMatch(
                    display = "English",
                    reaction = "That's great that you're focusing on English!",
                    questions = listOf(
                        "Which part feels easiest for you right now — speaking, listening, or writing?",
                        "What kind of situations do you want to speak English confidently in?"
                    ),
                    hint = "Share your favorite way to practice or where you hope to use English."
                )
            }
            return ContextualMatch(
                display = pretty,
                reaction = "Oh nice, $pretty is a fascinating subject!",
                questions = listOf(
                    "What makes $pretty interesting to you — is there a topic you liked recently?",
                    "Do you find $pretty easy or does it challenge you?"
                ),
                hint = "Share what you enjoy about studying $pretty."
            )
        }
        return when {
            hasAnyWord(lower, listOf("school", "college", "university", "class", "studying", "study", "student", "exam", "homework")) -> ContextualMatch(
                display = "school",
                reaction = "Got it! Keeping up with classes definitely keeps your days full.",
                questions = listOf(
                    "Which subject or part of the day at school do you look forward to the most?",
                    "Do you usually study with friends or prefer quiet focus on your own?"
                ),
                hint = "Mention a favorite subject, teacher, or activity at school."
            )
            hasAnyWord(lower, listOf("work", "job", "office", "career", "company", "business", "engineer", "teacher", "doctor")) -> ContextualMatch(
                display = "work",
                reaction = "That sounds like meaningful work!",
                questions = listOf(
                    "What does a typical day at work look like for you?",
                    "What part of your job gives you the biggest sense of achievement?"
                ),
                hint = "Describe one daily task or what you enjoy most about your work."
            )
            else -> null
        }
    }

    private fun findPersonMention(lower: String): String? {
        val people = listOf("family", "parents", "mother", "mom", "father", "dad", "brother", "sister", "friends", "friend", "cousin", "grandparents", "classmates")
        return people.firstOrNull { Regex("""\b$it\b""").containsMatchIn(lower) }
    }

    private fun hasAnyWord(lower: String, targets: List<String>): Boolean {
        return targets.any { t ->
            if (t.contains(" ")) lower.contains(t)
            else Regex("""\b${Regex.escape(t)}\b""").containsMatchIn(lower)
        }
    }

    private fun extractFocusPhrase(text: String): String? {
        val tokens = text.split(Regex("\\s+"))
            .map { it.replace(Regex("[^A-Za-z0-9'-]"), "").trim('\'', '-') }
            .filter { it.length >= 3 && it.lowercase() !in stopWords }
        if (tokens.isEmpty()) return null
        return tokens.takeLast(2).joinToString(" ").lowercase()
    }

    private fun detectFriendlyGrammarTip(spoken: String): String? {
        val lower = spoken.lowercase()
        return when {
            Regex("""\bi\s+is\b""").containsMatchIn(lower) ->
                "Quick friendly tip: we usually say 'I am' instead of 'I is'."
            Regex("""\byesterday\s+i\s+go\b""").containsMatchIn(lower) ||
                Regex("""\bi\s+go\s+.*\byesterday\b""").containsMatchIn(lower) ->
                "Quick friendly tip: for yesterday, use past tense 'I went' instead of 'I go'."
            Regex("""\b(?:she|he)\s+have\b""").containsMatchIn(lower) ->
                "Quick tip: with 'he' or 'she', say 'has' instead of 'have'."
            Regex("""\bin\s+night\b""").containsMatchIn(lower) ->
                "Quick natural tip: in English we say 'at night' instead of 'in night'."
            Regex("""\bin\s+morning\b""").containsMatchIn(lower) ->
                "Quick tip: remember 'the' here — 'in the morning' sounds super natural."
            Regex("""\bi\s+am\s+go\b""").containsMatchIn(lower) ->
                "Quick tip: say either 'I go' or 'I am going'!"
            else -> null
        }
    }

    private fun buildHintForQuestion(question: String): String {
        val qLower = question.lowercase()
        return when {
            qLower.contains("where are you") -> "Say your city, town, or state in a full sentence, like 'I live in...'."
            qLower.contains("name") -> "Introduce yourself warmly: 'My name is..., and I enjoy...'."
            qLower.contains("free time") || qLower.contains("hobby") -> "Share one activity you enjoy and why it makes you happy."
            qLower.contains("yesterday") || qLower.contains("last") || qLower.contains("memory") -> "Use past tense verbs like 'went', 'played', 'saw', or 'felt'."
            qLower.contains("morning") || qLower.contains("evening") || qLower.contains("schedule") -> "Use time words like 'at 7 AM', 'in the morning', or 'on weekends'."
            else -> "Answer naturally in 1 or 2 full sentences with one extra detail or reason."
        }
    }
}
