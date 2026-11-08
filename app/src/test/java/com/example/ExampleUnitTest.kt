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
}
