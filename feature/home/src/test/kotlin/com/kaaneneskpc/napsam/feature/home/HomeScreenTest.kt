package com.kaaneneskpc.napsam.feature.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Kritik akış arayüzde (spesifikasyon B13): NAPSAM? → kart, daraltma paneli, "Tamam, yapıyorum" mesajı.
 * Başlıktaki nabız noktası sonsuz animasyon olduğu için Compose saati elle ilerletilir.
 */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w393dp-h2600dp-xhdpi")
class HomeScreenTest {
    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val fixture = HomeTestFixture()

    @Test
    fun `NAPSAM kartta yeni oneri gosterir`() {
        show()
        val first = requireNotNull(fixture.viewModel.uiState.value.suggestion).title
        composeRule.onNodeWithText(first).assertExists()

        composeRule.onNodeWithText("NAPSAM?").performClick()
        advance()

        val second = requireNotNull(fixture.viewModel.uiState.value.suggestion).title
        assertNotEquals(first, second)
        composeRule.onNodeWithText(second).assertExists()
    }

    @Test
    fun `daraltma paneli kapali baslar, kelime secimi ozet ve temizle gosterir`() {
        show()
        composeRule.onNodeWithText("Kelime Seç").assertDoesNotExist()

        composeRule.onNodeWithText("İstersen daralt — kelime seç ya da yaz").performClick()
        advance()
        composeRule.onNodeWithText("Kelime Seç").assertExists()

        composeRule.onNodeWithText("fotoğraf").performClick()
        advance()
        composeRule.onAllNodesWithText("fotoğraf").assertCountEquals(2)

        composeRule.onNodeWithText("Temizle").performClick()
        advance()
        composeRule.onAllNodesWithText("fotoğraf").assertCountEquals(1)
        composeRule.onNodeWithText("Temizle").assertDoesNotExist()
    }

    @Test
    fun `tamam yapiyorum mesaji gosterir ve kapatilir`() {
        show()
        composeRule.onNodeWithText("TAMAM, YAPIYORUM").performClick()
        advance()
        composeRule.onNodeWithText("Harika seçim. Şimdi ekranı kapat.").assertExists()
        assertEquals(1, fixture.userStates.done.size)

        composeRule.onNodeWithContentDescription("Kapat").performClick()
        advance()
        composeRule.onNodeWithText("Harika seçim. Şimdi ekranı kapat.").assertDoesNotExist()
    }

    private fun show() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            NapsamTheme(darkTheme = false) {
                HomeRoute(onOpenPrivacy = {}, viewModel = fixture.viewModel)
            }
        }
        advance()
    }

    private fun advance() {
        composeRule.mainClock.advanceTimeBy(ANIMATION_SETTLE_MS)
    }

    private companion object {
        const val ANIMATION_SETTLE_MS = 1_000L
    }
}
