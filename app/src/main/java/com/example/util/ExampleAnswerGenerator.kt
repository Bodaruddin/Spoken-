package com.example.util

import com.example.data.model.SpeakingPrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Generates contextual, question-specific and topic-specific native model answers
 * for Nova AI Chat's Example Model Answers dialog and practice prompts.
 */
object ExampleAnswerGenerator {

    private val legacyGenericPlaceholders = setOf(
        "I usually start my day at 7:30 in the morning on weekdays.",
        "Speaking about this, my schedule starts early and stays busy.",
        "In my personal experience, it makes a big difference in my routine.",
        "One memorable story I recall was when I tried this with my classmates.",
        "I usually enjoy spending time with family.",
        "Another activity I like is reading.",
        "I usually do this in the afternoon.",
        "On weekends, I prefer relaxing at home."
    )

    private val greetingExamples = setOf(
        "Hello! I am doing great today, thank you for asking. How about you?",
        "Hi Nova! I'm doing really well and excited to practice speaking English today.",
        "Hello! I'm doing great today. My name is Alex, and I'm excited to practice English with you.",
        "Hi! Nice to meet you. I enjoy learning new things, reading books, and chatting with friends."
    )

    private val aiExamplesCache = ConcurrentHashMap<String, List<String>>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun isLegacyGenericExampleSet(examples: List<String>): Boolean {
        if (examples.isEmpty()) return true
        return examples.all { it in legacyGenericPlaceholders }
    }

    fun isGreetingExampleSet(examples: List<String>): Boolean {
        if (examples.isEmpty()) return false
        return examples.all { it in greetingExamples }
    }

    /**
     * Returns curated, exact question-matched examples when available, or null for open-ended/dynamic questions.
     * Matches strictly on the active question text so follow-up turns never get hijacked by topic titles.
     */
    fun matchCuratedExamples(question: String, topic: SpeakingPrompt? = null): List<String>? {
        val clean = AssameseTranslator.extractCleanQuestion(question)
        val lower = clean.lowercase().trim()

        return when {
            lower.contains("how are you") || lower == "hello" || lower == "hi" ||
                    lower.startsWith("hello!") || lower.startsWith("hi!") -> listOf(
                "Hello! I am doing great today, thank you for asking. How about you?",
                "Hi Nova! I'm doing really well and excited to practice speaking English today."
            )
            lower.contains("what is your name") || lower.contains("tell me about yourself") || lower.contains("introduce yourself") -> listOf(
                "Hello! I am a student who enjoys learning new skills and practicing spoken English every day.",
                "Hi! Nice to meet you. In my free time, I love reading books, listening to music, and talking with friends."
            )
            lower.contains("go to school") -> listOf(
                "I usually go to school at 8:00 AM on weekdays.",
                "I leave home for school around 8:15 in the morning with my friends."
            )
            lower.contains("wake up") -> listOf(
                "I usually wake up at 6:30 in the morning every weekday.",
                "On weekdays, I wake up early at 6:00 AM, while on weekends I sleep until 8:00 AM."
            )
            lower.contains("sundays") -> listOf(
                "On Sundays, I enjoy playing sports with my friends in the evening.",
                "On Sundays, I spend quality time reading books and helping my family at home."
            )
            lower.contains("how was your weekend") || lower.contains("anything fun or exciting") -> listOf(
                "My weekend was wonderful! I visited my relatives and played games with my friends.",
                "I had a relaxing weekend at home reading a good book and watching a movie with my family."
            )
            lower.contains("how was your day") -> listOf(
                "My day was very productive! I attended my classes and practiced English speaking in the afternoon.",
                "Today went well—I finished my homework early and spent the evening outdoors."
            )
            lower.contains("birthday") && lower.contains("month") -> listOf(
                "My birthday is in October. On that day, I celebrate with delicious food and my family.",
                "It is in March, and we always share sweets and warm wishes with our neighbors and friends."
            )
            lower.contains("midnight") && lower.contains("new year") -> listOf(
                "At midnight on New Year's Eve, I will be at home watching the countdown with my family.",
                "I'll be celebrating with my friends and wishing everyone a Happy New Year at midnight."
            )
            lower.contains("hobbies") || lower.contains("free time") -> listOf(
                "One of my favorite hobbies is reading and sketching because it feels relaxing and sparks creativity.",
                "In my free time, I enjoy playing outdoor sports on weekends to stay active and refreshed."
            )
            lower.contains("job or profession") || lower.contains("work or studies") -> listOf(
                "I am focusing on my studies right now, and what I like most is learning practical skills with classmates.",
                "My goal is to build strong communication skills so I can collaborate effectively in my future career."
            )
            lower.contains("travel anywhere") -> listOf(
                "If I could travel anywhere tomorrow, I would visit Japan to explore its rich culture and local cuisine.",
                "I would love to visit the mountains in Switzerland because the breathtaking scenery looks peaceful."
            )
            lower.contains("favorite meal") || lower.contains("traditionally prepared") -> listOf(
                "My all-time favorite meal is homemade rice with traditional fish curry, cooked slowly with aromatic spices.",
                "In my home, we love preparing warm, crispy snacks together using fresh local ingredients."
            )
            lower.contains("artificial intelligence") || lower.contains("smartphones") -> listOf(
                "In the next five years, Artificial Intelligence will make personalized education faster and much more convenient.",
                "Modern smartphones already help us practice languages anywhere, and smart automation will save time daily."
            )
            lower.contains("memorable movie") || lower.contains("book that left") -> listOf(
                "One memorable movie I watched had an inspiring main character who never gave up despite tough obstacles.",
                "My favorite book taught me that honesty, patience, and kindness leave a lasting impact on people."
            )
            lower.contains("ideal morning") || lower.contains("evening routine") -> listOf(
                "First thing in the morning, I exercise and plan my tasks so I stay disciplined throughout the day.",
                "Towards the evening, I review what I learned and prepare my schedule to stay organized and motivated."
            )
            lower.contains("holiday or festival") || lower.contains("celebrated in your region") -> listOf(
                "During our biggest festival, families gather in traditional clothes with joyful music and homemade sweets.",
                "It is a vibrant celebration where neighbors visit each other, share festive meals, and enjoy cultural dances."
            )
            lower.contains("protect the environment") || lower.contains("cities clean") -> listOf(
                "People can protect the environment by reducing plastic waste, recycling regularly, and planting more trees.",
                "Simple daily habits like saving electricity and walking short distances help keep our cities clean and green."
            )
            lower.contains("difficult challenge") || lower.contains("overcome it") -> listOf(
                "Speaking fluently in public used to feel difficult, but practicing a little every day built my confidence.",
                "When I faced a tough exam, staying resilient and organizing a steady study routine helped me succeed."
            )
            lower.contains("why do you want to learn english") -> listOf(
                "I want to learn English because it helps me communicate confidently with people from anywhere.",
                "Speaking English fluently will open better academic and career opportunities for my future."
            )
            lower.contains("plans for tomorrow") -> listOf(
                "Tomorrow morning, I plan to finish my studies early and practice speaking English for twenty minutes.",
                "I am going to meet my friends in the afternoon and help my family in the evening."
            )
            lower.contains("where do you live") -> listOf(
                "I live in Assam, a peaceful region known for its lush greenery, rivers, and warm hospitality.",
                "I live in a friendly neighborhood with my family, close to my school and local market."
            )
            lower.contains("coffee or tea") -> listOf(
                "I definitely prefer warm Assam tea in the morning because it refreshes me for the whole day.",
                "I enjoy both, though I usually drink tea at home and coffee when studying late."
            )
            lower.contains("languages can you speak") -> listOf(
                "I can speak Assamese and Hindi fluently, and I am actively practicing English every day.",
                "I speak my mother tongue at home and use English at school to improve my fluency."
            )
            lower.contains("historical") && (lower.contains("person") || lower.contains("figure") || lower.contains("leader")) -> listOf(
                "One of my favorite historical figures to read about is Dr. A. P. J. Abdul Kalam because his dedication to science and education inspires me.",
                "I really enjoy reading about Mahatma Gandhi and how patience, truth, and courage helped shape modern history."
            )
            lower.contains("connect with your weekend or personal goals") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this topic"
                listOf(
                    "Connecting $label to my personal goals keeps me motivated to practice consistently on weekends.",
                    "On weekends, I reflect on how $label supports my long-term growth and daily habits."
                )
            }
            lower.contains("memorable story or experience") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this topic"
                listOf(
                    "One memorable experience I had with $label was sharing my thoughts in class and gaining confidence.",
                    "I clearly remember discussing $label with my friends, which taught me a new perspective."
                )
            }
            lower.contains("something challenging") || lower.contains("what advice would you give") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this skill"
                listOf(
                    "The hardest part of $label is staying consistent at first, so my advice is to start small every day.",
                    "Beginners often worry about making mistakes, but steady practice matters much more than perfection."
                )
            }
            lower.contains("if you could change one thing") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this topic"
                listOf(
                    "If I could change one thing related to $label, I would make daily group practice easier for everyone.",
                    "I would create more fun, real-life activities around $label so learners stay excited."
                )
            }
            lower.contains("another detail") || lower.contains("what else can you tell me") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this"
                listOf(
                    "Another detail about $label is that practicing a few minutes every day brings steady improvement.",
                    "I would also add that $label feels much easier when you relate it to real everyday situations."
                )
            }
            lower.contains("another activity or routine") -> {
                val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this topic"
                listOf(
                    "Another activity I enjoy related to $label is discussing fresh ideas with my classmates or family.",
                    "I also like spending fifteen quiet minutes reading or reflecting on $label in the evening."
                )
            }
            else -> null
        }
    }

    /**
     * Generates contextual example answers directly answering [question], falling back gracefully
     * to dynamic question-pattern synthesis when offline.
     */
    fun generateForQuestion(question: String, topic: SpeakingPrompt? = null): List<String> {
        matchCuratedExamples(question, topic)?.let { return it }

        val clean = AssameseTranslator.extractCleanQuestion(question)
        val lower = clean.lowercase().trim()

        // Dynamic question-pattern synthesis based on the actual question asked
        when {
            lower.startsWith("who is your favorite") || lower.startsWith("who's your favorite") -> {
                val subject = clean.substringAfter("favorite", "")
                    .replace("?", "")
                    .trim()
                    .ifBlank { "person" }
                return listOf(
                    "My favorite $subject is someone who works hard, stays kind, and inspires me to keep learning.",
                    "I really admire my favorite $subject because their dedication and positive attitude motivate me every day."
                )
            }
            lower.startsWith("what is your favorite") || lower.startsWith("what's your favorite") -> {
                val subject = clean.substringAfter("favorite", "")
                    .replace("?", "")
                    .trim()
                    .ifBlank { "choice" }
                return listOf(
                    "My favorite $subject is something I enjoy very often because it brings me joy and helps me relax.",
                    "If I had to pick a favorite $subject, I would choose the one that I share with my family and friends."
                )
            }
            lower.contains("read about") || lower.contains("favorite book") || lower.contains("author") -> {
                return listOf(
                    "I love reading inspiring biographies and real-life stories because they teach valuable life lessons.",
                    "Reading about remarkable people motivates me to work harder and broaden my perspective."
                )
            }
            lower.startsWith("why ") -> {
                return listOf(
                    "I feel that way mainly because it helps me grow, stay confident, and learn practical skills.",
                    "For me, the biggest reason is that it brings positive energy into my daily life."
                )
            }
            lower.startsWith("how ") -> {
                return listOf(
                    "Usually, I start step by step, stay consistent every day, and learn from my mistakes along the way.",
                    "I approach it calmly by practicing regularly and asking for helpful feedback whenever I need it."
                )
            }
            lower.startsWith("where ") -> {
                return listOf(
                    "I usually enjoy going to a quiet, comfortable place near my home where I can focus and relax.",
                    "My favorite spot is a peaceful place surrounded by nature or with my close friends and family."
                )
            }
            lower.startsWith("when ") -> {
                return listOf(
                    "I usually do this in the evening or on weekends when I have free time to focus properly.",
                    "Most of the time, I prefer doing it early in the day when my mind feels fresh."
                )
            }
            lower.startsWith("have you ever") || lower.startsWith("did you ever") -> {
                return listOf(
                    "Yes, I tried that once with my friends, and it turned out to be a wonderful learning experience.",
                    "I haven't experienced that yet, but I would definitely love the opportunity to try it soon."
                )
            }
        }

        val label = topic?.title?.takeIf { it.isNotBlank() } ?: "this topic"
        val kws = topic?.recommendedKeywords.orEmpty().filter { it.isNotBlank() }

        return if (kws.size >= 2) {
            listOf(
                "Speaking about $label, I believe ${kws[0]} and ${kws[1]} play a meaningful role in our daily lives.",
                "In my personal experience with $label, focusing on ${kws.getOrElse(2) { kws[0] }} helps me explain my thoughts clearly."
            )
        } else if (kws.size == 1) {
            listOf(
                "When I think about $label, ${kws[0]} is the first thing that comes to mind from my everyday life.",
                "I really enjoy talking about $label because it gives me a chance to share real experiences."
            )
        } else {
            listOf(
                "Speaking about $label, I really enjoy sharing my honest thoughts and everyday experiences on this.",
                "In my personal view, $label is very interesting because we can all learn something practical from it."
            )
        }
    }

    /**
     * Uses Gemini AI to generate natural model answers directly answering [question],
     * falling back gracefully to [generateForQuestion].
     */
    suspend fun generateWithAi(
        question: String,
        topic: SpeakingPrompt?,
        apiKey: String,
        model: String = "gemini-3.5-flash-lite"
    ): List<String> = withContext(Dispatchers.IO) {
        val clean = AssameseTranslator.extractCleanQuestion(question)
        matchCuratedExamples(clean, topic)?.let { return@withContext it }

        val cacheKey = "${topic?.id.orEmpty()}::${clean.lowercase().trim()}"
        aiExamplesCache[cacheKey]?.let { return@withContext it }

        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank() || trimmedKey == "MY_GEMINI_API_KEY" || trimmedKey == "your_api_key_here") {
            return@withContext generateForQuestion(clean, topic)
        }

        try {
            val cleanModel = model.trim().ifEmpty { "gemini-3.5-flash-lite" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$trimmedKey"
            val topicLine = topic?.let { "Topic: ${it.title} (${it.difficultyLevel})." } ?: ""
            val prompt = """
                Provide 2 short, natural, conversational English example answers that a student could speak aloud directly in response to this exact question.
                Make sure BOTH answers specifically and directly answer the question below.
                $topicLine
                Question: "$clean"

                Respond ONLY with valid JSON:
                {
                  "examples": ["...", "..."]
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val root = JSONObject(respBody)
                val rawText = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val arr = JSONObject(rawText).optJSONArray("examples")
                val list = mutableListOf<String>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val s = arr.optString(i).trim()
                        if (s.isNotBlank()) list.add(s)
                    }
                }
                if (list.isNotEmpty()) {
                    aiExamplesCache[cacheKey] = list
                    return@withContext list
                }
            }
        } catch (_: Throwable) {}

        val fallback = generateForQuestion(clean, topic)
        aiExamplesCache[cacheKey] = fallback
        return@withContext fallback
    }
}
