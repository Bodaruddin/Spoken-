package com.example

import com.example.data.model.Role
import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAdminSwitchToTeacherAndBackWithoutPin() {
        val repository = LinguaQuestRepository(null)
        // Set user to Admin
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)

        // Admin switches to Teacher
        repository.switchRole(Role.TEACHER)
        assertEquals(Role.TEACHER, repository.currentUser.value.role)
        // Admin session must remain active!
        assertTrue(repository.isAdminSession.value)

        // Can switch back to Admin without PIN
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)
    }

    @Test
    fun testAdminSwitchToStudentAndBackWithoutPin() {
        val repository = LinguaQuestRepository(null)
        // Set user to Admin
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)

        // Admin switches to Student
        repository.switchRole(Role.STUDENT)
        assertEquals(Role.STUDENT, repository.currentUser.value.role)
        // Admin session must remain active!
        assertTrue(repository.isAdminSession.value)

        // Can switch back to Admin directly!
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)
    }

    @Test
    fun testMcqExamCreationAndGradingWithCertificate() {
        val repository = LinguaQuestRepository(null)
        val initialExams = repository.exams.value
        assertTrue(initialExams.isNotEmpty())

        val testQuestions = listOf(
            com.example.data.model.ExamQuestion("q1", "What is the capital of UK?", listOf("Paris", "London", "Berlin", "Rome"), 1, 10),
            com.example.data.model.ExamQuestion("q2", "Identify the noun:", listOf("Run", "Happiness", "Quickly", "Very"), 1, 10)
        )

        val createdExam = repository.createExam(
            title = "Unit Test English Assessment",
            description = "Quick assessment",
            teacherName = "Prof. Test",
            targetClass = "Class 10",
            subjectCategory = "English Grammar",
            durationMinutes = 10,
            passingPercentage = 60,
            questions = testQuestions
        )

        assertEquals("Unit Test English Assessment", createdExam.title)
        assertEquals(20, createdExam.totalMarks)
        assertEquals(12, createdExam.passingMarks)

        // Submit exam with 1 correct out of 2 (10/20 = 50% -> Fail if pass is 60%)
        val subFail = repository.submitExam(
            examId = createdExam.id,
            studentId = "stu_1",
            studentName = "Test Student",
            studentClass = "Class 10",
            studentAvatar = "🎓",
            selectedAnswers = mapOf("q1" to 1, "q2" to 0), // q1 correct (1), q2 wrong (0 instead of 1)
            timeTakenSeconds = 120
        )
        assertEquals(10, subFail.scoreObtained)
        assertEquals(50, subFail.percentage)
        assertFalse(subFail.isPassed)

        // Submit exam with 2 correct out of 2 (20/20 = 100% -> Grade O Passed)
        val subPass = repository.submitExam(
            examId = createdExam.id,
            studentId = "stu_1",
            studentName = "Test Student",
            studentClass = "Class 10",
            studentAvatar = "🎓",
            selectedAnswers = mapOf("q1" to 1, "q2" to 1),
            timeTakenSeconds = 150
        )
        assertEquals(20, subPass.scoreObtained)
        assertEquals(100, subPass.percentage)
        assertTrue(subPass.isPassed)
        assertTrue(subPass.certificateId.startsWith("SPK-2026-"))
    }

    @Test
    fun testFarihaYesminCertificateFromScreenshotDetails() {
        val repository = LinguaQuestRepository(null)
        val submissions = repository.examSubmissions.value
        val farihaCert = submissions.firstOrNull { it.studentName == "Fariha Yesmin" }

        assertNotNull(farihaCert)
        farihaCert?.let { cert ->
            assertEquals("Fariha Yesmin", cert.studentName)
            assertEquals("English Grammar Test", cert.examTitle)
            assertEquals("VIII", cert.studentClass)
            assertEquals(100, cert.totalMarks)
            assertEquals(88, cert.scoreObtained)
            assertEquals(88, cert.percentage)
            assertEquals("A", cert.grade)
            assertEquals("Excellent Performance", cert.performanceTitle)
            assertEquals("27 Nov 2026", cert.completedAt)
            assertEquals("SPK-2026-000368", cert.certificateId)
            assertTrue(cert.isPassed)
        }
    }

    @Test
    fun testMissingLettersAndSentenceBuilderRemoteMergeAndPersistence() {
        val repository = LinguaQuestRepository(null)

        // Test remote merge of custom Missing Letters
        val remoteML = listOf(
            com.example.data.model.SupabaseGameChallenge(
                id = "ml_test_101",
                type = "MISSING_LETTERS",
                titleOrWord = "ELEPHANT",
                hintOrMeaning = "A large mammal with tusks",
                category = "Wildlife",
                extraData = "1,3,5",
                createdBy = "Teacher Test"
            )
        )
        repository.mergeRemoteGameChallenges(remoteML)
        val mlList = repository.missingLettersChallenges.value
        val mergedMl = mlList.find { it.id == "ml_test_101" }
        assertNotNull(mergedMl)
        assertEquals("ELEPHANT", mergedMl?.fullWord)
        assertEquals("Wildlife", mergedMl?.category)
        assertEquals(listOf(1, 3, 5), mergedMl?.missingIndices)
        // Verify custom category was registered
        assertTrue(repository.missingLettersCustomCategories.value.contains("Wildlife"))

        // Test update of Missing Letters
        val updatedMl = mergedMl!!.copy(hint = "Updated hint for elephant")
        repository.updateMissingLettersChallenge(updatedMl)
        val refreshedMl = repository.missingLettersChallenges.value.find { it.id == "ml_test_101" }
        assertEquals("Updated hint for elephant", refreshedMl?.hint)

        // Test remote merge of Sentence Builder
        val remoteSB = listOf(
            com.example.data.model.SupabaseGameChallenge(
                id = "sb_test_202",
                type = "SENTENCE_BUILDER",
                titleOrWord = "She sings beautifully in the choir.",
                hintOrMeaning = "Subject + Verb + Adverbial",
                category = "Syntax",
                extraData = "তাই গীতগায়",
                createdBy = "Teacher Test"
            )
        )
        repository.mergeRemoteGameChallenges(remoteSB)
        val sbList = repository.sentenceBuilderChallenges.value
        val mergedSb = sbList.find { it.id == "sb_test_202" }
        assertNotNull(mergedSb)
        assertEquals("She sings beautifully in the choir.", mergedSb?.targetSentence)
        assertEquals("Subject + Verb + Adverbial", mergedSb?.hint)
        assertEquals("তাই গীতগায়", mergedSb?.assameseTranslation)

        // Test update of Sentence Builder
        val updatedSb = mergedSb!!.copy(targetSentence = "She sings wonderfully in the choir.")
        repository.updateSentenceBuilderChallenge(updatedSb)
        val refreshedSb = repository.sentenceBuilderChallenges.value.find { it.id == "sb_test_202" }
        assertEquals("She sings wonderfully in the choir.", refreshedSb?.targetSentence)
    }

    @Test
    fun testExamsAndSubmissionsRemoteMerge() {
        val repository = LinguaQuestRepository(null)

        val remoteExam = listOf(
            com.example.data.model.Exam(
                id = "exam_cloud_303",
                title = "Midterm Vocabulary Test",
                description = "Comprehensive vocab evaluation",
                teacherName = "Prof. Smith",
                targetClass = "Class 9",
                subjectCategory = "Vocabulary",
                durationMinutes = 20,
                totalMarks = 50,
                passingPercentage = 60,
                questions = listOf(
                    com.example.data.model.ExamQuestion("eq1", "Synonym of Benevolent?", listOf("Kind", "Cruel", "Fast", "Weak"), 0, 10)
                ),
                createdAt = "Nov 2026",
                dueDate = "Next Week",
                isPublished = true
            )
        )
        repository.mergeRemoteExams(remoteExam)
        val exams = repository.exams.value
        val merged = exams.find { it.id == "exam_cloud_303" }
        assertNotNull(merged)
        assertEquals("Midterm Vocabulary Test", merged?.title)

        val remoteSubmissions = listOf(
            com.example.data.model.ExamSubmission(
                id = "sub_cloud_404",
                examId = "exam_cloud_303",
                examTitle = "Midterm Vocabulary Test",
                studentId = "stu_99",
                studentName = "Rahul Barman",
                studentClass = "Class 9",
                studentAvatar = "👦",
                scoreObtained = 45,
                totalMarks = 50,
                percentage = 90,
                grade = "O",
                performanceTitle = "Outstanding",
                timeTakenSeconds = 750,
                completedAt = "Nov 2026",
                certificateId = "SPK-2026-000999",
                isPassed = true
            )
        )
        repository.mergeRemoteExamSubmissions(remoteSubmissions)
        val submissions = repository.examSubmissions.value
        val mergedSub = submissions.find { it.id == "sub_cloud_404" }
        assertNotNull(mergedSub)
        assertEquals("Rahul Barman", mergedSub?.studentName)
        assertEquals(90, mergedSub?.percentage)
        assertTrue(mergedSub?.isPassed == true)
    }

    @Test
    fun testAssameseTranslatorGreetingsAndNoShortSubstringCollision() {
        val introGreeting = com.example.util.AssameseTranslator.translate(
            "Hi! I'm Nova 😊\nLet's practice “Introduction”.\n\n🕒 Hello! How are you?"
        )
        assertEquals("নমস্কাৰ! আপুনি কেনে আছে?", introGreeting.assameseText)

        // Ensure "hi" inside "this" does not trigger the "hi" -> "নমস্কাৰ!" curated entry
        val thisSentence = com.example.util.AssameseTranslator.translate("What do you think about this?")
        assertNotEquals("নমস্কাৰ!", thisSentence.assameseText)
        assertTrue(thisSentence.assameseText.contains("ভবা") || thisSentence.assameseText.contains("এই"))

        // Multi-sentence Nova reply extracts and translates the follow-up question cleanly
        val followUp = com.example.util.AssameseTranslator.translate(
            "Great job! That was spoken with natural clarity. What is another detail you would like to share?"
        )
        assertEquals("এই বিষয়ে আপুনি আন কিবা কথা ক'ব বিচাৰিবনে?", followUp.assameseText)
    }

    @Test
    fun testSpeakingPromptMilestoneTurnsCreationAndClamping() {
        val repository = LinguaQuestRepository(null)
        val initialSize = repository.speakingPrompts.value.size

        // Custom milestone prompt
        val customPrompt = repository.addSpeakingPrompt(
            title = "Custom Debate",
            category = "Debate",
            question = "What is your opinion on remote learning?",
            followUpTip = "Give two reasons.",
            recommendedKeywords = listOf("online", "flexible"),
            minSpeakingSeconds = 30,
            emoji = "🎤",
            difficultyLevel = "Intermediate",
            milestoneTurns = 12
        )
        val prompts = repository.speakingPrompts.value
        assertEquals(initialSize + 1, prompts.size)
        val created = prompts.firstOrNull { it.title == "Custom Debate" }
        assertNotNull(created)
        assertEquals(12, created?.milestoneTurns)

        // Update milestone target down (teacher decreasing milestone for topic)
        repository.updateSpeakingPrompt(customPrompt.copy(milestoneTurns = 4))
        assertEquals(4, repository.speakingPrompts.value.firstOrNull { it.title == "Custom Debate" }?.milestoneTurns)

        // Clamped upper bound (max 30)
        repository.addSpeakingPrompt(
            title = "Extended Marathon",
            category = "Fluency",
            question = "Tell me about your childhood.",
            followUpTip = "Use past tense.",
            recommendedKeywords = listOf("school", "friends"),
            minSpeakingSeconds = 45,
            emoji = "🌟",
            difficultyLevel = "Advanced",
            milestoneTurns = 99
        )
        val clamped = repository.speakingPrompts.value.firstOrNull { it.title == "Extended Marathon" }
        assertEquals(30, clamped?.milestoneTurns)
    }
}

