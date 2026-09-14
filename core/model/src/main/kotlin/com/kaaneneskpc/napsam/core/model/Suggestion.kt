package com.kaaneneskpc.napsam.core.model

/**
 * Tek bir somut öneri (spesifikasyon C5.2).
 *
 * İçerik kuralları (başlık ≤ 6 kelime, 2–3 adım, zorunlu fallback, konum bağımsızlığı) web
 * doğrulayıcısında zorlanır; uygulama yalnızca doğrulanmış havuzu okur. Modelde şehir/ilçe
 * alanı yoktur ve eklenmeyecektir.
 */
public data class Suggestion(
    public val slug: String,
    public val title: String,
    public val hook: String,
    public val steps: List<String>,
    public val fallback: String,
    public val category: Category,
    public val costMin: Int,
    public val costMax: Int,
    public val costNote: String,
    public val durationMin: Int,
    public val durationMax: Int,
    public val companions: List<CompanionType>,
    public val energy: Energy,
    public val place: Place,
    public val venueType: String,
    public val weatherNeed: WeatherNeed,
    /** Ay numaraları (1–12); boş liste tüm yıl geçerli demektir. */
    public val seasonality: List<Int>,
    public val culturalTags: List<String>,
    public val requiredItems: List<String>,
    public val themeTags: List<String>,
    /** Kelime modu eşleşmesi için etiketler; chip metinleriyle birebir aynıdır. */
    public val tags: List<String>,
    /** Gece geç saatte tek başına önerilmemeli. */
    public val nightUnsafeSolo: Boolean,
    public val source: SuggestionSource = SuggestionSource.SEED,
) {
    public val isFree: Boolean
        get() = costMax == 0
}
