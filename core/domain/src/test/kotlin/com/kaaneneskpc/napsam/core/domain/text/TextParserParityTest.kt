package com.kaaneneskpc.napsam.core.domain.text

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.PlacePreference
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextParserParityTest {
    @Test
    fun `164 cumlede ayristirma web ile ayni`() {
        val cases = TestContract.cases("textmode.json")
        assertEquals(164, cases.size)
        val mismatches = cases.mapNotNull { case ->
            val text = case.string("text")
            val expected = case.getValue("parsed").jsonObject.toParsedText()
            val actual = TextParser.parse(text, TestContract.budget)
            if (expected == actual) null else "\"$text\": beklenen=$expected gelen=$actual"
        }
        assertTrue(mismatches.joinToString("\n"), mismatches.isEmpty())
    }

    @Test
    fun `bosluk ve rakam siniflari Python str desenleriyle ayni`() {
        // Python `\s`: bölünmez boşluk ve \x1c-\x1f de boşluktur; `\d`: her Unicode ondalık rakam.
        assertEquals(180, TextParser.parse("3\u00A0saat", TestContract.budget).durationMax)
        assertEquals(20, TextParser.parse("20\u001Cdakika", TestContract.budget).durationMax)
        assertEquals(180, TextParser.parse("\u0663 saat", TestContract.budget).durationMax)
        // Boşluklar tek boşluğa indirgenir; "param yok" ifadesi ideografik boşlukla yazılsa da eşleşir.
        assertEquals("bedava", TextParser.parse("param\u3000\u2003yok", TestContract.budget).budgetKey)
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
