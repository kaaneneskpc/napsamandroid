package com.kaaneneskpc.napsam.core.model

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Enum anahtarları web'deki değerlerle ve içerik dosyasıyla birebir eşleşmeli. */
class ContentKeysTest {
    @Test
    fun `icerikteki 500 oneri modele eksiksiz donusuyor`() {
        // ContractFixtures bilinmeyen bir anahtarda hata fırlatır; sayı da tam olmalı.
        val meta = ContractFixtures.json("content/meta.json").jsonObject
        assertEquals(meta.getValue("count").jsonPrimitive.content.toInt(), ContractFixtures.suggestions.size)
    }

    @Test
    fun `kategori anahtarlari meta dosyasiyla ayni`() {
        assertEquals(metaKeys("categories"), Category.entries.map { it.key }.toSet())
    }

    @Test
    fun `kiminle anahtarlari meta dosyasiyla ayni`() {
        assertEquals(metaKeys("companions"), CompanionType.entries.map { it.key }.toSet())
    }

    @Test
    fun `anahtarlar benzersiz`() {
        listOf(
            Category.entries.map { it.key },
            Place.entries.map { it.key },
            Energy.entries.map { it.key },
            WeatherNeed.entries.map { it.key },
            CompanionType.entries.map { it.key },
            SuggestionSource.entries.map { it.key },
            StateStatus.entries.map { it.key },
            SuggestionMode.entries.map { it.key },
            PlacePreference.entries.map { it.key },
        ).forEach { keys -> assertEquals(keys.toSet().size, keys.size) }
    }

    @Test
    fun `bilinmeyen anahtar null doner`() {
        assertNull(Category.fromKey("uydurma"))
        assertNull(Place.fromKey(""))
        assertNull(CompanionType.fromKey("SOLO"))
    }

    @Test
    fun `yer tercihi anahtarlari web istegiyle ayni`() {
        assertEquals(PlacePreference.HOME, PlacePreference.fromKey("ev"))
        assertEquals(PlacePreference.OUTSIDE, PlacePreference.fromKey("disari"))
    }

    private fun metaKeys(field: String): Set<String> = ContractFixtures.json("content/meta.json").jsonObject
        .getValue(field).jsonArray
        .map { it.jsonObject.getValue("key").jsonPrimitive.content }
        .toSet()
}
