package com.kaaneneskpc.napsam.core.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rozet kuralları web ile birebir aynı olmalı (spesifikasyon A6.4, C8).
 *
 * Altın veri: `contract/parity/badges.json`, web motorunun 500 önerinin her biri için
 * ürettiği rozet metinleri.
 */
class SuggestionBadgesParityTest {
    private val suggestions = ContractFixtures.suggestions.associateBy { it.slug }
    private val cases: List<JsonObject> =
        ContractFixtures.json("parity/badges.json").jsonObject.getValue("cases").jsonArray.map { it.jsonObject }

    @Test
    fun `her oneri icin parite verisi var`() {
        assertEquals(suggestions.keys, cases.map { it.getValue("slug").jsonPrimitive.content }.toSet())
    }

    @Test
    fun `rozetler 500 onerinin tamaminda web ile ayni`() {
        val farklar = cases.mapNotNull { case ->
            val slug = case.getValue("slug").jsonPrimitive.content
            val suggestion = suggestions.getValue(slug)
            val badges = suggestion.badges()

            val beklenen = listOf(
                case.getValue("isFree").jsonPrimitive.boolean.toString(),
                case.getValue("cost").jsonPrimitive.content,
                case.getValue("duration").jsonPrimitive.content,
                case.getValue("place").jsonPrimitive.content,
                case.getValue("companion").jsonPrimitive.content,
            )
            val bulunan = listOf(
                suggestion.isFree.toString(),
                WebBadgeText.cost(badges.cost),
                WebBadgeText.duration(badges.duration),
                WebBadgeText.place(badges.place),
                WebBadgeText.companion(badges.companion),
            )
            if (beklenen == bulunan) null else "$slug: beklenen $beklenen, bulunan $bulunan"
        }
        assertTrue("${farklar.size} öneride fark:\n" + farklar.take(10).joinToString("\n"), farklar.isEmpty())
    }

    @Test
    fun `mekan turleri kucuk harfle yazili`() {
        // Web ilk harfi büyütüp geri kalanını küçültür (Python capitalize); uygulama yalnızca ilk
        // harfi Türkçe kuralla büyütür. İkisi ancak içerik küçük harfle yazıldığında aynı sonucu verir.
        val turkish = java.util.Locale.forLanguageTag("tr-TR")
        val aykiri = suggestions.values
            .map { it.venueType }
            .filter { it != it.lowercase(turkish) }
        assertTrue("Büyük harf içeren mekan türleri: $aykiri", aykiri.isEmpty())
    }
}
