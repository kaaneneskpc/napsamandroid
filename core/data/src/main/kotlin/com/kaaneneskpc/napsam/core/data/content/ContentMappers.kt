package com.kaaneneskpc.napsam.core.data.content

import com.kaaneneskpc.napsam.core.data.database.entity.SuggestionEntity
import com.kaaneneskpc.napsam.core.data.network.dto.toModel
import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.Place
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.SuggestionSource
import com.kaaneneskpc.napsam.core.model.WeatherNeed
import java.math.BigDecimal

/**
 * İçeriği veritabanına yazmadan önce doğrular: bilinmeyen bir anahtar içeren içerik hiç yazılmaz,
 * böylece okurken eşleme başarısız olamaz.
 */
internal fun ContentSuggestionDto.toEntity(): SuggestionEntity {
    requireKnown("category", category, Category::fromKey)
    companions.forEach { requireKnown("companions", it, CompanionType::fromKey) }
    requireKnown("energy", energy, Energy::fromKey)
    requireKnown("place", place, Place::fromKey)
    requireKnown("weather_need", weatherNeed, WeatherNeed::fromKey)
    require(seasonality.all { it in 1..12 }) { "$slug: geçersiz mevsim $seasonality" }
    return SuggestionEntity(
        slug = slug,
        title = title,
        hook = hook,
        steps = steps,
        fallback = fallback,
        category = category,
        costMin = costMin,
        costMax = costMax,
        costNote = costNote,
        durationMin = durationMin,
        durationMax = durationMax,
        companions = companions,
        energy = energy,
        place = place,
        venueType = venueType,
        weatherNeed = weatherNeed,
        seasonality = seasonality,
        culturalTags = culturalTags,
        requiredItems = requiredItems,
        themeTags = themeTags,
        tags = tags,
        nightUnsafeSolo = nightUnsafeSolo,
    )
}

internal fun SuggestionEntity.toModel(): Suggestion = Suggestion(
    slug = slug,
    title = title,
    hook = hook,
    steps = steps,
    fallback = fallback,
    category = known("category", category, Category::fromKey),
    costMin = costMin,
    costMax = costMax,
    costNote = costNote,
    durationMin = durationMin,
    durationMax = durationMax,
    companions = companions.map { known("companions", it, CompanionType::fromKey) },
    energy = known("energy", energy, Energy::fromKey),
    place = known("place", place, Place::fromKey),
    venueType = venueType,
    weatherNeed = known("weather_need", weatherNeed, WeatherNeed::fromKey),
    seasonality = seasonality,
    culturalTags = culturalTags,
    requiredItems = requiredItems,
    themeTags = themeTags,
    tags = tags,
    nightUnsafeSolo = nightUnsafeSolo,
    source = SuggestionSource.SEED,
)

/** Sunucu yanıtı yokken geçerli yapılandırma: gömülü meta + AI kapalı + tema yerel rotasyonla. */
internal fun ContentMetaDto.toDefaultConfig(): AppConfig = AppConfig(
    wage = BigDecimal(wage),
    currency = currency,
    budgetTiers = budgetTiers.map { it.toModel() },
    weeklyTheme = null,
    themeRotation = themeRotation,
    aiEnabled = false,
    contentVersion = contentVersion,
    suggestionCount = count,
    dismissCooldownDays = dismissCooldownDays,
    androidMinVersionCode = 1,
)

private fun <T> ContentSuggestionDto.requireKnown(field: String, key: String, fromKey: (String) -> T?) {
    requireNotNull(fromKey(key)) { "$slug: bilinmeyen $field değeri '$key'" }
}

private fun <T> SuggestionEntity.known(field: String, key: String, fromKey: (String) -> T?): T =
    requireNotNull(fromKey(key)) { "$slug: bilinmeyen $field değeri '$key'" }
