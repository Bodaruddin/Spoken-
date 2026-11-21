package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.VocabularyActivityType
import java.io.InputStream

enum class BulkImportGameType {
    WORD_MATCH,
    LISTEN_CHOOSE,
    PRONUNCIATION_BATTLE,
    FAST_VOCABULARY,
    PICTURE_VOCAB,
    GRAMMAR_CHALLENGE,
    ENGLISH_ADVENTURE
}

sealed interface ParsedGameImportRow {
    val previewTitle: String
    val previewSubtitle: String
    val previewBadge: String

    data class WordMatchRow(
        val word: String,
        val definition: String,
        val activityType: VocabularyActivityType,
        val category: String = "General",
        val pictureEmoji: String = "✨",
        val synonym: String = "",
        val antonym: String = "",
        val distractors: List<String> = emptyList(),
        val scrambleHint: String = ""
    ) : ParsedGameImportRow {
        override val previewTitle: String
            get() = if (activityType == VocabularyActivityType.PICTURE_MATCHING) "$pictureEmoji $word" else word
        override val previewSubtitle: String
            get() = when (activityType) {
                VocabularyActivityType.SYNONYMS -> "Synonym: $synonym • $definition"
                VocabularyActivityType.OPPOSITE_WORDS -> "Opposite: $antonym • $definition"
                VocabularyActivityType.WORD_SCRAMBLE -> "Hint: ${scrambleHint.ifBlank { definition }}"
                else -> definition
            }
        override val previewBadge: String get() = "${activityType.emoji} ${activityType.title}"
    }

    data class ListenChooseRow(
        val audioScript: String,
        val question: String,
        val options: List<String>,
        val correctIndex: Int
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = question
        override val previewSubtitle: String get() = "Audio: \"$audioScript\" • Ans: ${options.getOrElse(correctIndex) { "" }}"
        override val previewBadge: String get() = "${options.size} Options"
    }

    data class PronunciationBattleRow(
        val sentence: String,
        val phonemeFocus: String,
        val difficulty: String,
        val passingScore: Int
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = sentence
        override val previewSubtitle: String get() = "Focus: $phonemeFocus • Pass: $passingScore%"
        override val previewBadge: String get() = difficulty
    }

    data class FastVocabularyRow(
        val word: String,
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = word
        override val previewSubtitle: String get() = "$prompt → ${options.getOrElse(correctIndex) { "" }}"
        override val previewBadge: String get() = "${options.size} Options"
    }

    data class PictureVocabRow(
        val word: String,
        val emoji: String,
        val phonetic: String,
        val clue: String,
        val options: List<String>
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = "$emoji $word"
        override val previewSubtitle: String get() = "$phonetic • $clue"
        override val previewBadge: String get() = "${options.size} Options"
    }

    data class GrammarChallengeRow(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String,
        val category: String
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = question
        override val previewSubtitle: String get() = "Ans: ${options.getOrElse(correctIndex) { "" }} • $explanation"
        override val previewBadge: String get() = category
    }

    data class EnglishAdventureRow(
        val title: String,
        val description: String,
        val targetSentence: String,
        val emoji: String,
        val xp: Int
    ) : ParsedGameImportRow {
        override val previewTitle: String get() = "$emoji $title"
        override val previewSubtitle: String get() = targetSentence.ifBlank { description }
        override val previewBadge: String get() = "+$xp XP"
    }
}

object GameExcelImportParser {

    fun parseFromUri(
        context: Context,
        uri: Uri,
        fileName: String? = null,
        gameType: BulkImportGameType,
        defaultVocabMode: VocabularyActivityType = VocabularyActivityType.MEANING_MATCHING,
        defaultCategory: String = "General"
    ): List<ParsedGameImportRow> {
        val resolvedName = fileName ?: uri.lastPathSegment ?: "sheet.csv"
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            if (resolvedName.endsWith(".xlsx", ignoreCase = true)) {
                parseFromXlsxStream(stream, gameType, defaultVocabMode, defaultCategory)
            } else {
                val rawText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                parseFromText(rawText, gameType, defaultVocabMode, defaultCategory)
            }
        } ?: emptyList()
    }

    fun parseFromXlsxStream(
        inputStream: InputStream,
        gameType: BulkImportGameType,
        defaultVocabMode: VocabularyActivityType = VocabularyActivityType.MEANING_MATCHING,
        defaultCategory: String = "General"
    ): List<ParsedGameImportRow> {
        val table = ExcelWordSheetParser.extractRawTableFromXlsxStream(inputStream)
        return mapTableToRows(table, gameType, defaultVocabMode, defaultCategory)
    }

    fun parseFromText(
        rawText: String,
        gameType: BulkImportGameType,
        defaultVocabMode: VocabularyActivityType = VocabularyActivityType.MEANING_MATCHING,
        defaultCategory: String = "General"
    ): List<ParsedGameImportRow> {
        val cleaned = rawText.removePrefix("\uFEFF")
        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        val table = lines
            .map { splitDelimitedLine(it) }
            .filter { row -> row.isNotEmpty() && row.any { it.isNotBlank() } }
        return mapTableToRows(table, gameType, defaultVocabMode, defaultCategory)
    }

    fun getColumnGuide(gameType: BulkImportGameType): List<String> = when (gameType) {
        BulkImportGameType.WORD_MATCH -> listOf(
            "Word", "Definition / Meaning", "Activity Mode (optional)", "Category", "Emoji", "Synonym / Opposite / Hint", "Wrong Options (; separated)"
        )
        BulkImportGameType.LISTEN_CHOOSE -> listOf(
            "Audio Script (TTS)", "Question", "Option A", "Option B", "Option C", "Option D (optional)", "Correct (A/B/C/D or 1-4)"
        )
        BulkImportGameType.PRONUNCIATION_BATTLE -> listOf(
            "Challenge Sentence", "Phoneme Focus", "Difficulty (Easy/Medium/Hard)", "Passing Score (e.g. 80)"
        )
        BulkImportGameType.FAST_VOCABULARY -> listOf(
            "Word", "Prompt", "Option A", "Option B", "Option C", "Option D (optional)", "Correct (A/B/C/D or 1-4)"
        )
        BulkImportGameType.PICTURE_VOCAB -> listOf(
            "Word", "Emoji", "Phonetic", "Clue / Hint", "Quiz Options (; or | separated)"
        )
        BulkImportGameType.GRAMMAR_CHALLENGE -> listOf(
            "Question (with ___)", "Option A", "Option B", "Option C", "Option D (optional)", "Correct (A/B/C/D or 1-4)", "Explanation"
        )
        BulkImportGameType.ENGLISH_ADVENTURE -> listOf(
            "Level Title", "Description", "Target Speaking Sentence", "Emoji", "XP Reward"
        )
    }

    fun getSampleCsv(gameType: BulkImportGameType): String = when (gameType) {
        BulkImportGameType.WORD_MATCH -> """
Word,Definition,Activity Mode,Category,Emoji,Synonym / Opposite / Hint,Wrong Options
Telescope,An optical instrument used to observe distant planets and stars,Picture Matching,Science,🔭,,Microscope;Compass;Camera
Volcano,A mountain that erupts with molten lava and ash,Picture Matching,Nature,🌋,,Glacier;Desert;Canyon
Eloquent,Fluent and persuasive in speaking or writing,Meaning Matching,Communication,🗣️,,Silent and shy;Very forgetful;Slow to react
Diligent,Showing careful and persistent effort in work,Synonyms,Character,⭐,Hardworking,Lazy;Careless;Fearful
Ancient,Belonging to the very distant past,Opposite Words,History,🏛️,Modern,Old;Historic;Classic
Compass,A magnetic tool that always points north for navigation,Word Scramble,Travel,🧭,Helps travelers find north,
        """.trimIndent()

        BulkImportGameType.LISTEN_CHOOSE -> """
Audio Script,Question,Option A,Option B,Option C,Option D,Correct
The morning train to Guwahati departs from platform four at seven thirty.,Which platform does the train depart from?,Platform four,Platform two,Platform six,Platform nine,A
Please remember to bring your library card before borrowing any science books.,What must you bring to borrow books?,A library card,A lunch box,A sports jersey,A bus ticket,A
Yesterday evening it rained heavily so the football match was postponed to Friday.,When was the football match moved to?,Monday,Wednesday,Friday,Sunday,C
Ananya practices speaking English aloud for twenty minutes every morning.,How long does Ananya practice each morning?,Ten minutes,Twenty minutes,Forty minutes,One hour,B
Our school science exhibition opens in the main auditorium at ten o'clock.,Where is the science exhibition being held?,In the playground,In the main auditorium,In the cafeteria,In the computer lab,B
        """.trimIndent()

        BulkImportGameType.PRONUNCIATION_BATTLE -> """
Challenge Sentence,Phoneme Focus,Difficulty,Passing Score
The thoughtful student thanked the teacher for the thorough feedback.,Voiceless TH sound,Medium,80
She sells shiny seashells smoothly along the sunny seashore.,S and SH contrast,Medium,80
Clear rhythm and steady breathing make spoken English sound natural.,R and L clarity,Easy,75
The brilliant astronomer discovered three bright planets through the telescope.,Consonant clusters br/tr/pl,Hard,85
Please pronounce every syllable slowly and confidently during the interview.,Syllable stress & pacing,Easy,75
        """.trimIndent()

        BulkImportGameType.FAST_VOCABULARY -> """
Word,Prompt,Option A,Option B,Option C,Option D,Correct
Radiant,Choose the closest meaning for Radiant:,Shining brightly,Very quiet,Slightly damp,Completely empty,A
Courageous,Select the synonym for Courageous:,Timid,Brave,Sleepy,Fragile,B
Abundant,What does Abundant mean?,Very rare,Plentiful,Extremely tiny,Broken down,B
Tranquil,Pick the best meaning for Tranquil:,Noisy and wild,Calm and peaceful,Fast and sharp,Cold and dark,B
Curious,Select the closest meaning for Curious:,Eager to learn,Tired of waiting,Afraid to speak,Ready to sleep,A
Resilient,What describes a Resilient person?,Gives up easily,Recovers quickly from setbacks,Never asks questions,Avoids teamwork,B
        """.trimIndent()

        BulkImportGameType.PICTURE_VOCAB -> """
Word,Emoji,Phonetic,Clue,Options
Microscope,🔬,/ˈmaɪ.krə.skəʊp/,Laboratory instrument used to view tiny cells,Microscope;Telescope;Camera;Thermometer
Compass,🧭,/ˈkʌm.pəs/,Navigation device that shows magnetic north,Compass;Clock;Thermometer;Radar
Satellite,🛰️,/ˈsæt.əl.aɪt/,Spacecraft orbiting Earth for communication and weather,Satellite;Rocket;Submarine;Airplane
Lighthouse,🗼,/ˈlaɪt.haʊs/,Tall coastal tower whose bright beam guides ships safely,Lighthouse;Windmill;Bridge;Castle
Volcano,🌋,/vɒlˈkeɪ.nəʊ/,Mountain opening where molten rock and ash erupt,Volcano;Waterfall;Glacier;Island
Trophy,🏆,/ˈtrəʊ.fi/,Golden cup awarded to the winner of a competition,Trophy;Medal;Crown;Shield
        """.trimIndent()

        BulkImportGameType.GRAMMAR_CHALLENGE -> """
Question,Option A,Option B,Option C,Option D,Correct,Explanation
She ___ to the English club every Saturday morning.,go,goes,going,gone,B,Third-person singular subjects take -es/-s in Present Simple.
By the time we arrived the movie ___ already started.,has,have,had,was,C,Past Perfect (had + past participle) shows an earlier past action.
Neither Rahul nor his friends ___ ready for the debate.,is,are,was,be,B,With neither...nor the verb agrees with the nearer plural subject.
If it ___ tomorrow we will hold the workshop indoors.,rains,rained,will rain,raining,A,First conditional uses Present Simple in the if-clause.
She asked me where I ___ living at that time.,am,was,will be,have,B,Reported speech shifts present continuous to past continuous.
He bought ___ umbrella before walking into the rain.,a,an,the,some,B,Use 'an' before words starting with a vowel sound.
        """.trimIndent()

        BulkImportGameType.ENGLISH_ADVENTURE -> """
Level Title,Description,Target Speaking Sentence,Emoji,XP
Morning Greeting Quest,Practice polite everyday greetings with clear tone,Good morning everyone I am excited to learn today.,🌅,60
Market Bargaining Trail,Use polite requests while shopping in English,Could you please tell me the price of these fresh apples?,🧺,70
Library Explorer,Ask clear questions in a school library setting,Excuse me where can I find books about world history?,📚,75
Weather Reporter,Describe today's forecast smoothly and clearly,The sky is bright and sunny with a gentle afternoon breeze.,⛅,80
Stage Confidence Peak,Deliver a short self-introduction with steady rhythm,Hello my name is Maya and I love speaking English with confidence.,🎤,90
        """.trimIndent()
    }

    private fun mapTableToRows(
        table: List<List<String>>,
        gameType: BulkImportGameType,
        defaultVocabMode: VocabularyActivityType,
        defaultCategory: String
    ): List<ParsedGameImportRow> {
        if (table.isEmpty()) return emptyList()
        return when (gameType) {
            BulkImportGameType.WORD_MATCH -> parseWordMatchTable(table, defaultVocabMode, defaultCategory)
            BulkImportGameType.LISTEN_CHOOSE -> parseListenChooseTable(table)
            BulkImportGameType.PRONUNCIATION_BATTLE -> parsePronunciationBattleTable(table)
            BulkImportGameType.FAST_VOCABULARY -> parseFastVocabTable(table)
            BulkImportGameType.PICTURE_VOCAB -> parsePictureVocabTable(table)
            BulkImportGameType.GRAMMAR_CHALLENGE -> parseGrammarTable(table, defaultCategory)
            BulkImportGameType.ENGLISH_ADVENTURE -> parseAdventureTable(table)
        }
    }

    private fun isSerialHeader(cell: String): Boolean {
        return cell.lowercase().trim() in setOf("sl", "sl.", "sl no", "no", "no.", "#", "id", "sr", "sr.")
    }

    private fun parseWordMatchTable(
        table: List<List<String>>,
        defaultMode: VocabularyActivityType,
        defaultCategory: String
    ): List<ParsedGameImportRow.WordMatchRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = firstRow.firstOrNull()?.lowercase()?.trim() in setOf("word", "english word", "term", "vocabulary") ||
            isSerialHeader(firstRow.firstOrNull() ?: "") ||
            (joined.contains("word") && (joined.contains("definition") || joined.contains("meaning") || joined.contains("mode") || joined.contains("synonym")))

        var wordCol = 0
        var defCol = 1
        var modeCol = 2
        var catCol = 3
        var emojiCol = 4
        var extraCol = 5
        var distractorsCol = 6

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            wordCol = offset
            defCol = offset + 1
            modeCol = offset + 2
            catCol = offset + 3
            emojiCol = offset + 4
            extraCol = offset + 5
            distractorsCol = offset + 6

            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h == "word" || h.contains("english word") || h == "term" -> wordCol = idx
                    h.contains("def") || h.contains("meaning") || h.contains("desc") -> defCol = idx
                    h.contains("mode") || h.contains("activity") || h.contains("type") -> modeCol = idx
                    h.contains("cat") || h.contains("topic") -> catCol = idx
                    h.contains("emoji") || h.contains("icon") || h.contains("pic") -> emojiCol = idx
                    h.contains("syn") || h.contains("ant") || h.contains("opp") || h.contains("hint") || h.contains("clue") -> extraCol = idx
                    h.contains("distract") || h.contains("wrong") || h.contains("option") -> distractorsCol = idx
                }
            }
            table.drop(1)
        } else {
            table
        }

        val cleanDefaultCat = defaultCategory.trim().ifBlank { "General" }
        return dataRows.mapNotNull { cells ->
            val word = cells.getOrNull(wordCol)?.trim() ?: ""
            if (word.isBlank() || word.equals("word", ignoreCase = true)) return@mapNotNull null

            val rawDef = cells.getOrNull(defCol)?.trim() ?: ""
            val rawMode = cells.getOrNull(modeCol)?.trim() ?: ""
            val mode = resolveVocabMode(rawMode, defaultMode)
            val category = cells.getOrNull(catCol)?.trim()?.ifBlank { cleanDefaultCat } ?: cleanDefaultCat
            val emoji = cells.getOrNull(emojiCol)?.trim()?.ifBlank { "✨" } ?: "✨"
            val extra = cells.getOrNull(extraCol)?.trim() ?: ""
            val distractors = splitOptionList(cells.getOrNull(distractorsCol) ?: "")

            val synonym = if (mode == VocabularyActivityType.SYNONYMS) extra.ifBlank { rawDef } else ""
            val antonym = if (mode == VocabularyActivityType.OPPOSITE_WORDS) extra.ifBlank { rawDef } else ""
            val scrambleHint = if (mode == VocabularyActivityType.WORD_SCRAMBLE) extra.ifBlank { rawDef } else ""
            val finalDef = rawDef.ifBlank {
                when (mode) {
                    VocabularyActivityType.PICTURE_MATCHING -> "Visual vocabulary item ($word)"
                    VocabularyActivityType.SYNONYMS -> "Similar in meaning to $synonym"
                    VocabularyActivityType.OPPOSITE_WORDS -> "Opposite in meaning to $antonym"
                    VocabularyActivityType.WORD_SCRAMBLE -> scrambleHint.ifBlank { "Unscramble the letters to spell $word" }
                    VocabularyActivityType.MEANING_MATCHING -> "English vocabulary word: $word"
                }
            }

            ParsedGameImportRow.WordMatchRow(
                word = word,
                definition = finalDef,
                activityType = mode,
                category = category,
                pictureEmoji = emoji,
                synonym = synonym,
                antonym = antonym,
                distractors = distractors,
                scrambleHint = scrambleHint
            )
        }
    }

    private fun resolveVocabMode(raw: String, fallback: VocabularyActivityType): VocabularyActivityType {
        val s = raw.lowercase().trim()
        if (s.isBlank()) return fallback
        return when {
            s.contains("picture") || s.contains("blitz") || s.contains("image") -> VocabularyActivityType.PICTURE_MATCHING
            s.contains("meaning") || s.contains("definition") -> VocabularyActivityType.MEANING_MATCHING
            s.contains("synonym") -> VocabularyActivityType.SYNONYMS
            s.contains("opposite") || s.contains("antonym") -> VocabularyActivityType.OPPOSITE_WORDS
            s.contains("scramble") || s.contains("jumble") -> VocabularyActivityType.WORD_SCRAMBLE
            else -> fallback
        }
    }

    private fun parseListenChooseTable(table: List<List<String>>): List<ParsedGameImportRow.ListenChooseRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            joined.contains("audio") || joined.contains("script") ||
            (joined.contains("question") && joined.contains("option"))

        var scriptCol = 0
        var questionCol = 1
        val optionCols = mutableListOf(2, 3, 4, 5)
        var correctCol = 6

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            scriptCol = offset
            questionCol = offset + 1
            val foundOpts = mutableListOf<Int>()
            var foundCorrect = -1
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h.contains("audio") || h.contains("script") || h.contains("spoken") || h.contains("tts") -> scriptCol = idx
                    h.contains("question") || h.contains("prompt") -> questionCol = idx
                    h.contains("correct") || h.contains("answer") || h == "ans" -> foundCorrect = idx
                    h.contains("option") || h.contains("choice") || h.contains("distract") || h in setOf("a", "b", "c", "d") -> foundOpts.add(idx)
                }
            }
            if (foundOpts.isNotEmpty()) {
                optionCols.clear()
                optionCols.addAll(foundOpts)
            } else {
                optionCols.clear()
                optionCols.addAll(listOf(offset + 2, offset + 3, offset + 4, offset + 5))
            }
            correctCol = if (foundCorrect >= 0) foundCorrect else (optionCols.maxOrNull() ?: (offset + 5)) + 1
            table.drop(1)
        } else {
            table
        }

        return dataRows.mapNotNull { cells ->
            val script = cells.getOrNull(scriptCol)?.trim() ?: ""
            val question = cells.getOrNull(questionCol)?.trim() ?: ""
            if (script.isBlank() || script.equals("audio script", ignoreCase = true)) return@mapNotNull null

            val rawOptions = optionCols.mapNotNull { idx ->
                cells.getOrNull(idx)?.trim()?.takeIf { it.isNotBlank() }
            }
            val options = if (rawOptions.size >= 2) {
                rawOptions
            } else {
                // Support single semicolon-separated options column if teacher used 3-4 columns
                val splitSingle = splitOptionList(cells.getOrNull(optionCols.firstOrNull() ?: 2) ?: "")
                if (splitSingle.size >= 2) splitSingle else listOf("Option A", "Option B", "Option C")
            }

            val rawCorrect = cells.getOrNull(correctCol)?.trim() ?: ""
            val correctIdx = parseCorrectIndex(rawCorrect, options)

            ParsedGameImportRow.ListenChooseRow(
                audioScript = script,
                question = question.ifBlank { "What did you hear in the audio clip?" },
                options = options,
                correctIndex = correctIdx
            )
        }
    }

    private fun parsePronunciationBattleTable(table: List<List<String>>): List<ParsedGameImportRow.PronunciationBattleRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            joined.contains("sentence") || joined.contains("phrase") || joined.contains("phoneme") || joined.contains("difficulty")

        var sentenceCol = 0
        var phonemeCol = 1
        var diffCol = 2
        var scoreCol = 3

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            sentenceCol = offset
            phonemeCol = offset + 1
            diffCol = offset + 2
            scoreCol = offset + 3
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h.contains("sentence") || h.contains("phrase") || h.contains("text") -> sentenceCol = idx
                    h.contains("phoneme") || h.contains("focus") || h.contains("sound") -> phonemeCol = idx
                    h.contains("diff") || h.contains("level") -> diffCol = idx
                    h.contains("score") || h.contains("pass") || h.contains("target") -> scoreCol = idx
                }
            }
            table.drop(1)
        } else {
            table
        }

        return dataRows.mapNotNull { cells ->
            val sentence = cells.getOrNull(sentenceCol)?.trim() ?: ""
            if (sentence.isBlank() || sentence.equals("challenge sentence", ignoreCase = true)) return@mapNotNull null
            val phoneme = cells.getOrNull(phonemeCol)?.trim()?.ifBlank { "Clear pronunciation" } ?: "Clear pronunciation"
            val diff = cells.getOrNull(diffCol)?.trim()?.ifBlank { "Medium" } ?: "Medium"
            val score = cells.getOrNull(scoreCol)?.trim()?.replace("%", "")?.toIntOrNull()?.coerceIn(50, 98) ?: 80

            ParsedGameImportRow.PronunciationBattleRow(
                sentence = sentence,
                phonemeFocus = phoneme,
                difficulty = diff,
                passingScore = score
            )
        }
    }

    private fun parseFastVocabTable(table: List<List<String>>): List<ParsedGameImportRow.FastVocabularyRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            firstRow.firstOrNull()?.lowercase()?.trim() in setOf("word", "target word") ||
            (joined.contains("word") && (joined.contains("prompt") || joined.contains("option")))

        var wordCol = 0
        var promptCol = 1
        val optionCols = mutableListOf(2, 3, 4, 5)
        var correctCol = 6

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            wordCol = offset
            promptCol = offset + 1
            val foundOpts = mutableListOf<Int>()
            var foundCorrect = -1
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h == "word" || h.contains("target word") -> wordCol = idx
                    h.contains("prompt") || h.contains("question") -> promptCol = idx
                    h.contains("correct") || h.contains("answer") || h == "ans" -> foundCorrect = idx
                    h.contains("option") || h.contains("choice") || h.contains("distract") || h in setOf("a", "b", "c", "d") -> foundOpts.add(idx)
                }
            }
            if (foundOpts.isNotEmpty()) {
                optionCols.clear()
                optionCols.addAll(foundOpts)
            } else {
                optionCols.clear()
                optionCols.addAll(listOf(offset + 2, offset + 3, offset + 4, offset + 5))
            }
            correctCol = if (foundCorrect >= 0) foundCorrect else (optionCols.maxOrNull() ?: (offset + 5)) + 1
            table.drop(1)
        } else {
            table
        }

        return dataRows.mapNotNull { cells ->
            val word = cells.getOrNull(wordCol)?.trim() ?: ""
            if (word.isBlank() || word.equals("word", ignoreCase = true)) return@mapNotNull null

            val prompt = cells.getOrNull(promptCol)?.trim()?.ifBlank { "Choose the closest meaning for '$word':" }
                ?: "Choose the closest meaning for '$word':"

            val rawOptions = optionCols.mapNotNull { idx ->
                cells.getOrNull(idx)?.trim()?.takeIf { it.isNotBlank() }
            }
            val options = if (rawOptions.size >= 2) rawOptions else listOf("Correct meaning", "Wrong meaning 1", "Wrong meaning 2")
            val rawCorrect = cells.getOrNull(correctCol)?.trim() ?: ""
            val correctIdx = parseCorrectIndex(rawCorrect, options)

            ParsedGameImportRow.FastVocabularyRow(
                word = word,
                prompt = prompt,
                options = options,
                correctIndex = correctIdx
            )
        }
    }

    private fun parsePictureVocabTable(table: List<List<String>>): List<ParsedGameImportRow.PictureVocabRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            firstRow.firstOrNull()?.lowercase()?.trim() in setOf("word", "target word", "picture") ||
            (joined.contains("word") && (joined.contains("emoji") || joined.contains("clue") || joined.contains("phonetic")))

        var wordCol = 0
        var emojiCol = 1
        var phoneticCol = 2
        var clueCol = 3
        var optionsCol = 4

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            wordCol = offset
            emojiCol = offset + 1
            phoneticCol = offset + 2
            clueCol = offset + 3
            optionsCol = offset + 4
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h == "word" || h.contains("target") -> wordCol = idx
                    h.contains("emoji") || h.contains("icon") || h.contains("pic") -> emojiCol = idx
                    h.contains("phonetic") || h.contains("ipa") -> phoneticCol = idx
                    h.contains("clue") || h.contains("hint") || h.contains("meaning") || h.contains("def") -> clueCol = idx
                    h.contains("option") || h.contains("choice") -> optionsCol = idx
                }
            }
            table.drop(1)
        } else {
            table
        }

        return dataRows.mapNotNull { cells ->
            val word = cells.getOrNull(wordCol)?.trim() ?: ""
            if (word.isBlank() || word.equals("word", ignoreCase = true)) return@mapNotNull null

            val emoji = cells.getOrNull(emojiCol)?.trim()?.ifBlank { "🖼️" } ?: "🖼️"
            val phonetic = cells.getOrNull(phoneticCol)?.trim()?.ifBlank { "/${word.lowercase()}/" } ?: "/${word.lowercase()}/"
            val clue = cells.getOrNull(clueCol)?.trim()?.ifBlank { "Visual vocabulary recognition challenge" }
                ?: "Visual vocabulary recognition challenge"

            val rawOptList = if (cells.size > optionsCol + 1) {
                // Teacher placed options across separate columns
                cells.drop(optionsCol).map { it.trim() }.filter { it.isNotBlank() }
            } else {
                splitOptionList(cells.getOrNull(optionsCol) ?: "")
            }

            val options = rawOptList
                .let { list ->
                    if (list.any { it.equals(word, ignoreCase = true) }) list else listOf(word) + list
                }
                .distinctBy { it.lowercase() }
                .let { list ->
                    if (list.size >= 2) list else listOf(word, "Notebook", "Compass", "Lantern").distinct()
                }

            ParsedGameImportRow.PictureVocabRow(
                word = word,
                emoji = emoji,
                phonetic = phonetic,
                clue = clue,
                options = options
            )
        }
    }

    private fun parseGrammarTable(
        table: List<List<String>>,
        defaultCategory: String
    ): List<ParsedGameImportRow.GrammarChallengeRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            firstRow.firstOrNull()?.lowercase()?.trim() in setOf("question", "sentence", "prompt") ||
            (joined.contains("question") && (joined.contains("option") || joined.contains("explanation")))

        var questionCol = 0
        val optionCols = mutableListOf(1, 2, 3, 4)
        var correctCol = 5
        var explanationCol = 6
        var categoryCol = -1

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            questionCol = offset
            val foundOpts = mutableListOf<Int>()
            var foundCorrect = -1
            var foundExp = -1
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h.contains("question") || h.contains("sentence") || h.contains("prompt") -> questionCol = idx
                    h.contains("correct") || h.contains("answer") || h == "ans" -> foundCorrect = idx
                    h.contains("expl") || h.contains("rule") || h.contains("hint") -> foundExp = idx
                    h.contains("cat") || h.contains("topic") -> categoryCol = idx
                    h.contains("option") || h.contains("choice") || h.contains("distract") || h in setOf("a", "b", "c", "d") -> foundOpts.add(idx)
                }
            }
            if (foundOpts.isNotEmpty()) {
                optionCols.clear()
                optionCols.addAll(foundOpts)
            } else {
                optionCols.clear()
                optionCols.addAll(listOf(offset + 1, offset + 2, offset + 3, offset + 4))
            }
            val lastOpt = optionCols.maxOrNull() ?: (offset + 4)
            correctCol = if (foundCorrect >= 0) foundCorrect else lastOpt + 1
            explanationCol = if (foundExp >= 0) foundExp else correctCol + 1
            table.drop(1)
        } else {
            table
        }

        val cleanCat = defaultCategory.trim().ifBlank { "Grammar" }
        return dataRows.mapNotNull { cells ->
            val question = cells.getOrNull(questionCol)?.trim() ?: ""
            if (question.isBlank() || question.equals("question", ignoreCase = true)) return@mapNotNull null

            val rawOptions = optionCols.mapNotNull { idx ->
                cells.getOrNull(idx)?.trim()?.takeIf { it.isNotBlank() }
            }
            val options = if (rawOptions.size >= 2) rawOptions else listOf("Option A", "Option B", "Option C")
            val rawCorrect = cells.getOrNull(correctCol)?.trim() ?: ""
            val correctIdx = parseCorrectIndex(rawCorrect, options)
            val explanation = cells.getOrNull(explanationCol)?.trim()?.ifBlank { "Standard grammatical syntax rule." }
                ?: "Standard grammatical syntax rule."
            val category = if (categoryCol >= 0) {
                cells.getOrNull(categoryCol)?.trim()?.ifBlank { cleanCat } ?: cleanCat
            } else {
                cleanCat
            }

            ParsedGameImportRow.GrammarChallengeRow(
                question = question,
                options = options,
                correctIndex = correctIdx,
                explanation = explanation,
                category = category
            )
        }
    }

    private fun parseAdventureTable(table: List<List<String>>): List<ParsedGameImportRow.EnglishAdventureRow> {
        val firstRow = table.first()
        val joined = firstRow.joinToString(" ").lowercase()
        val hasHeader = isSerialHeader(firstRow.firstOrNull() ?: "") ||
            joined.contains("title") || joined.contains("level") ||
            (joined.contains("description") && joined.contains("sentence"))

        var titleCol = 0
        var descCol = 1
        var sentenceCol = 2
        var emojiCol = 3
        var xpCol = 4

        val dataRows = if (hasHeader) {
            val offset = if (isSerialHeader(firstRow.firstOrNull() ?: "")) 1 else 0
            titleCol = offset
            descCol = offset + 1
            sentenceCol = offset + 2
            emojiCol = offset + 3
            xpCol = offset + 4
            firstRow.forEachIndexed { idx, raw ->
                val h = raw.lowercase().trim()
                when {
                    h.contains("title") || h.contains("level") || h.contains("name") -> titleCol = idx
                    h.contains("desc") || h.contains("summary") -> descCol = idx
                    h.contains("sentence") || h.contains("phrase") || h.contains("target") || h.contains("speak") -> sentenceCol = idx
                    h.contains("emoji") || h.contains("icon") -> emojiCol = idx
                    h.contains("xp") || h.contains("reward") || h.contains("point") -> xpCol = idx
                }
            }
            table.drop(1)
        } else {
            table
        }

        return dataRows.mapNotNull { cells ->
            val title = cells.getOrNull(titleCol)?.trim() ?: ""
            if (title.isBlank() || title.equals("level title", ignoreCase = true)) return@mapNotNull null

            val desc = cells.getOrNull(descCol)?.trim()?.ifBlank { "Explore and conquer this spoken English quest." }
                ?: "Explore and conquer this spoken English quest."
            val sentence = cells.getOrNull(sentenceCol)?.trim() ?: ""
            val emoji = cells.getOrNull(emojiCol)?.trim()?.ifBlank { "⭐" } ?: "⭐"
            val xp = cells.getOrNull(xpCol)?.trim()?.filter { it.isDigit() }?.toIntOrNull()?.coerceIn(20, 500) ?: 80

            ParsedGameImportRow.EnglishAdventureRow(
                title = title,
                description = desc,
                targetSentence = sentence,
                emoji = emoji,
                xp = xp
            )
        }
    }

    private fun parseCorrectIndex(raw: String, options: List<String>): Int {
        if (options.isEmpty()) return 0
        val clean = raw.trim()
        if (clean.isBlank()) return 0

        // Letter format: A, B, C, D or Option A
        val letter = clean.removePrefix("Option ").removePrefix("option ").trim().uppercase()
        when (letter) {
            "A" -> return 0.coerceIn(0, options.lastIndex)
            "B" -> return 1.coerceIn(0, options.lastIndex)
            "C" -> return 2.coerceIn(0, options.lastIndex)
            "D" -> return 3.coerceIn(0, options.lastIndex)
        }

        // Numeric format: 1-based (1..4) or 0
        val numeric = clean.toIntOrNull()
        if (numeric != null) {
            return when {
                numeric in 1..options.size -> numeric - 1
                numeric == 0 -> 0
                else -> 0
            }
        }

        // Exact option text match
        val matchedIndex = options.indexOfFirst { it.equals(clean, ignoreCase = true) }
        return if (matchedIndex >= 0) matchedIndex else 0
    }

    private fun splitOptionList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val delimiter = when {
            raw.contains(";") -> ";"
            raw.contains("|") -> "|"
            else -> ","
        }
        return raw.split(delimiter).map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun splitDelimitedLine(line: String): List<String> {
        val delimiter = when {
            line.contains('\t') -> '\t'
            line.count { it == ',' } >= line.count { it == ';' } -> ','
            else -> ';'
        }
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }
}
