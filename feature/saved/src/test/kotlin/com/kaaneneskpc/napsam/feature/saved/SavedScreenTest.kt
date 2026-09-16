package com.kaaneneskpc.napsam.feature.saved

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w393dp-h1200dp-xhdpi")
class SavedScreenTest {
    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val suggestions = savedSuggestions()

    @Test
    fun `kayitlar listelenir ve listeden cikarilir`() {
        val repository = FakeUserStateRepository(suggestions)
        show(repository)

        composeRule.onNodeWithText(suggestions.first().title).assertExists()
        composeRule.onNodeWithText(suggestions[1].title).assertExists()

        composeRule.onAllNodesWithContentDescription("Listeden çıkar").onFirst().performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(suggestions.first().slug), repository.removed)
        composeRule.onNodeWithText(suggestions.first().title).assertDoesNotExist()
        composeRule.onNodeWithText(suggestions[1].title).assertExists()
    }

    @Test
    fun `kayit yoksa bos durum metni gorunur`() {
        show(FakeUserStateRepository())

        composeRule.onNodeWithText("Henüz bir şey kaydetmedin.").assertExists()
        composeRule.onNodeWithText("Bir öneriyi beğendiğinde kalp ikonuna bas, burada birikir.").assertExists()
    }

    private fun show(repository: FakeUserStateRepository) {
        // ViewModel composable içinde kurulmaz (lint: ViewModelConstructorInComposable).
        val viewModel = SavedViewModel(repository)
        composeRule.setContent {
            NapsamTheme(darkTheme = false) {
                SavedRoute(onBack = {}, viewModel = viewModel)
            }
        }
    }
}
