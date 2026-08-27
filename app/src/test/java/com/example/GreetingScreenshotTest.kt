package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.slmplay.data.db.TrackEntity
import com.example.slmplay.ui.components.MiniPlayer
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTrack = TrackEntity(
        id = "test_01",
        title = "SLM Neon Dream",
        artist = "Small Language Model",
        album = "HOpE°pLaY Volume 1",
        durationMs = 180000L,
        uriString = "procedural://synthwave",
        coverResName = "cover_neon"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        MiniPlayer(
            currentTrack = sampleTrack,
            isPlaying = true,
            currentPositionMs = 45000L,
            durationMs = 180000L,
            onTogglePlayPause = {},
            onSkipNext = {},
            onExpand = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
