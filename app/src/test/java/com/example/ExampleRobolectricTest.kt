package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Speakly", appName)
  }

  @Test
  fun `verify game challenge creation and student management`() {
    val repo = com.example.data.repository.LinguaQuestRepository()
    val initialMatches = repo.wordMatchChallenges.value.size
    repo.addWordMatchChallenge("Vibrant", "Full of energy and life", "Vocabulary", "Teacher")
    assertEquals(initialMatches + 1, repo.wordMatchChallenges.value.size)

    val initialStudents = repo.adminStudents.value.size
    repo.addStudent("New Learner", "learner@test.com", "Batch Alpha")
    assertEquals(initialStudents + 1, repo.adminStudents.value.size)
  }

  @Test
  fun `verify sentence builder assamese translation`() {
    val res = com.example.util.AssameseTranslator.translate("She speaks English with natural confidence and clarity")
    org.junit.Assert.assertTrue(res.assameseText.contains("ইংৰাজী"))
    org.junit.Assert.assertTrue(res.assameseText.contains("আত্মবিশ্বাস"))
  }
}
