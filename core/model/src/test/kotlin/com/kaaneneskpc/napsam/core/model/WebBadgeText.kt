package com.kaaneneskpc.napsam.core.model

import java.util.Locale

/**
 * Rozet yapısını web'in Türkçe metnine çevirir; YALNIZCA parite testi içindir.
 *
 * Uygulamadaki gerçek metinler arayüz kaynaklarından gelir (Faz 8). Buradaki kelimeler,
 * web'deki `core/models.py` rozet özelliklerinin ürettiği metinlerdir.
 */
internal object WebBadgeText {
    private val turkish = Locale.forLanguageTag("tr-TR")

    private val companionLabels = mapOf(
        CompanionType.SOLO to "Tek başına",
        CompanionType.COUPLE to "Çift",
        CompanionType.FRIENDS to "Arkadaşlarla",
        CompanionType.FAMILY to "Ailece",
        CompanionType.KIDS to "Çocuklu",
    )

    fun cost(badge: CostBadge): String = when (badge) {
        CostBadge.Free -> "₺0 Bedava"
        is CostBadge.Approximate -> if (badge.note == null) "~₺${badge.max}" else "~₺${badge.max} ${badge.note}"
    }

    fun duration(badge: DurationBadge): String =
        if (badge.isExact) "${badge.max} dk" else "${badge.min}-${badge.max} dk"

    fun place(badge: PlaceBadge): String = when (badge) {
        PlaceBadge.Home -> "Evde"
        PlaceBadge.Nearby -> "Dışarı / Yakın"
        PlaceBadge.VenueUnspecified -> "Mekanlı"
        is PlaceBadge.Venue -> badge.venueType.replaceFirstChar { it.titlecase(turkish) }
    }

    fun companion(badge: CompanionBadge): String = when (badge) {
        CompanionBadge.Anyone -> "Farketmez"
        is CompanionBadge.Specific -> badge.companions.joinToString(" / ") { companionLabels.getValue(it) }
    }
}
