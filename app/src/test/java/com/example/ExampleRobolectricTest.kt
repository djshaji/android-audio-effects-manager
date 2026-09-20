package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AudioEffectsConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Audio Effects", appName)
  }

  @Test
  fun `default audio effects config has correct defaults`() {
    val config = AudioEffectsConfig()
    assertTrue(config.masterEnabled)
    assertEquals(0.0f, config.masterGainDb, 0.01f)
    assertTrue(config.equalizerEnabled)
    assertEquals(5, config.eqBandLevels.size)
    assertTrue(config.compressorEnabled)
    assertTrue(config.limiterEnabled)
    assertTrue(config.normalizerEnabled)
  }
}
