package com.kaaneneskpc.napsam.feature.saved

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SavedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `kayitlar listelenir, kaldirilan listeden cikar`() = runTest {
        val suggestions = savedSuggestions()
        val repository = FakeUserStateRepository(suggestions)
        val viewModel = SavedViewModel(repository)
        subscribe(viewModel)

        assertEquals(suggestions, viewModel.uiState.value.items)

        viewModel.onRemove(suggestions.first().slug)
        runCurrent()

        assertEquals(suggestions.drop(1), viewModel.uiState.value.items)
        assertEquals(listOf(suggestions.first().slug), repository.removed)
    }

    @Test
    fun `liste gelmeden bos durum gosterilmez`() = runTest {
        val viewModel = SavedViewModel(FakeUserStateRepository())

        assertFalse("abone olunmadan önce liste yüklenmiş sayılmaz", viewModel.uiState.value.loaded)

        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertTrue(state.loaded)
        assertTrue(state.items.isEmpty())
    }

    /** `WhileSubscribed`: akış ancak abone olunca başlar. */
    private fun TestScope.subscribe(viewModel: SavedViewModel) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }
}
