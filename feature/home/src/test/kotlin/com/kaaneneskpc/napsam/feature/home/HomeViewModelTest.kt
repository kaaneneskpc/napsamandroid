package com.kaaneneskpc.napsam.feature.home

import com.kaaneneskpc.napsam.core.domain.chips.ChipCatalog
import com.kaaneneskpc.napsam.core.model.StateStatus
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Ana ekran akışları web `static/js/napsam.js` ile aynı (spesifikasyon A6). */
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Kural ana dispatcher'ı test başlarken kurar; ViewModel ondan sonra oluşturulmalı.
    private val fixture by lazy { HomeTestFixture() }
    private val viewModel by lazy { fixture.viewModel }
    private val state get() = viewModel.uiState.value
    private val card get() = requireNotNull(state.suggestion)

    @Test
    fun `acilista bedava kart, saatin chipleri ve kademe tutarlari hazir`() {
        assertTrue(card.isFree)
        assertEquals(ChipCatalog.MIDDAY, state.chips)
        assertEquals(
            listOf("0 TL", "~1-150 TL", "~150-500 TL", "~500-1.500 TL", "1.500 TL+"),
            state.budgetOptions.map { it.amountText },
        )
        assertEquals("bedava", state.selectedBudget)
        assertFalse("daraltma kapalı başlar", state.refineExpanded)
    }

    @Test
    fun `NAPSAM secilen kademenin bandindan oneri getirir ve kart bolumune kaydirir`() {
        viewModel.onBudgetSelect("iyi")
        assertEquals("kademe seçmek tek başına öneri istemez", 0, state.stageScrollRequest)

        viewModel.onNapsamClick()

        assertTrue("${card.costMax}", card.costMax in 501..1500)
        assertEquals(1, state.stageScrollRequest)
    }

    @Test
    fun `secilen kelime sert filtredir, ozet ve temizle web gibi`() {
        viewModel.onRefineToggle()
        viewModel.onChipToggle("fotoğraf")
        assertEquals(RefineSummary.Keyword("fotoğraf"), state.refineSummary)

        viewModel.onNapsamClick()
        assertTrue("fotoğraf" in card.tags)

        viewModel.onChipToggle("yemek")
        assertEquals(RefineSummary.KeywordCount(2), state.refineSummary)
        viewModel.onChipToggle("fotoğraf")
        assertEquals(listOf("yemek"), state.selectedKeywords)

        viewModel.onRefineClear()
        assertNull(state.refineSummary)
        assertTrue(state.selectedKeywords.isEmpty())
    }

    @Test
    fun `yaz modunda metin yerel ayristiriciyla baglama islenir, gonder kaydirmaz`() {
        viewModel.onBudgetSelect("bol")
        viewModel.onRefineModeSelect(RefineMode.TEXT)
        viewModel.onTextChange("param yok, 20 dakikam var")
        assertEquals(RefineSummary.Written, state.refineSummary)

        viewModel.onTextSubmit()

        assertTrue("metindeki bütçe ifadesi seçili kademeyi ezer", card.isFree)
        assertTrue("${card.durationMin}", card.durationMin <= 20)
        assertEquals(0, state.stageScrollRequest)
    }

    @Test
    fun `yaz modunda secili kelimeler, kelime modunda yazilan metin kullanilmaz`() {
        viewModel.onChipToggle("olmayan-etiket")
        viewModel.onRefineModeSelect(RefineMode.TEXT)
        viewModel.onNapsamClick()
        assertTrue(card.isFree)

        viewModel.onRefineModeSelect(RefineMode.CHIPS)
        viewModel.onRefineClear()
        viewModel.onChipToggle("fotoğraf")
        viewModel.onTextChange("param var")
        viewModel.onNapsamClick()
        assertTrue("fotoğraf" in card.tags)
        assertTrue(card.isFree)
    }

    @Test
    fun `oturumda gosterilen son 30 oneri tekrar gelmez`() {
        val shown = mutableListOf(card.slug)
        repeat(40) {
            viewModel.onNapsamClick()
            shown += card.slug
        }
        shown.forEachIndexed { index, slug ->
            val window = shown.subList(maxOf(0, index - 30), index)
            assertFalse("$index. öneri son 30 içinde tekrar etti: $slug", slug in window)
        }
    }

    @Test
    fun `baska fikir mevcut oneriyi gecer, yenisini getirir ve karta kaydirir`() {
        val first = card.slug

        viewModel.onAnotherIdea()

        assertEquals(listOf(first), fixture.userStates.dismissed)
        assertNotEquals(first, card.slug)
        assertEquals(1, state.stageScrollRequest)
    }

    @Test
    fun `kaydet ve kayittan cikar`() = runTest {
        viewModel.onSaveToggle()
        assertTrue(state.isSaved)
        assertEquals(StateStatus.SAVED, fixture.userStates.states().single().status)

        viewModel.onSaveToggle()
        assertFalse(state.isSaved)
        assertEquals(StateStatus.SHOWN, fixture.userStates.states().single().status)
    }

    @Test
    fun `tamam yapiyorum mesaj gosterir, kapatilir, yeni oneri mesaji kaldirir`() {
        val slug = card.slug
        viewModel.onCommit()
        assertTrue(state.showCommitMessage)
        assertEquals(listOf(slug), fixture.userStates.done)
        assertEquals(1, state.commitMessageScrollRequest)

        viewModel.onCommitMessageClose()
        assertFalse(state.showCommitMessage)

        viewModel.onCommit()
        viewModel.onNapsamClick()
        assertFalse(state.showCommitMessage)
    }

    @Test
    fun `config degisince kademeler guncellenir, kaybolan secim bedavaya doner`() {
        viewModel.onBudgetSelect("bol")

        fixture.config.config.value = testConfig().copy(
            wage = BigDecimal("56150"),
            budgetTiers = testConfig().budgetTiers.filterNot { it.key == "bol" },
        )

        assertEquals(listOf("bedava", "az", "orta", "iyi"), state.budgetOptions.map { it.key })
        assertEquals("~1-300 TL", state.budgetOptions.single { it.key == "az" }.amountText)
        assertEquals("bedava", state.selectedBudget)
    }
}
