package com.kaaneneskpc.napsam.core.domain

import com.kaaneneskpc.napsam.core.model.ContractFixtures
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class EngineConstantsParityTest {
    @Test
    fun `motor sabitleri web ile ayni`() {
        val expected = ContractFixtures.json("parity/engine.json").jsonObject
        val actual: Map<String, Number> = mapOf(
            "NIGHT_START_HOUR" to EngineConstants.NIGHT_START_HOUR,
            "NIGHT_END_HOUR" to EngineConstants.NIGHT_END_HOUR,
            "VENUE_OPEN_FROM" to EngineConstants.VENUE_OPEN_FROM,
            "VENUE_OPEN_UNTIL" to EngineConstants.VENUE_OPEN_UNTIL,
            "W_KEYWORD" to EngineConstants.W_KEYWORD,
            "W_THEME" to EngineConstants.W_THEME,
            "W_ENERGY" to EngineConstants.W_ENERGY,
            "W_COMPANION" to EngineConstants.W_COMPANION,
            "W_PLACE_PREF" to EngineConstants.W_PLACE_PREF,
            "W_FREE_BONUS" to EngineConstants.W_FREE_BONUS,
            "W_FREE_WHEN_PAID" to EngineConstants.W_FREE_WHEN_PAID,
            "W_RECENT_CATEGORY_PENALTY" to EngineConstants.W_RECENT_CATEGORY_PENALTY,
            "W_ALREADY_SHOWN_PENALTY" to EngineConstants.W_ALREADY_SHOWN_PENALTY,
            "W_JITTER" to EngineConstants.W_JITTER,
            "NEAR_SCORE" to EngineConstants.NEAR_SCORE,
            "TOP_N_FRACTION" to EngineConstants.TOP_N_FRACTION,
            "TOP_N_MIN" to EngineConstants.TOP_N_MIN,
            "DISMISS_COOLDOWN_DAYS" to EngineConstants.DISMISS_COOLDOWN_DAYS,
            "RECENT_STATES" to EngineConstants.RECENT_STATES,
            "SESSION_EXCLUDED" to EngineConstants.SESSION_EXCLUDED,
            "MAX_KEYWORDS" to EngineConstants.MAX_KEYWORDS,
            "MAX_EXCLUDED" to EngineConstants.MAX_EXCLUDED,
            "MAX_TEXT_LEN" to EngineConstants.MAX_TEXT_LEN,
        )

        assertEquals(expected.keys, actual.keys)
        actual.forEach { (name, value) ->
            assertEquals(name, expected.getValue(name).jsonPrimitive.double, value.toDouble(), 0.0)
        }
    }
}
