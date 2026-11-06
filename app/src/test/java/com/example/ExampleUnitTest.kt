package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verify_assamese_dictionary_contains_words() {
    val count = com.example.util.AssameseTranslator.getTotalWordCount()
    assertTrue("Assamese dictionary should have words", count > 100)
  }
}
