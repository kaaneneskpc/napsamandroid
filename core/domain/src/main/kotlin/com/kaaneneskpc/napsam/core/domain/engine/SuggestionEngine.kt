package com.kaaneneskpc.napsam.core.domain.engine

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.model.BudgetTier
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.Place
import com.kaaneneskpc.napsam.core.model.PlacePreference
import com.kaaneneskpc.napsam.core.model.StateStatus
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import com.kaaneneskpc.napsam.core.model.SuggestionSource
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import com.kaaneneskpc.napsam.core.model.WeatherNeed
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.random.Random

/** Motorun bir istek için okuduğu veriler. */
public data class EngineInput(
    /** İçerik havuzu; motor yalnızca [SuggestionSource.SEED] olanları önerir. */
    public val suggestions: List<Suggestion>,
    public val budget: BudgetCalculator,
    /** Cihazdaki kullanıcı kayıtları: bekleme süresi ve "son kayıtlar" cezası için. */
    public val userStates: List<UserSuggestionState> = emptyList(),
    /** Bu haftanın teması (`WeeklyThemeRule`); null ise tema puanı yok. */
    public val themeKey: String? = null,
)

/**
 * Öneri motoru (spesifikasyon C4, web `core/engine.py` ile birebir).
 *
 * Sert filtreler havuzu daraltır, yumuşak puan sıralar; seçim en iyi puana
 * [EngineConstants.NEAR_SCORE] kadar yakın adaylar arasından rastgele yapılır. Havuz boşsa kısıtlar
 * sırayla gevşetilir (bütçe ASLA); o da yetmezse rastgele bir bedava öneri döner, boş ekran olmaz.
 *
 * Rastgelelik [random] ile enjekte edilir. Bağlamdaki yerel saat, bekleme süresiyle karşılaştırmak
 * için [zone] ile zaman damgasına çevrilir.
 */
public class SuggestionEngine(private val random: Random, private val zone: ZoneId) {

    /** Bağlama en uygun TEK öneri; havuzda hiç bedava öneri yoksa null. */
    public fun pick(context: SuggestionContext, input: EngineInput): Suggestion? {
        val seeds = input.seeds()
        val recent = recentStates(input.userStates)
        val categoryBySlug = input.suggestions.associate { it.slug to it.category }
        val recentCategories = recent.mapNotNullTo(mutableSetOf()) { categoryBySlug[it.slug] }
        val shownSlugs = recent.mapTo(mutableSetOf()) { it.slug }

        var current: SuggestionContext? = context
        while (current != null) {
            val ctx: SuggestionContext = current
            val pool = candidates(ctx, input, seeds)
            if (pool.isNotEmpty()) {
                val scored = pool
                    .map { it to score(it, ctx, input.themeKey, recentCategories, shownSlugs) }
                    .sortedByDescending { it.second }
                val best = scored.first().second
                val near = scored.filter { best - it.second <= EngineConstants.NEAR_SCORE }.map { it.first }
                return near.take(sampleSize(pool.size)).random(random)
            }
            current = relax(ctx)
        }
        // Son çare: havuzdaki herhangi bir bedava öneri. Boş ekran gösterme.
        return seeds.filter { it.isFree }.randomOrNull(random)
    }

    /** Sert filtrelerden geçen öneriler (sıra önemsiz). */
    public fun candidates(context: SuggestionContext, input: EngineInput): List<Suggestion> =
        candidates(context, input, input.seeds())

    private fun candidates(context: SuggestionContext, input: EngineInput, seeds: List<Suggestion>): List<Suggestion> {
        val hour = context.now.hour
        val companions = context.companions
        var pool = seeds.asSequence()

        // 1) Bütçe tavanı: asla aşılmaz.
        val ceiling = input.budget.ceiling(context.budgetKey)
        if (ceiling != null) pool = pool.filter { it.costMax <= ceiling }

        // 2) Saat: gece tek başına riskli öneriler ve büyük olasılıkla kapalı mekanlar düşer.
        if (hour >= EngineConstants.NIGHT_START_HOUR || hour < EngineConstants.NIGHT_END_HOUR) {
            if (companions == null || companions == CompanionType.SOLO) pool = pool.filter { !it.nightUnsafeSolo }
            if (hour !in EngineConstants.VENUE_OPEN_FROM until EngineConstants.VENUE_OPEN_UNTIL) {
                pool = pool.filter { it.place != Place.VENUE }
            }
        }

        // 3) Hava: yalnızca biliniyorsa (v1'de gönderilmez).
        val weather = context.weather
        if (weather != null) {
            if (weather.condition.lowercase() in WET_CONDITIONS) {
                pool = pool.filter { it.weatherNeed != WeatherNeed.DRY && it.weatherNeed != WeatherNeed.WARM }
            }
            val tempC = weather.tempC
            if (tempC != null && tempC < COLD_BELOW_C) pool = pool.filter { it.weatherNeed != WeatherNeed.WARM }
        }

        // 4) Süre
        val durationMax = context.activeDurationMax
        if (durationMax != null) pool = pool.filter { it.durationMin <= durationMax }

        // 5) Enerji: düşük istendiğinde yüksek eforlu, yüksek istendiğinde düşük eforlu düşer.
        when (context.energy) {
            Energy.LOW -> pool = pool.filter { it.energy != Energy.HIGH }
            Energy.HIGH -> pool = pool.filter { it.energy != Energy.LOW }
            Energy.MEDIUM, null -> Unit
        }

        // 6) Ev / dışarı
        when (context.placePreference) {
            PlacePreference.HOME -> pool = pool.filter { it.place == Place.HOME }
            PlacePreference.OUTSIDE -> pool = pool.filter { it.place != Place.HOME }
            null -> Unit
        }

        // 7) Hariç tutulanlar: oturumda görülenler ve bekleme süresindeki geçilmiş öneriler.
        val now = context.now.atZone(zone).toInstant()
        val coolingDown = input.userStates
            .filter { it.status == StateStatus.DISMISSED && it.isCoolingDown(now) }
            .mapTo(mutableSetOf()) { it.slug }
        pool = pool.filter { it.slug !in context.excluded && it.slug !in coolingDown }

        // 8) Kiminle, mevsim (boş liste = tüm yıl) ve kelimeler (kullanıcının tek açık sinyali, en az biri tutmalı).
        val month = context.now.monthValue
        val keywords = context.keywords.mapTo(mutableSetOf()) { it.lowercase() }
        val result = pool.filter { item ->
            (companions == null || companions in item.companions) &&
                (item.seasonality.isEmpty() || month in item.seasonality) &&
                (keywords.isEmpty() || item.tags.any { it.lowercase() in keywords })
        }.toList()

        // 9) Bütçe bandı: ücretli bir kademe seçildiyse o parayı gerçekten kullanan öneriler varsa yalnızca onlar.
        if (context.budgetKey != BudgetTier.DEFAULT_KEY) {
            val bandLow = input.budget.bandLow(context.budgetKey)
            val inBand = result.filter { it.costMax > bandLow }
            if (inBand.isNotEmpty()) return inBand
        }
        return result
    }

    internal fun score(
        suggestion: Suggestion,
        context: SuggestionContext,
        themeKey: String?,
        recentCategories: Set<Category>,
        shownSlugs: Set<String>,
    ): Double {
        var value = 0.0

        if (context.keywords.isNotEmpty()) {
            val tags = suggestion.tags.mapTo(mutableSetOf()) { it.lowercase() }
            value += EngineConstants.W_KEYWORD * context.keywords.count { it.lowercase() in tags }
        }
        if (themeKey != null && themeKey in suggestion.themeTags) value += EngineConstants.W_THEME
        if (context.energy != null && suggestion.energy == context.energy) value += EngineConstants.W_ENERGY

        val companions = context.companions
        if (companions != null && companions in suggestion.companions) value += EngineConstants.W_COMPANION

        val placeMatches = when (context.placePreference) {
            PlacePreference.HOME -> suggestion.place == Place.HOME
            PlacePreference.OUTSIDE -> suggestion.place != Place.HOME
            null -> false
        }
        if (placeMatches) value += EngineConstants.W_PLACE_PREF

        // Bedava kademede bedava öne çıkar; ücretli kademe seçildiyse bedava olan geri düşer.
        if (suggestion.isFree) {
            value += if (context.budgetKey == BudgetTier.DEFAULT_KEY) {
                EngineConstants.W_FREE_BONUS
            } else {
                EngineConstants.W_FREE_WHEN_PAID
            }
        }

        if (suggestion.category in recentCategories) value += EngineConstants.W_RECENT_CATEGORY_PENALTY
        if (suggestion.slug in shownSlugs) value += EngineConstants.W_ALREADY_SHOWN_PENALTY

        // Küçük rastgelelik: aynı bağlamda hep aynı kart çıkmasın.
        value += random.nextDouble(0.0, EngineConstants.W_JITTER)
        return value
    }
}

/**
 * Havuz boşken kısıtları sırayla gevşetir: hariç tutulanlar → kelimeler → süre → kiminle → enerji →
 * ev/dışarı → hava. Bütçe hiçbir zaman gevşetilmez; gevşetilecek bir şey kalmadıysa null.
 */
internal fun relax(context: SuggestionContext): SuggestionContext? = when {
    context.excluded.isNotEmpty() -> context.copy(excluded = emptySet())
    context.keywords.isNotEmpty() -> context.copy(keywords = emptyList())
    context.activeDurationMax != null -> context.copy(durationMax = null)
    context.companions != null -> context.copy(companions = null)
    context.energy != null -> context.copy(energy = null)
    context.placePreference != null -> context.copy(placePreference = null)
    context.weather != null -> context.copy(weather = null)
    else -> null
}

/** Örnekleme kapağı: en az [EngineConstants.TOP_N_MIN], en az havuzun %30'u (yukarı yuvarlanır). */
internal fun sampleSize(poolSize: Int): Int =
    maxOf(EngineConstants.TOP_N_MIN, ceil(poolSize * EngineConstants.TOP_N_FRACTION).toInt())

/** "Son kayıtlar" cezasının baktığı en yeni kullanıcı kayıtları. */
internal fun recentStates(states: List<UserSuggestionState>): List<UserSuggestionState> =
    states.sortedByDescending { it.updatedAt }.take(EngineConstants.RECENT_STATES)

private val WET_CONDITIONS = setOf("rain", "snow", "storm", "sleet")
private const val COLD_BELOW_C = 8.0

private fun EngineInput.seeds(): List<Suggestion> = suggestions.filter { it.source == SuggestionSource.SEED }

/** Web `if ctx.duration_max:` gibi 0 süre sınırı sayılmaz. */
private val SuggestionContext.activeDurationMax: Int?
    get() = durationMax?.takeIf { it != 0 }
