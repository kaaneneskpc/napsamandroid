package com.kaaneneskpc.napsam.core.model

/**
 * Öneri kartındaki rozetlerin YAPISI (spesifikasyon A6.4).
 *
 * Metin burada üretilmez: "Bedava", "dk", "Evde" gibi kelimeler arayüz kaynaklarında
 * (strings.xml, Türkçe + İngilizce) durur. Hangi parçanın gösterileceğine dair kurallar
 * web'deki `Suggestion.cost_badge`, `duration_badge`, `place_badge` ve `companion_badge`
 * ile birebir aynıdır ve `contract/parity/badges.json` ile test edilir.
 */
public data class SuggestionBadges(
    public val cost: CostBadge,
    public val duration: DurationBadge,
    public val place: PlaceBadge,
    public val companion: CompanionBadge,
)

public sealed interface CostBadge {
    /** Maliyet 0 TL. */
    public data object Free : CostBadge

    /** Yaklaşık üst maliyet; [note] boş değilse tutarın yanında gösterilir ("tek seans"). */
    public data class Approximate(public val max: Int, public val note: String?) : CostBadge
}

public data class DurationBadge(public val min: Int, public val max: Int) {
    /** Alt ve üst süre aynıysa tek sayı gösterilir. */
    public val isExact: Boolean
        get() = min == max
}

public sealed interface PlaceBadge {
    public data object Home : PlaceBadge

    public data object Nearby : PlaceBadge

    /** Mekan türü, içerikte küçük harfle yazılır; gösterimde ilk harf Türkçe kuralla büyütülür. */
    public data class Venue(public val venueType: String) : PlaceBadge

    /** Mekan gerektiren ama türü boş olan öneri (doğrulayıcı izin vermez; savunma amaçlı). */
    public data object VenueUnspecified : PlaceBadge
}

public sealed interface CompanionBadge {
    /** Kiminle bilgisi yok ya da neredeyse herkese uygun: "Farketmez". */
    public data object Anyone : CompanionBadge

    /** İçerikteki sırayla en fazla iki kişi türü. */
    public data class Specific(public val companions: List<CompanionType>) : CompanionBadge
}

// Web'deki companion_badge kuralı: liste boşsa ya da en az 4 tür içeriyorsa "Farketmez",
// değilse ilk 2 tür gösterilir.
private const val ANYONE_COMPANION_COUNT = 4
private const val SHOWN_COMPANION_COUNT = 2

public fun Suggestion.badges(): SuggestionBadges = SuggestionBadges(
    cost = if (isFree) {
        CostBadge.Free
    } else {
        // Web'de boş olmayan her not gösterilir (Python'da "" dışındaki dize doğrudur).
        CostBadge.Approximate(max = costMax, note = costNote.ifEmpty { null })
    },
    duration = DurationBadge(min = durationMin, max = durationMax),
    place = when (place) {
        Place.HOME -> PlaceBadge.Home
        Place.NEARBY -> PlaceBadge.Nearby
        Place.VENUE -> if (venueType.isEmpty()) PlaceBadge.VenueUnspecified else PlaceBadge.Venue(venueType)
    },
    companion = if (companions.isEmpty() || companions.size >= ANYONE_COMPANION_COUNT) {
        CompanionBadge.Anyone
    } else {
        CompanionBadge.Specific(companions.take(SHOWN_COMPANION_COUNT))
    },
)
