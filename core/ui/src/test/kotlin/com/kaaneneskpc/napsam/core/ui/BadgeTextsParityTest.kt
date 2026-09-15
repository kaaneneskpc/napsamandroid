package com.kaaneneskpc.napsam.core.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.badges
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Rozet ve kategori metinleri 500 öneride web ile aynı (`contract/parity/badges.json`). */
@RunWith(AndroidJUnit4::class)
class BadgeTextsParityTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun `rozet ve kategori metinleri web ile birebir`() {
        val cases = ContractFixtures.json("parity/badges.json").jsonObject.getValue("cases").jsonArray
            .associateBy { it.jsonObject.getValue("slug").jsonPrimitive.content }
        val suggestions = ContractFixtures.suggestions
        assertEquals(cases.size, suggestions.size)

        val mismatches = suggestions.mapNotNull { suggestion ->
            val expected = cases.getValue(suggestion.slug).jsonObject
            fun field(name: String) = expected.getValue(name).jsonPrimitive.content
            val actual = suggestion.badges().toTexts(resources)
            val expectedTexts = BadgeTexts(field("cost"), field("duration"), field("place"), field("companion"))
            val category = resources.getString(suggestion.category.labelRes)
            if (actual == expectedTexts && category == field("categoryLabel")) {
                null
            } else {
                "${suggestion.slug}: beklenen=$expectedTexts/${field("categoryLabel")} gelen=$actual/$category"
            }
        }
        assertTrue(mismatches.take(10).joinToString("\n"), mismatches.isEmpty())
    }
}
