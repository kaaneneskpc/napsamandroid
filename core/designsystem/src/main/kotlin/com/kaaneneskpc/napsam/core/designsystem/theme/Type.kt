package com.kaaneneskpc.napsam.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kaaneneskpc.napsam.core.designsystem.R

/**
 * Manrope (DESIGN.md), uygulamaya gömülü tek değişken yazı tipi dosyasından (OFL, `assets/licenses`).
 * Stitch'in örnek HTML'i Plus Jakarta Sans yüklese de tasarım sistemi ve web Manrope kullanır.
 */
val Manrope: FontFamily = FontFamily(
    listOf(400, 500, 600, 700, 800).map { weight ->
        Font(
            resId = R.font.manrope_variable,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
        )
    },
)

/**
 * Tipografi (Stitch mobil tasarımındaki Tailwind boyutları). Büyük harfli etiketler kullanım yerinde
 * uygulama diline göre büyütülür (Türkçede "i" → "İ"). Tüm boyutlar sp'dir.
 *
 * Tailwind karşılıkları: tracking-tight -0.025em, wide 0.025em, wider 0.05em, widest 0.1em;
 * leading-tight 1.25, snug 1.375, relaxed 1.625.
 */
@Immutable
data class NapsamTypography(
    val logo: TextStyle,
    val topBarAction: TextStyle,
    val overline: TextStyle,
    val heroTitle: TextStyle,
    val heroSubtitle: TextStyle,
    val sectionLabel: TextStyle,
    val budgetLabel: TextStyle,
    val budgetAmount: TextStyle,
    val cta: TextStyle,
    val ctaNote: TextStyle,
    val refineToggle: TextStyle,
    val stageHeader: TextStyle,
    val categoryChip: TextStyle,
    val badge: TextStyle,
    val cardTitle: TextStyle,
    val cardHook: TextStyle,
    val stepsLabel: TextStyle,
    val step: TextStyle,
    val stepNumber: TextStyle,
    val fallbackTitle: TextStyle,
    val fallbackText: TextStyle,
    val actionPrimary: TextStyle,
    val actionSecondary: TextStyle,
    val chip: TextStyle,
    val valueKicker: TextStyle,
    val valueTitle: TextStyle,
    val valueText: TextStyle,
    val footer: TextStyle,
    val body: TextStyle,
    val bodyTitle: TextStyle,
)

private fun manrope(size: Double, lineHeight: Double, weight: Int, letterSpacingEm: Double = 0.0) = TextStyle(
    fontFamily = Manrope,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = FontWeight(weight),
    letterSpacing = letterSpacingEm.em,
)

internal val DefaultNapsamTypography = NapsamTypography(
    logo = manrope(size = 22.0, lineHeight = 28.0, weight = 700, letterSpacingEm = -0.025),
    topBarAction = manrope(size = 12.0, lineHeight = 16.0, weight = 500),
    overline = manrope(size = 10.0, lineHeight = 14.0, weight = 600, letterSpacingEm = 0.05),
    heroTitle = manrope(size = 30.0, lineHeight = 37.5, weight = 800, letterSpacingEm = -0.025),
    heroSubtitle = manrope(size = 12.0, lineHeight = 19.5, weight = 400),
    sectionLabel = manrope(size = 10.0, lineHeight = 14.0, weight = 700, letterSpacingEm = 0.1),
    budgetLabel = manrope(size = 11.0, lineHeight = 13.75, weight = 700),
    budgetAmount = manrope(size = 9.0, lineHeight = 12.0, weight = 400),
    cta = manrope(size = 14.0, lineHeight = 20.0, weight = 700, letterSpacingEm = 0.025),
    ctaNote = manrope(size = 9.0, lineHeight = 12.0, weight = 600, letterSpacingEm = 0.05),
    refineToggle = manrope(size = 12.0, lineHeight = 16.0, weight = 400),
    stageHeader = manrope(size = 11.0, lineHeight = 16.0, weight = 700, letterSpacingEm = 0.05),
    categoryChip = manrope(size = 10.0, lineHeight = 14.0, weight = 800, letterSpacingEm = 0.1),
    badge = manrope(size = 10.0, lineHeight = 14.0, weight = 500),
    cardTitle = manrope(size = 20.0, lineHeight = 28.0, weight = 800, letterSpacingEm = -0.025),
    cardHook = manrope(size = 12.0, lineHeight = 19.5, weight = 400),
    stepsLabel = manrope(size = 10.0, lineHeight = 14.0, weight = 700, letterSpacingEm = 0.05),
    step = manrope(size = 12.0, lineHeight = 15.0, weight = 400),
    stepNumber = manrope(size = 10.0, lineHeight = 12.0, weight = 700),
    fallbackTitle = manrope(size = 10.0, lineHeight = 14.0, weight = 700, letterSpacingEm = 0.05),
    fallbackText = manrope(size = 12.0, lineHeight = 16.5, weight = 400),
    actionPrimary = manrope(size = 12.0, lineHeight = 16.0, weight = 700),
    actionSecondary = manrope(size = 12.0, lineHeight = 16.0, weight = 600),
    chip = manrope(size = 12.0, lineHeight = 16.0, weight = 600, letterSpacingEm = 0.04),
    valueKicker = manrope(size = 9.0, lineHeight = 12.0, weight = 700, letterSpacingEm = 0.05),
    valueTitle = manrope(size = 14.0, lineHeight = 20.0, weight = 700),
    valueText = manrope(size = 12.0, lineHeight = 19.5, weight = 400),
    footer = manrope(size = 11.0, lineHeight = 17.875, weight = 400),
    body = manrope(size = 15.0, lineHeight = 24.0, weight = 400, letterSpacingEm = -0.005),
    bodyTitle = manrope(size = 20.0, lineHeight = 28.0, weight = 600, letterSpacingEm = -0.015),
)
