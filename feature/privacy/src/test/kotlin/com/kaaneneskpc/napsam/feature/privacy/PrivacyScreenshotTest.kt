package com.kaaneneskpc.napsam.feature.privacy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBackground
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h1800dp-xhdpi")
class PrivacyScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun gizlilik_acik() = capture("gizlilik_acik", dark = false)

    @Test
    fun gizlilik_koyu() = capture("gizlilik_koyu", dark = true)

    private fun capture(name: String, dark: Boolean) {
        composeRule.setContent {
            NapsamTheme(darkTheme = dark) {
                NapsamBackground(modifier = Modifier.fillMaxSize()) {
                    PrivacyScreen(onBack = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage("${roborazziSystemPropertyOutputDirectory()}/$name.png")
    }
}
