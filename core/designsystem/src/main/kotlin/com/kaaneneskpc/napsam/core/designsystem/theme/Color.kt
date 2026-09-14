package com.kaaneneskpc.napsam.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * NAPSAM renk token'ları.
 *
 * Kaynak: Stitch son tasarımı, `docs/design/mobile-light.html` ve `mobile-dark.html` (Tailwind
 * zinc/orange paleti). Değerler tasarımla BİREBİR aynıdır; yorumlarda karşılık gelen Tailwind
 * sınıfı yazılıdır. Tasarımdan gelen AA kontrast istisnaları ColorContrastTest'te belgelenir.
 */
@Immutable
data class NapsamColors(
    val isDark: Boolean,
    // Zemin
    val background: Color,
    val heroGlow: Color,
    val topBarDivider: Color,
    val topBarSeparator: Color,
    val footerDivider: Color,
    // Metin
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val separator: Color,
    val divider: Color,
    // Aksan
    val accent: Color,
    val accentDot: Color,
    val themeIcon: Color,
    val ctaGradientStart: Color,
    val ctaGradientEnd: Color,
    val onCta: Color,
    val ctaShadow: Color,
    // Paneller
    val console: Color,
    val consoleBorder: Color,
    val card: Color,
    val cardBorder: Color,
    val valueCard: Color,
    val valueCardBorder: Color,
    // Başlık rozeti
    val overline: Color,
    val overlineBorder: Color,
    val overlineText: Color,
    // Bütçe ve seçili durum ("Tamam, yapıyorum" da bu zemini kullanır)
    val budgetItem: Color,
    val budgetItemBorder: Color,
    val budgetItemLabel: Color,
    val budgetItemAmount: Color,
    val selected: Color,
    val onSelected: Color,
    val onSelectedMuted: Color,
    // Kart bandı
    val bandStart: Color,
    val bandMiddle: Color,
    val bandEnd: Color,
    val bandOverlay: Color,
    val bandBottomBorder: Color,
    val categoryChip: Color,
    val categoryChipBorder: Color,
    val categoryChipText: Color,
    val badge: Color,
    val badgeBorder: Color,
    val badgeText: Color,
    val badgeIcon: Color,
    // Kart içi
    val well: Color,
    val wellBorder: Color,
    val wellText: Color,
    val stepNumber: Color,
    val onStepNumber: Color,
    val fallback: Color,
    val fallbackBorder: Color,
    val fallbackTitle: Color,
    val fallbackText: Color,
    val fallbackIcon: Color,
    // İkincil düğmeler
    val secondaryButton: Color,
    val secondaryButtonBorder: Color,
    val secondaryButtonText: Color,
    val secondaryButtonIcon: Color,
    val iconButtonIcon: Color,
    // Kelime chip'i (DESIGN.md "Chips & Activity Tags")
    val chip: Color,
    val chipBorder: Color,
    val chipText: Color,
)

internal val LightNapsamColors = NapsamColors(
    isDark = false,
    background = Color(0xFFF9F9F8),
    heroGlow = Color(0x00000000),
    topBarDivider = Color(0xCCE4E4E7), // zinc-200/80
    topBarSeparator = Color(0xFFD4D4D8), // zinc-300
    footerDivider = Color(0xFFE4E4E7), // zinc-200
    textPrimary = Color(0xFF18181B), // zinc-900
    textSecondary = Color(0xFF52525B), // zinc-600
    textTertiary = Color(0xFF71717A), // zinc-500
    separator = Color(0xFFD4D4D8), // zinc-300
    divider = Color(0xFFE4E4E7), // zinc-200
    accent = Color(0xFFEA580C), // orange-600
    accentDot = Color(0xFFF97316), // orange-500
    themeIcon = Color(0xFFF97316), // orange-500
    ctaGradientStart = Color(0xFFEA580C),
    ctaGradientEnd = Color(0xFFE75A2D),
    onCta = Color(0xFFFFFFFF),
    ctaShadow = Color(0x33431407), // orange-950/20
    console = Color(0xFFFFFFFF),
    consoleBorder = Color(0xFFE4E4E7),
    card = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE4E4E7),
    valueCard = Color(0xFFFFFFFF),
    valueCardBorder = Color(0xFFE4E4E7),
    overline = Color(0xCCFFF7ED), // orange-50/80
    overlineBorder = Color(0xB3FED7AA), // orange-200/70
    overlineText = Color(0xFF3F3F46), // zinc-700
    budgetItem = Color(0xE6F4F4F5), // zinc-100/90
    budgetItemBorder = Color(0xFFE4E4E7),
    budgetItemLabel = Color(0xFF27272A), // zinc-800
    budgetItemAmount = Color(0xFF71717A),
    selected = Color(0xFF18181B), // zinc-900
    onSelected = Color(0xFFFFFFFF),
    onSelectedMuted = Color(0xFFD4D4D8), // zinc-300
    bandStart = Color(0xFFF4F4F5), // zinc-100
    bandMiddle = Color(0xB3E4E4E7), // zinc-200/70
    bandEnd = Color(0xFFF4F4F5),
    bandOverlay = Color(0x00000000),
    bandBottomBorder = Color(0x99E4E4E7), // zinc-200/60
    categoryChip = Color(0xE6FFFFFF), // white/90
    categoryChipBorder = Color(0xFFE4E4E7),
    categoryChipText = Color(0xFF18181B),
    badge = Color(0xE6FFFFFF),
    badgeBorder = Color(0xFFE4E4E7),
    badgeText = Color(0xFF3F3F46),
    badgeIcon = Color(0xFF71717A),
    well = Color(0xFFFAFAFA), // zinc-50
    wellBorder = Color(0xFFE4E4E7),
    wellText = Color(0xFF3F3F46),
    stepNumber = Color(0xFFEA580C),
    onStepNumber = Color(0xFFFFFFFF),
    fallback = Color(0x80FFF7ED), // orange-50/50
    fallbackBorder = Color(0x99FED7AA), // orange-200/60
    fallbackTitle = Color(0xFF27272A),
    fallbackText = Color(0xFF52525B),
    fallbackIcon = Color(0xFFEA580C),
    secondaryButton = Color(0xFFFFFFFF),
    secondaryButtonBorder = Color(0xFFD4D4D8), // zinc-300
    secondaryButtonText = Color(0xFF3F3F46),
    secondaryButtonIcon = Color(0xFF71717A),
    iconButtonIcon = Color(0xFF52525B),
    chip = Color(0xFFFFFFFF),
    chipBorder = Color(0xFFE6E4DD),
    chipText = Color(0xFF706E6B),
)

internal val DarkNapsamColors = NapsamColors(
    isDark = true,
    background = Color(0xFF0C0C0D),
    heroGlow = Color(0x14EA580C), // rgba(234,88,12,.08)
    topBarDivider = Color(0x0FFFFFFF), // white/6
    topBarSeparator = Color(0xFF27272A), // zinc-800
    footerDivider = Color(0xFF18181B), // zinc-900
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA), // zinc-400
    textTertiary = Color(0xFFA1A1AA),
    separator = Color(0xFF3F3F46), // zinc-700
    divider = Color(0xCC27272A), // zinc-800/80
    accent = Color(0xFFF97316), // orange-500
    accentDot = Color(0xFFF97316),
    themeIcon = Color(0xE6FB923C), // orange-400/90
    ctaGradientStart = Color(0xFFEA580C),
    ctaGradientEnd = Color(0xFFE75A2D),
    onCta = Color(0xFFFFFFFF),
    ctaShadow = Color(0x66431407), // orange-950/40
    console = Color(0xFF141416),
    consoleBorder = Color(0xFF242428),
    card = Color(0xFF151518),
    cardBorder = Color(0xFF26262B),
    valueCard = Color(0xFF131316),
    valueCardBorder = Color(0xCC27272A),
    overline = Color(0xE618181B), // zinc-900/90
    overlineBorder = Color(0xFF27272A),
    overlineText = Color(0xFFA1A1AA),
    budgetItem = Color(0xFF1A1A1D),
    budgetItemBorder = Color(0xCC27272A),
    budgetItemLabel = Color(0xFFD4D4D8), // zinc-300
    budgetItemAmount = Color(0xFFA1A1AA),
    selected = Color(0xFFFFFFFF),
    onSelected = Color(0xFF09090B), // zinc-950
    onSelectedMuted = Color(0xBF09090B), // opacity-75
    bandStart = Color(0xFF3F3F46), // zinc-700
    bandMiddle = Color(0xFF27272A), // zinc-800
    bandEnd = Color(0xFF18181B), // zinc-900
    bandOverlay = Color(0x33000000), // black/20
    bandBottomBorder = Color(0x00000000),
    categoryChip = Color(0x66000000), // black/40
    categoryChipBorder = Color(0x1AFFFFFF), // white/10
    categoryChipText = Color(0xFFD4D4D8),
    badge = Color(0x99000000), // black/60
    badgeBorder = Color(0x1AFFFFFF),
    badgeText = Color(0xFFE4E4E7), // zinc-200
    badgeIcon = Color(0xFFA1A1AA),
    well = Color(0xFF19191D),
    wellBorder = Color(0xCC27272A),
    wellText = Color(0xFFD4D4D8),
    stepNumber = Color(0xFFEA580C),
    onStepNumber = Color(0xFFFFFFFF),
    fallback = Color(0xFF1B1B20),
    fallbackBorder = Color(0xE627272A), // zinc-800/90
    fallbackTitle = Color(0xFFD4D4D8),
    fallbackText = Color(0xFFA1A1AA),
    fallbackIcon = Color(0xFFFB923C), // orange-400
    secondaryButton = Color(0xFF1F1F24),
    secondaryButtonBorder = Color(0xFF27272A),
    secondaryButtonText = Color(0xFFD4D4D8),
    secondaryButtonIcon = Color(0xFFA1A1AA),
    iconButtonIcon = Color(0xFFA1A1AA),
    chip = Color(0xFF1F1F24),
    chipBorder = Color(0xFF27272A),
    chipText = Color(0xFFA1A1AA),
)
