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
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
}

