package com.example

import com.example.service.EvaluationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEvaluationEngine_exactMatchSpecifically() {
        val eval = EvaluationEngine.evaluateSpeech(
            spokenText = "Specifically",
            targetPhrase = "Specifically"
        )
        assertNotNull(eval)
        assertTrue("Overall score should be high for exact target match", eval.overallScore >= 80)
        assertTrue("Pronunciation score should be high", eval.pronunciationScore >= 85)
        assertEquals("Specifically", eval.spokenText)
    }

    @Test
    fun testEvaluationEngine_comfortablePronunciationMatch() {
        val eval = EvaluationEngine.evaluateSpeech(
            spokenText = "Comfortable",
            targetPhrase = "Comfortable"
        )
        assertNotNull(eval)
        assertTrue(eval.overallScore >= 80)
    }

    @Test
    fun testEvaluationEngine_emptySpeechHandledSafely() {
        val eval = EvaluationEngine.evaluateSpeech(
            spokenText = "",
            targetPhrase = "Specifically"
        )
        assertEquals(0, eval.overallScore)
        assertTrue(eval.feedback.contains("No speech detected", ignoreCase = true))
    }

    @Test
    fun testExcelWordSheetParser_sampleTemplate() {
        val sampleCsv = com.example.util.ExcelWordSheetParser.getSampleExcelTemplateCsv()
        val parsedRows = com.example.util.ExcelWordSheetParser.parseRawText(sampleCsv)

        assertTrue("Should parse at least 5 words from sample template", parsedRows.size >= 5)
        val firstRow = parsedRows.firstOrNull { it.englishWord.equals("Confidence", ignoreCase = true) }
        assertNotNull("Should contain Confidence", firstRow)
        assertEquals("আত্মবিশ্বাস", firstRow?.assameseMeaning)
        assertTrue("IPA should be populated", firstRow?.ipa?.contains("kɒnfɪdəns") == true)
    }

    @Test
    fun testExcelWordSheetParser_simpleTwoColumns() {
        val input = """
            English, Assamese
            Brilliant, উজ্জ্বল
            Courage, সাহস
        """.trimIndent()
        val parsedRows = com.example.util.ExcelWordSheetParser.parseRawText(input)
        assertEquals(2, parsedRows.size)
        assertEquals("Brilliant", parsedRows[0].englishWord)
        assertEquals("উজ্জ্বল", parsedRows[0].assameseMeaning)
        assertEquals("Courage", parsedRows[1].englishWord)
        assertEquals("সাহস", parsedRows[1].assameseMeaning)
    }
}
