package com.kaaneneskpc.napsam.core.model

import java.io.File
import java.math.BigDecimal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Web'den dışa aktarılan `contract/` dosyalarını testlerde okur (`:core:model` ve `:core:domain`).
 *
 * Klasör yolu Gradle tarafından `napsam.contract.dir` sistem özelliğiyle verilir
 * (JvmLibraryConventionPlugin). Uygulamanın asıl içerik eşleyicisi `:core:data`'dadır;
 * bu dosya yalnızca testlerin ihtiyacı kadar ayrıştırma yapar.
 */
public object ContractFixtures {
    private val root: File by lazy {
        val path = System.getProperty("napsam.contract.dir")
            ?: error("napsam.contract.dir tanımlı değil; testleri Gradle ile çalıştır.")
        File(path)
    }

    public fun json(relativePath: String): JsonElement = Json.parseToJsonElement(root.resolve(relativePath).readText())

    public val suggestions: List<Suggestion> by lazy {
        json("content/suggestions.json").jsonArray.map { it.jsonObject.toSuggestion() }
    }

    private val meta: JsonObject by lazy { json("content/meta.json").jsonObject }

    /** Uygulamaya gömülen varsayılan bütçe kademeleri (web seed değerleri). */
    public val budgetTiers: List<BudgetTier> by lazy {
        meta.getValue("budgetTiers").jsonArray.map { budgetTier(it.jsonObject) }
    }

    /** Dışa aktarma anındaki net asgari ücret. */
    public val wage: BigDecimal by lazy { BigDecimal(meta.string("wage")) }

    /** Mobil API ve dışa aktarma biçimindeki bir kademe: oranlar hassasiyet kaybı olmasın diye metindir. */
    public fun budgetTier(json: JsonObject): BudgetTier = BudgetTier(
        key = json.string("key"),
        label = json.string("label"),
        hint = json.string("hint"),
        order = json.int("order"),
        ratioMin = BigDecimal(json.string("ratioMin")),
        ratioMax = json.getValue("ratioMax").jsonPrimitive.contentOrNull?.let { BigDecimal(it) },
    )

    private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

    private fun JsonObject.int(name: String): Int = getValue(name).jsonPrimitive.int

    private fun JsonObject.strings(name: String): List<String> = getValue(name).jsonArray.map {
        it.jsonPrimitive.content
    }

    private fun <T> JsonObject.enumValue(name: String, fromKey: (String) -> T?): T {
        val key = string(name)
        return requireNotNull(fromKey(key)) { "${string("slug")}: bilinmeyen $name değeri '$key'" }
    }

    private fun JsonObject.toSuggestion(): Suggestion = Suggestion(
        slug = string("slug"),
        title = string("title"),
        hook = string("hook"),
        steps = strings("steps"),
        fallback = string("fallback"),
        category = enumValue("category", Category::fromKey),
        costMin = int("cost_min"),
        costMax = int("cost_max"),
        costNote = string("cost_note"),
        durationMin = int("duration_min"),
        durationMax = int("duration_max"),
        companions = strings("companions").map { key ->
            requireNotNull(CompanionType.fromKey(key)) { "${string("slug")}: bilinmeyen companions değeri '$key'" }
        },
        energy = enumValue("energy", Energy::fromKey),
        place = enumValue("place", Place::fromKey),
        venueType = string("venue_type"),
        weatherNeed = enumValue("weather_need", WeatherNeed::fromKey),
        seasonality = getValue("seasonality").jsonArray.map { it.jsonPrimitive.int },
        culturalTags = strings("cultural_tags"),
        requiredItems = strings("required_items"),
        themeTags = strings("theme_tags"),
        tags = strings("tags"),
        nightUnsafeSolo = getValue("night_unsafe_solo").jsonPrimitive.boolean,
    )
}
