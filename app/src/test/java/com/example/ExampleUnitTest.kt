package com.example

import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testClassAdditionAndDeletion() {
    val repository = LinguaQuestRepository(appPreferences = null)
    val initialClasses = repository.academicClasses.value
    assertTrue(initialClasses.isNotEmpty())

    // Add a new class
    val added = repository.addClass("Class 5")
    assertTrue("Class 5 should be added", added)
    assertTrue("Class 5 should be present in classes", repository.academicClasses.value.contains("Class 5"))

    // Duplicate addition should fail
    val duplicateAdd = repository.addClass("Class 5")
    assertFalse("Duplicate class addition should return false", duplicateAdd)

    // Delete class
    val deleted = repository.deleteClass("Class 5")
    assertTrue("Class 5 should be deleted", deleted)
    assertFalse("Class 5 should no longer be present", repository.academicClasses.value.contains("Class 5"))
  }

  @Test
  fun testStudentEnrollmentWithClass() {
    val repository = LinguaQuestRepository(appPreferences = null)
    repository.addClass("Spoken English Advanced")

    val initialCount = repository.adminStudents.value.size
    repository.addStudent("Maya Sharma", "maya.s@test.com", "Spoken English Advanced")

    val updatedStudents = repository.adminStudents.value
    assertEquals(initialCount + 1, updatedStudents.size)
    val enrolled = updatedStudents.find { it.email == "maya.s@test.com" }
    assertNotNull(enrolled)
    assertEquals("Spoken English Advanced", enrolled?.batch)
  }

  @Test
  fun testGameChallengesAndRewards() {
    val repository = LinguaQuestRepository(appPreferences = null)
    assertTrue("Word match challenges should be non-empty", repository.wordMatchChallenges.value.isNotEmpty())
    assertTrue("Missing letters challenges should be non-empty", repository.missingLettersChallenges.value.isNotEmpty())
    assertTrue("Sentence builder challenges should be non-empty", repository.sentenceBuilderChallenges.value.isNotEmpty())
    assertTrue("Listen choose challenges should be non-empty", repository.listenChooseChallenges.value.isNotEmpty())
    assertTrue("Pronunciation battle challenges should be non-empty", repository.pronunciationBattleChallenges.value.isNotEmpty())
    assertTrue("Fast vocabulary challenges should be non-empty", repository.fastVocabularyChallenges.value.isNotEmpty())
    assertTrue("Grammar challenges should be non-empty", repository.grammarChallenges.value.isNotEmpty())

    val initialXp = repository.studentProfile.value.xpPoints
    val initialCoins = repository.studentProfile.value.coins
    repository.addRewards(50, 15)
    assertEquals(initialXp + 50, repository.studentProfile.value.xpPoints)
    assertEquals(initialCoins + 15, repository.studentProfile.value.coins)
  }
}
