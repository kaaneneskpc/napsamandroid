package com.kaaneneskpc.napsam.core.domain.engine

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.domain.strings
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import kotlin.random.Random
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/** `engine.pick`'in gevşetme döngüsü, puanlamadan izlenir (web `mobile_export.relax_trace`). */
class RelaxParityTest {
    private val engine = SuggestionEngine(Random(0), TestContract.zone)
    private val input = EngineInput(ContractFixtures.suggestions, TestContract.budget)

    @Test
    fun `gevsetme alan sirasi web ile ayni`() {
        assertEquals(TestContract.file("relax.json").strings("order"), FIELD_ORDER)
    }

    @Test
    fun `alti baglamda dusurulen alanlar ve ilk dolu havuz web ile ayni`() {
        val cases = TestContract.cases("relax.json")
        assertEquals(6, cases.size)
        cases.forEach { case ->
            val id = case.string("id")
            val trace = trace(TestContract.context(case.getValue("context").jsonObject))
            assertEquals(id, case.strings("dropped"), trace.dropped)
            assertEquals(id, case.strings("slugs").toSet(), trace.slugs)
            assertEquals(id, case.getValue("lastResort").jsonPrimitive.boolean, trace.lastResort)
        }
    }

    private data class Trace(val dropped: List<String>, val slugs: Set<String>, val lastResort: Boolean)

    private fun trace(context: SuggestionContext): Trace {
        val dropped = mutableListOf<String>()
        var current = context
        while (true) {
            val pool = engine.candidates(current, input)
            if (pool.isNotEmpty()) return Trace(dropped, pool.mapTo(mutableSetOf()) { it.slug }, lastResort = false)
            val next = relax(current) ?: return Trace(dropped, emptySet(), lastResort = true)
            dropped += changedField(current, next)
            current = next
        }
    }

    private fun changedField(before: SuggestionContext, after: SuggestionContext): String = when {
        before.excluded != after.excluded -> "excluded"
        before.keywords != after.keywords -> "keywords"
        before.durationMax != after.durationMax -> "durationMax"
        before.companions != after.companions -> "companions"
        before.energy != after.energy -> "energy"
        before.placePreference != after.placePreference -> "placePref"
        before.weather != after.weather -> "weather"
        else -> error("gevşetme hiçbir alanı değiştirmedi")
    }

    private companion object {
        val FIELD_ORDER = listOf("excluded", "keywords", "durationMax", "companions", "energy", "placePref", "weather")
    }
}
