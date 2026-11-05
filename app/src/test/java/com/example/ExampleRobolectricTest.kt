package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

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
    assertTrue(res.assameseText.contains("ইংৰাজী"))
    assertTrue(res.assameseText.contains("আত্মবিশ্বাস"))
  }
}
