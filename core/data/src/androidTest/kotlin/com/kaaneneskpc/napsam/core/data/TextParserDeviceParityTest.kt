package com.kaaneneskpc.napsam.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.domain.text.ParsedText
import com.kaaneneskpc.napsam.core.domain.text.TextParser
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.PlacePreference
import java.math.BigDecimal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * "Yaz" ayrıştırıcısının 164 web cümlesiyle paritesi, bu kez cihazda (ART + ICU regex motoru).
 *
 * JVM'deki `TextParserParityTest` bu sınıfta hatayı göremez: Android'in regex motoru JVM'den farklıdır
 * (ör. `(?U)` bayrağı desteklenmez ve ilk kullanımda uygulamayı çökertir). Sürüm öncesi emülatörde koşar
 * (docs/RELEASE.md): `./gradlew :core:data:connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class TextParserDeviceParityTest {
    private val assets = InstrumentationRegistry.getInstrumentation().context.assets

    private fun json(path: String): JsonObject =
        Json.parseToJsonElement(assets.open(path).bufferedReader().use { it.readText() }).jsonObject

    @Test
    fun cumlelerCihazdaWebIleAyniAyrisir() {
        val meta = json("content/meta.json")
        val budget = BudgetCalculator(
            meta.getValue("budgetTiers").jsonArray.map { ContractFixtures.budgetTier(it.jsonObject) },
            BigDecimal(meta.getValue("wage").jsonPrimitive.content),
        )
        val cases = json("textmode.json").getValue("cases").jsonArray.map { it.jsonObject }
        assertEquals(164, cases.size)

        val mismatches = cases.mapNotNull { case ->
            val text = case.getValue("text").jsonPrimitive.content
            val expected = case.getValue("parsed").jsonObject.toParsedText()
            val actual = TextParser.parse(text, budget)
            if (expected == actual) null else "\"$text\": beklenen=$expected gelen=$actual"
        }
        assertTrue(mismatches.joinToString("\n"), mismatches.isEmpty())
    }

    @Test
    fun boslukVeRakamSiniflariCihazdaDaPythonIleAyni() {
        val budget = BudgetCalculator(emptyList(), BigDecimal.ONE)
        assertEquals(180, TextParser.parse("3\u00A0saat", budget).durationMax)
        assertEquals(20, TextParser.parse("20\u001Cdakika", budget).durationMax)
        assertEquals(180, TextParser.parse("\u0663 saat", budget).durationMax)
        assertEquals("bedava", TextParser.parse("param\u3000\u2003yok", budget).budgetKey)
    }

    private fun JsonObject.toParsedText(): ParsedText = ParsedText(
        budgetKey = this["budget_key"]?.jsonPrimitive?.content,
        durationMax = this["duration_max"]?.jsonPrimitive?.int,
        companions = this["companions"]?.jsonPrimitive?.content?.let { requireNotNull(CompanionType.fromKey(it)) },
        energy = this["energy"]?.jsonPrimitive?.content?.let { requireNotNull(Energy.fromKey(it)) },
        placePreference = this["place_pref"]?.jsonPrimitive?.content?.let {
            requireNotNull(PlacePreference.fromKey(it))
        },
        keywords = this["keywords"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty(),
    )
}
