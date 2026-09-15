package com.kaaneneskpc.napsam.core.domain.repository

import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.Place

/** Mobil API `POST /api/v1/suggest/` sonucu (spesifikasyon C3.4). */
public data class RemoteSuggestionResult(
    /** null: sunucu hiç öneri bulamadı; istemci yerel motora düşer. */
    public val suggestion: RemoteSuggestion?,
    /** 0 ise o gün AI yeniden denenmez; AI kapalıyken null. */
    public val aiQuotaLeft: Int?,
)

public sealed interface RemoteSuggestion {
    /** Sunucu yerel havuzdaki bir öneriyi seçti (ör. AI kotası doldu); kart yerel içerikten çizilir. */
    public data class Seed(public val slug: String) : RemoteSuggestion

    /**
     * AI'ın ürettiği, yerel havuzda olmayan öneri. Yanıt yalnızca kart alanlarını taşır; rozet
     * metinleri sunucuda web kurallarıyla üretilmiştir.
     */
    public data class Generated(
        public val slug: String,
        public val title: String,
        public val hook: String,
        public val steps: List<String>,
        public val fallback: String,
        public val category: Category?,
        public val categoryLabel: String,
        public val badges: GeneratedBadges,
        public val requiredItems: List<String>,
        public val place: Place?,
        public val venueType: String,
        public val isFree: Boolean,
    ) : RemoteSuggestion
}

public data class GeneratedBadges(
    public val cost: String,
    public val duration: String,
    public val place: String,
    public val companion: String,
)
