package com.kaaneneskpc.napsam.core.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Rozet kurallarının sınır değerleri (spesifikasyon B13). */
class SuggestionBadgesTest {
    private val base = Suggestion(
        slug = "ornek",
        title = "Örnek öneri",
        hook = "Tek cümle.",
        steps = listOf("Bir.", "İki."),
        fallback = "Olmazsa: başka bir şey.",
        category = Category.EV_ICI,
        costMin = 0,
        costMax = 0,
        costNote = "",
        durationMin = 20,
        durationMax = 40,
        companions = listOf(CompanionType.SOLO),
        energy = Energy.LOW,
        place = Place.HOME,
        venueType = "",
        weatherNeed = WeatherNeed.ANY,
        seasonality = emptyList(),
        culturalTags = emptyList(),
        requiredItems = emptyList(),
        themeTags = emptyList(),
        tags = emptyList(),
        nightUnsafeSolo = false,
    )

    @Test
    fun `bedava oneride not olsa bile bedava rozeti`() {
        val badges = base.copy(costMax = 0, costNote = "evdekilerle").badges()
        assertEquals(CostBadge.Free, badges.cost)
    }

    @Test
    fun `ucretli oneride bos not gosterilmez`() {
        assertEquals(CostBadge.Approximate(max = 150, note = null), base.copy(costMax = 150).badges().cost)
        assertEquals(
            CostBadge.Approximate(max = 150, note = "iki kişi"),
            base.copy(costMax = 150, costNote = "iki kişi").badges().cost,
        )
    }

    @Test
    fun `ayni alt ve ust sure tek sayidir`() {
        assertTrue(base.copy(durationMin = 30, durationMax = 30).badges().duration.isExact)
        assertFalse(base.badges().duration.isExact)
    }

    @Test
    fun `yer rozeti`() {
        assertEquals(PlaceBadge.Home, base.badges().place)
        assertEquals(PlaceBadge.Nearby, base.copy(place = Place.NEARBY).badges().place)
        assertEquals(PlaceBadge.Venue("sinema"), base.copy(place = Place.VENUE, venueType = "sinema").badges().place)
        assertEquals(PlaceBadge.VenueUnspecified, base.copy(place = Place.VENUE, venueType = "").badges().place)
    }

    @Test
    fun `kiminle rozeti bos ve dort ve uzeri listede farketmez`() {
        assertEquals(CompanionBadge.Anyone, base.copy(companions = emptyList()).badges().companion)
        val dortlu = listOf(CompanionType.SOLO, CompanionType.COUPLE, CompanionType.FRIENDS, CompanionType.FAMILY)
        assertEquals(CompanionBadge.Anyone, base.copy(companions = dortlu).badges().companion)
    }

    @Test
    fun `kiminle rozeti uc ve alti listede ilk iki tur`() {
        val uclu = listOf(CompanionType.KIDS, CompanionType.SOLO, CompanionType.FAMILY)
        assertEquals(
            CompanionBadge.Specific(listOf(CompanionType.KIDS, CompanionType.SOLO)),
            base.copy(companions = uclu).badges().companion,
        )
        assertEquals(CompanionBadge.Specific(listOf(CompanionType.SOLO)), base.badges().companion)
    }

    @Test
    fun `gecilen oneri bekleme suresi`() {
        val now = Instant.parse("2026-09-14T12:00:00Z")
        val state = UserSuggestionState(
            slug = "ornek",
            status = StateStatus.DISMISSED,
            dismissedUntil = now.plusSeconds(60),
            savedAt = null,
            doneAt = null,
            updatedAt = now,
        )
        assertTrue(state.isCoolingDown(now))
        assertFalse(state.isCoolingDown(now.plusSeconds(60)))
        assertFalse(state.copy(dismissedUntil = null).isCoolingDown(now))
    }
}
