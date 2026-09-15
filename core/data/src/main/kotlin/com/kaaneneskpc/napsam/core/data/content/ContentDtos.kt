package com.kaaneneskpc.napsam.core.data.content

import com.kaaneneskpc.napsam.core.data.network.dto.BudgetTierDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `contract/content/suggestions.json` içindeki bir öneri (web fixture biçimi, snake_case). */
@Serializable
internal data class ContentSuggestionDto(
    val slug: String,
    val title: String,
    val hook: String,
    val steps: List<String>,
    val fallback: String,
    val category: String,
    @SerialName("cost_min") val costMin: Int,
    @SerialName("cost_max") val costMax: Int,
    @SerialName("cost_note") val costNote: String,
    @SerialName("duration_min") val durationMin: Int,
    @SerialName("duration_max") val durationMax: Int,
    val companions: List<String>,
    val energy: String,
    val place: String,
    @SerialName("venue_type") val venueType: String,
    @SerialName("weather_need") val weatherNeed: String,
    val seasonality: List<Int>,
    @SerialName("cultural_tags") val culturalTags: List<String>,
    @SerialName("required_items") val requiredItems: List<String>,
    @SerialName("theme_tags") val themeTags: List<String>,
    val tags: List<String>,
    @SerialName("night_unsafe_solo") val nightUnsafeSolo: Boolean,
)

/** `contract/content/meta.json`: uygulamanın yerel varsayılanları (web `mobile_export.build_meta`). */
@Serializable
internal data class ContentMetaDto(
    val schema: Int,
    val contentVersion: String,
    val count: Int,
    val currency: String,
    val wage: String,
    val budgetTiers: List<BudgetTierDto>,
    val defaultBudget: String,
    val themeRotation: List<String>,
    val dismissCooldownDays: Int,
    val sessionExcluded: Int,
)
