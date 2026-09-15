package com.kaaneneskpc.napsam.feature.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetOption
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.domain.chips.ChipCatalog
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Suggestion
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Ana ekran ekran görüntüleri (spesifikasyon B13): tasarımdaki ana ekran açık/koyu, %200 yazı ölçeği,
 * açık daraltma paneli, "Yaz" modu ve en uzun Türkçe metinli öneri. Tasarımla karşılaştırma için
 * `docs/design/mobile-light.png`, `mobile-dark.png`. Nabız animasyonu yüzünden saat durdurulur.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h3200dp-xhdpi")
class HomeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val budget = BudgetCalculator(ContractFixtures.budgetTiers, ContractFixtures.wage)

    /** Stitch tasarımındaki örnek öneri. */
    private val designSample = suggestion("ufak-fatura-arsivini-duzenle")

    private val longest = ContractFixtures.suggestions.maxBy { suggestion ->
        with(suggestion) { title.length + hook.length + fallback.length + steps.sumOf { it.length } }
    }

    private val base = HomeUiState(
        budgetOptions = budget.tiers.map { BudgetOption(it.key, it.label, budget.amountText(it)) }.toImmutableList(),
        chips = ChipCatalog.MIDDAY.toImmutableList(),
        suggestion = designSample,
    )

    @Test
    fun anasayfa_acik() = capture("anasayfa_acik", base, dark = false)

    @Test
    fun anasayfa_koyu() = capture("anasayfa_koyu", base, dark = true)

    @Test
    @Config(qualifiers = "w393dp-h6400dp-xhdpi")
    fun anasayfa_yazi200() = capture("anasayfa_yazi200", base, dark = false, fontScale = 2f)

    @Test
    fun anasayfa_kelime_secimi() = capture(
        name = "anasayfa_kelime_secimi",
        state = base.copy(refineExpanded = true, selectedKeywords = persistentListOf("fotoğraf", "yemek")),
        dark = false,
    )

    @Test
    fun anasayfa_yaz_modu_koyu() = capture(
        name = "anasayfa_yaz_modu_koyu",
        state = base.copy(refineExpanded = true, refineMode = RefineMode.TEXT, text = "3 saatim var param yok"),
        dark = true,
    )

    @Test
    fun anasayfa_en_uzun_oneri_kayitli_mesajli() = capture(
        name = "anasayfa_en_uzun_oneri_kayitli_mesajli",
        state = base.copy(suggestion = longest, isSaved = true, showCommitMessage = true),
        dark = false,
    )

    private fun capture(name: String, state: HomeUiState, dark: Boolean, fontScale: Float = 1f) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density = density.density, fontScale = fontScale)) {
                NapsamTheme(darkTheme = dark) {
                    HomeScreen(
                        state = state,
                        onBudgetSelect = {},
                        onNapsamClick = {},
                        onRefineToggle = {},
                        onRefineModeSelect = {},
                        onChipToggle = {},
                        onTextChange = {},
                        onTextSubmit = {},
                        onRefineClear = {},
                        onCommit = {},
                        onAnotherIdea = {},
                        onSaveToggle = {},
                        onCommitMessageClose = {},
                        onOpenPrivacy = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage("${roborazziSystemPropertyOutputDirectory()}/$name.png")
    }

    private fun suggestion(slug: String): Suggestion = ContractFixtures.suggestions.single { it.slug == slug }
}
