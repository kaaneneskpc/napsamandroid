package com.kaaneneskpc.napsam.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.CompanionBadge
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.CostBadge
import com.kaaneneskpc.napsam.core.model.PlaceBadge
import com.kaaneneskpc.napsam.core.model.SuggestionBadges
import java.util.Locale

/** Kart ve Kaydedilenler satırındaki rozet metinleri; web'deki metinlerle birebir (`badges.json` ile test edilir). */
@Immutable
data class BadgeTexts(val cost: String, val duration: String, val place: String, val companion: String)

private val Turkish: Locale = Locale.forLanguageTag("tr-TR")

fun SuggestionBadges.toTexts(resources: Resources): BadgeTexts = BadgeTexts(
    cost = when (val cost = cost) {
        CostBadge.Free -> resources.getString(R.string.badge_cost_free)

        is CostBadge.Approximate ->
            cost.note
                ?.let { resources.getString(R.string.badge_cost_approximate_note, cost.max, it) }
                ?: resources.getString(R.string.badge_cost_approximate, cost.max)
    },
    duration = if (duration.isExact) {
        resources.getString(R.string.badge_duration_exact, duration.max)
    } else {
        resources.getString(R.string.badge_duration_range, duration.min, duration.max)
    },
    place = when (val place = place) {
        PlaceBadge.Home -> resources.getString(R.string.badge_place_home)

        PlaceBadge.Nearby -> resources.getString(R.string.badge_place_nearby)

        PlaceBadge.VenueUnspecified -> resources.getString(R.string.badge_place_venue)

        // İçerikte mekan türü küçük harfle yazılır; ilk harf Türkçe kuralla büyütülür ("İnternet kafe").
        is PlaceBadge.Venue -> place.venueType.replaceFirstChar { it.titlecase(Turkish) }
    },
    companion = when (val companion = companion) {
        CompanionBadge.Anyone -> resources.getString(R.string.badge_companion_anyone)

        is CompanionBadge.Specific -> companion.companions.joinToString(
            separator = resources.getString(R.string.badge_companion_separator),
        ) { resources.getString(it.labelRes) }
    },
)

val Category.labelRes: Int
    @StringRes get() = when (this) {
        Category.EV_ICI -> R.string.category_ev_ici
        Category.MUTFAK -> R.string.category_mutfak
        Category.DISARI_YAKIN -> R.string.category_disari_yakin
        Category.YEME_ICME -> R.string.category_yeme_icme
        Category.HAREKET -> R.string.category_hareket
        Category.KULTUR -> R.string.category_kultur
        Category.SOSYAL -> R.string.category_sosyal
        Category.OGRENME -> R.string.category_ogrenme
        Category.URETME -> R.string.category_uretme
        Category.DOGA -> R.string.category_doga
        Category.KENDINE_BAKIM -> R.string.category_kendine_bakim
        Category.IYILIK -> R.string.category_iyilik
        Category.OYUN -> R.string.category_oyun
        Category.UFAK_ISLER -> R.string.category_ufak_isler
    }

val CompanionType.labelRes: Int
    @StringRes get() = when (this) {
        CompanionType.SOLO -> R.string.companion_solo
        CompanionType.COUPLE -> R.string.companion_couple
        CompanionType.FRIENDS -> R.string.companion_friends
        CompanionType.FAMILY -> R.string.companion_family
        CompanionType.KIDS -> R.string.companion_kids
    }
