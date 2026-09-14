package com.kaaneneskpc.napsam.core.model

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Web'den dışa aktarılan `contract/` dosyalarını testlerde okur.
 *
 * Klasör yolu Gradle tarafından `napsam.contract.dir` sistem özelliğiyle verilir
 * (JvmLibraryConventionPlugin). Uygulamanın asıl içerik eşleyicisi `:core:data`'dadır;
 * bu dosya yalnızca model testlerinin ihtiyacı kadar ayrıştırma yapar.
 */
internal object ContractFixtures {
    private val root: File by lazy {
        val path = System.getProperty("napsam.contract.dir")
            ?: error("napsam.contract.dir tanımlı değil; testleri Gradle ile çalıştır.")
        File(path)
    }

    fun json(relativePath: String): JsonElement = Json.parseToJsonElement(root.resolve(relativePath).readText())

    val suggestions: List<Suggestion> by lazy {
        json("content/suggestions.json").jsonArray.map { it.jsonObject.toSuggestion() }
    }

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
