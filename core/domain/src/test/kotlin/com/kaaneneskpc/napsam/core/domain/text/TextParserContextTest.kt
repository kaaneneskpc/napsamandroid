package com.kaaneneskpc.napsam.core.domain.text

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.PlacePreference
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** Metnin seçimlerle birleştirilmesi (web `views.build_context`). */
class TextParserContextTest {
    private val budget = TestContract.budget
    private val base = SuggestionContext(now = LocalDateTime.of(2026, 3, 10, 14, 0))

    @Test
    fun `metindeki kelimeler secilenlere eklenir, sira korunur, tekrar yok`() {
        val context = TextParser.applyTo(
            base.copy(keywords = listOf("kahve", "sakin")),
            "canım deniz ve kahve istiyor",
            budget,
        )
        assertEquals(listOf("kahve", "sakin", "dışarı"), context.keywords)
    }

    @Test
    fun `metinde butce ifadesi varsa secili kademeyi ezer, yoksa dokunmaz`() {
        assertEquals("bedava", TextParser.applyTo(base.copy(budgetKey = "bol"), "param yok", budget).budgetKey)
        assertEquals("orta", TextParser.applyTo(base.copy(budgetKey = "bol"), "300 tl var", budget).budgetKey)
        assertEquals("bol", TextParser.applyTo(base.copy(budgetKey = "bol"), "sıkıldım", budget).budgetKey)
    }

    @Test
    fun `elle secilen alanlar metinle ezilmez, secilmeyenler metinden dolar`() {
        val text = "sevgilimle evde yorgunum 3 saat"
        val selected = base.copy(
            companions = CompanionType.FRIENDS,
            durationMax = 60,
            energy = Energy.HIGH,
            placePreference = PlacePreference.OUTSIDE,
        )
        assertEquals(selected, TextParser.applyTo(selected, text, budget))
        assertEquals(
            base.copy(
                companions = CompanionType.COUPLE,
                durationMax = 180,
                energy = Energy.LOW,
                placePreference = PlacePreference.HOME,
            ),
            TextParser.applyTo(base, text, budget),
        )
    }

    @Test
    fun `metin sinirdan sonra kesilir, bos metin baglami degistirmez`() {
        val longText = "a".repeat(EngineConstants.MAX_TEXT_LEN) + " param yok"
        assertEquals(base, TextParser.applyTo(base, longText, budget))
        assertEquals(base, TextParser.applyTo(base, "   ", budget))
    }
}
