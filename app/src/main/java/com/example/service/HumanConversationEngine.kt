package com.example.service

import com.example.data.model.NovaChatMessage
import com.example.data.model.SpeakingEvaluation
import com.example.data.model.SpeakingPrompt
import com.example.util.AssameseTranslator
import com.example.util.ExampleAnswerGenerator

/**
 * Pedagogical English teacher conversation engine for Nova AI.
 * Enforces strict topic adherence, asks one logically connected question at a time,
 * gently corrects student English grammar with simple explanations and natural phrasing,
 * avoids repeating questions, and summarizes the session after 5–8 turns.
 */
object HumanConversationEngine {

    private enum class TopicDomain {
        INTRODUCTION,
        FAMILY,
        SCHOOL,
        ROUTINE,
        HOBBIES,
        TRAVEL,
        FOOD,
        CAREER,
        TECHNOLOGY,
        NATURE,
        CUSTOM
    }

    private data class GrammarAnalysis(
        val hasError: Boolean,
        val correctedSentence: String? = null,
        val naturalExpression: String? = null,
        val spokenCorrectionLine: String? = null,
        val shortTip: String? = null
    )

    private val genericNames = setOf(
        "", "student", "guest", "user", "admin", "teacher", "faculty", "learner", "default_student"
    )

    private val nonNameAdjectivesAndNouns = setOf(
        "fine", "good", "great", "well", "happy", "okay", "ok", "tired", "busy", "sad",
        "excited", "ready", "hungry", "sleepy", "nervous", "bored", "sorry", "sure",
        "student", "teacher", "engineer", "doctor", "boy", "girl", "man", "woman",
        "here", "home", "from", "going", "doing", "trying", "learning", "practicing",
        "speaking", "living", "working", "studying", "indian", "assamese", "very", "really",
        "pretty", "quite", "not", "just", "feeling", "also", "still", "always", "never",
        "class", "school", "years", "year"
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
     * Builds a warm, teacher-like opening message (display text + clean TTS prompt)
     * anchored strictly to the selected topic with a single opening question.
     */
    fun buildHumanGreeting(
        topic: SpeakingPrompt,
        studentName: String? = null
    ): Pair<String, String> {
        val displayTitle = topic.title.trim().ifBlank { "English Conversation" }
        val domain = resolveTopicDomain(topic)
        val defaultOpening = when (domain) {
            TopicDomain.INTRODUCTION -> "Hello! Welcome to English speaking practice. What is your name?"
            TopicDomain.FAMILY -> "Who lives with you in your family?"
            TopicDomain.SCHOOL -> "Which class do you study in at school?"
            TopicDomain.ROUTINE -> "What time do you usually wake up in the morning?"
            TopicDomain.HOBBIES -> "What do you enjoy doing in your free time?"
            TopicDomain.TRAVEL -> "Which place would you love to visit for a trip?"
            TopicDomain.FOOD -> "What is your favorite meal or dish?"
            TopicDomain.CAREER -> "What do you hope to become in the future?"
            TopicDomain.TECHNOLOGY -> "What do you use a phone or computer for most often?"
            TopicDomain.NATURE -> "What kind of weather or season do you like the most?"
            TopicDomain.CUSTOM -> "What do you think is most interesting about $displayTitle?"
        }
        val rawQ = topic.question.trim().ifBlank { defaultOpening }
        val firstName = extractCleanFirstName(studentName)
        val greetingWord = if (firstName != null) "Hey $firstName!" else "Hello!"

        val openerLine = when {
            displayTitle.contains("Introduction", ignoreCase = true) ||
                rawQ.contains("How are you", ignoreCase = true) ->
                "${if (firstName != null) "Hey $firstName!" else "Hey there!"} I'm Nova 😊 Really glad we get to chat about “$displayTitle” today."
            else ->
                "$greetingWord Welcome to English speaking practice. Let's talk about “$displayTitle” today."
        }

        val displayText = "$openerLine\n\n$rawQ"
        val cleanAudioTitle = displayTitle.replace("(", "").replace(")", "")
        val audioPrompt = "${openerLine.replace("😊", "").replace("“", "").replace("”", "").trim()} $rawQ"
            .replace(Regex("\\s+"), " ")
            .trim()
        return displayText to audioPrompt
    }

    /**
     * Generates a structured teacher response:
     * 1. Evaluates and gently corrects grammar in simple English (or praises + suggests natural phrasing).
     * 2. Reacts directly to the student's meaning while staying strictly on the selected topic.
     * 3. Asks ONLY ONE non-repeated, logically connected follow-up question — OR gives a closing summary
     *    with 2–3 improvement suggestions when currentTurn >= targetTurns.
     */
    fun generateReply(
        spokenText: String,
        topic: SpeakingPrompt,
        previousMessages: List<NovaChatMessage>,
        lastNovaQuestion: String,
        baseEval: SpeakingEvaluation,
        studentName: String? = null,
        currentTurn: Int = 1,
        targetTurns: Int = 6
    ): SpeakingEvaluation {
        val cleanSpoken = spokenText.trim()
        val lower = cleanSpoken.lowercase()
        val words = cleanSpoken.split(Regex("\\s+")).filter { it.isNotBlank() }
        val domain = resolveTopicDomain(topic)
        val previouslyAsked = collectAskedQuestions(previousMessages, lastNovaQuestion)

        val grammar = analyzeStudentEnglish(cleanSpoken, domain)
        val studentAskedQuestion = detectAndAnswerStudentQuestion(cleanSpoken, lower, topic)

        // Closing session summary after completing target turns
        if (currentTurn >= targetTurns) {
            return buildSessionSummaryEvaluation(
                cleanSpoken = cleanSpoken,
                topic = topic,
                domain = domain,
                previousMessages = previousMessages,
                baseEval = baseEval,
                grammar = grammar,
                studentName = studentName
            )
        }

        val (reaction, candidates, hintOverride) = buildTopicAnchoredResponse(
            cleanSpoken = cleanSpoken,
            lower = lower,
            words = words,
            topic = topic,
            domain = domain,
            lastNovaQuestion = lastNovaQuestion,
            previousMessages = previousMessages,
            studentName = studentName,
            currentTurn = currentTurn
        )

        val chosenQuestion = ensureSingleQuestion(
            pickFreshTopicQuestion(
                candidates = candidates,
                previouslyAsked = previouslyAsked,
                topic = topic,
                domain = domain,
                cleanSpoken = cleanSpoken,
                currentTurn = currentTurn
            )
        )

        val responseParts = mutableListOf<String>()

        if (studentAskedQuestion != null) {
            responseParts.add(studentAskedQuestion)
        }

        // Pedagogical teacher praise or gentle grammar correction
        when {
            grammar.hasError && !grammar.spokenCorrectionLine.isNullOrBlank() -> {
                responseParts.add(grammar.spokenCorrectionLine)
                // Only add extra reaction if it adds specific topic acknowledgement (like a person's name or hobby)
                if (words.size >= 5 && reaction.isNotBlank() && !isGenericPraise(reaction)) {
                    responseParts.add(reaction)
                }
            }
            grammar.naturalExpression != null && !grammar.spokenCorrectionLine.isNullOrBlank() -> {
                responseParts.add(grammar.spokenCorrectionLine)
                if (reaction.isNotBlank() && !isGenericPraise(reaction) && words.size >= 6) {
                    responseParts.add(reaction)
                }
            }
            else -> {
                if (studentAskedQuestion == null && reaction.isNotBlank()) {
                    responseParts.add(reaction)
                } else if (studentAskedQuestion != null && words.size > 5 && reaction.isNotBlank()) {
                    responseParts.add(reaction)
                }
            }
        }

        responseParts.add(chosenQuestion)

        val fullReply = responseParts
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val computedCefr = baseEval.cefrLevel.ifBlank { computeCefrFromScore(baseEval.overallScore) }
        val resolvedHint = hintOverride ?: buildHintForQuestion(chosenQuestion, topic)
        val resolvedExamples = ExampleAnswerGenerator.generateForQuestion(chosenQuestion, topic)
        val improvedTip = grammar.shortTip ?: baseEval.suggestedImprovement

        return baseEval.copy(
            cefrLevel = computedCefr,
            aiCoachResponse = fullReply,
            followUpQuestion = chosenQuestion,
            suggestedImprovement = improvedTip,
            correctedSentence = grammar.correctedSentence,
            naturalExpression = grammar.naturalExpression,
            isSessionSummary = false,
            improvementSuggestions = emptyList(),
            hint = resolvedHint,
            exampleAnswers = resolvedExamples
        )
    }

    /**
     * Generates a fresh, single, topic-anchored question when the student taps "Next".
     */
    fun generateNextTopicQuestion(
        topic: SpeakingPrompt,
        previousMessages: List<NovaChatMessage>
    ): Triple<String, String, String> {
        val previouslyAsked = collectAskedQuestions(previousMessages, "")
        val domain = resolveTopicDomain(topic)
        val pool = getDomainQuestionPool(topic, domain, studentName = null, knownWord = null)

        val freshQ = ensureSingleQuestion(
            pool.firstOrNull { (q, _) -> !isAlreadyAsked(q, previouslyAsked) }?.first
                ?: pool.first().first
        )
        val freshHint = pool.firstOrNull { ensureSingleQuestion(it.first) == freshQ }?.second
            ?: buildHintForQuestion(freshQ, topic)

        val transitions = listOf(
            "Let's try the next question on ${topic.title.ifBlank { "this topic" }}.",
            "Good! Here is another question for our practice:",
            "Great job so far! Let's continue talking about ${topic.title.ifBlank { "this topic" }}:"
        )
        val transition = transitions[previousMessages.size % transitions.size]
        val fullText = "$transition\n\n$freshQ"
        val audioText = "$transition $freshQ"
        return Triple(freshQ, fullText, "$freshHint|||$audioText")
    }

    // =========================================================================
    // SESSION COMPLETION SUMMARY (After 5–8 meaningful turns)
    // =========================================================================

    private fun buildSessionSummaryEvaluation(
        cleanSpoken: String,
        topic: SpeakingPrompt,
        domain: TopicDomain,
        previousMessages: List<NovaChatMessage>,
        baseEval: SpeakingEvaluation,
        grammar: GrammarAnalysis,
        studentName: String?
    ): SpeakingEvaluation {
        val topicLabel = topic.title.trim().ifBlank { "this topic" }
        val knownName = extractCleanFirstName(studentName)
        val studentMessages = previousMessages.filter { !it.isNova }
        val pastScores = previousMessages.mapNotNull { it.evaluation?.overallScore }.filter { it > 0 } +
            listOf(baseEval.overallScore).filter { it > 0 }
        val avgScore = if (pastScores.isNotEmpty()) pastScores.average().toInt() else baseEval.overallScore
        val avgGrammar = (previousMessages.mapNotNull { it.evaluation?.grammarScore } + baseEval.grammarScore)
            .filter { it > 0 }
            .ifEmpty { listOf(baseEval.grammarScore) }
            .average()
            .toInt()
        val avgVocab = (previousMessages.mapNotNull { it.evaluation?.vocabularyScore } + baseEval.vocabularyScore)
            .filter { it > 0 }
            .ifEmpty { listOf(baseEval.vocabularyScore) }
            .average()
            .toInt()

        val hadCorrections = previousMessages.any { !it.evaluation?.correctedSentence.isNullOrBlank() } || grammar.hasError
        val suggestions = mutableListOf<String>()

        if (hadCorrections) {
            suggestions.add("Check linking verbs like 'is', 'am', and 'are' in every sentence (e.g., 'My name is...').")
        } else {
            suggestions.add("Keep using full sentences with clear 'is/am/are' and verb forms.")
        }

        when (domain) {
            TopicDomain.ROUTINE -> suggestions.add("Use accurate time prepositions: 'at' for clock times, 'in' for the morning/evening, and 'on' for weekdays.")
            TopicDomain.SCHOOL -> suggestions.add("Expand answers about your subjects and classes using 'because' to give clear reasons.")
            TopicDomain.FAMILY -> suggestions.add("Practise descriptive words like 'caring', 'supportive', and 'together' when describing family members.")
            TopicDomain.INTRODUCTION -> suggestions.add("Use natural expressions like 'I am in Class...' and share 2–3 connected details smoothly.")
            else -> suggestions.add("Add one example or reason using 'because' or 'for example' to make your speaking richer.")
        }

        val avgWordCount = (studentMessages.map { it.text.split(Regex("\\s+")).size } +
            cleanSpoken.split(Regex("\\s+")).size).average()
        if (avgWordCount < 7.0) {
            suggestions.add("Try speaking 1–2 complete sentences instead of short phrases to build fluency.")
        } else {
            suggestions.add("Practice speaking at a steady pace and review 3 new words from \"$topicLabel\" today.")
        }

        val finalSentenceFeedback = when {
            grammar.hasError && !grammar.spokenCorrectionLine.isNullOrBlank() -> grammar.spokenCorrectionLine
            grammar.naturalExpression != null && !grammar.spokenCorrectionLine.isNullOrBlank() -> grammar.spokenCorrectionLine
            else -> "Great answer! That was a clear closing response."
        }

        val grammarNote = when {
            avgGrammar >= 82 -> "Your grammar was very accurate throughout our \"$topicLabel\" chat."
            hadCorrections -> "You improved your sentence structure step by step during our \"$topicLabel\" practice."
            else -> "You formed clear, understandable sentences throughout our \"$topicLabel\" chat."
        }
        val vocabNote = if (avgVocab >= 80) {
            "You used good everyday vocabulary for this topic."
        } else {
            "Practising a few topic words aloud will boost your confidence even more."
        }

        val nameCall = if (knownName != null) "Well done, $knownName!" else "Well done!"
        val closingQuestion = "Would you like to practise another topic?"

        val fullSummary = buildString {
            append(finalSentenceFeedback)
            append(" ")
            append("$nameCall You completed our \"$topicLabel\" practice with an average score of $avgScore%. ")
            append("$grammarNote $vocabNote ")
            append("Keep practising every day — you are making real progress! ")
            append(closingQuestion)
        }.replace(Regex("\\s+"), " ").trim()

        val computedCefr = baseEval.cefrLevel.ifBlank { computeCefrFromScore(avgScore) }

        return baseEval.copy(
            cefrLevel = computedCefr,
            aiCoachResponse = fullSummary,
            followUpQuestion = closingQuestion,
            suggestedImprovement = suggestions.firstOrNull() ?: baseEval.suggestedImprovement,
            correctedSentence = grammar.correctedSentence,
            naturalExpression = grammar.naturalExpression,
            isSessionSummary = true,
            improvementSuggestions = suggestions.take(3),
            hint = "Tap 'Choose Another Topic' to start a new topic or tap 'Restart' to practise \"$topicLabel\" again.",
            exampleAnswers = listOf(
                "Yes, I would love to practise another topic!",
                "Sure, let's talk about Daily Routine next."
            )
        )
    }

    // =========================================================================
    // SYSTEMATIC GRAMMAR CORRECTION & NATURAL EXPRESSION ENGINE
    // =========================================================================

    private fun analyzeStudentEnglish(spoken: String, domain: TopicDomain): GrammarAnalysis {
        val trimmed = spoken.trim()
        val lower = trimmed.lowercase()

        // 1. "My name Ali" -> missing "is"
        Regex("""^\s*my\s+name\s+(?!is\b|was\b)([A-Za-z]{2,20})\s*[.!]?\s*$""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val name = m.groupValues[1].replaceFirstChar { it.uppercaseChar() }
                val fixed = "My name is $name."
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to use \"is\" here.",
                    shortTip = "Remember to use \"is\" in \"My name is $name.\""
                )
            }

        // 2. "I 15 years old" -> missing "am"
        Regex("""\bi\s+(\d{1,2})\s+years?\s+old\b""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val age = m.groupValues[1]
                val fixed = "I am $age years old."
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to use \"am\" after \"I\".",
                    shortTip = "Use \"I am $age years old\" with \"am\"."
                )
            }

        // 3. "I am 15 year old" -> plural "years"
        Regex("""\bi\s*(?:am|'m)\s+(\d{1,2})\s+year\s+old\b""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val age = m.groupValues[1]
                val fixed = "I am $age years old."
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Nice try! A better sentence is: \"$fixed\" Use \"years\" for numbers more than one.",
                    shortTip = "Say \"$age years old\" (plural \"years\")."
                )
            }

        // 4. "I is ..." -> "I am ..."
        if (Regex("""\bi\s+is\b""").containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\b[Ii]\s+is\b"""), "I am").ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to use \"am\" with \"I\".",
                shortTip = "Use \"I am\" instead of \"I is\"."
            )
        }

        // 5. "I am live in ..." / "I am go ..."
        if (Regex("""\bi\s+(?:am|'m)\s+live\b""").containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\b[Ii]\s+(?:am|'m)\s+live\b""", RegexOption.IGNORE_CASE), "I live")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" You don't need \"am\" before \"live\".",
                shortTip = "Say \"I live in...\" without \"am\"."
            )
        }

        if (Regex("""\bi\s+(?:am|'m)\s+go\b""").containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\b[Ii]\s+(?:am|'m)\s+go\b""", RegexOption.IGNORE_CASE), "I go")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Good try! Say \"$fixed\" or \"I am going\" instead of \"I am go\".",
                shortTip = "Say \"I go\" for habits or \"I am going\" for right now."
            )
        }

        // 6. "I live Guwahati" -> missing "in"
        Regex("""\bi\s+live\s+(?!in\b|at\b|on\b|with\b|near\b|here\b|there\b|alone\b)([A-Za-z]{3,20})\b""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val city = m.groupValues[1].replaceFirstChar { it.uppercaseChar() }
                val fixed = "I live in $city."
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to add \"in\" before a place.",
                    shortTip = "Use \"in\" before a city or town name."
                )
            }

        // 7. "My hobby cricket" / "My hobby is read"
        Regex("""^\s*my\s+hobby\s+(?!is\b|was\b|are\b)([A-Za-z ]{3,25})\s*[.!]?\s*$""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val h = m.groupValues[1].trim()
                val fixed = "My hobby is $h."
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to use \"is\" here.",
                    shortTip = "Use \"is\" in \"My hobby is...\"."
                )
            }

        // 8. "he/she have" -> "he/she has"
        Regex("""\b(he|she)\s+have\b""", RegexOption.IGNORE_CASE).find(trimmed)?.let { m ->
            val pron = m.groupValues[1].replaceFirstChar { it.uppercaseChar() }
            val fixed = trimmed.replace(Regex("""\b(he|she)\s+have\b""", RegexOption.IGNORE_CASE), "$pron has")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Nice try! A better sentence is: \"$fixed\" Use \"has\" with \"he\" or \"she\".",
                shortTip = "Use \"has\" instead of \"have\" after \"he\" or \"she\"."
            )
        }

        // 9. "yesterday I go / come / see / eat / wake"
        val pastFixes = mapOf(
            "go" to "went", "come" to "came", "see" to "saw",
            "eat" to "ate", "wake" to "woke", "play" to "played", "study" to "studied"
        )
        for ((present, past) in pastFixes) {
            if (Regex("""\byesterday\b.*\bi\s+$present\b|\bi\s+$present\b.*\byesterday\b""", RegexOption.IGNORE_CASE)
                    .containsMatchIn(lower)
            ) {
                val fixed = trimmed.replace(Regex("""\b[Ii]\s+$present\b""", RegexOption.IGNORE_CASE), "I $past")
                    .ensureSentencePeriod()
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Use past tense \"$past\" for yesterday.",
                    shortTip = "Use past tense \"$past\" when talking about yesterday."
                )
            }
        }

        // 10. "didn't went" -> "didn't go"
        if (Regex("""\bdidn't\s+went\b|\bdid\s+not\s+went\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\b(didn't|did\s+not)\s+went\b""", RegexOption.IGNORE_CASE), "didn't go")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Nice try! A better sentence is: \"$fixed\" After \"didn't\", use the base verb \"go\".",
                shortTip = "After \"didn't\", always use the base verb (didn't go)."
            )
        }

        // 11. Time prepositions: "in night", "in morning", "on 7 am", "in 7 o'clock"
        if (Regex("""\bin\s+night\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\bin\s+night\b""", RegexOption.IGNORE_CASE), "at night")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Good try! In English, we say \"at night\" instead of \"in night\". So: \"$fixed\"",
                shortTip = "Use \"at night\" (not \"in night\")."
            )
        }

        if (Regex("""\bin\s+(morning|afternoon|evening)\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            val part = Regex("""\bin\s+(morning|afternoon|evening)\b""", RegexOption.IGNORE_CASE)
                .find(lower)?.groupValues?.get(1) ?: "morning"
            val fixed = trimmed.replace(
                Regex("""\bin\s+(morning|afternoon|evening)\b""", RegexOption.IGNORE_CASE),
                "in the $part"
            ).ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Remember to use \"the\" before \"$part\".",
                shortTip = "Say \"in the $part\" with \"the\"."
            )
        }

        Regex("""\b(?:in|on)\s+(\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.|o'clock))\b""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val timePart = m.groupValues[1]
                val fixed = trimmed.replace(
                    Regex("""\b(?:in|on)\s+${Regex.escape(timePart)}""", RegexOption.IGNORE_CASE),
                    "at $timePart"
                ).ensureSentencePeriod()
                return GrammarAnalysis(
                    hasError = true,
                    correctedSentence = fixed,
                    spokenCorrectionLine = "Good try! A better sentence is: \"$fixed\" Always use \"at\" for exact clock times.",
                    shortTip = "Use \"at\" before exact clock times like \"at $timePart\"."
                )
            }

        // 12. "more better" -> "better" / "much better"
        if (Regex("""\bmore\s+better\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            val fixed = trimmed.replace(Regex("""\bmore\s+better\b""", RegexOption.IGNORE_CASE), "much better")
                .ensureSentencePeriod()
            return GrammarAnalysis(
                hasError = true,
                correctedSentence = fixed,
                spokenCorrectionLine = "Nice try! Say \"better\" or \"much better\" instead of \"more better\". So: \"$fixed\"",
                shortTip = "Say \"much better\" rather than \"more better\"."
            )
        }

        // 13. Valid sentence -> offer a natural everyday expression when helpful
        Regex("""\bi\s+(?:study|read)\s+in\s+class\s+(\d{1,2}[A-Za-z]?)\b""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val cls = m.groupValues[1].uppercase()
                val natural = "I am in Class $cls."
                return GrammarAnalysis(
                    hasError = false,
                    naturalExpression = natural,
                    spokenCorrectionLine = "Very good! You can also say, \"$natural\"",
                    shortTip = "Try natural phrasing like \"$natural\"."
                )
            }

        Regex("""\bmy\s+favourite?\s+(?:subject|sport|food|color)\s+is\s+([A-Za-z ]{2,20})""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.let { m ->
                val item = m.groupValues[1].trim().trimEnd('.', '!')
                val natural = "I really enjoy $item."
                return GrammarAnalysis(
                    hasError = false,
                    naturalExpression = natural,
                    spokenCorrectionLine = "Excellent! Your sentence is correct. You can also say, \"$natural\""
                )
            }

        if (domain == TopicDomain.ROUTINE &&
            Regex("""\bi\s+wake\s+up\s+at\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower) &&
            !lower.contains("usually")
        ) {
            val timePart = extractTimeMention(trimmed)
            if (timePart != null) {
                return GrammarAnalysis(
                    hasError = false,
                    naturalExpression = "I usually wake up at $timePart.",
                    spokenCorrectionLine = "Excellent! Your sentence is correct. Adding \"usually\" makes it sound even more natural."
                )
            }
        }

        return GrammarAnalysis(hasError = false)
    }

    // =========================================================================
    // CONTEXTUAL TOPIC-ANCHORED RESPONSE BUILDER
    // =========================================================================

    private fun buildTopicAnchoredResponse(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        topic: SpeakingPrompt,
        domain: TopicDomain,
        lastNovaQuestion: String,
        previousMessages: List<NovaChatMessage>,
        studentName: String?,
        currentTurn: Int
    ): Triple<String, List<String>, String?> {
        val extractedName = extractSpokenPersonName(cleanSpoken)
        val extractedPlace = extractPlace(cleanSpoken, lower)
        val hobbyMatch = findHobbyMatch(lower)
        val foodMatch = findFoodMatch(lower)
        val timeMatch = extractTimeMention(cleanSpoken)
        val studyMatch = findStudyOrWorkMatch(lower)
        val personMention = findPersonMention(lower)
        val positiveMood = hasAnyWord(lower, listOf("fine", "good", "great", "awesome", "wonderful", "fantastic", "happy", "amazing", "excited", "well", "blessed")) ||
            lower.contains("not bad") || lower.contains("pretty good") || lower.contains("doing well")
        val tiredMood = hasAnyWord(lower, listOf("tired", "sleepy", "exhausted", "busy", "stressed", "sad", "bored", "sick", "bad", "rough", "hard"))
        val knownName = extractedName ?: extractCleanFirstName(studentName)

        // Pure short greeting ("Hi", "Hello")
        val pureGreeting = words.size <= 3 && hasAnyWord(lower, listOf("hi", "hello", "hey", "namaskar", "good morning", "good evening", "good afternoon"))
        if (pureGreeting) {
            val nameBit = if (knownName != null) " $knownName" else ""
            return Triple(
                "Hello$nameBit! Glad to practise with you.",
                getDomainQuestionPool(topic, domain, knownName, null).map { it.first },
                "Answer in 1 or 2 clear sentences about ${topic.title.ifBlank { "this topic" }}."
            )
        }

        // Student introduced their name ("My name is Rahat" / "My name Ali")
        if (extractedName != null) {
            val placeTail = if (extractedPlace != null) " $extractedPlace sounds like a wonderful place!" else ""
            val reaction = if (domain == TopicDomain.INTRODUCTION) {
                "Nice to meet you, $extractedName!$placeTail"
            } else {
                "Nice to meet you, $extractedName!"
            }
            val qs = when (domain) {
                TopicDomain.INTRODUCTION -> listOf(
                    "How old are you, $extractedName?",
                    "Which class do you study in, $extractedName?",
                    "Where do you live, $extractedName?",
                    "What is your favourite subject or hobby, $extractedName?"
                )
                else -> getDomainQuestionPool(topic, domain, extractedName, null).map { it.first }
            }
            return Triple(reaction, qs, "Answer clearly in 1 or 2 full sentences.")
        }

        // Student answered a mood / "How are you?" opener
        val lastAskedHowAreYou = lastNovaQuestion.contains("how are you", ignoreCase = true) ||
            lastNovaQuestion.contains("how's your day", ignoreCase = true) ||
            lastNovaQuestion.contains("how has your day", ignoreCase = true)
        if ((positiveMood || tiredMood) && words.size <= 12 && hobbyMatch == null && foodMatch == null && extractedPlace == null && studyMatch == null && timeMatch == null && personMention == null) {
            if (tiredMood) {
                val qs = if (domain == TopicDomain.ROUTINE) {
                    listOf(
                        "What time do you usually get some rest in the evening?",
                        "What helps you relax after a busy day?"
                    )
                } else {
                    getDomainQuestionPool(topic, domain, knownName, null).map { it.first }
                }
                return Triple(
                    "I understand, busy days can be tiring.",
                    qs,
                    "Take a breath and reply in one simple sentence."
                )
            }
            if (positiveMood) {
                val reaction = if (lastAskedHowAreYou) {
                    "I am glad to hear you are doing well!"
                } else {
                    "That is great to hear!"
                }
                val qs = if (domain == TopicDomain.INTRODUCTION) {
                    val hasAskedName = previousMessages.any { !it.isNova && extractSpokenPersonName(it.text) != null }
                    if (!hasAskedName) {
                        listOf(
                            "What is your name?",
                            "Where do you live?",
                            "Which class do you study in?",
                            "What do you enjoy doing in your free time?"
                        )
                    } else {
                        listOf(
                            "Which class do you study in?",
                            "What is your favourite subject?",
                            "What do you enjoy doing in your free time?"
                        )
                    }
                } else {
                    getDomainQuestionPool(topic, domain, knownName, null).map { it.first }
                }
                return Triple(reaction, qs, "Share a simple, clear sentence about yourself.")
            }
        }

        // Domain-specific contextual logic (keeps topic strictly locked!)
        return when (domain) {
            TopicDomain.INTRODUCTION -> buildIntroductionFollowUp(
                cleanSpoken, lower, words, extractedPlace, hobbyMatch, foodMatch, studyMatch, personMention, knownName, currentTurn
            )
            TopicDomain.FAMILY -> buildFamilyFollowUp(
                cleanSpoken, lower, words, personMention, foodMatch, hobbyMatch, studyMatch, topic
            )
            TopicDomain.SCHOOL -> buildSchoolFollowUp(
                cleanSpoken, lower, words, studyMatch, hobbyMatch, personMention, timeMatch, topic
            )
            TopicDomain.ROUTINE -> buildRoutineFollowUp(
                cleanSpoken, lower, words, timeMatch, foodMatch, studyMatch, hobbyMatch, topic
            )
            TopicDomain.HOBBIES -> buildHobbiesFollowUp(
                cleanSpoken, lower, words, hobbyMatch, timeMatch, personMention, topic
            )
            TopicDomain.TRAVEL -> buildTravelFollowUp(
                cleanSpoken, lower, words, extractedPlace, foodMatch, personMention, topic
            )
            TopicDomain.FOOD -> buildFoodFollowUp(
                cleanSpoken, lower, words, foodMatch, personMention, timeMatch, topic
            )
            TopicDomain.CAREER -> buildCareerFollowUp(
                cleanSpoken, lower, words, studyMatch, personMention, topic
            )
            TopicDomain.TECHNOLOGY -> buildTechnologyFollowUp(
                cleanSpoken, lower, words, studyMatch, hobbyMatch, topic
            )
            TopicDomain.NATURE -> buildNatureFollowUp(
                cleanSpoken, lower, words, extractedPlace, timeMatch, topic
            )
            TopicDomain.CUSTOM -> buildCustomTopicFollowUp(
                cleanSpoken, lower, words, topic, hobbyMatch, studyMatch, personMention
            )
        }
    }

    private fun buildIntroductionFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        extractedPlace: String?,
        hobbyMatch: ContextualMatch?,
        foodMatch: String?,
        studyMatch: ContextualMatch?,
        personMention: String?,
        knownName: String?,
        currentTurn: Int
    ): Triple<String, List<String>, String?> {
        // Age mentioned ("I am 15 years old")
        if (Regex("""\b\d{1,2}\s+years?\s+old\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return Triple(
                "Excellent! Your sentence is correct.",
                listOf(
                    "Which class do you study in?",
                    "Where do you live?",
                    "What is your favourite subject?"
                ),
                "Answer using 'I am in Class...' or 'I study in Class...'."
            )
        }
        // Class / grade mentioned ("I study in class 9")
        if (Regex("""\bclass\s+\d{1,2}\b|\bgrade\s+\d{1,2}\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return Triple(
                "Very good!",
                listOf(
                    "What is your favourite subject?",
                    "Where do you live?",
                    "What do you enjoy doing in your free time?"
                ),
                "Name one subject you enjoy and share a short reason."
            )
        }
        if (hobbyMatch != null) {
            return Triple(hobbyMatch.reaction, hobbyMatch.questions, hobbyMatch.hint)
        }
        if (studyMatch != null) {
            return Triple(studyMatch.reaction, studyMatch.questions, studyMatch.hint)
        }
        if (extractedPlace != null) {
            return Triple(
                "Oh, $extractedPlace sounds like a nice place!",
                listOf(
                    "What do you like most about living in $extractedPlace?",
                    "Who do you live with in $extractedPlace?",
                    "What do you usually do on weekends in $extractedPlace?"
                ),
                "Share one detail you enjoy about $extractedPlace."
            )
        }
        if (personMention != null) {
            return Triple(
                "It is lovely that you mentioned your $personMention!",
                listOf(
                    "What do you enjoy doing together with your $personMention?",
                    "What is your favourite hobby in your free time?"
                ),
                "Describe one activity in a full sentence."
            )
        }
        if (foodMatch != null) {
            return Triple(
                "Ooh, $foodMatch sounds delicious!",
                listOf(
                    "Who makes $foodMatch best at your home?",
                    "What do you usually enjoy doing in your free time?"
                ),
                "Reply in 1 or 2 clear sentences."
            )
        }
        val focus = extractFocusPhrase(cleanSpoken)
        val qs = if (focus != null && words.size >= 3) {
            listOf(
                "Why do you like $focus?",
                "What do you enjoy doing in your free time?",
                "What is one goal you have for learning English?"
            )
        } else {
            listOf(
                "Which class do you study in?",
                "What is your favourite subject?",
                "What do you enjoy doing in your free time?",
                "Where do you live?"
            )
        }
        val praise = if (currentTurn <= 2) "Good! Thanks for sharing." else "Nice! Keep going."
        return Triple(praise, qs, "Answer clearly in 1 or 2 simple sentences.")
    }

    private fun buildFamilyFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        personMention: String?,
        foodMatch: String?,
        hobbyMatch: ContextualMatch?,
        studyMatch: ContextualMatch?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (Regex("""\b\d+\s+(?:people|members|persons)\b|\bbrothers?\b|\bsisters?\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return Triple(
                "Thank you for sharing about your family!",
                listOf(
                    "What does your father or mother do?",
                    "What do you enjoy doing together with your family?",
                    "Who are you closest to in your family?"
                ),
                "Talk about one family member using simple present tense."
            )
        }
        if (personMention != null) {
            return Triple(
                "It sounds like your $personMention means a lot to you!",
                listOf(
                    "What is one nice quality you admire in your $personMention?",
                    "What do you and your $personMention usually do together?",
                    "How do you help your family at home?"
                ),
                "Describe what you enjoy doing together in 1–2 sentences."
            )
        }
        if (foodMatch != null) {
            return Triple(
                "Yum, sharing $foodMatch with family is always special!",
                listOf(
                    "Who usually cooks $foodMatch in your family?",
                    "Do you all eat dinner together every evening?"
                ),
                "Tell Nova who cooks or shares meals in your family."
            )
        }
        if (hobbyMatch != null) {
            return Triple(
                "Oh nice, ${hobbyMatch.display} sounds fun! Bringing it back to your family:",
                listOf(
                    "Does anyone in your family enjoy ${hobbyMatch.display} with you?",
                    "What does your family like doing together on Sundays?"
                ),
                "Connect your answer back to your family."
            )
        }
        if (studyMatch != null) {
            return Triple(
                "That's great!",
                listOf(
                    "Who in your family helps you most with your studies?",
                    "What is a favourite memory you have with your family?"
                ),
                "Mention a family member and how they support you."
            )
        }
        val focus = extractFocusPhrase(cleanSpoken)
        return Triple(
            "Thanks for sharing that about your family!",
            if (focus != null) listOf(
                "What do you enjoy doing with your family on weekends?",
                "How do you help your parents at home?",
                "What is a happy memory you share with your family?"
            ) else getDomainQuestionPool(topic, TopicDomain.FAMILY, null, null).map { it.first },
            "Share a real detail about your family in 1 or 2 full sentences."
        )
    }

    private fun buildSchoolFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        studyMatch: ContextualMatch?,
        hobbyMatch: ContextualMatch?,
        personMention: String?,
        timeMatch: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (Regex("""\bclass\s+\d{1,2}\b|\bgrade\s+\d{1,2}\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return Triple(
                "Very good!",
                listOf(
                    "Which subject do you like the most at school?",
                    "How do you usually go to school in the morning?",
                    "Who is your favourite teacher at school?"
                ),
                "Name a subject or teacher and give one short reason."
            )
        }
        if (studyMatch != null) {
            return Triple(
                studyMatch.reaction,
                listOf(
                    "Why do you find ${studyMatch.display} interesting?",
                    "Who is your ${studyMatch.display} teacher, and what do you like about their class?",
                    "What other activities do you enjoy at school?"
                ),
                "Give one simple reason using 'because'."
            )
        }
        if (hobbyMatch != null) {
            return Triple(
                "Nice, ${hobbyMatch.display} is great! Keeping our chat focused on your school:",
                listOf(
                    "Do you get to enjoy ${hobbyMatch.display} at school with your classmates?",
                    "What is your favourite part of a normal school day?"
                ),
                "Tell Nova how that activity connects to your school life."
            )
        }
        if (personMention != null) {
            return Triple(
                "Having supportive $personMention makes school much happier!",
                listOf(
                    "What do you and your school friends usually do during lunch break?",
                    "Which subject do you enjoy studying with them?"
                ),
                "Describe your break time or class activity at school."
            )
        }
        if (timeMatch != null) {
            return Triple(
                "Nice, $timeMatch sounds like a steady school timing!",
                listOf(
                    "What is your first class when you reach school?",
                    "What do you enjoy most about your school?"
                ),
                "Share your first class or favourite school activity."
            )
        }
        return Triple(
            "Thanks for sharing about your school!",
            getDomainQuestionPool(topic, TopicDomain.SCHOOL, null, null).map { it.first },
            "Answer in 1 or 2 simple sentences about your school."
        )
    }

    private fun buildRoutineFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        timeMatch: String?,
        foodMatch: String?,
        studyMatch: ContextualMatch?,
        hobbyMatch: ContextualMatch?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (timeMatch != null) {
            return Triple(
                "Got it — around $timeMatch is a clear daily habit!",
                listOf(
                    "What is the first thing you do right after $timeMatch?",
                    "What time do you usually eat lunch or take a break?",
                    "What do you normally do in the evening?",
                    "What time do you go to bed at night?"
                ),
                "Use clear time words like 'then', 'after that', 'in the evening', or 'at 9 PM'."
            )
        }
        if (foodMatch != null) {
            return Triple(
                "Sounds tasty! Having $foodMatch is a nice part of the day.",
                listOf(
                    "What time do you usually eat your meals?",
                    "What do you do after finishing your studies in the evening?"
                ),
                "Use 'at' before clock times and 'in the evening/morning' for parts of the day."
            )
        }
        if (studyMatch != null) {
            return Triple(
                "Good focus on your studies!",
                listOf(
                    "How many hours do you usually study each evening?",
                    "What do you do to relax before going to sleep?"
                ),
                "Mention the time you study and what you do afterward."
            )
        }
        if (hobbyMatch != null) {
            return Triple(
                "Nice way to spend part of your day with ${hobbyMatch.display}!",
                listOf(
                    "At what time of day do you usually do ${hobbyMatch.display}?",
                    "How is your Sunday routine different from school days?"
                ),
                "Use 'on Sundays' or 'in the afternoon' in your reply."
            )
        }
        return Triple(
            "That sounds like a good routine!",
            getDomainQuestionPool(topic, TopicDomain.ROUTINE, null, null).map { it.first },
            "Share a clear time or daily habit using 'at', 'in', or 'on'."
        )
    }

    private fun buildHobbiesFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        hobbyMatch: ContextualMatch?,
        timeMatch: String?,
        personMention: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (hobbyMatch != null) {
            return Triple(hobbyMatch.reaction, hobbyMatch.questions, hobbyMatch.hint)
        }
        if (personMention != null) {
            return Triple(
                "Spending free time with your $personMention sounds fun!",
                listOf(
                    "What game or hobby do you and your $personMention enjoy the most?",
                    "How often do you get time for your favourite hobby?"
                ),
                "Name one activity and say why you like it."
            )
        }
        if (timeMatch != null) {
            return Triple(
                "Making time around $timeMatch sounds relaxing!",
                listOf(
                    "Why do you enjoy doing that hobby?",
                    "Is there any new hobby you would like to try?"
                ),
                "Give a simple reason using 'because'."
            )
        }
        return Triple(
            "Nice! Free time activities keep us energized.",
            getDomainQuestionPool(topic, TopicDomain.HOBBIES, null, null).map { it.first },
            "Talk about one hobby you enjoy and when you do it."
        )
    }

    private fun buildTravelFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        extractedPlace: String?,
        foodMatch: String?,
        personMention: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (extractedPlace != null) {
            return Triple(
                "$extractedPlace sounds like a wonderful place to visit!",
                listOf(
                    "What would you love to see first in $extractedPlace?",
                    "Who would you like to travel to $extractedPlace with?",
                    "How would you prefer to travel there — by train, bus, or plane?"
                ),
                "Describe one place or reason you want to visit $extractedPlace."
            )
        }
        if (personMention != null) {
            return Triple(
                "Traveling with your $personMention makes every trip more memorable!",
                listOf(
                    "What is the best trip you have taken so far?",
                    "Do you prefer visiting quiet mountains or busy cities?"
                ),
                "Share one memory or place you enjoyed visiting."
            )
        }
        if (foodMatch != null) {
            return Triple(
                "Trying local $foodMatch is one of the best parts of traveling!",
                listOf(
                    "Which place would you love to visit next for its food and sights?",
                    "What do you always pack when going on a trip?"
                ),
                "Name a destination or a favorite travel memory."
            )
        }
        return Triple(
            "That sounds like a lovely travel idea!",
            getDomainQuestionPool(topic, TopicDomain.TRAVEL, null, null).map { it.first },
            "Use 'I want to visit...' or 'Last year I went to...'."
        )
    }

    private fun buildFoodFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        foodMatch: String?,
        personMention: String?,
        timeMatch: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (foodMatch != null) {
            return Triple(
                "Ooh, $foodMatch sounds delicious!",
                listOf(
                    "Who cooks $foodMatch best at your home?",
                    "How often do you enjoy eating $foodMatch?",
                    "What makes $foodMatch taste so good to you?"
                ),
                "Describe the taste or who prepares $foodMatch."
            )
        }
        if (personMention != null) {
            return Triple(
                "Eating together with your $personMention is always warm and cheerful!",
                listOf(
                    "What dish does your $personMention cook best?",
                    "Do you prefer sweet snacks or spicy dishes?"
                ),
                "Name a dish and tell why you like it."
            )
        }
        return Triple(
            "Food brings everyone together!",
            getDomainQuestionPool(topic, TopicDomain.FOOD, null, null).map { it.first },
            "Describe a meal you enjoy using simple taste words like 'tasty', 'spicy', or 'sweet'."
        )
    }

    private fun buildCareerFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        studyMatch: ContextualMatch?,
        personMention: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        if (studyMatch != null) {
            return Triple(
                "Working hard on ${studyMatch.display} is a smart step toward your future!",
                listOf(
                    "What career or job do you dream of in the future?",
                    "Why does that goal inspire you?"
                ),
                "Use 'I want to become...' and give one clear reason."
            )
        }
        if (personMention != null) {
            return Triple(
                "Learning from your $personMention can really inspire your goals!",
                listOf(
                    "What skills do you want to improve to reach your future goal?",
                    "How can speaking good English help you in that career?"
                ),
                "Explain in 1 or 2 simple sentences."
            )
        }
        return Triple(
            "That is a thoughtful goal!",
            getDomainQuestionPool(topic, TopicDomain.CAREER, null, null).map { it.first },
            "Use phrases like 'I hope to become...' or 'I want to work in...'."
        )
    }

    private fun buildTechnologyFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        studyMatch: ContextualMatch?,
        hobbyMatch: ContextualMatch?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        val focus = extractFocusPhrase(cleanSpoken)
        if (focus != null && words.size >= 3) {
            return Triple(
                "Good point about $focus!",
                listOf(
                    "How does $focus help you with school or learning?",
                    "How much time do you spend using a phone or computer each day?",
                    "What is one helpful rule students should follow when using technology?"
                ),
                "Explain one benefit or good habit about technology."
            )
        }
        return Triple(
            "Technology is part of everyday student life today!",
            getDomainQuestionPool(topic, TopicDomain.TECHNOLOGY, null, null).map { it.first },
            "Give a simple example of how you use a phone or computer."
        )
    }

    private fun buildNatureFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        extractedPlace: String?,
        timeMatch: String?,
        topic: SpeakingPrompt
    ): Triple<String, List<String>, String?> {
        val focus = extractFocusPhrase(cleanSpoken)
        if (extractedPlace != null) {
            return Triple(
                "The nature around $extractedPlace sounds lovely!",
                listOf(
                    "What season or weather is nicest in $extractedPlace?",
                    "What is one easy way students can help keep nature clean?"
                ),
                "Describe the weather, trees, or seasons in simple words."
            )
        }
        if (focus != null && words.size >= 3) {
            return Triple(
                "Yes, $focus is so refreshing!",
                listOf(
                    "Why do you enjoy $focus in nature?",
                    "How can we take better care of our environment every day?"
                ),
                "Share one reason or small habit for protecting nature."
            )
        }
        return Triple(
            "Spending time close to nature is really refreshing!",
            getDomainQuestionPool(topic, TopicDomain.NATURE, null, null).map { it.first },
            "Describe a place in nature or your favourite season."
        )
    }

    private fun buildCustomTopicFollowUp(
        cleanSpoken: String,
        lower: String,
        words: List<String>,
        topic: SpeakingPrompt,
        hobbyMatch: ContextualMatch?,
        studyMatch: ContextualMatch?,
        personMention: String?
    ): Triple<String, List<String>, String?> {
        val label = topic.title.trim().ifBlank { "this topic" }
        val focus = extractFocusPhrase(cleanSpoken)
        val qs = buildList {
            if (focus != null && words.size >= 3) {
                add("What makes $focus important when thinking about $label?")
            }
            add("Can you give a short real-life example related to $label?")
            add("Why is $label useful or interesting for students today?")
            add("How has your experience with $label been so far?")
        }
        val reaction = when {
            hobbyMatch != null -> "Nice! Linking that to $label,"
            studyMatch != null -> "Good point about ${studyMatch.display}!"
            personMention != null -> "Nice that you mentioned your $personMention!"
            focus != null -> "Good answer about $focus!"
            else -> "Thank you for sharing clearly!"
        }
        return Triple(
            reaction,
            qs,
            topic.followUpTip.ifBlank { "Stay on '$label' and answer in 1 or 2 complete sentences." }
        )
    }

    // =========================================================================
    // TOPIC RESOLUTION & STRUCTURED QUESTION POOLS
    // =========================================================================

    private fun resolveTopicDomain(topic: SpeakingPrompt): TopicDomain {
        val text = "${topic.title} ${topic.category}".lowercase()
        return when {
            text.contains("intro") || text.contains("greeting") || text.contains("myself") || text.contains("about me") -> TopicDomain.INTRODUCTION
            text.contains("family") || text.contains("parent") -> TopicDomain.FAMILY
            text.contains("school") || text.contains("college") || text.contains("class") || text.contains("study") || text.contains("education") -> TopicDomain.SCHOOL
            text.contains("time") || text.contains("routine") || text.contains("daily") || text.contains("habit") || text.contains("schedule") -> TopicDomain.ROUTINE
            text.contains("hobby") || text.contains("hobbies") || text.contains("free time") || text.contains("sport") -> TopicDomain.HOBBIES
            text.contains("travel") || text.contains("vacation") || text.contains("trip") -> TopicDomain.TRAVEL
            text.contains("food") || text.contains("dish") || text.contains("meal") || text.contains("cook") -> TopicDomain.FOOD
            text.contains("career") || text.contains("ambition") || text.contains("future") || text.contains("job") -> TopicDomain.CAREER
            text.contains("tech") || text.contains("phone") || text.contains("internet") || text.contains("computer") -> TopicDomain.TECHNOLOGY
            text.contains("nature") || text.contains("weather") || text.contains("environment") -> TopicDomain.NATURE
            else -> TopicDomain.CUSTOM
        }
    }

    private fun getDomainQuestionPool(
        topic: SpeakingPrompt,
        domain: TopicDomain,
        studentName: String?,
        knownWord: String?
    ): List<Pair<String, String>> {
        val nameSuffix = if (!studentName.isNullOrBlank()) ", $studentName" else ""
        return when (domain) {
            TopicDomain.INTRODUCTION -> listOf(
                "What is your name$nameSuffix?" to "Say 'My name is...' cleanly.",
                "How old are you?" to "Say 'I am ... years old.'",
                "Which class do you study in?" to "Try 'I study in Class...' or 'I am in Class...'",
                "What is your favourite subject?" to "Name one subject and say why you like it.",
                "Where do you live?" to "Use 'in' before your city or town, like 'I live in...'.",
                "What do you enjoy doing in your free time?" to "Mention one hobby in a full sentence."
            )
            TopicDomain.FAMILY -> listOf(
                "How many people are in your family?" to "Say 'There are ... people in my family.'",
                "Who do you spend the most time with at home?" to "Mention a parent, sibling, or grandparent.",
                "What do you and your family enjoy doing together on weekends?" to "Share one simple activity you do together.",
                "How do you help your family at home?" to "Give one everyday example using present simple.",
                "What is a happy memory you have with your family?" to "Use simple past verbs like 'went', 'ate', or 'laughed'."
            )
            TopicDomain.SCHOOL -> listOf(
                "Which class do you study in?" to "Say 'I am in Class...'.",
                "What is your favourite subject at school?" to "Mention one subject and give a short reason.",
                "How do you go to school each morning?" to "Use 'by bus', 'by bicycle', or 'on foot'.",
                "Who is your best friend at school?" to "Share their name and one thing you do together.",
                "What do you enjoy doing during your school break?" to "Talk about talking with friends, eating, or playing."
            )
            TopicDomain.ROUTINE -> listOf(
                "What time do you usually wake up on weekdays?" to "Use 'at' before the time, e.g., 'I wake up at 6 AM.'",
                "What do you usually eat for breakfast?" to "Say 'I usually have... for breakfast.'",
                "What time do you reach school or start studying?" to "Use 'at' with your exact timing.",
                "What do you usually do in the evening?" to "Remember to say 'in the evening'.",
                "How is your Sunday routine different from weekdays?" to "Use 'on Sundays' to describe one relaxing habit."
            )
            TopicDomain.HOBBIES -> listOf(
                "What is your favourite hobby?" to "Say 'My favourite hobby is...' or 'I love...'.",
                "When did you start enjoying that hobby?" to "Say 'I started...' or 'For two years'.",
                "Who do you usually share this hobby with?" to "Talk about friends or family members.",
                "How do you feel when you spend time on your hobby?" to "Use feeling words like 'relaxed', 'happy', or 'excited'.",
                "Is there a new skill or hobby you want to learn next?" to "Use 'I want to learn...'."
            )
            TopicDomain.TRAVEL -> listOf(
                "What is one place you would love to visit?" to "Say 'I would love to visit...'.",
                "What makes that place special or interesting to you?" to "Mention nature, food, or famous sights.",
                "Who would you love to go with on that trip?" to "Say 'I would go with my...'.",
                "What is the best trip you have taken so far?" to "Share one real travel memory using past tense.",
                "Do you prefer mountains or beaches for a holiday?" to "Pick one and explain briefly using 'because'."
            )
            TopicDomain.FOOD -> listOf(
                "What is your favourite dish or meal?" to "Name one food you really enjoy.",
                "Who makes that dish best?" to "Mention your mother, father, or a favourite shop.",
                "What do you usually eat for dinner at home?" to "Keep your sentence simple and clear.",
                "Do you prefer homemade food or restaurant food?" to "Give one short reason using 'because'.",
                "Have you ever tried cooking any simple dish yourself?" to "Share what you cooked or want to learn to cook."
            )
            TopicDomain.CAREER -> listOf(
                "What job or career do you dream of in the future?" to "Say 'I want to become a...'.",
                "Why does that profession interest you?" to "Share one reason using 'because'.",
                "Which school subject helps the most with that goal?" to "Connect your studies to your goal.",
                "Who inspires you the most when thinking about your future?" to "Mention a parent, teacher, or role model.",
                "How will speaking fluent English help you in that career?" to "Give one practical example."
            )
            TopicDomain.TECHNOLOGY -> listOf(
                "What do you use a phone or computer for most often?" to "Share one study or everyday use.",
                "Which app or website helps you most with learning?" to "Name one helpful tool and why.",
                "How much time do you spend on screens each day?" to "Reply using 'about' or 'around'.",
                "What is one advantage of learning English using technology?" to "Mention practice, videos, or speaking.",
                "Why is it good to take breaks from phones every day?" to "Give one simple reason."
            )
            TopicDomain.NATURE -> listOf(
                "What kind of weather or season do you like the most?" to "Say 'My favourite season is... because...'.",
                "Is there a park, river, or hill near your home that you like?" to "Describe it in 1 or 2 simple sentences.",
                "What do you enjoy doing outdoors on a sunny day?" to "Mention walking, playing, or relaxing outside.",
                "Why are trees and clean water important for everyone?" to "Give one clear, simple reason.",
                "What small habit can students follow to keep their surroundings clean?" to "Share one practical action."
            )
            TopicDomain.CUSTOM -> {
                val label = topic.title.trim().ifBlank { "this topic" }
                val kwCue = topic.recommendedKeywords.firstOrNull { it.isNotBlank() }
                buildList {
                    add("What comes to your mind first when you hear about $label?" to "Share one clear idea about $label.")
                    if (kwCue != null) {
                        add("How would you describe $kwCue in $label?" to "Try using '$kwCue' inside a full sentence.")
                    }
                    add("Can you share a short personal experience related to $label?" to "Tell a brief 1–2 sentence story.")
                    add("Why is $label important in everyday life?" to "Explain using 'because'.")
                    add("What is one question or thought you find most interesting about $label?" to "Keep your answer clear and simple.")
                }
            }
        }
    }

    private fun pickFreshTopicQuestion(
        candidates: List<String>,
        previouslyAsked: Set<String>,
        topic: SpeakingPrompt,
        domain: TopicDomain,
        cleanSpoken: String,
        currentTurn: Int
    ): String {
        for (q in candidates) {
            val single = ensureSingleQuestion(q)
            if (single.isNotBlank() && !isAlreadyAsked(single, previouslyAsked)) {
                return single
            }
        }
        val pool = getDomainQuestionPool(topic, domain, studentName = null, knownWord = null)
        for ((q, _) in pool) {
            val single = ensureSingleQuestion(q)
            if (single.isNotBlank() && !isAlreadyAsked(single, previouslyAsked)) {
                return single
            }
        }
        val label = topic.title.trim().ifBlank { "this topic" }
        val fallbackPool = listOf(
            "What else would you like to share about $label?",
            "Can you give one short example about $label from your life?",
            "What do you enjoy most when talking about $label?"
        )
        return fallbackPool.firstOrNull { !isAlreadyAsked(it, previouslyAsked) } ?: ensureSingleQuestion(candidates.first())
    }

    // =========================================================================
    // HELPERS & TEXT SANITIZATION
    // =========================================================================

    /**
     * Guarantees Nova asks at most ONE question per message.
     */
    private fun ensureSingleQuestion(raw: String): String {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        val firstQMark = cleaned.indexOf('?')
        if (firstQMark == -1) return if (cleaned.endsWith(".")) cleaned else "$cleaned?"
        // Keep only the single sentence containing the first '?'
        val upToFirstQ = cleaned.substring(0, firstQMark + 1)
        val sentenceStart = maxOf(
            upToFirstQ.lastIndexOf(". "),
            upToFirstQ.lastIndexOf("! "),
            upToFirstQ.lastIndexOf("\n")
        )
        val singleSentence = if (sentenceStart >= 0) {
            upToFirstQ.substring(sentenceStart + 1).trim()
        } else {
            upToFirstQ.trim()
        }
        // Also simplify stacked ", and what..." or " and where..." within the same question
        return singleSentence
            .replace(Regex(",\\s*and\\s+(?:what|where|when|who|why|how)\\b[^?]*\\?", RegexOption.IGNORE_CASE), "?")
            .trim()
    }

    private fun String.ensureSentencePeriod(): String {
        val t = trim()
        if (t.isEmpty()) return t
        return if (t.endsWith(".") || t.endsWith("!") || t.endsWith("?")) t else "$t."
    }

    private fun isGenericPraise(reaction: String): Boolean {
        val l = reaction.lowercase()
        return l.startsWith("good!") || l.startsWith("nice!") || l.startsWith("thank you for sharing")
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
            prev.length > 12 && (prev.contains(norm) || norm.contains(prev))
        }
    }

    private fun detectAndAnswerStudentQuestion(
        cleanSpoken: String,
        lower: String,
        topic: SpeakingPrompt
    ): String? {
        return when {
            lower.contains("how about you") ||
                lower.contains("what about you") ||
                lower.contains("and you") ||
                lower.contains("how are you") ||
                lower.contains("how r u") ->
                "I'm doing great, thanks so much for asking!"

            lower.contains("who are you") || lower.contains("what is your name") || lower.contains("what's your name") ->
                "I'm Nova, your English speaking teacher!"

            lower.contains("where are you from") || lower.contains("where do you live") ->
                "I'm right here online to help you practise English anytime."

            lower.contains("do you like ") -> {
                val likedThing = cleanSpoken.substringAfter("like ", "")
                    .replace("?", "")
                    .trim()
                    .take(30)
                if (likedThing.isNotBlank()) {
                    "Yes, I think $likedThing is great!"
                } else {
                    "Yes, I think that sounds great!"
                }
            }

            else -> null
        }
    }

    private fun extractSpokenPersonName(text: String): String? {
        val patterns = listOf(
            Regex("""\bmy name is\s+([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE),
            Regex("""\bmy name\s+(?!is\b|was\b)([A-Za-z]{2,20})\b""", RegexOption.IGNORE_CASE),
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
            Regex("""\b(?:live in|live|stay in|located in|am from|i'm from|come from|hometown is)\s+([A-Za-z][A-Za-z ]{1,24}?)(?=[.,!?]|\band\b|\bbut\b|\bwith\b|\bwhere\b|$)""", RegexOption.IGNORE_CASE),
            Regex("""\b(?:visit|travel to|go to)\s+([A-Z][a-z]{2,20}(?:\s+[A-Z][a-z]{2,20})?)""")
        )
        for (p in placePatterns) {
            val raw = p.find(text)?.groupValues?.getOrNull(1)?.trim() ?: continue
            val firstWordLower = raw.lowercase().split(" ").firstOrNull().orEmpty()
            if (raw.length >= 3 && firstWordLower !in setOf("a", "an", "the", "my", "in", "at", "school", "home", "bed", "work", "class")) {
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
                        "How often do you play $sport?",
                        "Who is your favourite $sport player?",
                        "Where do you usually play $sport with your friends?"
                    ),
                    hint = "Talk about how often you play $sport or who you play with."
                )
            }
            hasAnyWord(lower, listOf("reading", "read", "book", "books", "novel", "novels", "stories")) -> ContextualMatch(
                display = "reading books",
                reaction = "Reading books is a wonderful habit!",
                questions = listOf(
                    "What kind of books do you enjoy reading the most?",
                    "What is the best book you have read recently?",
                    "Do you prefer reading in the evening or before bed?"
                ),
                hint = "Share a book type or story you liked."
            )
            hasAnyWord(lower, listOf("music", "song", "songs", "sing", "singing", "guitar", "piano", "listening")) -> ContextualMatch(
                display = "music",
                reaction = "Music is a great way to relax!",
                questions = listOf(
                    "What kind of songs do you like listening to?",
                    "Do you play any musical instrument?",
                    "When do you listen to music the most?"
                ),
                hint = "Name a music style or singer you like."
            )
            hasAnyWord(lower, listOf("movie", "movies", "film", "films", "cinema", "anime", "series", "show")) -> ContextualMatch(
                display = "movies",
                reaction = "Watching a good movie is always enjoyable!",
                questions = listOf(
                    "What kind of movies do you like the most?",
                    "Which movie did you enjoy watching recently?"
                ),
                hint = "Mention a movie type like comedy, action, or family."
            )
            hasAnyWord(lower, listOf("drawing", "painting", "sketch", "sketching", "art", "craft")) -> ContextualMatch(
                display = "drawing",
                reaction = "Drawing is such a creative hobby!",
                questions = listOf(
                    "What do you enjoy drawing the most?",
                    "How did you get interested in drawing?"
                ),
                hint = "Describe what you enjoy drawing."
            )
            hasAnyWord(lower, listOf("cooking", "cook", "baking", "bake")) -> ContextualMatch(
                display = "cooking",
                reaction = "Cooking is a very useful skill!",
                questions = listOf(
                    "What dish do you enjoy cooking the most?",
                    "Who taught you how to cook?"
                ),
                hint = "Name one dish you can cook."
            )
            hasAnyWord(lower, listOf("game", "games", "gaming", "pubg", "free fire", "minecraft")) -> ContextualMatch(
                display = "games",
                reaction = "Playing games can be fun in your free time!",
                questions = listOf(
                    "Which game do you enjoy playing the most?",
                    "Do you usually play with your friends?"
                ),
                hint = "Tell which game you like and why."
            )
            hasAnyWord(lower, listOf("cycling", "swimming", "running", "gym", "workout", "yoga", "walking")) -> {
                val act = listOf("cycling", "swimming", "running", "gym", "workout", "yoga", "walking")
                    .first { Regex("""\b$it\b""").containsMatchIn(lower) }
                ContextualMatch(
                    display = act,
                    reaction = "Staying active with $act is great for health!",
                    questions = listOf(
                        "Do you usually do $act in the morning or evening?",
                        "How does $act make you feel afterward?"
                    ),
                    hint = "Mention what time of day you do it."
                )
            }
            else -> null
        }
    }

    private fun findFoodMatch(lower: String): String? {
        val foods = listOf(
            "biryani", "fish curry", "rice", "curry", "chicken", "mutton", "pitha", "momo",
            "noodles", "pizza", "burger", "pasta", "sandwich", "tea", "chai", "coffee",
            "chocolate", "ice cream", "mango", "paneer", "samosa", "dosa", "dal", "roti", "breakfast"
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
        val subjects = listOf(
            "english", "math", "mathematics", "science", "physics",
            "chemistry", "biology", "history", "geography", "computer science", "computer"
        )
        val matchedSub = subjects.firstOrNull { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(lower) }
        if (matchedSub != null) {
            val pretty = matchedSub.replaceFirstChar { it.uppercaseChar() }
            return ContextualMatch(
                display = pretty,
                reaction = "Oh nice, $pretty is a great subject!",
                questions = listOf(
                    "Why do you like $pretty?",
                    "What topic in $pretty did you enjoy learning recently?"
                ),
                hint = "Share why you enjoy studying $pretty."
            )
        }
        return when {
            hasAnyWord(lower, listOf("school", "college", "university", "class", "studying", "study", "student", "exam", "homework")) -> ContextualMatch(
                display = "school",
                reaction = "That's great!",
                questions = listOf(
                    "What is your favourite subject at school?",
                    "What do you enjoy most about your school?"
                ),
                hint = "Mention a favourite subject or activity."
            )
            hasAnyWord(lower, listOf("work", "job", "office", "career", "company", "business", "engineer", "teacher", "doctor")) -> ContextualMatch(
                display = "career",
                reaction = "That sounds like meaningful work!",
                questions = listOf(
                    "What inspired you to choose that path?",
                    "What skills are most useful for that work?"
                ),
                hint = "Describe one reason or skill for that career."
            )
            else -> null
        }
    }

    private fun findPersonMention(lower: String): String? {
        val people = listOf(
            "family", "parents", "mother", "mom", "father", "dad",
            "brother", "sister", "friends", "friend", "cousin", "grandparents", "classmates", "teacher"
        )
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

    private fun buildHintForQuestion(question: String, topic: SpeakingPrompt): String {
        val qLower = question.lowercase()
        return when {
            qLower.contains("where do you live") || qLower.contains("where are you") ->
                "Say your city or town in a full sentence, like 'I live in...'."
            qLower.contains("name") ->
                "Introduce yourself clearly: 'My name is...'."
            qLower.contains("how old") ->
                "Reply using a full sentence: 'I am ... years old.'"
            qLower.contains("which class") ->
                "Reply using 'I am in Class...' or 'I study in Class...'."
            qLower.contains("subject") ->
                "Name one subject and say why using 'because'."
            qLower.contains("family") || qLower.contains("parents") ->
                "Talk about how many people are in your family or what you do together."
            qLower.contains("time") || qLower.contains("morning") || qLower.contains("evening") || qLower.contains("routine") ->
                "Use clear time words like 'at 7 AM', 'in the morning', or 'on Sundays'."
            qLower.contains("free time") || qLower.contains("hobby") ->
                "Share one hobby you enjoy and why."
            else ->
                topic.followUpTip.ifBlank { "Answer naturally in 1 or 2 simple, complete sentences." }
        }
    }
}
