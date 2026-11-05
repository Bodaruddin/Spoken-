package com.example

import com.example.data.model.Role
import com.example.data.repository.LinguaQuestRepository
import com.example.service.EvaluationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `evaluation engine calculates accurate scores for speech`() {
        val target = "I would like a flat white with oat milk please"
        val spoken = "I would like a flat white with oat milk please"
        val result = EvaluationEngine.evaluateSpeech(spoken, target)

        assertTrue("Overall score should be >= 80 for exact speech match", result.overallScore >= 80)
        assertTrue("Pronunciation score should be >= 85", result.pronunciationScore >= 85)
        assertTrue("Fluency score should be >= 70", result.fluencyScore >= 70)
    }

    @Test
    fun `evaluation engine detects grammar issues in speech`() {
        val spoken = "i is very happy today"
        val result = EvaluationEngine.evaluateSpeech(spoken)

        assertTrue("Grammar score should penalize subject-verb disagreement", result.grammarScore < 95)
    }

    @Test
    fun `repository handles role switching and gamification`() {
        val repo = LinguaQuestRepository()
        assertEquals(Role.STUDENT, repo.currentUser.value.role)

        repo.switchRole(Role.TEACHER)
        assertEquals(Role.TEACHER, repo.currentUser.value.role)

        repo.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repo.currentUser.value.role)

        val initialXp = repo.studentProfile.value.xpPoints
        repo.addRewards(60, 25)
        assertEquals(initialXp + 60, repo.studentProfile.value.xpPoints)
    }

    @Test
    fun `repository manages curriculum stages progression`() {
        val repo = LinguaQuestRepository()
        val stageId = "w2_s4"
        repo.completeStage(stageId, 3, 50, 15)

        val updated = repo.stages.value.find { it.id == stageId }
        assertTrue("Stage should be marked completed", updated?.isCompleted == true)
        assertEquals(3, updated?.stars)
    }
}
