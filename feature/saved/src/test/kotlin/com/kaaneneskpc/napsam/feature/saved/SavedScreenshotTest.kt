package com.kaaneneskpc.napsam.feature.saved

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h1400dp-xhdpi")
class SavedScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val filled = SavedUiState(items = savedSuggestions(3).toImmutableList(), loaded = true)

    @Test
    fun kaydedilenler_acik() = capture("kaydedilenler_acik", filled, dark = false)

    @Test
    fun kaydedilenler_koyu() = capture("kaydedilenler_koyu", filled, dark = true)

    @Test
    fun kaydedilenler_bos() = capture("kaydedilenler_bos", SavedUiState(loaded = true), dark = false)

    private fun capture(name: String, state: SavedUiState, dark: Boolean) {
        composeRule.setContent {
            NapsamTheme(darkTheme = dark) {
                SavedScreen(state = state, onRemove = {}, onBack = {})
            }
        }
        composeRule.onRoot().captureRoboImage("${roborazziSystemPropertyOutputDirectory()}/$name.png")
    }
}
