package com.kaaneneskpc.napsam.core.model

import java.time.LocalDateTime

/**
 * Bir öneri isteğinin tüm bağlamı (spesifikasyon C4.1).
 *
 * Şehir, ilçe veya koordinat alanı YOKTUR ve eklenmeyecektir. [now] cihazın yerel saatidir.
 */
public data class SuggestionContext(
    public val now: LocalDateTime,
    public val budgetKey: String = BudgetTier.DEFAULT_KEY,
    public val keywords: List<String> = emptyList(),
    public val durationMax: Int? = null,
    public val companions: CompanionType? = null,
    public val energy: Energy? = null,
    public val placePreference: PlacePreference? = null,
    /** v1'de gönderilmez (konum izni istenmez); motor biliniyorsa kullanır. */
    public val weather: Weather? = null,
    public val excluded: Set<String> = emptySet(),
)

public data class Weather(
    /** Örn. "rain", "snow", "storm", "sleet", "clear". */
    public val condition: String,
    public val tempC: Double?,
)
