package com.example.service

import com.example.data.model.NovaChatMessage
import com.example.util.AssameseTranslator

/**
 * Engaging, warm Indian female English mentor ("Nova") brain for on-device chat
 * and dynamic quick-reply suggestions across both on-device and Cloud Gemini modes.
 */
object NovaAssistantBrain {

    fun generateSmartReply(
        prompt: String,
        mode: String,
        studentName: String,
        chatHistory: List<NovaChatMessage>,
        persona: NovaVoicePersona = NovaVoicePersona.ANANYA
    ): String {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()
        val firstName = studentName.trim().substringBefore(" ")
            .takeIf { it.isNotBlank() && !it.equals("Student", true) && !it.equals("Friend", true) }
            ?: "friend"
        val voiceName = persona.displayName

        // 1. Translator mode or explicit translation request
        if (mode == "Translator" || lower.contains("translate")) {
            return buildTranslationReply(trimmed)
        }

        // 2. Grammar Fix mode or explicit check/fix request
        if (mode == "Grammar Fix" ||
            lower.startsWith("check ") ||
            lower.startsWith("fix ") ||
            lower.startsWith("correct ") ||
            lower.contains("check my sentence") ||
            lower.contains("is this correct")
        ) {
            return buildGrammarFixReply(trimmed)
        }

        // 3. Roleplay mode or explicit roleplay request
        if (mode == "Roleplay" ||
            lower.contains("roleplay") ||
            lower.contains("role play") ||
            lower.contains("act as") ||
            lower.contains("pretend") ||
            isOngoingRoleplay(chatHistory)
        ) {
            return buildRoleplayReply(trimmed, lower, firstName, chatHistory)
        }

        // 4. Casual greetings, "how are you", "who are you", boredom, jokes, motivation
        if (matchesGreeting(lower)) {
            val greetings = listOf(
                "Hey $firstName! So nice to chat with you! I'm $voiceName, your Nova speaking buddy. How's your day going so far — chill or super busy?",
                "Hi $firstName! Welcome back! Ready to make English feel effortless and fun today? Tell me one good thing that happened today, or tap a chip below for a quick roleplay!",
                "Hey there $firstName! Glad you popped in! Just finished a cup of virtual masala chai and I'm ready to practice with you. What's on your mind today?"
            )
            return greetings[(trimmed.length + chatHistory.size) % greetings.size]
        }

        if (lower.contains("how are you") || lower.contains("how r u") || lower.contains("what's up") || lower.contains("whats up")) {
            return "I'm doing super well, $firstName, thanks for asking! Honestly, chatting with you is the best part of my day. How are you feeling today — energetic, sleepy, or ready for a fun challenge?"
        }

        if (lower.contains("who are you") || lower.contains("your name")) {
            return "I'm $voiceName (Nova AI) — your Indian English speaking buddy and tutor! Think of me like a friendly senior who helps you speak naturally, fix tricky grammar without judgment, and nail interviews or everyday chats. What should we try first?"
        }

        if (lower.contains("bored") || lower.contains("boring") || lower.contains("fun") || lower.contains("game")) {
            return "Let's fix that boredom right now, $firstName! Pick one mini-challenge and reply:\n\n" +
                "1. 🎭 Café Roleplay: Pretend you're ordering a cold coffee & sandwich.\n" +
                "2. 🗣️ Tongue Twister: Say \"She sells seashells by the seashore\" fast!\n" +
                "3. ✨ Upgrade Challenge: Tell me one boring sentence like \"I am very tired\", and I'll make it sound like a pro!\n\nWhich one sounds fun?"
        }

        if (lower.contains("joke") || lower.contains("laugh") || lower.contains("funny")) {
            val jokes = listOf(
                "Here's a quick English wordplay joke for you, $firstName!\n\nWhy was the English book always so sad?\nBecause it had too many tense situations! 😄\n\nSpeaking of tenses — want to try making a funny sentence in Past Tense?",
                "Okay, catch this one!\n\nTeacher: \"Use 'I' in a sentence.\"\nStudent: \"I is...\"\nTeacher: \"No! Always say 'I am'!\"\nStudent: \"Fine... I am the ninth letter of the alphabet!\" 😄\n\nWant another one or a fun tongue twister?"
            )
            return jokes[chatHistory.size % jokes.size]
        }

        if (lower.contains("motivat") || lower.contains("nervous") || lower.contains("scared") || lower.contains("afraid") || lower.contains("shy") || lower.contains("hesitat")) {
            return "Hey $firstName, listen to me — every fluent speaker you admire once stumbled over simple words too. Even native speakers make grammar slips! The secret isn't perfection; it's speaking one real sentence every single day without fear.\n\nYou're already doing great by showing up here. Tell me one thing you love doing, in just one relaxed sentence!"
        }

        if (lower.contains("thank") || lower.contains("thanks") || lower.contains("awesome") || lower.contains("nice") || lower.contains("cool")) {
            return "Anytime, $firstName! You're making awesome progress. Want to keep going with a quick 2-line roleplay, a pronunciation drill, or an English upgrade?"
        }

        // 5. "Upgrade my boring English" / natural phrasing
        if (lower.contains("upgrade") || lower.contains("natural") || lower.contains("better way") || lower.contains("instead of")) {
            return buildUpgradeEnglishReply(trimmed, lower)
        }

        // 6. Tongue twister / pronunciation
        if (lower.contains("tongue twister") || lower.contains("pronunciation") || lower.contains("accent") || lower.contains("fluency")) {
            return buildPronunciationWorkout(chatHistory.size)
        }

        // 7. Interview & self-introduction
        if (lower.contains("interview") || lower.contains("introduce myself") || lower.contains("self intro") || lower.contains("introduction")) {
            return buildInterviewCoachReply(lower, firstName)
        }

        // 8. Tenses / Grammar topics
        if (lower.contains("tense")) {
            return "Let's make tenses super simple, $firstName! Forget heavy textbook rules — just remember these 4 everyday patterns:\n\n" +
                "1. Present Simple (habits): \"I drink chai every morning.\"\n" +
                "2. Present Continuous (right now): \"I am chatting with Nova right now.\"\n" +
                "3. Past Simple (finished): \"I watched a great movie yesterday.\"\n" +
                "4. Present Perfect (experience/result): \"I have improved my English a lot this month!\"\n\n" +
                "Your turn: write 1 real sentence about your day today, and I'll check & polish it!"
        }

        // 9. Idioms / Vocabulary / Daily phrases
        if (lower.contains("idiom") || lower.contains("phrase") || lower.contains("vocabulary") || lower.contains("words") || lower.contains("slang")) {
            return "Here are 4 natural phrases real speakers use every day (way more fun than textbook English!):\n\n" +
                "• \"No worries at all!\" → Friendly way to say \"It's okay.\"\n" +
                "• \"I'm swamped today\" → \"I'm super busy today.\"\n" +
                "• \"To be honest...\" → Great way to start sharing your opinion.\n" +
                "• \"Let's catch up soon!\" → \"Let's talk/meet again soon.\"\n\n" +
                "Try using one of these in a sentence right now — I'll cheer you on or polish it!"
        }

        // 10. Writing / Email / Speech / Essay help
        if (mode == "Writing Coach" || lower.contains("email") || lower.contains("letter") || lower.contains("essay") || lower.contains("speech") || lower.contains("write ")) {
            return buildWritingCoachReply(trimmed, lower, firstName)
        }

        // 11. Speaking Tutor / general sentence conversation -> check grammar warmly + keep the chat alive!
        val grammarCheck = HumanConversationEngine.analyzeGrammarDeterministic(trimmed, isVoiceInput = false)
        if (grammarCheck.hasError && !grammarCheck.correctedSentence.isNullOrBlank()) {
            return HumanConversationEngine.formatMandatoryMistakeResponse(
                originalSentence = trimmed,
                correctedSentence = grammarCheck.correctedSentence,
                explanation = grammarCheck.explanation ?: grammarCheck.shortTip ?: "Try this cleaner structure.",
                naturalEnglish = grammarCheck.naturalExpression,
                continuationText = "Nice thought, $firstName! Let's keep chatting — can you tell me a little more about that in your next sentence?"
            )
        }

        // Intelligent conversational follow-up based on what the student actually talked about
        return buildContextualConversationalReply(trimmed, lower, firstName, mode)
    }

    fun getQuickReplies(
        mode: String,
        lastNovaText: String?,
        hasUserMessages: Boolean
    ): List<Pair<String, String>> {
        val lastLower = lastNovaText.orEmpty().lowercase()
        return when {
            lastLower.contains("café") || lastLower.contains("coffee") || lastLower.contains("order") -> listOf(
                "☕ Order iced coffee" to "Hi! Can I please get one iced latte and a veg sandwich?",
                "💳 Ask for the bill" to "Excuse me, could I get the bill please?",
                "🎭 Switch to Interview" to "Let's roleplay a job interview next!"
            )
            lastLower.contains("interview") || lastLower.contains("tell me about yourself") -> listOf(
                "👋 Practice my intro" to "Hi, I'm a curious student who loves learning new skills and working with people.",
                "💪 Strengths answer" to "My biggest strength is that I stay calm and learn quickly.",
                "✨ Upgrade my answer" to "Upgrade my sentence: I am good in English and I do hard work."
            )
            lastLower.contains("tongue twister") -> listOf(
                "🗣️ Another twister" to "Give me another fun tongue twister for V and W!",
                "✨ Upgrade English" to "Show me how to upgrade boring English phrases",
                "🎭 Let's roleplay" to "Let's do a café order roleplay!"
            )
            mode == "Roleplay" -> listOf(
                "☕ Café Order" to "Let's roleplay ordering at a café!",
                "💼 Job Interview" to "Let's roleplay a job interview!",
                "✈️ Airport Check-in" to "Let's roleplay an airport check-in!"
            )
            mode == "Grammar Fix" -> listOf(
                "✍️ Test a sentence" to "Fix this: Yesterday I go to market and buy mangoes.",
                "✨ Make it natural" to "How can I say 'I am very tired' in a more natural way?",
                "📖 Quick tense check" to "Check this: She don't likes coffee."
            )
            mode == "Translator" -> listOf(
                "🔤 Translate greeting" to "Translate: Good morning! Have you had your breakfast?",
                "🔤 Polite request" to "Translate: Could you please help me with this question?",
                "🔤 Daily chat" to "Translate: Let's meet in the evening for tea."
            )
            mode == "Writing Coach" -> listOf(
                "📧 Leave email" to "Help me write a polite email to my teacher for 1 day leave.",
                "🎤 1-min speech" to "Help me write a short 1-minute speech on confidence.",
                "✨ Polish my line" to "Make this sound professional: I want to ask you for help."
            )
            !hasUserMessages -> listOf(
                "🎭 Café Roleplay" to "Let's do a fun café roleplay!",
                "✨ Upgrade my English" to "Teach me 5 natural phrases to replace boring English!",
                "🗣️ Tongue Twister" to "Give me a fun Indian-friendly tongue twister!",
                "💼 Interview Prep" to "Help me practice introducing myself in an interview!"
            )
            else -> listOf(
                "🎭 Try Roleplay" to "Let's do a quick real-life roleplay!",
                "✨ Daily Phrases" to "Teach me 4 natural everyday idioms or phrases!",
                "🗣️ Pronunciation Drill" to "Give me a fun tongue twister!",
                "😄 Tell me a joke" to "Tell me a funny English joke!"
            )
        }
    }

    private fun matchesGreeting(lower: String): Boolean {
        val clean = lower.replace(Regex("[^a-z\\s]"), "").trim()
        return clean in setOf(
            "hi", "hii", "hiii", "hello", "hey", "heyy", "namaste",
            "good morning", "good afternoon", "good evening", "hi nova", "hello nova", "hey nova"
        )
    }

    private fun isOngoingRoleplay(history: List<NovaChatMessage>): Boolean {
        val recentNova = history.lastOrNull { it.isNova }?.text?.lowercase().orEmpty()
        return recentNova.contains("roleplay") ||
            recentNova.contains("barista") ||
            recentNova.contains("interviewer") ||
            recentNova.contains("your turn:")
    }

    private fun buildRoleplayReply(
        trimmed: String,
        lower: String,
        firstName: String,
        history: List<NovaChatMessage>
    ): String {
        val recentNova = history.lastOrNull { it.isNova }?.text?.lowercase().orEmpty()

        return when {
            lower.contains("interview") ->
                "🎭 Roleplay Started: Job / College Interview!\n\n" +
                    "I'll be your friendly interviewer today. Ready? Here's question #1:\n\n" +
                    "\"Welcome, $firstName! Thanks for coming in today. To start off, tell me a little about yourself and what excites you most right now.\"\n\n" +
                    "💡 Quick Tip: Keep it crisp — name + what you're studying/doing + 1 passion!"

            lower.contains("airport") || lower.contains("flight") || lower.contains("travel") ->
                "🎭 Roleplay Started: Airport Check-In!\n\n" +
                    "I'm at the airline counter smiling at you:\n\n" +
                    "\"Good morning! Welcome to SkyIndia Airlines. May I see your passport and ticket please? Would you prefer a window or an aisle seat today?\"\n\n" +
                    "Your turn! Answer naturally in 1–2 sentences."

            lower.contains("café") || lower.contains("cafe") || lower.contains("coffee") || lower.contains("restaurant") || lower.contains("order") ->
                "🎭 Roleplay Started: Cozy Café Order!\n\n" +
                    "I'm your barista behind the counter:\n\n" +
                    "\"Hey there! Welcome to Chai & Brew! What can I get started for you today — something hot, iced, or a quick snack?\"\n\n" +
                    "💡 Try polite phrasing like: \"Hi! Could I please have...\""

            recentNova.contains("barista") || lower.contains("latte") || lower.contains("sandwich") || lower.contains("chai") || lower.contains("bill") ->
                "☕ \"Awesome choice, coming right up! Would you like that dine-in or takeaway? And anything sweet to go with it?\"\n\n" +
                    "✨ Coach note: Great polite tone! You can also say \"To go, please\" or \"I'll have it here.\""

            recentNova.contains("interviewer") ->
                "💼 \"That's a really solid answer, $firstName! Follow-up question: What is one challenge you've faced recently, and how did you handle it?\"\n\n" +
                    "✨ Coach note: Remember to smile in your voice and use a real short example!"

            recentNova.contains("airline") || lower.contains("window") || lower.contains("aisle") ->
                "✈️ \"Got it, one window seat coming right up! Here is your boarding pass — your gate is B12 and boarding starts in 35 minutes. Have a wonderful flight!\"\n\n" +
                    "Want to try another roleplay (Café, Interview, or Doctor Visit)?"

            else ->
                "🎭 Pick a fun real-life scene and jump right in, $firstName!\n\n" +
                    "• Say \"Café\" — order food & coffee like a pro\n" +
                    "• Say \"Interview\" — practice answering an interviewer\n" +
                    "• Say \"Airport\" — handle check-in & boarding smoothly\n\n" +
                    "Which scene should we act out?"
        }
    }

    private fun buildUpgradeEnglishReply(trimmed: String, lower: String): String {
        return when {
            lower.contains("very tired") || lower.contains("tired") ->
                "Let's upgrade \"I am very tired\" so you sound super natural:\n\n" +
                    "• Everyday friendly: \"I'm completely exhausted today.\"\n" +
                    "• Casual & modern: \"I'm totally drained!\"\n" +
                    "• Polite at work/school: \"It's been a long day — I could really use some rest.\"\n\n" +
                    "Send me another everyday sentence and I'll upgrade it for you!"

            lower.contains("good in english") || lower.contains("hard work") ->
                "Let's polish that sentence right up!\n\n" +
                    "Instead of: \"I am good in English and I do hard work.\"\n" +
                    "✅ Say: \"I am good at English and I'm a hard worker.\"\n" +
                    "🌟 Even smoother: \"I'm confident communicating in English, and I'm dedicated to my work.\"\n\n" +
                    "Notice we always say \"good AT\" a subject or skill, never \"good in\"!"

            else ->
                "Here are 5 instant upgrades that turn textbook English into real human English:\n\n" +
                    "1. Instead of \"I am fine, thank you\" → \"I'm doing great, thanks! How about you?\"\n" +
                    "2. Instead of \"Give me water\" → \"Could I have some water, please?\"\n" +
                    "3. Instead of \"I didn't understand\" → \"Sorry, could you say that one more time?\"\n" +
                    "4. Instead of \"Very good\" → \"That's awesome!\" / \"Really impressive!\"\n" +
                    "5. Instead of \"Tell me\" → \"I'd love to hear your thoughts.\"\n\n" +
                    "Paste any sentence you use often and I'll give it a natural makeover!"
        }
    }

    private fun buildPronunciationWorkout(seed: Int): String {
        val workouts = listOf(
            "🗣️ Indian-Friendly Pronunciation Workout: V vs W!\n\n" +
                "• For 'V' (very, voice, visit): gently touch your top teeth to your bottom lip.\n" +
                "• For 'W' (water, warm, weekend): round your lips like whistling, no teeth!\n\n" +
                "Try saying this 3 times fast:\n\"Vikram watched warm waves from his village veranda.\"\n\nTap the mic and try saying it!",
            "🗣️ Crisp Clarity Workout: S vs SH!\n\n" +
                "• 'S' (see, sun, smile): smile slightly as air passes out.\n" +
                "• 'SH' (she, shine, share): push your lips forward gently.\n\n" +
                "Say this tongue twister smoothly:\n\"Sheena shared sweet shiny seashells on Sunday.\"\n\nWant another drill?",
            "🗣️ Rhythm & Confidence Workout:\n\n" +
                "Try stressing the key words in a sentence instead of rushing every word equally:\n" +
                "\"I REALLY enjoyed chatting with my FRIENDS this WEEKEND.\"\n\n" +
                "Try tapping the mic and speaking one sentence with clear pauses!"
        )
        return workouts[seed % workouts.size]
    }

    private fun buildInterviewCoachReply(lower: String, firstName: String): String {
        return if (lower.contains("strength")) {
            "Great interview answer template for \"What is your strength?\":\n\n" +
                "🌟 \"One of my biggest strengths is staying calm under pressure and learning quickly. For example, when my team had a tight deadline recently, I organized our tasks and we finished on time.\"\n\n" +
                "Formula: Strength + Short 1-line example! Try writing yours!"
        } else {
            "Let's nail your 30-second Self-Introduction, $firstName! Use this 3-step formula:\n\n" +
                "1. Present: \"Hi, I'm $firstName. I'm currently focusing on...\"\n" +
                "2. Passion/Strength: \"I really enjoy... and I'm proud of...\"\n" +
                "3. Future Goal: \"Right now, I'm excited to grow my skills in...\"\n\n" +
                "Try typing or speaking your intro in 2–3 lines, and I'll polish it so it sounds super confident!"
        }
    }

    private fun buildWritingCoachReply(trimmed: String, lower: String, firstName: String): String {
        return when {
            lower.contains("leave") || lower.contains("email") ->
                "Here is a clean, polite leave email you can customize right away:\n\n" +
                    "Subject: Leave Request for [Date]\n\n" +
                    "Dear Sir/Ma'am,\n" +
                    "I hope you are doing well. I am writing to kindly request leave on [Date] due to [a personal/family commitment]. I will make sure to catch up on all missed notes and assignments promptly.\n\n" +
                    "Thank you for your understanding.\nWarm regards,\n$firstName"

            lower.contains("speech") || lower.contains("confidence") ->
                "Here is a punchy 1-minute opening for a speech on Confidence:\n\n" +
                    "\"Good morning everyone! Have you ever held back an idea just because you were afraid of making a mistake? True confidence isn't about never feeling nervous — it's about speaking up even when your voice shakes a little. Every small step we take builds unstoppable courage. Thank you!\"\n\n" +
                    "Want me to adjust the topic or make it longer?"

            else -> {
                val grammarCheck = HumanConversationEngine.analyzeGrammarDeterministic(trimmed, isVoiceInput = false)
                if (grammarCheck.hasError && !grammarCheck.correctedSentence.isNullOrBlank()) {
                    HumanConversationEngine.formatMandatoryMistakeResponse(
                        originalSentence = trimmed,
                        correctedSentence = grammarCheck.correctedSentence,
                        explanation = grammarCheck.explanation ?: "Polished grammar for clear writing.",
                        naturalEnglish = grammarCheck.naturalExpression,
                        continuationText = "Send the next line of your draft whenever you're ready!"
                    )
                } else {
                    "Your writing looks clear, $firstName! Tell me who you're writing to (a teacher, friend, or manager) and I'll help you make the tone sound natural and impressive."
                }
            }
        }
    }

    private fun buildTranslationReply(prompt: String): String {
        val targetSentence = prompt
            .replace(Regex("(?i)translate( this)?( into| to)?( assamese| hindi| bengali)?[:\\s-]*"), "")
            .trim()
            .ifBlank { prompt }
        val translated = AssameseTranslator.translate(targetSentence)
        return buildString {
            append("Here's a quick translation helper:\n\n")
            append("English: \"$targetSentence\"\n")
            if (translated.assameseText.isNotBlank()) {
                append("Assamese: ${translated.assameseText}\n")
            }
            append("\nTip: Speak the English version aloud twice to lock in the rhythm!")
        }
    }

    private fun buildGrammarFixReply(prompt: String): String {
        val targetSentence = prompt
            .replace(Regex("(?i)^(check|fix|correct)( this| my)?( sentence| grammar)?[:\\s-]*"), "")
            .trim()
            .ifBlank { prompt }
        val grammarCheck = HumanConversationEngine.analyzeGrammarDeterministic(targetSentence, isVoiceInput = false)
        return if (grammarCheck.hasError && !grammarCheck.correctedSentence.isNullOrBlank()) {
            HumanConversationEngine.formatMandatoryMistakeResponse(
                originalSentence = targetSentence,
                correctedSentence = grammarCheck.correctedSentence,
                explanation = grammarCheck.explanation ?: grammarCheck.shortTip
                    ?: "Check the sentence grammar and verb form.",
                naturalEnglish = grammarCheck.naturalExpression,
                continuationText = "Got another sentence you'd like me to polish?"
            )
        } else {
            "Sounds clean and grammatically solid! \"$targetSentence\" works great in everyday English.\n\nWant to make it sound even more expressive, or test another sentence?"
        }
    }

    private fun buildContextualConversationalReply(
        trimmed: String,
        lower: String,
        firstName: String,
        mode: String
    ): String {
        return when {
            lower.contains("cricket") || lower.contains("football") || lower.contains("sport") ->
                "Ooh, sports is such a fun topic, $firstName! Your English flowed nicely there. Who's your favourite player or team right now, and what makes them exciting to watch?"
            lower.contains("chai") || lower.contains("tea") || lower.contains("coffee") || lower.contains("food") || lower.contains("biryani") ->
                "Now you're talking my language — food always makes a great conversation! Tell me: are you a chai person or a coffee person, and what's your ultimate comfort snack?"
            lower.contains("movie") || lower.contains("music") || lower.contains("song") ->
                "Love that! Talking about movies and music is honestly one of the fastest ways to boost natural English. What's a song or movie you could rewatch anytime?"
            lower.contains("?") ->
                "Great question, $firstName! Here's a handy way to think about it in English: try breaking your idea into one short statement + one reason using 'because'. Want to test saying your answer in 1–2 sentences?"
            mode == "Speaking Tutor" ->
                "Nice sentence, $firstName! That sounded natural and clear. To keep the flow going — what happened next, or why do you enjoy that?"
            else ->
                "I love that, $firstName! Your sentence is clear and natural. Let's keep the momentum going — tell me one more detail about it, or tap a fun challenge chip below!"
        }
    }
}
