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

    @Test
    fun testExampleAnswerGeneratorMatchesQuestionAndTopic() {
        val introTopic = com.example.data.model.SpeakingPrompt(
            id = "topic_intro",
            title = "Introduction",
            category = "Greetings",
            question = "Hello! How are you?",
            followUpTip = "Greet politely and share how you feel.",
            recommendedKeywords = listOf("hello", "doing great", "thank you")
        )
        val introExamples = com.example.util.ExampleAnswerGenerator.generateForQuestion(
            "Hi! I'm Nova 😊\nLet's practice “Introduction”.\n\n🕒 Hello! How are you?",
            introTopic
        )
        assertEquals(2, introExamples.size)
        assertFalse(com.example.util.ExampleAnswerGenerator.isLegacyGenericExampleSet(introExamples))
        assertTrue(introExamples.any { it.contains("doing great", ignoreCase = true) || it.contains("doing really well", ignoreCase = true) })

        // Verify legacy generic placeholders are caught for cached sessions
        val legacy = listOf(
            "I usually start my day at 7:30 in the morning on weekdays.",
            "Speaking about this, my schedule starts early and stays busy."
        )
        assertTrue(com.example.util.ExampleAnswerGenerator.isLegacyGenericExampleSet(legacy))

        // Custom topic uses its own title and keywords
        val customTopic = com.example.data.model.SpeakingPrompt(
            id = "topic_cricket",
            title = "School Sports",
            category = "Sports",
            question = "Which outdoor game do you enjoy playing at school?",
            followUpTip = "Talk about teamwork.",
            recommendedKeywords = listOf("teamwork", "discipline", "fitness")
        )
        val customExamples = com.example.util.ExampleAnswerGenerator.generateForQuestion(
            customTopic.question,
            customTopic
        )
        assertEquals(2, customExamples.size)
        assertTrue(customExamples.any { it.contains("School Sports") && it.contains("teamwork") })

        // Follow-up question on Introduction topic must NOT return greeting examples
        val turn4Question = "Who is your favorite historical person to read about?"
        val curatedTurn4 = com.example.util.ExampleAnswerGenerator.matchCuratedExamples(
            turn4Question,
            introTopic
        )
        assertNotNull(curatedTurn4)
        assertFalse(com.example.util.ExampleAnswerGenerator.isGreetingExampleSet(curatedTurn4!!))
        assertTrue(curatedTurn4.any { it.contains("Kalam", ignoreCase = true) || it.contains("Gandhi", ignoreCase = true) })

        val dynamicBookExamples = com.example.util.ExampleAnswerGenerator.generateForQuestion(
            "What is your favorite book to read on weekends?",
            introTopic
        )
        assertEquals(2, dynamicBookExamples.size)
        assertFalse(com.example.util.ExampleAnswerGenerator.isGreetingExampleSet(dynamicBookExamples))
        assertTrue(dynamicBookExamples.any { it.contains("book", ignoreCase = true) })
    }

    @Test
    fun testSentenceBuilderBulkExcelAndCsvImport() {
        // 1. Sample template & quoted CSV parsing
        val sampleCsv = com.example.util.ExcelWordSheetParser.getSampleSentenceBuilderCsv()
        val parsedSample = com.example.util.ExcelWordSheetParser.parseSentenceBuildersFromText(sampleCsv)
        assertEquals(10, parsedSample.size)
        assertEquals("She speaks English fluently every day.", parsedSample.first().targetSentence)
        assertEquals("Present Simple • Daily Habit", parsedSample.first().hint)
        assertTrue(parsedSample.first().assameseTranslation.isNotBlank())

        // 2. Quoted comma sentence + missing hint falls back to defaultHint
        val customCsv = """
            Target Sentence,Grammar Hint,Assamese Translation
            "Yes, I practice speaking every morning.",Daily Routine,হয় মই প্ৰতিদিনে ৰাতিপুৱা কথা কোৱা অভ্যাস কৰোঁ।
            "Honestly, fluency takes patience.",,
        """.trimIndent()
        val parsedCustom = com.example.util.ExcelWordSheetParser.parseSentenceBuildersFromText(
            rawText = customCsv,
            defaultHint = "Spoken Practice"
        )
        assertEquals(2, parsedCustom.size)
        assertEquals("Yes, I practice speaking every morning.", parsedCustom[0].targetSentence)
        assertEquals("Daily Routine", parsedCustom[0].hint)
        assertEquals("Honestly, fluency takes patience.", parsedCustom[1].targetSentence)
        assertEquals("Spoken Practice", parsedCustom[1].hint)
        assertEquals("", parsedCustom[1].assameseTranslation)

        // 3. Real .xlsx ZIP stream with sparse columns (row 2 omits column B hint -> keeps column C Assamese aligned!)
        val baos = java.io.ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(java.util.zip.ZipEntry("xl/sharedStrings.xml"))
            val sharedXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="5" uniqueCount="5">
                  <si><t>Target Sentence</t></si>
                  <si><t>Grammar Hint</t></si>
                  <si><t>Assamese Translation</t></si>
                  <si><t>They arrived early today.</t></si>
                  <si><t>তেওঁলোক আজি সোনকালে আহি পালে।</t></si>
                </sst>
            """.trimIndent()
            zip.write(sharedXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"))
            val sheetXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <sheetData>
                    <row r="1">
                      <c r="A1" t="s"><v>0</v></c>
                      <c r="B1" t="s"><v>1</v></c>
                      <c r="C1" t="s"><v>2</v></c>
                    </row>
                    <row r="2">
                      <c r="A2" t="s"><v>3</v></c>
                      <c r="C2" t="s"><v>4</v></c>
                    </row>
                  </sheetData>
                </worksheet>
            """.trimIndent()
            zip.write(sheetXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        val xlsxRows = com.example.util.ExcelWordSheetParser.parseSentenceBuildersFromXlsxStream(
            inputStream = baos.toByteArray().inputStream(),
            defaultHint = "Past Simple"
        )
        assertEquals(1, xlsxRows.size)
        assertEquals("They arrived early today.", xlsxRows[0].targetSentence)
        assertEquals("Past Simple", xlsxRows[0].hint)
        assertEquals("তেওঁলোক আজি সোনকালে আহি পালে।", xlsxRows[0].assameseTranslation)

        // 4. Repository bulk import persists all rows
        val repository = LinguaQuestRepository(null)
        val initialCount = repository.sentenceBuilderChallenges.value.size
        val imported = repository.bulkImportSentenceBuilders(
            rows = xlsxRows.map { Triple(it.targetSentence, it.hint, it.assameseTranslation) } +
                parsedCustom.map { Triple(it.targetSentence, it.hint, it.assameseTranslation) },
            createdBy = "Prof. Eleanor Vance"
        )
        assertEquals(3, imported)
        assertEquals(initialCount + 3, repository.sentenceBuilderChallenges.value.size)
        assertTrue(repository.sentenceBuilderChallenges.value.any { it.targetSentence == "They arrived early today." && it.createdBy == "Prof. Eleanor Vance" })
    }

    @Test
    fun testSentenceBuilderPacksCrudMultiSentenceAndRemoteSync() {
        val repository = LinguaQuestRepository(null)

        // 1. Default Easy / Medium / Hard challenge packs each contain starter sentences
        val packs = repository.sentenceBuilderPacks.value
        assertTrue(packs.any { it.id == "sb_pack_easy" && it.title == "Easy Challenge" })
        assertTrue(packs.any { it.id == "sb_pack_medium" && it.title == "Medium Challenge" })
        assertTrue(packs.any { it.id == "sb_pack_hard" && it.title == "Hard Challenge" })
        assertTrue(repository.sentenceBuilderChallenges.value.any { it.packId == "sb_pack_easy" })
        assertTrue(repository.sentenceBuilderChallenges.value.any { it.packId == "sb_pack_medium" })
        assertTrue(repository.sentenceBuilderChallenges.value.any { it.packId == "sb_pack_hard" })

        // 2. Teacher creates a custom challenge pack and edits it
        val createdPack = repository.addSentenceBuilderPack(
            title = "Daily Verbs Pack",
            description = "Practice everyday verb phrases",
            difficulty = "Medium",
            xpReward = 75,
            createdBy = "Prof. Eleanor Vance"
        )
        assertTrue(repository.sentenceBuilderPacks.value.any { it.id == createdPack.id && it.title == "Daily Verbs Pack" })

        repository.updateSentenceBuilderPack(createdPack.copy(title = "Daily Verbs Mastery", xpReward = 80))
        val updatedPack = repository.sentenceBuilderPacks.value.first { it.id == createdPack.id }
        assertEquals("Daily Verbs Mastery", updatedPack.title)
        assertEquals(80, updatedPack.xpReward)

        // 3. Teacher adds many sentences inside that single challenge pack (single + bulk)
        val s1 = repository.addSentenceBuilderChallenge("She opens the window every morning", "Present Simple", "", "Prof. Eleanor Vance", packId = createdPack.id)
        val bulkCount = repository.bulkImportSentenceBuilders(
            rows = listOf(
                Triple("They practice reading together after school", "Daily routine", ""),
                Triple("We always listen carefully in class", "Adverbs of frequency", "")
            ),
            createdBy = "Prof. Eleanor Vance",
            packId = createdPack.id
        )
        assertEquals(2, bulkCount)
        val sentencesInPack = repository.sentenceBuilderChallenges.value.filter { it.packId == createdPack.id }
        assertEquals(3, sentencesInPack.size)
        assertTrue(sentencesInPack.any { it.id == s1.id })

        // 4. Remote merge syncs both SENTENCE_BUILDER_PACK and scoped SENTENCE_BUILDER items
        repository.mergeRemoteGameChallenges(
            listOf(
                com.example.data.model.SupabaseGameChallenge(
                    id = "sb_pack_remote_1",
                    type = "SENTENCE_BUILDER_PACK",
                    titleOrWord = "Travel Phrases",
                    hintOrMeaning = "Build airport and hotel sentences",
                    category = "Hard",
                    correctIndex = 120,
                    createdBy = "Remote Teacher"
                ),
                com.example.data.model.SupabaseGameChallenge(
                    id = "sb_remote_s_1",
                    type = "SENTENCE_BUILDER",
                    titleOrWord = "Please show me your boarding pass.",
                    hintOrMeaning = "Polite request",
                    category = "sb_pack_remote_1",
                    extraData = "অনুগ্ৰহ কৰি আপোনাৰ বৰ্ডিং পাছ দেখুৱাওক।",
                    createdBy = "Remote Teacher"
                )
            )
        )
        assertTrue(repository.sentenceBuilderPacks.value.any { it.id == "sb_pack_remote_1" && it.xpReward == 120 })
        assertTrue(repository.sentenceBuilderChallenges.value.any { it.id == "sb_remote_s_1" && it.packId == "sb_pack_remote_1" })

        // 5. Deleting a challenge pack removes the pack and cascades to its sentences
        val removedIds = repository.deleteSentenceBuilderPack(createdPack.id)
        assertEquals(3, removedIds.size)
        assertFalse(repository.sentenceBuilderPacks.value.any { it.id == createdPack.id })
        assertTrue(repository.sentenceBuilderChallenges.value.none { it.packId == createdPack.id })
    }

    @Test
    fun testHumanConversationEngineTailorsFollowUpsToStudentAnswers() {
        val introTopic = com.example.data.model.SpeakingPrompt(
            id = "topic_intro",
            title = "Introduction",
            category = "Greetings",
            question = "Hello! How are you?",
            followUpTip = "Greet warmly and share how you feel.",
            recommendedKeywords = listOf("hello", "doing great", "thank you")
        )

        // 1. Human greeting preserves topic & question naturally
        val (greetingDisplay, greetingAudio) = com.example.service.HumanConversationEngine.buildHumanGreeting(
            topic = introTopic,
            studentName = "Arjun Das"
        )
        assertTrue(greetingDisplay.contains("Hey Arjun!"))
        assertTrue(greetingDisplay.contains("Introduction"))
        assertTrue(greetingDisplay.contains("Hello! How are you?"))
        assertTrue(greetingAudio.contains("Hey Arjun!"))

        // 2. Student answers mood AND asks Nova back ("I am fine, how about you?")
        val base1 = com.example.service.EvaluationEngine.evaluateSpeech("I am fine, how about you?")
        assertEquals("B2", base1.cefrLevel)
        val turn1Eval = com.example.service.HumanConversationEngine.generateReply(
            spokenText = "I am fine, how about you?",
            topic = introTopic,
            previousMessages = emptyList(),
            lastNovaQuestion = "Hello! How are you?",
            baseEval = base1
        )
        val reply1 = turn1Eval.aiCoachResponse.orEmpty()
        assertFalse(reply1.contains("What is another detail you would like to share", ignoreCase = true))
        assertTrue(reply1.contains("thanks so much for asking", ignoreCase = true) || reply1.contains("doing great", ignoreCase = true))
        assertFalse(turn1Eval.followUpQuestion.isNullOrBlank())

        // 3. Student shares name ("My name is Rahat") -> Nova greets Rahat by name
        val base2 = com.example.service.EvaluationEngine.evaluateSpeech("My name is Rahat")
        val turn2Eval = com.example.service.HumanConversationEngine.generateReply(
            spokenText = "My name is Rahat",
            topic = introTopic,
            previousMessages = listOf(
                com.example.data.model.NovaChatMessage(
                    isNova = true,
                    text = reply1,
                    evaluation = turn1Eval
                )
            ),
            lastNovaQuestion = turn1Eval.followUpQuestion.orEmpty(),
            baseEval = base2
        )
        assertTrue(turn2Eval.aiCoachResponse.orEmpty().contains("Rahat"))
        assertTrue(turn2Eval.followUpQuestion.orEmpty().contains("Rahat"))

        // 4. Student shares hobby ("I love playing cricket with my friends") -> cricket-specific follow-up
        val base3 = com.example.service.EvaluationEngine.evaluateSpeech("I love playing cricket with my friends")
        val turn3Eval = com.example.service.HumanConversationEngine.generateReply(
            spokenText = "I love playing cricket with my friends",
            topic = introTopic,
            previousMessages = emptyList(),
            lastNovaQuestion = "So tell me, what do you usually enjoy doing in your free time?",
            baseEval = base3
        )
        assertTrue(turn3Eval.aiCoachResponse.orEmpty().contains("cricket", ignoreCase = true))
        assertTrue(turn3Eval.followUpQuestion.orEmpty().contains("cricket", ignoreCase = true))

        // 5. Anti-repetition: repeating a cricket input does NOT repeat turn3Eval.followUpQuestion
        val turn3RepeatEval = com.example.service.HumanConversationEngine.generateReply(
            spokenText = "Yes I play cricket every Sunday too",
            topic = introTopic,
            previousMessages = listOf(
                com.example.data.model.NovaChatMessage(
                    isNova = true,
                    text = turn3Eval.aiCoachResponse.orEmpty(),
                    evaluation = turn3Eval
                )
            ),
            lastNovaQuestion = turn3Eval.followUpQuestion.orEmpty(),
            baseEval = base3
        )
        assertNotEquals(turn3Eval.followUpQuestion, turn3RepeatEval.followUpQuestion)
    }

    @Test
    fun testGrammarChallengePacksCrudMultiQuestionAndRemoteSync() {
        val repository = LinguaQuestRepository(null)

        // 1. Default 4 challenge packs matching screenshot each have 10 starter questions
        val packs = repository.grammarChallengePacks.value
        assertEquals(4, packs.size)
        assertTrue(packs.any { it.id == "gc_pack_tenses" && it.title == "Tenses Challenge" })
        assertTrue(packs.any { it.id == "gc_pack_narration" && it.title == "Narration Challenge" })
        assertTrue(packs.any { it.id == "gc_pack_determiners" && it.title == "Determiners Challenge" })
        assertTrue(packs.any { it.id == "gc_pack_prepositions" && it.title == "Prepositions Challenge" })

        packs.forEach { pack ->
            val packQuestions = repository.grammarChallenges.value.filter { it.packId == pack.id }
            assertEquals("Expected 10 starter questions for ${pack.title}", 10, packQuestions.size)
        }

        // 2. Teacher creates a custom Grammar Challenge pack and edits it
        val createdPack = repository.addGrammarChallengePack(
            title = "Voice Challenge",
            description = "Practice Active and Passive Voice.",
            themeStyle = "Narration",
            xpReward = 60,
            createdBy = "Prof. Eleanor Vance"
        )
        assertTrue(repository.grammarChallengePacks.value.any { it.id == createdPack.id && it.title == "Voice Challenge" })

        repository.updateGrammarChallengePack(createdPack.copy(title = "Active & Passive Voice", xpReward = 75))
        val updatedPack = repository.grammarChallengePacks.value.first { it.id == createdPack.id }
        assertEquals("Active & Passive Voice", updatedPack.title)
        assertEquals(75, updatedPack.xpReward)

        // 3. Teacher adds many questions inside that single challenge pack
        val q1 = repository.addGrammarChallenge(
            question = "The letter ___ by John yesterday.",
            options = listOf("wrote", "was written", "is writing", "writes"),
            correctIndex = 1,
            explanation = "Use past simple passive 'was + past participle'.",
            category = updatedPack.themeStyle,
            createdBy = "Prof. Eleanor Vance",
            packId = createdPack.id
        )
        val q2 = repository.addGrammarChallenge(
            question = "A new bridge ___ right now.",
            options = listOf("is built", "was built", "is being built", "builds"),
            correctIndex = 2,
            explanation = "Present continuous passive uses 'is being + past participle'.",
            category = updatedPack.themeStyle,
            createdBy = "Prof. Eleanor Vance",
            packId = createdPack.id
        )
        val questionsInPack = repository.grammarChallenges.value.filter { it.packId == createdPack.id }
        assertEquals(2, questionsInPack.size)
        assertEquals(1, questionsInPack.first { it.id == q1.id }.correctIndex)
        assertEquals(2, questionsInPack.first { it.id == q2.id }.correctIndex)

        // 4. Remote merge syncs GRAMMAR_CHALLENGE_PACK and scoped GRAMMAR_CHALLENGE items
        repository.mergeRemoteGameChallenges(
            listOf(
                com.example.data.model.SupabaseGameChallenge(
                    id = "gc_pack_remote_1",
                    type = "GRAMMAR_CHALLENGE_PACK",
                    titleOrWord = "Modals Challenge",
                    hintOrMeaning = "Practice can, could, may and might.",
                    category = "Determiners",
                    correctIndex = 50,
                    createdBy = "Remote Teacher"
                ),
                com.example.data.model.SupabaseGameChallenge(
                    id = "gc_remote_q_1",
                    type = "GRAMMAR_CHALLENGE",
                    titleOrWord = "You ___ wear a helmet while riding.",
                    hintOrMeaning = "'Must' expresses strong obligation.",
                    category = "Modals",
                    options = listOf("can", "must", "might", "could"),
                    correctIndex = 1,
                    extraData = "gc_pack_remote_1",
                    createdBy = "Remote Teacher"
                )
            )
        )
        assertTrue(repository.grammarChallengePacks.value.any { it.id == "gc_pack_remote_1" && it.xpReward == 50 })
        val mergedQuestion = repository.grammarChallenges.value.firstOrNull { it.id == "gc_remote_q_1" }
        assertNotNull(mergedQuestion)
        assertEquals("gc_pack_remote_1", mergedQuestion?.packId)
        assertEquals(1, mergedQuestion?.correctIndex)

        // 5. Deleting a challenge pack removes the pack and cascades to its questions
        val removedIds = repository.deleteGrammarChallengePack(createdPack.id)
        assertEquals(2, removedIds.size)
        assertFalse(repository.grammarChallengePacks.value.any { it.id == createdPack.id })
        assertTrue(repository.grammarChallenges.value.none { it.packId == createdPack.id })
    }
}

