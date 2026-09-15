package com.kaaneneskpc.napsam.core.data.network.dto

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.repository.GeneratedBadges
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestion
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestionResult
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.Place
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import com.kaaneneskpc.napsam.core.model.SuggestionMode
import com.kaaneneskpc.napsam.core.model.SuggestionSource
import kotlinx.serialization.Serializable

/** `POST /api/v1/suggest/` gövdesi (web istemcisiyle aynı alanlar, spesifikasyon C3.4). Konum alanı yoktur. */
@Serializable
internal data class SuggestRequestDto(
    val mode: String,
    val budget: String,
    val keywords: List<String>,
    val text: String,
    val filters: FiltersDto,
    val excluded: List<String>,
)

@Serializable
internal data class FiltersDto(
    val duration: Int? = null,
    val companions: String? = null,
    val energy: String? = null,
    val place: String? = null,
)

@Serializable
internal data class SuggestResponseDto(val suggestion: RemoteSuggestionDto? = null, val meta: SuggestMetaDto? = null)

/** Web `suggestion_to_dict`. */
@Serializable
internal data class RemoteSuggestionDto(
    val slug: String,
    val title: String,
    val hook: String,
    val steps: List<String>,
    val fallback: String,
    val category: String,
    val categoryLabel: String,
    val badges: BadgesDto,
    val requiredItems: List<String> = emptyList(),
    val place: String,
    val venueType: String = "",
    val isFree: Boolean,
    val source: String,
)

@Serializable
internal data class BadgesDto(val cost: String, val duration: String, val place: String, val companion: String)

@Serializable
internal data class SuggestMetaDto(val aiQuotaLeft: Int? = null)

/** Web isteğinin sınırlarıyla: kelime ≤ 8, metin ≤ 280, hariç tutulan ≤ 40. */
internal fun SuggestionContext.toSuggestRequest(text: String): SuggestRequestDto = SuggestRequestDto(
    mode = SuggestionMode.TEXT.key,
    budget = budgetKey,
    keywords = keywords.take(EngineConstants.MAX_KEYWORDS),
    text = text.take(EngineConstants.MAX_TEXT_LEN),
    filters = FiltersDto(
        duration = durationMax,
        companions = companions?.key,
        energy = energy?.key,
        place = placePreference?.key,
    ),
    excluded = excluded.take(EngineConstants.MAX_EXCLUDED),
)

/** Bilinmeyen `source` değeri: null (geçersiz yanıt). */
internal fun SuggestResponseDto.toResultOrNull(): RemoteSuggestionResult? {
    val remote = suggestion?.let { dto ->
        when (SuggestionSource.fromKey(dto.source)) {
            SuggestionSource.SEED -> RemoteSuggestion.Seed(dto.slug)
            SuggestionSource.AI -> dto.toGenerated()
            null -> return null
        }
    }
    return RemoteSuggestionResult(suggestion = remote, aiQuotaLeft = meta?.aiQuotaLeft)
}

private fun RemoteSuggestionDto.toGenerated(): RemoteSuggestion.Generated = RemoteSuggestion.Generated(
    slug = slug,
    title = title,
    hook = hook,
    steps = steps,
    fallback = fallback,
    category = Category.fromKey(category),
    categoryLabel = categoryLabel,
    badges = GeneratedBadges(
        cost = badges.cost,
        duration = badges.duration,
        place = badges.place,
        companion = badges.companion,
    ),
    requiredItems = requiredItems,
    place = Place.fromKey(place),
    venueType = venueType,
    isFree = isFree,
)
