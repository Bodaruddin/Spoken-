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
  fun `verify Assamese dictionary contains over 300 words`() {
    val count = com.example.util.AssameseTranslator.getTotalWordCount()
    org.junit.Assert.assertTrue("Assamese dictionary should have more than 300 words", count > 300)
  }
}
