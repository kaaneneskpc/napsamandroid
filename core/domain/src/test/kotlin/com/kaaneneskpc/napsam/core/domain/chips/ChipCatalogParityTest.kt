package com.kaaneneskpc.napsam.core.domain.chips

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.strings
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class ChipCatalogParityTest {
    @Test
    fun `24 saatin chip seti web ile ayni`() {
        val cases = TestContract.cases("chips.json")
        assertEquals(24, cases.size)
        cases.forEach { case ->
            val hour = case.getValue("hour").jsonPrimitive.int
            assertEquals("saat $hour", case.strings("tags"), ChipCatalog.forHour(hour))
        }
    }

    @Test
    fun `her chip metni icerikte bir etiket`() {
        val contentTags = ContractFixtures.suggestions.flatMap { it.tags }.toSet()
        val chips = with(ChipCatalog) { MORNING + MIDDAY + EVENING + NIGHT }.toSet()
        assertEquals(emptySet<String>(), chips - contentTags)
    }
}
