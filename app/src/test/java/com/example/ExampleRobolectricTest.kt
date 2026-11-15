package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppPreferences
import com.example.data.model.Exam
import com.example.data.model.ExamQuestion
import com.example.data.model.NovaChatMessage
import com.example.data.model.Role
import com.example.data.model.SpeakingPrompt
import com.example.data.model.VocabularyActivityType
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.screens.student.StudentHomeScreen
import com.example.ui.screens.student.VocabularyChallengeGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `verify application name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Speakly", appName)
  }

  @Test
  fun `renamed introduction topic invalidates stale time words session`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = AppPreferences(app)
    val vm = MainViewModel(app)
    val studentId = vm.currentUser.value.id.ifBlank { "default_student" }

    // Seed stale cached Time Words session under topic_time_words
    prefs.saveNovaSession(
      studentId,
      "topic_time_words",
      listOf(
        NovaChatMessage(
          id = "stale_1",
          isNova = true,
          text = "Hi! I'm Nova 😊\nLet's practice “Time Words (In, On, At)”.\n\n🕒 When do you usually go to school?",
          audioPrompt = "When do you usually go to school?",
          isQuestion = true,
          questionNumber = 1
        )
      )
    )

    val introTopic = SpeakingPrompt(
      id = "topic_time_words",
      title = "Introduction",
      category = "Greetings",
      question = "Hello! How are you?",
      followUpTip = "Greet politely and share how you feel.",
      recommendedKeywords = listOf("hello", "doing great", "thank you"),
      emoji = "🕒"
    )

    vm.selectConversationTopic(introTopic)
    val messages = vm.novaChatMessages.value
    assertEquals(1, messages.size)
    val bubbleText = messages.first().text
    assertTrue(bubbleText.contains("Introduction"))
    assertTrue(bubbleText.contains("Hello! How are you?"))
    assertFalse(bubbleText.contains("Time Words"))
    assertFalse(bubbleText.contains("When do you usually go to school?"))
    assertEquals("Hello! How are you?", vm.activeCoachQuestion.value)
  }

  @Test
  fun `exam time window accurately detects before start, open, and expired auto-hide states`() {
    val baseTime = 1_750_000_000_000L
    val scheduledExam = Exam(
      id = "exam_timed_1",
      title = "Timed Grammar Quiz",
      description = "Windowed test",
      teacherName = "Prof. Eleanor",
      availableFromMillis = baseTime + 60_000L,
      availableUntilMillis = baseTime + 300_000L
    )

    // Before start time
    assertTrue(scheduledExam.isBeforeStartAt(baseTime))
    assertFalse(scheduledExam.isExpiredAt(baseTime))
    assertFalse(scheduledExam.isOpenForStudentAt(baseTime))

    // During open window
    val insideWindow = baseTime + 120_000L
    assertFalse(scheduledExam.isBeforeStartAt(insideWindow))
    assertFalse(scheduledExam.isExpiredAt(insideWindow))
    assertTrue(scheduledExam.isOpenForStudentAt(insideWindow))

    // After end time -> automatically hidden from student panel
    val afterWindow = baseTime + 301_000L
    assertFalse(scheduledExam.isBeforeStartAt(afterWindow))
    assertTrue(scheduledExam.isExpiredAt(afterWindow))
    assertFalse(scheduledExam.isOpenForStudentAt(afterWindow))

    // Always open exam (0L bounds)
    val alwaysOpenExam = Exam(
      id = "exam_open_1",
      title = "Always Open Quiz",
      description = "No deadline",
      teacherName = "Prof. Eleanor"
    )
    assertFalse(alwaysOpenExam.hasTimeWindow)
    assertTrue(alwaysOpenExam.isOpenForStudentAt(afterWindow))
    assertFalse(alwaysOpenExam.isExpiredAt(afterWindow))
  }

  @Test
  fun `teacher updateExam updates exam fields, questions, and schedule window in place`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = AppPreferences(app)
    val vm = MainViewModel(app)

    val initialQ = ExamQuestion(
      id = "q_init_1",
      question = "She ___ to school daily.",
      options = listOf("go", "goes", "going", "gone"),
      correctOptionIndex = 1,
      marks = 10
    )

    val created = vm.createExam(
      title = "Initial Grammar Quiz",
      description = "Draft description",
      targetClass = "Class 8",
      subjectCategory = "English Grammar",
      durationMinutes = 15,
      passingPercentage = 60,
      questions = listOf(initialQ),
      availableFromMillis = 0L,
      availableUntilMillis = 0L
    )

    val startMillis = 1_800_000_000_000L
    val endMillis = startMillis + 1_800_000L
    val updatedQuestions = listOf(
      initialQ.copy(question = "He ___ to school every morning.", marks = 15),
      ExamQuestion(
        id = "q_added_2",
        question = "They ___ football yesterday.",
        options = listOf("play", "plays", "played", "playing"),
        correctOptionIndex = 2,
        marks = 15
      )
    )

    val updated = vm.updateExam(
      examId = created.id,
      title = "Updated Grammar Midterm",
      description = "Timed exam for Class 9",
      targetClass = "Class 9",
      subjectCategory = "English Grammar",
      durationMinutes = 30,
      passingPercentage = 70,
      questions = updatedQuestions,
      availableFromMillis = startMillis,
      availableUntilMillis = endMillis,
      dueDate = "Scheduled Window"
    )

    assertNotNull(updated)
    assertEquals(created.id, updated!!.id)
    assertEquals("Updated Grammar Midterm", updated.title)
    assertEquals("Class 9", updated.targetClass)
    assertEquals(30, updated.durationMinutes)
    assertEquals(70, updated.passingPercentage)
    assertEquals(30, updated.totalMarks)
    assertEquals(2, updated.questions.size)
    assertEquals(startMillis, updated.availableFromMillis)
    assertEquals(endMillis, updated.availableUntilMillis)

    val inState = vm.exams.value.firstOrNull { it.id == created.id }
    assertNotNull(inState)
    assertEquals("Updated Grammar Midterm", inState!!.title)
    assertEquals(endMillis, inState.availableUntilMillis)

    val persisted = prefs.getExams()?.firstOrNull { it.id == created.id }
    assertNotNull(persisted)
    assertEquals(startMillis, persisted!!.availableFromMillis)
    assertEquals(endMillis, persisted.availableUntilMillis)
  }

  @Test
  fun `unifiedLogin automatically detects Student, Faculty, and Admin roles from credentials`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MainViewModel(app)

    // Student credentials -> automatically detected as STUDENT
    val studentRes = vm.unifiedLogin("alex.morgan@linguaquest.edu", "1234")
    assertTrue(studentRes is MainViewModel.UnifiedLoginResult.Success)
    assertEquals(Role.STUDENT, (studentRes as MainViewModel.UnifiedLoginResult.Success).role)

    // Logout and re-login as Student -> must still detect STUDENT, not ADMIN
    vm.logout()
    val reloginStudent = vm.unifiedLogin("alex.morgan@linguaquest.edu", "1234")
    assertTrue(reloginStudent is MainViewModel.UnifiedLoginResult.Success)
    assertEquals(Role.STUDENT, (reloginStudent as MainViewModel.UnifiedLoginResult.Success).role)

    // Faculty credentials -> automatically detected as TEACHER
    vm.logout()
    val teacherRes = vm.unifiedLogin("e.vance@linguaquest.edu", "7321")
    assertTrue(teacherRes is MainViewModel.UnifiedLoginResult.Success)
    assertEquals(Role.TEACHER, (teacherRes as MainViewModel.UnifiedLoginResult.Success).role)

    // Admin credentials -> automatically detected as ADMIN
    vm.logout()
    val adminRes = vm.unifiedLogin("admin@linguaquest.edu", "1234")
    assertTrue(adminRes is MainViewModel.UnifiedLoginResult.Success)
    assertEquals(Role.ADMIN, (adminRes as MainViewModel.UnifiedLoginResult.Success).role)
  }

  @Test
  fun `grammar challenge packs and scoped questions persist across preferences`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = AppPreferences(app)
    val vm = MainViewModel(app)

    val newPack = vm.addGrammarChallengePack(
      title = "Conditionals Challenge",
      description = "Master Zero, First, and Second Conditionals.",
      themeStyle = "Tenses",
      xpReward = 55,
      createdBy = "Prof. Eleanor Vance"
    )
    vm.addGrammarChallenge(
      question = "If it rains tomorrow, we ___ at home.",
      options = listOf("stay", "will stay", "stayed", "staying"),
      correctIndex = 1,
      explanation = "First conditional uses 'will + base verb' in the result clause.",
      category = newPack.themeStyle,
      createdBy = "Prof. Eleanor Vance",
      packId = newPack.id
    )
    vm.addGrammarChallenge(
      question = "If I ___ rich, I would travel the world.",
      options = listOf("am", "was", "were", "will be"),
      correctIndex = 2,
      explanation = "Second conditional uses 'were' for hypothetical states.",
      category = newPack.themeStyle,
      createdBy = "Prof. Eleanor Vance",
      packId = newPack.id
    )

    val savedPacks = prefs.getGrammarChallengePacks()
    assertNotNull(savedPacks)
    assertTrue(savedPacks!!.any { it.id == newPack.id && it.title == "Conditionals Challenge" && it.xpReward == 55 })

    val savedQuestions = prefs.getGrammarChallenges()
    assertNotNull(savedQuestions)
    val packQuestions = savedQuestions!!.filter { it.packId == newPack.id }
    assertEquals(2, packQuestions.size)
  }

  @Test
  fun `fast vocabulary packs, multi-question items, and progress persist across preferences`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = AppPreferences(app)
    val vm = MainViewModel(app)

    val newPack = vm.addFastVocabularyPack(
      title = "Business English",
      description = "Essential workplace & interview vocabulary.",
      themeStyle = "Medium",
      durationSeconds = 90,
      xpReward = 75,
      createdBy = "Prof. Eleanor Vance"
    )
    vm.addFastVocabularyChallenge(
      word = "Consensus",
      prompt = "What does 'Consensus' mean?",
      options = listOf("General agreement", "Formal complaint", "Strict rule", "Sudden delay"),
      correctIndex = 0,
      createdBy = "Prof. Eleanor Vance",
      packId = newPack.id
    )
    vm.addFastVocabularyChallenge(
      word = "Delegate",
      prompt = "To 'Delegate' a task means to:",
      options = listOf("Ignore it completely", "Entrust it to someone else", "Delete the file", "Refuse to participate"),
      correctIndex = 1,
      createdBy = "Prof. Eleanor Vance",
      packId = newPack.id
    )
    vm.recordFastVocabularyResult(
      packId = newPack.id,
      correctCount = 2,
      totalCount = 2,
      xpAwarded = 75
    )

    val savedPacks = prefs.getFastVocabularyPacks()
    assertNotNull(savedPacks)
    assertTrue(savedPacks!!.any { it.id == newPack.id && it.title == "Business English" && it.durationSeconds == 90 && it.xpReward == 75 })

    val savedQuestions = prefs.getFastVocabularyChallenges()
    assertNotNull(savedQuestions)
    val packQuestions = savedQuestions!!.filter { it.packId == newPack.id }
    assertEquals(2, packQuestions.size)

    val savedProgress = prefs.getFastVocabularyProgress()
    assertNotNull(savedProgress)
    assertTrue(newPack.id in savedProgress!!.playedPackIds)
    assertEquals(75, savedProgress.totalXpEarned)
    assertEquals(100, savedProgress.bestScorePercent)
  }

  @Test
  fun `tapping ai speaking opens challenge topic selection roadmap before nova chat`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MainViewModel(app)
    vm.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
    vm.selectStudentTab(StudentTab.HOME)

    composeTestRule.setContent {
      StudentHomeScreen(viewModel = vm)
    }

    composeTestRule.onNodeWithTag("student_home_screen")
      .performScrollToNode(hasTestTag("home_ai_speaking_card"))
    composeTestRule.onNodeWithTag("home_ai_speaking_card").performClick()

    assertEquals(StudentTab.PRACTICE, vm.currentStudentTab.value)
    assertEquals(SpeakingSubTab.TOPICS_ROADMAP, vm.speakingSubTab.value)

    // Selecting a topic from the challenge roadmap launches Nova AI Chat for that topic
    val chosenTopic = vm.speakingPrompts.value.first()
    vm.startTopicConversation(chosenTopic)
    assertEquals(SpeakingSubTab.AI_PARTNER, vm.speakingSubTab.value)
    assertEquals(chosenTopic.id, vm.activeConversationTopic.value?.id)

    // Re-entering Practice tab via bottom nav or footer returns to the challenge selection page
    vm.selectStudentTab(StudentTab.HOME)
    vm.selectStudentTab(StudentTab.PRACTICE)
    assertEquals(SpeakingSubTab.TOPICS_ROADMAP, vm.speakingSubTab.value)
  }

  @Test
  fun `vocabulary challenge 5-activity items, mode configs, and progress persist across preferences`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = AppPreferences(app)
    val vm = MainViewModel(app)

    val createdPic = vm.addWordMatchChallenge(
      word = "Rainbow",
      definition = "Colorful arc in the sky after rain",
      category = "Nature",
      createdBy = "Prof. Eleanor Vance",
      activityType = VocabularyActivityType.PICTURE_MATCHING.name,
      pictureEmoji = "🌈",
      distractors = listOf("Volcano", "Anchor", "Glacier")
    )
    val createdScramble = vm.addWordMatchChallenge(
      word = "Horizon",
      definition = "Line where the earth meets the sky",
      category = "Nature",
      createdBy = "Prof. Eleanor Vance",
      activityType = VocabularyActivityType.WORD_SCRAMBLE.name,
      pictureEmoji = "🌅",
      scrambleHint = "Where sky and land seem to touch"
    )

    val picMode = vm.vocabularyModeConfigs.value.first { it.activityType == VocabularyActivityType.PICTURE_MATCHING }
    val oppMode = vm.vocabularyModeConfigs.value.first { it.activityType == VocabularyActivityType.OPPOSITE_WORDS }
    vm.updateVocabularyModeConfig(picMode.copy(title = "Picture Blitz", questionsPerRound = 6, xpReward = 70))
    vm.updateVocabularyModeConfig(oppMode.copy(isEnabled = false))

    vm.recordVocabularyChallengeResult(
      activityType = VocabularyActivityType.SYNONYMS,
      correctCount = 4,
      totalCount = 5,
      xpAwarded = 45
    )

    val savedItems = prefs.getWordMatchChallenges()
    assertNotNull(savedItems)
    assertTrue(savedItems!!.any { it.id == createdPic.id && it.pictureEmoji == "🌈" && it.activityType == "PICTURE_MATCHING" })
    assertTrue(savedItems.any { it.id == createdScramble.id && it.scrambleHint == "Where sky and land seem to touch" && it.activityType == "WORD_SCRAMBLE" })

    val savedConfigs = prefs.getVocabularyModeConfigs()
    assertNotNull(savedConfigs)
    val persistedPic = savedConfigs!!.first { it.activityType == VocabularyActivityType.PICTURE_MATCHING }
    val persistedOpp = savedConfigs.first { it.activityType == VocabularyActivityType.OPPOSITE_WORDS }
    assertEquals("Picture Blitz", persistedPic.title)
    assertEquals(6, persistedPic.questionsPerRound)
    assertEquals(70, persistedPic.xpReward)
    assertFalse(persistedOpp.isEnabled)

    val savedProgress = prefs.getVocabularyChallengeProgress()
    assertNotNull(savedProgress)
    assertTrue(VocabularyActivityType.SYNONYMS.name in savedProgress!!.completedModes)
    assertEquals(45, savedProgress.totalXpEarned)
    assertEquals(80, savedProgress.bestAccuracyPercent)
  }

  @Test
  fun `vocabulary challenge UI renders 5 modes, plays picture matching, and respects teacher toggle`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MainViewModel(app)

    composeTestRule.setContent {
      VocabularyChallengeGame(
        viewModel = vm,
        onClose = {}
      )
    }

    composeTestRule.onNodeWithTag("vocab_challenge_modes_list")
      .performScrollToNode(hasTestTag("vocab_mode_card_picture_matching"))
    composeTestRule.onNodeWithTag("vocab_mode_card_picture_matching").assertIsDisplayed()

    composeTestRule.onNodeWithTag("vocab_challenge_modes_list")
      .performScrollToNode(hasTestTag("vocab_mode_card_synonyms"))
    composeTestRule.onNodeWithTag("vocab_mode_card_synonyms").assertIsDisplayed()

    // Teacher disables Synonyms mode -> immediately removed from Student Portal selection list
    val synMode = vm.vocabularyModeConfigs.value.first { it.activityType == VocabularyActivityType.SYNONYMS }
    vm.updateVocabularyModeConfig(synMode.copy(isEnabled = false))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("vocab_mode_card_synonyms").assertDoesNotExist()

    // Launch Picture Matching activity
    composeTestRule.onNodeWithTag("vocab_challenge_modes_list")
      .performScrollToNode(hasTestTag("vocab_play_btn_picture_matching"))
    composeTestRule.onNodeWithTag("vocab_play_btn_picture_matching").performClick()

    composeTestRule.onNodeWithTag("vocab_question_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("vocab_option_0").performScrollTo().assertIsDisplayed()

    // Back button returns to modes list
    composeTestRule.onNodeWithTag("vocab_play_back_btn").performClick()
    composeTestRule.onNodeWithTag("vocab_challenge_modes_list").assertIsDisplayed()
  }

  @Test
  fun `remote merge decodes vocabulary 5-activity metadata and mode config`() {
    val repository = com.example.data.repository.LinguaQuestRepository(null)
    val extraJson = org.json.JSONObject().apply {
      put("activityType", "OPPOSITE_WORDS")
      put("pictureEmoji", "⚖️")
      put("synonym", "")
      put("antonym", "Fragile")
      put("scrambleHint", "")
    }.toString()
    val modeExtraJson = org.json.JSONObject().apply {
      put("questionsPerRound", 8)
      put("isEnabled", true)
    }.toString()

    repository.mergeRemoteGameChallenges(
      listOf(
        com.example.data.model.SupabaseGameChallenge(
          id = "wm_remote_1",
          type = "WORD_MATCH",
          titleOrWord = "Durable",
          hintOrMeaning = "Able to withstand wear and damage",
          category = "Materials",
          options = listOf("Strong", "Heavy", "Bright"),
          extraData = extraJson,
          createdBy = "Remote Teacher"
        ),
        com.example.data.model.SupabaseGameChallenge(
          id = "vm_mode_OPPOSITE_WORDS",
          type = "VOCAB_MODE_CONFIG",
          titleOrWord = "Antonym Arena",
          hintOrMeaning = "Master opposite pairs",
          category = "OPPOSITE_WORDS",
          correctIndex = 90,
          extraData = modeExtraJson,
          createdBy = "Remote Teacher"
        )
      )
    )

    val mergedItem = repository.wordMatchChallenges.value.firstOrNull { it.id == "wm_remote_1" }
    assertNotNull(mergedItem)
    assertEquals("OPPOSITE_WORDS", mergedItem?.activityType)
    assertEquals("Fragile", mergedItem?.antonym)
    assertEquals(listOf("Strong", "Heavy", "Bright"), mergedItem?.distractors)

    val mergedMode = repository.vocabularyModeConfigs.value.first {
      it.activityType == VocabularyActivityType.OPPOSITE_WORDS
    }
    assertEquals("Antonym Arena", mergedMode.title)
    assertEquals(8, mergedMode.questionsPerRound)
    assertEquals(90, mergedMode.xpReward)
    assertTrue(mergedMode.isEnabled)
  }

  @Test
  fun `nova two-step gemini parser and chat bubble display original corrected and explanation`() {
    val voiceCorrectJson = """
      {
        "hasMistake": true,
        "correctedSentence": "I go to school every day.",
        "explanation": "Capitalize I and add a period.",
        "naturalEnglish": "",
        "grammarScore": 90
      }
    """.trimIndent()
    val parsedVoiceCorrect = com.example.service.GeminiSpeechEvaluator.parseGrammarEvaluationJson(
      textOutput = voiceCorrectJson,
      cleanSpoken = "i go to school every day",
      isVoiceInput = true
    ).getOrThrow()
    assertFalse(parsedVoiceCorrect.hasMistake)
    assertEquals(null, parsedVoiceCorrect.correctedSentence)

    val llmMissedErrorJson = """
      {
        "hasMistake": false,
        "correctedSentence": "",
        "explanation": "",
        "naturalEnglish": "",
        "overallScore": 92,
        "grammarScore": 95,
        "feedback": "Excellent!"
      }
    """.trimIndent()
    val caughtBySafetyNet = com.example.service.GeminiSpeechEvaluator.parseGrammarEvaluationJson(
      textOutput = llmMissedErrorJson,
      cleanSpoken = "I am go to school every day.",
      isVoiceInput = false
    ).getOrThrow()
    assertTrue(caughtBySafetyNet.hasMistake)
    assertEquals("I go to school every day.", caughtBySafetyNet.correctedSentence)
    assertTrue(caughtBySafetyNet.grammarScore <= 72)

    val step2Json = """
      {
        "aiCoachResponse": "Excellent! That sounds like a nice routine. What time do your classes start?",
        "followUpQuestion": "What time do your classes start?",
        "hint": "Say what time your first class begins.",
        "exampleAnswers": ["My classes start at 9 AM."],
        "isSessionSummary": false,
        "improvementSuggestions": []
      }
    """.trimIndent()
    val finalCloudEval = com.example.service.GeminiSpeechEvaluator.parseEvaluationOutput(
      textOutput = step2Json,
      cleanSpoken = "I am go to school every day.",
      targetPhrase = null,
      model = "gemini-3.5-flash-lite",
      isClosingTurn = false,
      grammarEval = caughtBySafetyNet
    ).getOrThrow()

    assertTrue(finalCloudEval.hasGrammarMistake)
    assertEquals("I go to school every day.", finalCloudEval.correctedSentence)
    val cloudReply = finalCloudEval.aiCoachResponse.orEmpty()
    assertTrue(cloudReply.contains("❌ Your Sentence: \"I am go to school every day.\""))
    assertTrue(cloudReply.contains("✅ Correct Sentence: \"I go to school every day.\""))
    assertTrue(cloudReply.contains("📖 Explanation:"))
    assertFalse(cloudReply.contains("Excellent!"))
    assertTrue(cloudReply.endsWith("What time do your classes start?"))

    val novaMsg = NovaChatMessage(
      id = "nova_test_correction",
      isNova = true,
      text = cloudReply,
      audioPrompt = com.example.service.HumanConversationEngine.buildSpokenAudioPrompt(cloudReply, finalCloudEval),
      evaluation = finalCloudEval,
      isQuestion = true
    )

    composeTestRule.setContent {
      com.example.ui.screens.student.NovaChatBubble(
        message = novaMsg,
        isPlayingAudio = false,
        onPlayAudio = {}
      )
    }

    composeTestRule.onNodeWithTag("nova_grammar_correction_card").assertIsDisplayed()
  }

  @Test
  fun `student nova ai page removes api connection option and restricts api config to teacher and admin`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MainViewModel(app)

    // Admin viewing as Student (Role.STUDENT) -> API connection option must not appear and cannot be opened/edited
    vm.unifiedLogin("admin@linguaquest.edu", "1234")
    vm.switchRole(Role.STUDENT)
    assertEquals(Role.STUDENT, vm.currentUser.value.role)

    composeTestRule.setContent {
      com.example.ui.screens.student.NovaGeminiAssistantScreen(viewModel = vm)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nova_gemini_assistant_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nova_gemini_status_pill").assertDoesNotExist()
    composeTestRule.onNodeWithText("Setup Gemini", substring = true, ignoreCase = true).assertDoesNotExist()
    composeTestRule.onNodeWithText("Gemini Live", substring = true, ignoreCase = true).assertDoesNotExist()

    val initialModel = vm.activeAiModel.value
    val initialKey = vm.geminiApiKey.value
    val initialProvider = vm.aiProviderType.value
    val initialEndpoint = vm.customEndpointUrl.value
    val initialCustomModel = vm.customModelName.value

    vm.openAiKeyDialog()
    assertFalse(vm.showAiKeyDialog.value)
    vm.saveCustomGeminiApiKey("blocked-student-key")
    vm.setAiModel("blocked-model")
    vm.setAiProviderType("CUSTOM_OPENAI")
    vm.setCustomEndpointUrl("https://blocked.example.com/v1")
    vm.setCustomModelName("blocked-custom-model")

    assertEquals(initialKey, vm.geminiApiKey.value)
    assertEquals(initialModel, vm.activeAiModel.value)
    assertEquals(initialProvider, vm.aiProviderType.value)
    assertEquals(initialEndpoint, vm.customEndpointUrl.value)
    assertEquals(initialCustomModel, vm.customModelName.value)

    // Teacher role -> allowed to open and manage API connection
    vm.switchRole(Role.TEACHER)
    vm.openAiKeyDialog()
    assertTrue(vm.showAiKeyDialog.value)
    vm.closeAiKeyDialog()
    assertFalse(vm.showAiKeyDialog.value)

    // Admin role -> allowed to open and manage API connection
    vm.switchRole(Role.ADMIN)
    vm.openAiKeyDialog()
    assertTrue(vm.showAiKeyDialog.value)
    vm.closeAiKeyDialog()
    assertFalse(vm.showAiKeyDialog.value)
  }

  @Test
  fun `nova ai does not automatically speak on fresh install startup or background session load`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    // Ensure fresh-install state (logged out, no saved Nova sessions)
    AppPreferences(app).clearSession()
    val vm = MainViewModel(app)

    // Initialization on fresh install must NEVER trigger TTS automatically
    assertEquals(0, vm.ttsManager.speakAttemptCount)

    // Background topic reload while logged out or on Home must NEVER trigger TTS
    val brandNewTopic = SpeakingPrompt(
      id = "fresh_install_topic_check",
      title = "Daily Routine",
      category = "Conversation",
      question = "What time do you wake up?",
      followUpTip = "Answer in one full sentence.",
      recommendedKeywords = listOf("wake up", "morning"),
      emoji = "☀️"
    )
    vm.loadNovaSession(brandNewTopic)
    assertEquals(0, vm.ttsManager.speakAttemptCount)

    // Background sync reload (autoSpeak = false) even when logged in on Home must NEVER trigger TTS
    vm.unifiedLogin("alex.morgan@linguaquest.edu", "1234")
    val anotherTopic = brandNewTopic.copy(id = "fresh_install_topic_check_2", title = "Hobbies")
    vm.loadNovaSession(anotherTopic, autoSpeak = false)
    assertEquals(0, vm.ttsManager.speakAttemptCount)

    // Explicit student topic start inside Practice speaks the greeting once
    val interactiveTopic = brandNewTopic.copy(id = "fresh_install_topic_check_3", title = "Weekend Plans")
    vm.startTopicConversation(interactiveTopic)
    assertEquals(1, vm.ttsManager.speakAttemptCount)
  }

  @Test
  fun `supabase testConnection normalizes urls, filters doh ips, and never returns status code 0`() {
    val service = com.example.data.remote.SupabaseService()

    assertEquals(
      "https://bdodzycvbbwmzstbgllg.supabase.co",
      service.normalizeSupabaseUrl("  bdodzycvbbwmzstbgllg.supabase.co/rest/v1/  ")
    )
    assertEquals(
      "https://bdodzycvbbwmzstbgllg.supabase.co",
      service.normalizeSupabaseUrl("https://bdodzycvbbwmzstbgllg.supabase.co/")
    )

    val dohJson = """
      {
        "Status": 0,
        "Answer": [
          {"name": "bdodzycvbbwmzstbgllg.supabase.co", "type": 1, "data": "127.0.0.1"},
          {"name": "bdodzycvbbwmzstbgllg.supabase.co", "type": 1, "data": "0.0.0.0"},
          {"name": "bdodzycvbbwmzstbgllg.supabase.co", "type": 1, "data": "104.18.38.10"},
          {"name": "bdodzycvbbwmzstbgllg.supabase.co", "type": 1, "data": "172.64.149.246"}
        ]
      }
    """.trimIndent()
    val parsedIps = service.parseDohIps(dohJson, "bdodzycvbbwmzstbgllg.supabase.co").map { it.hostAddress }
    assertEquals(listOf("104.18.38.10", "172.64.149.246"), parsedIps)

    // Unreachable local port -> must report real exception message, NEVER "status code 0"
    val closedSocket = java.net.ServerSocket(0)
    val closedPort = closedSocket.localPort
    closedSocket.close()

    val failedState = kotlinx.coroutines.runBlocking {
      service.testConnection("http://127.0.0.1:$closedPort", "test-key")
    }
    assertTrue(failedState is com.example.data.remote.SupabaseConnectionState.Error)
    val errorMsg = (failedState as com.example.data.remote.SupabaseConnectionState.Error).message
    assertFalse("Should not surface opaque status code 0, got: $errorMsg", errorMsg.contains("status code 0"))
    assertTrue("Should explain network error, got: $errorMsg", errorMsg.contains("Unable to reach Supabase server"))

    // Local mock HTTP 200 endpoint -> Connected
    val server = java.net.ServerSocket(0)
    val port = server.localPort
    val serverThread = Thread {
      try {
        server.accept().use { clientSocket ->
          val reader = clientSocket.getInputStream().bufferedReader()
          while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
          }
          val out = clientSocket.getOutputStream()
          val body = """{"version":"v2.197.0"}"""
          val response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${body.length}\r\nConnection: close\r\n\r\n$body"
          out.write(response.toByteArray(Charsets.UTF_8))
          out.flush()
        }
      } catch (_: Exception) {}
    }
    serverThread.start()

    val connectedState = kotlinx.coroutines.runBlocking {
      service.testConnection("http://127.0.0.1:$port", "test-key")
    }
    server.close()
    serverThread.join(1000)

    assertTrue("Expected Connected state, got: $connectedState", connectedState is com.example.data.remote.SupabaseConnectionState.Connected)
    assertEquals("http://127.0.0.1:$port", service.supabaseUrl)
  }

  @Test
  fun `student profile claim daily practice streak bonus increments streak, awards 30 xp, and marks claimed today`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MainViewModel(app)
    vm.unifiedLogin("alex.morgan@linguaquest.edu", "1234")

    // Ensure at least 1 daily mission is completed (speakingPracticeMinutes > 0)
    val initialProfile = vm.studentProfile.value
    if (initialProfile.speakingPracticeMinutes <= 0) {
      vm.repository.setStudentProfile(initialProfile.copy(speakingPracticeMinutes = 15))
    }
    val beforeProfile = vm.studentProfile.value
    val beforeStreak = beforeProfile.streakDays
    val beforeXp = beforeProfile.xpPoints
    val beforeCoins = beforeProfile.coins

    composeTestRule.setContent {
      com.example.ui.screens.student.StudentProfileScreen(viewModel = vm)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("student_profile_screen")
      .performScrollToNode(hasTestTag("claim_daily_streak_bonus_button"))
    composeTestRule.onNodeWithTag("claim_daily_streak_bonus_button")
      .assertIsDisplayed()
      .assertIsEnabled()
    composeTestRule.onNodeWithText("Claim Daily Practice Streak Bonus (+30 XP)", substring = true)
      .assertIsDisplayed()

    // Tap the button -> increments streak (+1), adds +30 XP and +10 Coins, sets CelebrationDialog, disables button
    composeTestRule.onNodeWithTag("claim_daily_streak_bonus_button").performClick()
    composeTestRule.waitForIdle()

    val afterProfile = vm.studentProfile.value
    assertEquals(beforeStreak + 1, afterProfile.streakDays)
    assertEquals(beforeXp + 30, afterProfile.xpPoints)
    assertEquals(beforeCoins + 10, afterProfile.coins)
    assertEquals(30 to 10, vm.celebrationReward.value)
    assertTrue(vm.dailyStreakBonusClaimedToday.value)

    composeTestRule.onNodeWithTag("claim_daily_streak_bonus_button").assertIsNotEnabled()
    composeTestRule.onNodeWithText("Daily Streak Bonus Claimed Today (+30 XP)", substring = true)
      .assertIsDisplayed()

    // Attempting a second claim on the same day must be blocked without duplicate XP or streak
    assertFalse(vm.incrementDailyPracticeStreak())
    assertEquals(beforeStreak + 1, vm.studentProfile.value.streakDays)
    assertEquals(beforeXp + 30, vm.studentProfile.value.xpPoints)
  }

  @Test
  fun `official certificate dialog removes switcher and only shows own student record`() {
    val ownSubmission = com.example.data.model.ExamSubmission(
      id = "sub_alex_own",
      examId = "exm_10",
      examTitle = "Class 10 English Grammar Test",
      studentId = "std_001",
      studentName = "Alex Morgan",
      studentClass = "Class 10",
      studentAvatar = "🦉",
      scoreObtained = 45,
      totalMarks = 50,
      percentage = 90,
      isPassed = true,
      grade = "O",
      completedAt = "Today, 10:15 AM",
      timeTakenSeconds = 320,
      certificateId = "SPK-EXM-2026-7841"
    )
    val otherStudent1 = ownSubmission.copy(
      id = "sub_fariha_other",
      studentId = "std_002",
      studentName = "Fariha Yesmin",
      studentClass = "VIII",
      scoreObtained = 88,
      totalMarks = 100,
      percentage = 88,
      certificateId = "SPK-EXM-2026-9001"
    )
    val otherStudent2 = ownSubmission.copy(
      id = "sub_sofia_other",
      studentId = "std_003",
      studentName = "Sofia Rahman",
      studentClass = "Class 9",
      scoreObtained = 42,
      totalMarks = 50,
      percentage = 84,
      certificateId = "SPK-EXM-2026-9002"
    )

    composeTestRule.setContent {
      com.example.ui.components.SpeaklyCertificatePreviewDialog(
        initialSubmission = ownSubmission,
        allSubmissions = listOf(otherStudent1, ownSubmission, otherStudent2),
        onDismiss = {},
        onDownloadPdf = {}
      )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Official Certificate").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alex Morgan").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("SWITCH CERTIFICATE", substring = true, ignoreCase = true).assertDoesNotExist()
    composeTestRule.onNodeWithText("Fariha Yesmin", substring = true).assertDoesNotExist()
    composeTestRule.onNodeWithText("Sofia Rahman", substring = true).assertDoesNotExist()
  }

  @Test
  fun `exam briefing dialog shows Not Now and Begin Exam buttons cleanly and begins exam`() {
    val sampleExam = com.example.data.model.Exam(
      id = "exm_briefing_test",
      title = "English",
      description = "Complete all multiple choice questions within the allotted time.",
      teacherName = "Prof. Vance",
      subjectCategory = "English Grammar",
      durationMinutes = 10,
      totalMarks = 25,
      passingPercentage = 60,
      questions = listOf(
        com.example.data.model.ExamQuestion(
          id = "q1",
          question = "She ___ to school every day.",
          options = listOf("go", "goes", "going", "gone"),
          correctOptionIndex = 1,
          marks = 5
        )
      )
    )

    composeTestRule.setContent {
      com.example.ui.screens.student.LiveExamModal(
        exam = sampleExam,
        onDismiss = {},
        onSubmit = { _, _ -> }
      )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Examination Instructions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exam_briefing_cancel_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("Not Now").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exam_briefing_begin_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("Begin Exam 🚀").assertIsDisplayed()

    composeTestRule.onNodeWithTag("exam_briefing_begin_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Question 1 of 1").assertIsDisplayed()
  }

  @Test
  fun `student exams show all new exams first then completed exams with accurate pending count`() {
    val now = 1_700_000_000_000L
    val completedExam = com.example.data.model.Exam(
      id = "exm_done_1",
      title = "Already Given Grammar Exam",
      description = "Completed test",
      teacherName = "Prof. Vance",
      targetClass = "Class 10"
    )
    val newOpenExam = com.example.data.model.Exam(
      id = "exm_new_open",
      title = "Brand New Vocabulary Exam",
      description = "Open now",
      teacherName = "Prof. Vance",
      targetClass = "Class 10"
    )
    val allClassPendingExam = com.example.data.model.Exam(
      id = "exm_all_classes",
      title = "Phonetics For All Classes",
      description = "Open to all classes",
      teacherName = "Dr. Reed",
      targetClass = "All Classes"
    )
    val otherClassExam = com.example.data.model.Exam(
      id = "exm_other_class",
      title = "Class 8 Grammar Exam",
      description = "For Class 8 only",
      teacherName = "Prof. Vance",
      targetClass = "Class 8"
    )
    val expiredPendingExam = com.example.data.model.Exam(
      id = "exm_expired",
      title = "Expired Class 10 Quiz",
      description = "Already ended",
      teacherName = "Prof. Vance",
      targetClass = "Class 10",
      availableUntilMillis = now - 60_000L
    )

    val allExams = listOf(completedExam, otherClassExam, newOpenExam, allClassPendingExam, expiredPendingExam)
    val eligible = allExams.filter {
      it.isPublished && com.example.ui.screens.student.isExamForStudentClass(it, studentClass = "Class 10")
    }
    val submissionsMap = mapOf(
      completedExam.id to com.example.data.model.ExamSubmission(
        id = "sub_done_1",
        examId = completedExam.id,
        examTitle = completedExam.title,
        studentId = "stu_alex_1",
        studentName = "Alex Morgan",
        studentClass = "Class 10",
        studentAvatar = "🦉",
        scoreObtained = 45,
        totalMarks = 50,
        percentage = 90,
        isPassed = true,
        grade = "O",
        completedAt = "Today",
        timeTakenSeconds = 200,
        certificateId = "SPK-EXM-2026-0001"
      )
    )

    val allTabResult = com.example.ui.screens.student.filterAndSortStudentExams(
      eligibleExams = eligible,
      studentSubmissionsMap = submissionsMap,
      selectedStatusFilter = "All",
      selectedCategoryFilter = "All Categories",
      searchQuery = "",
      currentTimeMillis = now
    )
    val pendingTabResult = com.example.ui.screens.student.filterAndSortStudentExams(
      eligibleExams = eligible,
      studentSubmissionsMap = submissionsMap,
      selectedStatusFilter = "Pending",
      selectedCategoryFilter = "All Categories",
      searchQuery = "",
      currentTimeMillis = now
    )
    val pendingCount = eligible.count { !submissionsMap.containsKey(it.id) && !it.isExpiredAt(now) }

    // All new/pending exams come first, followed by already-given exam; other-class and expired pending excluded
    assertEquals(listOf("exm_new_open", "exm_all_classes", "exm_done_1"), allTabResult.map { it.id })
    // Pending count matches the exact Pending tab list
    assertEquals(2, pendingCount)
    assertEquals(listOf("exm_new_open", "exm_all_classes"), pendingTabResult.map { it.id })
  }
}

