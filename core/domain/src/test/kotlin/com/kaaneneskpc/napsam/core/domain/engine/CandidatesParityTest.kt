package com.kaaneneskpc.napsam.core.domain.engine

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.domain.strings
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import kotlin.random.Random
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidatesParityTest {
    private val engine = SuggestionEngine(Random(0), TestContract.zone)
    private val input = EngineInput(ContractFixtures.suggestions, TestContract.budget)

    @Test
    fun `sert filtre sonucu 73 baglamda web ile ayni`() {
        val cases = TestContract.cases("candidates.json")
        assertEquals(73, cases.size)
        val mismatches = cases.mapNotNull { case ->
            val id = case.string("id")
            val actual = engine.candidates(TestContract.context(case.getValue("context").jsonObject), input).map {
                it.slug
            }
            assertEquals("$id: yinelenen öneri", actual.size, actual.toSet().size)
            val expected = case.strings("slugs").toSet()
            val actualSet = actual.toSet()
            if (expected == actualSet) {
                null
            } else {
                "$id: eksik=${(expected - actualSet).take(5)} fazla=${(actualSet - expected).take(5)}"
            }
        }
        assertTrue(mismatches.joinToString("\n"), mismatches.isEmpty())
    }

    @Test
    fun `ornekleme kapagi web ile ayni`() {
        val cases = TestContract.cases("sample.json")
        assertTrue(cases.isNotEmpty())
        cases.forEach { case ->
            val pool = case.getValue("pool").jsonPrimitive.int
            assertEquals("havuz $pool", case.getValue("cap").jsonPrimitive.int, sampleSize(pool))
        }
    }
}
