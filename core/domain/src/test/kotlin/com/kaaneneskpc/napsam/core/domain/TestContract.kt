package com.kaaneneskpc.napsam.core.domain

import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.PlacePreference
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import com.kaaneneskpc.napsam.core.model.Weather
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Parite dosyalarını motor tiplerine çevirir (web `mobile_export._context_json` biçimi). */
internal object TestContract {
    /** Parite verisi Türkiye saatiyle üretildi. */
    val zone: ZoneId = ZoneId.of("Europe/Istanbul")

    val budget: BudgetCalculator by lazy { BudgetCalculator(ContractFixtures.budgetTiers, ContractFixtures.wage) }

    fun file(name: String): JsonObject = ContractFixtures.json("parity/$name").jsonObject

    fun cases(name: String): List<JsonObject> = file(name).getValue("cases").jsonArray.map { it.jsonObject }

    fun context(json: JsonObject): SuggestionContext = SuggestionContext(
        now = LocalDateTime.parse(json.string("now")),
        budgetKey = json.string("budgetKey"),
        keywords = json.strings("keywords"),
        durationMax = json.getValue("durationMax").jsonPrimitive.intOrNull,
        companions = json.stringOrNull("companions")?.let { requireNotNull(CompanionType.fromKey(it)) },
        energy = json.stringOrNull("energy")?.let { requireNotNull(Energy.fromKey(it)) },
        placePreference = json.stringOrNull("placePref")?.let { requireNotNull(PlacePreference.fromKey(it)) },
        weather = (json.getValue("weather") as? JsonObject)?.let { weather ->
            Weather(condition = weather.string("condition"), tempC = weather["tempC"]?.jsonPrimitive?.doubleOrNull)
        },
        excluded = json.strings("excluded").toSet(),
    )
}

internal fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

internal fun JsonObject.stringOrNull(name: String): String? = getValue(name).jsonPrimitive.contentOrNull

internal fun JsonObject.strings(name: String): List<String> = getValue(name).jsonArray.map { it.jsonPrimitive.content }
