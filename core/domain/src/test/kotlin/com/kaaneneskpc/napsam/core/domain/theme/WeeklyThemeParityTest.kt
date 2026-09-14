package com.kaaneneskpc.napsam.core.domain.theme

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.domain.stringOrNull
import com.kaaneneskpc.napsam.core.domain.strings
import com.kaaneneskpc.napsam.core.model.WeeklyTheme
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeeklyThemeParityTest {
    private val cases = TestContract.cases("theme.json")

    @Test
    fun `rotasyon web ile ayni`() {
        assertEquals(TestContract.file("theme.json").strings("rotation"), WeeklyThemeRule.ROTATION)
    }

    @Test
    fun `sekiz senaryo on tarihte secilen tema web ile ayni`() {
        assertEquals(80, cases.size)
        cases.forEach { case ->
            val day = LocalDate.parse(case.string("date"))
            val label = "${case.string("scenario")} ${case.string("date")}"
            assertEquals(label, case.getValue("isoWeek").jsonPrimitive.int, day.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
            assertEquals(label, case.stringOrNull("expected"), WeeklyThemeRule.themeFor(day, themes(case))?.key)
        }
    }

    @Test
    fun `config temasi yoksa yerel rotasyon, varsa config temasi`() {
        cases.filter { it.string("scenario") == "yalnizca-rotasyon" }.forEach { case ->
            val day = LocalDate.parse(case.string("date"))
            assertEquals(case.string("date"), case.stringOrNull("expected"), WeeklyThemeRule.activeThemeKey(day, null))
            assertEquals("ozel", WeeklyThemeRule.activeThemeKey(day, serverThemeKey = "ozel"))
        }
        assertNull(WeeklyThemeRule.activeThemeKey(LocalDate.of(2026, 9, 14), null, rotation = emptyList()))
    }

    private fun themes(case: JsonObject): List<WeeklyTheme> = case.getValue("themes").jsonArray.map {
        val json = it.jsonObject
        WeeklyTheme(
            key = json.string("key"),
            startsOn = LocalDate.parse(json.string("startsOn")),
            isActive = json.getValue("isActive").jsonPrimitive.boolean,
        )
    }
}
