package com.kaaneneskpc.napsam.core.domain.theme

import com.kaaneneskpc.napsam.core.model.WeeklyTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

/**
 * Haftalık tema kuralı (spesifikasyon C4.7, web `config.theme_for_date`). Kullanıcı temayı görmez;
 * yalnızca sıralamayı hafifçe ağırlıklandırır. `contract/parity/theme.json` ile test edilir.
 */
public object WeeklyThemeRule {
    public val ROTATION: List<String> = listOf("yuru", "elini_kullan", "ekransiz", "duzen")

    /**
     * 1. Bu ISO haftanın pazartesisi ile [day] arasında başlayan etkin tema varsa en yenisi.
     * 2. Yoksa etkin temalardan ISO hafta numarasına göre sırayla biri; sıra [ROTATION], orada
     *    olmayanlar anahtar sırasıyla sona eklenir.
     */
    public fun themeFor(day: LocalDate, themes: List<WeeklyTheme>): WeeklyTheme? {
        val active = themes.filter { it.isActive }
        if (active.isEmpty()) return null

        val monday = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val startedThisWeek = active.filter { it.startsOn in monday..day }
        if (startedThisWeek.isNotEmpty()) {
            return startedThisWeek.maxWith(compareBy<WeeklyTheme>({ it.startsOn }, { it.key }))
        }

        val ordered = active.sortedWith(compareBy<WeeklyTheme>({ rotationIndex(it.key) }, { it.key }))
        return ordered[isoWeek(day) % ordered.size]
    }

    /**
     * Android'de sunucu tema kayıtları yoktur: config bu haftanın temasını bildirdiyse o, bildirmediyse
     * yerel rotasyon (`rotation[isoHafta mod adet]`).
     */
    public fun activeThemeKey(day: LocalDate, serverThemeKey: String?, rotation: List<String> = ROTATION): String? =
        serverThemeKey ?: rotation.getOrNull(isoWeek(day) % rotation.size.coerceAtLeast(1))

    private fun rotationIndex(key: String): Int = ROTATION.indexOf(key).takeIf { it >= 0 } ?: ROTATION.size

    private fun isoWeek(day: LocalDate): Int = day.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
}
