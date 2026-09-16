package com.kaaneneskpc.napsam.feature.privacy

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** KVKK aydınlatma metninin zorunlu öğeleri ekranda olmalı (md. 10; spesifikasyon B17, K15). */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w393dp-h1600dp-xhdpi")
class PrivacyScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `aydinlatma ogeleri ve ana ekrana donus var`() {
        var backClicked = false
        composeRule.setContent {
            NapsamTheme(darkTheme = false) {
                PrivacyRoute(onBack = { backClicked = true })
            }
        }

        composeRule.onNodeWithText("Veri sorumlusu").assertExists()
        composeRule.onNodeWithText("Kaan Enes KAPICI · kaaneneskpc1@gmail.com").assertExists()
        composeRule.onNodeWithText("Verilerini silmek").assertExists()
        composeRule.onNodeWithText("Sistem ayarlarından", substring = true).assertExists()
        composeRule.onNodeWithText("Sunucu ve yurt dışına aktarım").assertExists()
        composeRule.onNodeWithText("Frankfurt", substring = true).assertExists()
        composeRule.onNodeWithText("Hakların").assertExists()
        composeRule.onNodeWithText("KVKK md. 11", substring = true).assertExists()
        // AI kapalı (Faz 13): uygulama kimlik üretmez, metin de iddia etmemeli.
        composeRule.onNodeWithText("kurulum kimliği", substring = true).assertDoesNotExist()

        composeRule.onNodeWithText("Ana ekran").performClick()
        assertTrue(backClicked)
    }
}
