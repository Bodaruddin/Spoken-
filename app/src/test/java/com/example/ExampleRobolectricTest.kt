package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppPreferences
import com.example.data.model.NovaChatMessage
import com.example.data.model.SpeakingPrompt
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

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
}
