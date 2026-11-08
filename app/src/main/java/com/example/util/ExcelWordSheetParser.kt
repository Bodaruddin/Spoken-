package com.example.util

import android.content.Context
import android.net.Uri
import com.example.service.PhoneticsAiEngine
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class ParsedVocabRow(
    val englishWord: String,
    val assameseMeaning: String,
    val assameseTransliteration: String = "",
    val ipa: String = "",
    val syllables: String = "",
    val partOfSpeech: String = "noun",
    val category: String = "General",
    val exampleSentence: String = "",
    val coachingTip: String = ""
)

object ExcelWordSheetParser {

    /**
     * Parses an Excel (.xlsx) or CSV/TSV input stream from an Android content Uri or file.
     */
    fun parseFromUri(context: Context, uri: Uri, fileName: String? = null): List<ParsedVocabRow> {
        val resolvedName = fileName ?: uri.lastPathSegment ?: "words.csv"
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            if (resolvedName.endsWith(".xlsx", ignoreCase = true)) {
                parseXlsxStream(stream)
            } else {
                parseCsvStream(stream)
            }
        } ?: emptyList()
    }

    /**
     * Parse raw text (CSV or TSV rows) pasted by user or loaded from clipboard.
     */
    fun parseRawText(rawText: String): List<ParsedVocabRow> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val parsedRows = mutableListOf<List<String>>()
        for (line in lines) {
            val tokens = splitDelimitedLine(line)
            if (tokens.isNotEmpty() && tokens.any { it.isNotBlank() }) {
                parsedRows.add(tokens)
            }
        }

        return mapTableToVocabRows(parsedRows)
    }

    /**
     * Parse standard CSV/TSV stream line-by-line.
     */
    fun parseCsvStream(inputStream: InputStream): List<ParsedVocabRow> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rawTable = mutableListOf<List<String>>()
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    rawTable.add(splitDelimitedLine(trimmed))
                }
            }
        }
        return mapTableToVocabRows(rawTable)
    }

    /**
     * Native, zero-dependency .xlsx parser using Java's built-in ZipInputStream.
     * Reads shared strings from xl/sharedStrings.xml and cell rows from xl/worksheets/sheet1.xml.
     */
    fun parseXlsxStream(inputStream: InputStream): List<ParsedVocabRow> {
        val sharedStrings = mutableListOf<String>()
        val sheetRows = mutableListOf<List<String>>()

        try {
            val bytes = inputStream.readBytes()

            // Pass 1: Extract sharedStrings.xml
            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true) ||
                        entry.name.endsWith("sharedStrings.xml", ignoreCase = true)
                    ) {
                        val xmlContent = zip.reader(Charsets.UTF_8).readText()
                        sharedStrings.addAll(extractSharedStringsFromXml(xmlContent))
                    }
                    entry = zip.nextEntry
                }
            }

            // Pass 2: Extract sheet1.xml (or first worksheet)
            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.startsWith("xl/worksheets/sheet", ignoreCase = true) &&
                        entry.name.endsWith(".xml", ignoreCase = true)
                    ) {
                        val sheetXml = zip.reader(Charsets.UTF_8).readText()
                        val rows = extractRowsFromSheetXml(sheetXml, sharedStrings)
                        if (rows.isNotEmpty()) {
                            sheetRows.addAll(rows)
                            break
                        }
                    }
                    entry = zip.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (sheetRows.isNotEmpty()) {
            mapTableToVocabRows(sheetRows)
        } else {
            emptyList()
        }
    }

    /**
     * Extracts strings from sharedStrings.xml: handles <si><t>value</t></si> and <si><r><t>part</t></r></si>
     */
    private fun extractSharedStringsFromXml(xml: String): List<String> {
        val list = mutableListOf<String>()
        val siRegex = "<si>(.*?)</si>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val tRegex = "<t[^>]*>(.*?)</t>".toRegex(RegexOption.DOT_MATCHES_ALL)

        siRegex.findAll(xml).forEach { match ->
            val siBody = match.groupValues[1]
            val textParts = tRegex.findAll(siBody).map { it.groupValues[1] }.toList()
            val combined = textParts.joinToString("").trim()
            list.add(unescapeXml(combined))
        }
        return list
    }

    /**
     * Extracts matrix of cell strings from worksheet XML (<row>...<c t="s"><v>0</v></c>...).
     */
    private fun extractRowsFromSheetXml(xml: String, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val rowRegex = "<row[^>]*>(.*?)</row>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val cellRegex = """<c\s+r="([A-Z]+)[0-9]+"(?:\s+[^>]*?t="([a-z]+)")?[^>]*>(?:<v>([^<]*)</v>)?(?:<is><t>([^<]*)</t></is>)?</c>""".toRegex(RegexOption.DOT_MATCHES_ALL)

        rowRegex.findAll(xml).forEach { rowMatch ->
            val rowBody = rowMatch.groupValues[1]
            val cellMatches = cellRegex.findAll(rowBody).toList()
            if (cellMatches.isNotEmpty()) {
                val rowCells = mutableListOf<String>()
                for (c in cellMatches) {
                    val type = c.groupValues[2]
                    val value = c.groupValues[3]
                    val inlineStr = c.groupValues[4]

                    val textValue = when {
                        type == "s" -> {
                            val idx = value.toIntOrNull() ?: -1
                            sharedStrings.getOrNull(idx) ?: ""
                        }
                        inlineStr.isNotBlank() -> unescapeXml(inlineStr)
                        type == "inlineStr" || type == "str" -> unescapeXml(value)
                        else -> unescapeXml(value)
                    }
                    rowCells.add(textValue.trim())
                }
                if (rowCells.any { it.isNotBlank() }) {
                    rows.add(rowCells)
                }
            }
        }
        return rows
    }

    private fun unescapeXml(input: String): String {
        return input.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }

    /**
     * Splits a delimited line supporting comma, tab, and semicolon with quoted string support.
     */
    private fun splitDelimitedLine(line: String): List<String> {
        val delimiter = when {
            line.contains("\t") -> '\t'
            line.contains(";") && !line.contains(",") -> ';'
            else -> ','
        }

        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            val ch = line[i]
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Maps raw table (rows & columns) into structured ParsedVocabRow objects.
     * Detects headers and automatically enriches missing phonetic/syllable/tip data.
     */
    private fun mapTableToVocabRows(table: List<List<String>>): List<ParsedVocabRow> {
        if (table.isEmpty()) return emptyList()

        val firstRow = table.first()
        val hasHeader = isHeaderRow(firstRow)

        var wordCol = 0
        var assameseCol = 1
        var transliterationCol = 2
        var ipaCol = -1
        var syllablesCol = -1
        var posCol = -1
        var catCol = -1
        var exampleCol = -1
        var tipCol = -1

        val dataRows = if (hasHeader) {
            // Map header column names
            firstRow.forEachIndexed { idx, rawHeader ->
                val h = rawHeader.lowercase().trim()
                when {
                    h.contains("tip") || h.contains("coach") -> tipCol = idx
                    h.contains("transliteration") || h.contains("roman") || h.contains("উচ্চাৰণ") || (h.contains("pronunciation") && !h.contains("tip")) -> transliterationCol = idx
                    h.contains("ipa") || h.contains("phonetic") -> ipaCol = idx
                    h.contains("syllable") -> syllablesCol = idx
                    h.contains("part of speech") || h.contains("pos") -> posCol = idx
                    h.contains("category") || h.contains("topic") -> catCol = idx
                    h.contains("example") || h.contains("sentence") -> exampleCol = idx
                    h.contains("meaning") || h.contains("অসমীয়া") || h.contains("অর্থ") || h.contains("অৰ্থ") || h.contains("definition") || (h.contains("assamese") && !h.contains("transliterat")) -> assameseCol = idx
                    h.contains("word") || h.contains("term") || h.contains("ইংৰাজী") || (h.contains("english") && !h.contains("meaning")) -> wordCol = idx
                }
            }
            table.drop(1)
        } else {
            table
        }

        val result = mutableListOf<ParsedVocabRow>()

        for (cells in dataRows) {
            val eng = cells.getOrNull(wordCol)?.trim() ?: ""
            val asm = cells.getOrNull(assameseCol)?.trim() ?: ""

            if (eng.isNotBlank() && !isHeaderRow(cells)) {
                val cleanWord = eng.replaceFirstChar { it.uppercase() }
                val analysis = PhoneticsAiEngine.analyzeWord(cleanWord)

                val trans = cells.getOrNull(transliterationCol)?.trim()
                    ?.ifBlank { analysis.assameseTransliteration } ?: analysis.assameseTransliteration

                val finalAsm = if (asm.isNotBlank()) asm else analysis.assameseMeaning

                val ipa = if (ipaCol >= 0 && ipaCol < cells.size && cells[ipaCol].isNotBlank()) {
                    cells[ipaCol].trim()
                } else analysis.ipa

                val syl = if (syllablesCol >= 0 && syllablesCol < cells.size && cells[syllablesCol].isNotBlank()) {
                    cells[syllablesCol].trim()
                } else analysis.syllables

                val pos = if (posCol >= 0 && posCol < cells.size && cells[posCol].isNotBlank()) {
                    cells[posCol].trim()
                } else "noun"

                val cat = if (catCol >= 0 && catCol < cells.size && cells[catCol].isNotBlank()) {
                    cells[catCol].trim()
                } else "General"

                val ex = if (exampleCol >= 0 && exampleCol < cells.size && cells[exampleCol].isNotBlank()) {
                    cells[exampleCol].trim()
                } else analysis.exampleSentence

                val tip = if (tipCol >= 0 && tipCol < cells.size && cells[tipCol].isNotBlank()) {
                    cells[tipCol].trim()
                } else analysis.coachingTip

                result.add(
                    ParsedVocabRow(
                        englishWord = cleanWord,
                        assameseMeaning = finalAsm.ifBlank { "অৰ্থ" },
                        assameseTransliteration = trans,
                        ipa = ipa,
                        syllables = syl,
                        partOfSpeech = pos,
                        category = cat,
                        exampleSentence = ex,
                        coachingTip = tip
                    )
                )
            }
        }

        return result
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        if (row.isEmpty()) return false
        val firstCell = row.firstOrNull()?.lowercase()?.trim() ?: ""
        return firstCell == "english" || firstCell == "word" || firstCell == "english word" ||
                firstCell == "vocabulary" || firstCell == "term" || firstCell.startsWith("english ") ||
                firstCell == "sl" || firstCell == "no" || firstCell == "#" || firstCell == "id"
    }

    /**
     * Pre-formatted CSV sample for teachers to copy or test import immediately.
     */
    fun getSampleExcelTemplateCsv(): String {
        return """
English Word,Assamese Meaning,Assamese Transliteration,IPA Phonetic,Part of Speech,Category,Example Sentence,Coaching Tip
Confidence,আত্মবিশ্বাস,Aatmabixwaas,/ˈkɒnfɪdəns/,noun,Mindset,"Speaking English daily builds immense confidence.","Stress the first syllable: CON-fi-dence."
Opportunity,সুযোগ,Xujog,/ˌɒpəˈtjuːnɪti/,noun,Career,"Every conversation is a learning opportunity.","Light stress on 'OP', main stress on 'TUNE'."
Knowledge,জ্ঞান,Gyaan,/ˈnɒlɪdʒ/,noun,Education,"Practical speaking knowledge exceeds textbook rules.","The 'k' is completely silent; pronounce like 'NOL-idge'."
Experience,অভিজ্ঞতা,Obhigyata,/ɪkˈspɪəriəns/,noun,Professional,"Sharing your daily experience accelerates fluency.","Say: ik-SPEER-ee-unce."
Perseverance,অধ্যৱসায়,Odhyobosaay,/ˌpɜːsɪˈvɪərəns/,noun,Mindset,"Perseverance is the secret to mastering spoken English.","Glide through the middle: pur-suh-VEER-unce."
Curiosity,কৌতুহল,Koutuhol,/ˌkjʊəriˈɒsɪti/,noun,Learning,"Curiosity drives rapid English vocabulary growth.","Say: cure-ee-OSS-i-tee."
Achievement,কৃতকাৰ্যতা,Kritokaaryata,/əˈtʃiːvmənt/,noun,Success,"Fluency in English is a major personal achievement.","Notice the long 'ee' sound in 'chieve'."
Inspiration,অনুপ্ৰেৰণা,Onuprerona,/ˌɪnspɪˈreɪʃn/,noun,General,"Listening to inspiring speeches motivates speaking practice.","Accent the third syllable: in-spuh-RAY-shun."
Dedication,উৎসৰ্গা,Utxorga,/ˌdedɪˈkeɪʃn/,noun,Academic,"Her dedication to daily speaking practice yielded great results.","Stress 'KAY': ded-ih-KAY-shun."
Collaboration,সহযোগিতা,Xohayogita,/kəˌlæbəˈreɪʃn/,noun,Workplace,"Classroom collaboration helps students overcome shyness.","Say: kuh-lab-uh-RAY-shun."
        """.trimIndent()
    }
}
