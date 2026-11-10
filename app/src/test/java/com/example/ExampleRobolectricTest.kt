package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify application package setup`() {
    assertEquals("com.example", ExampleRobolectricTest::class.java.packageName)
  }
}
