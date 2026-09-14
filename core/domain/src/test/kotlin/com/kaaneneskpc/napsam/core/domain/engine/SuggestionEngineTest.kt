package com.kaaneneskpc.napsam.core.domain.engine

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.Place
import com.kaaneneskpc.napsam.core.model.PlacePreference
import com.kaaneneskpc.napsam.core.model.StateStatus
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import com.kaaneneskpc.napsam.core.model.SuggestionSource
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import com.kaaneneskpc.napsam.core.model.Weather
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rastgele seçimin davranışı. Seçim parite verisine girmez; olasılık dağılımı web'deki `VarietyTests`
 * ile aynı eşiklerle, sabit tohumlu [Random] ile doğrulanır.
 */
class SuggestionEngineTest {
    private val suggestions = ContractFixtures.suggestions
    private val input = EngineInput(suggestions, TestContract.budget, themeKey = "yuru")
    private val afternoon = LocalDateTime.of(2026, 3, 10, 14, 0)
    private val afternoonInstant: Instant = afternoon.atZone(TestContract.zone).toInstant()

    @Test
    fun `tema agirligi yakinlik esiginden kucuk`() {
        assertTrue(EngineConstants.W_THEME < EngineConstants.NEAR_SCORE)
    }

    @Test
    fun `havuzun buyuk kismi erisilebilir`() {
        val engine = engine(seed = 42)
        val context = SuggestionContext(now = afternoon)
        val pool = engine.candidates(context, input).size
        val seen = List(150) { requireNotNull(engine.pick(context, input)).slug }.toSet()
        assertTrue("$pool adaydan yalnızca ${seen.size} tanesi gösteriliyor", seen.size > pool * 0.4)
    }

    @Test
    fun `haftalik tema secimleri tekeline almiyor`() {
        val engine = engine(seed = 43)
        val context = SuggestionContext(now = afternoon)
        val themed = List(80) { requireNotNull(engine.pick(context, input)) }.count { "yuru" in it.themeTags }
        assertTrue("80 seçimin $themed tanesi temalı", themed / 80.0 < 0.85)
    }

    @Test
    fun `secilen kelime her secimde tutuyor`() {
        val engine = engine(seed = 44)
        listOf("kahve", "müzik", "fotoğraf").forEach { keyword ->
            val context = SuggestionContext(now = afternoon, budgetKey = "bol", keywords = listOf(keyword))
            repeat(25) { assertTrue(keyword, keyword in requireNotNull(engine.pick(context, input)).tags) }
        }
    }

    @Test
    fun `secim her zaman sert filtre havuzundan`() {
        val engine = engine(seed = 45)
        TestContract.cases("candidates.json").forEach { case ->
            val context = TestContract.context(case.getValue("context").jsonObject)
            val pool = engine.candidates(context, input).toSet()
            if (pool.isNotEmpty()) repeat(5) { assertTrue(case.string("id"), engine.pick(context, input) in pool) }
        }
    }

    @Test
    fun `gecilen oneri bekleme suresince havuza girmez`() {
        val engine = engine(seed = 46)
        val context = SuggestionContext(now = afternoon)
        val slug = engine.candidates(context, input).first().slug
        fun poolWith(status: StateStatus, until: Instant) = engine
            .candidates(context, input.copy(userStates = listOf(state(slug, status, until))))
            .map { it.slug }

        val cooldownEnd = afternoonInstant.plus(Duration.ofDays(EngineConstants.DISMISS_COOLDOWN_DAYS.toLong()))
        assertFalse("bekleme sürüyor", slug in poolWith(StateStatus.DISMISSED, cooldownEnd))
        assertTrue("bekleme tam şimdi bitti", slug in poolWith(StateStatus.DISMISSED, afternoonInstant))
        assertTrue("bekleme geçmiş", slug in poolWith(StateStatus.DISMISSED, afternoonInstant.minusSeconds(1)))
        assertTrue("geçilmemiş kayıt", slug in poolWith(StateStatus.SAVED, cooldownEnd))
    }

    @Test
    fun `bekleme suresi gevsetilmez, son care bedava oneri`() {
        val engine = engine(seed = 47)
        val context = SuggestionContext(now = afternoon)
        val cooldownEnd = afternoonInstant.plus(Duration.ofDays(EngineConstants.DISMISS_COOLDOWN_DAYS.toLong()))
        val allFreeDismissed = input.copy(
            userStates = suggestions.filter { it.isFree }.map { state(it.slug, StateStatus.DISMISSED, cooldownEnd) },
        )
        assertTrue(engine.candidates(context, allFreeDismissed).isEmpty())
        assertTrue(requireNotNull(engine.pick(context, allFreeDismissed)).isFree)
    }

    @Test
    fun `butce asla gevsetilmez`() {
        val engine = engine(seed = 48)
        val impossible = SuggestionContext(
            now = LocalDateTime.of(2026, 7, 15, 3, 0),
            keywords = listOf("olmayan-etiket"),
            durationMax = 5,
            companions = CompanionType.KIDS,
            energy = Energy.HIGH,
            placePreference = PlacePreference.OUTSIDE,
        )
        repeat(30) { assertTrue(requireNotNull(engine.pick(impossible, input)).isFree) }
    }

    @Test
    fun `yapay zeka onerileri havuza girmez, bos havuzda null`() {
        val engine = engine(seed = 49)
        val context = SuggestionContext(now = afternoon)
        val aiOnly = input.copy(suggestions = suggestions.map { it.copy(source = SuggestionSource.AI) })
        assertTrue(engine.candidates(context, aiOnly).isEmpty())
        assertNull(engine.pick(context, aiOnly))
        assertNull(engine.pick(context, input.copy(suggestions = emptyList())))
    }

    @Test
    fun `puan agirliklari web ile ayni toplaniyor`() {
        val engine = SuggestionEngine(NoJitter, TestContract.zone)
        val suggestion = suggestions.first { it.isFree && it.place == Place.HOME }.copy(
            tags = listOf("kahve", "Sakin"),
            themeTags = listOf("yuru"),
            energy = Energy.LOW,
            companions = listOf(CompanionType.COUPLE),
            category = Category.MUTFAK,
        )
        val context = SuggestionContext(
            now = afternoon,
            keywords = listOf("KAHVE", "sakin", "kahve"),
            energy = Energy.LOW,
            companions = CompanionType.COUPLE,
            placePreference = PlacePreference.HOME,
        )
        fun score(
            ctx: SuggestionContext = context,
            theme: String? = "yuru",
            recentCategories: Set<Category> = emptySet(),
            shownSlugs: Set<String> = emptySet(),
        ) = engine.score(suggestion, ctx, theme, recentCategories, shownSlugs)

        // Kelime 3 × 3.0 (tekrar ve büyük harf dahil) + tema 1.0 + enerji 2.0 + kiminle 2.0 + ev 2.5 + bedava 0.5
        assertEquals(17.0, score(), DELTA)
        assertEquals("tema yok", 16.0, score(theme = null), DELTA)
        assertEquals(
            "son kategori ve gösterilmiş",
            13.0,
            score(recentCategories = setOf(Category.MUTFAK), shownSlugs = setOf(suggestion.slug)),
            DELTA,
        )
        assertEquals("ücretli kademede bedava geri düşer", 14.5, score(ctx = context.copy(budgetKey = "orta")), DELTA)
        assertEquals(
            "dışarı tercihi evdeki öneriye puan vermez",
            14.5,
            score(ctx = context.copy(placePreference = PlacePreference.OUTSIDE)),
            DELTA,
        )
    }

    @Test
    fun `son kayitlar en yeni bes kayit`() {
        val states = (1..7).map { i ->
            state(
                "oneri-$i",
                StateStatus.SHOWN,
                dismissedUntil = null,
                updatedAt = afternoonInstant.plusSeconds(i.toLong()),
            )
        }.shuffled(Random(1))
        assertEquals((7 downTo 3).map { "oneri-$it" }, recentStates(states).map { it.slug })
    }

    @Test
    fun `gevsetme butceye dokunmadan sirayla ilerler`() {
        var context: SuggestionContext? = SuggestionContext(
            now = afternoon,
            budgetKey = "iyi",
            keywords = listOf("kahve"),
            durationMax = 30,
            companions = CompanionType.SOLO,
            energy = Energy.LOW,
            placePreference = PlacePreference.HOME,
            weather = Weather(condition = "rain", tempC = 3.0),
            excluded = setOf("bir-oneri"),
        )
        val steps = mutableListOf<SuggestionContext>()
        while (context != null) {
            steps += context
            context = relax(context)
        }
        assertEquals(8, steps.size)
        assertEquals(SuggestionContext(now = afternoon, budgetKey = "iyi"), steps.last())
        assertNull("0 dakika süre sınırı sayılmaz", relax(SuggestionContext(now = afternoon, durationMax = 0)))
    }

    private fun engine(seed: Int) = SuggestionEngine(Random(seed), TestContract.zone)

    private fun state(
        slug: String,
        status: StateStatus,
        dismissedUntil: Instant?,
        updatedAt: Instant = afternoonInstant,
    ) = UserSuggestionState(
        slug = slug,
        status = status,
        dismissedUntil = dismissedUntil,
        savedAt = null,
        doneAt = null,
        updatedAt = updatedAt,
    )

    /** Titreşimsiz puan: `nextDouble` hep 0 döner. */
    private object NoJitter : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
