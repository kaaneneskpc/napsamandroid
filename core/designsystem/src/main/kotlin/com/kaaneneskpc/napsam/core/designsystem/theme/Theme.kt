package com.kaaneneskpc.napsam.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNapsamColors = staticCompositionLocalOf { LightNapsamColors }
val LocalNapsamTypography = staticCompositionLocalOf { DefaultNapsamTypography }
val LocalNapsamSpacing = staticCompositionLocalOf { NapsamSpacing() }
val LocalNapsamShapes = staticCompositionLocalOf { NapsamShapes() }

/**
 * NAPSAM teması. Varsayılan olarak sistem temasını izler; üst bardaki tema düğmesiyle seçilen tema
 * [darkTheme] ile verilir (web'deki gibi).
 *
 * Material 3 yalnızca altyapıdır (ripple, metin alanı imleci); ekranlar [NapsamTheme] token'larını kullanır.
 */
@Composable
fun NapsamTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkNapsamColors else LightNapsamColors
    CompositionLocalProvider(
        LocalNapsamColors provides colors,
        LocalNapsamTypography provides DefaultNapsamTypography,
        LocalNapsamSpacing provides NapsamSpacing(),
        LocalNapsamShapes provides NapsamShapes(),
    ) {
        MaterialTheme(colorScheme = colors.toMaterialColorScheme(), content = content)
    }
}

object NapsamTheme {
    val colors: NapsamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNapsamColors.current

    val typography: NapsamTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNapsamTypography.current

    val spacing: NapsamSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalNapsamSpacing.current

    val shapes: NapsamShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalNapsamShapes.current
}

private fun NapsamColors.toMaterialColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = ctaGradientStart,
        onPrimary = onCta,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        onSurfaceVariant = textSecondary,
        outline = secondaryButtonBorder,
        outlineVariant = cardBorder,
    )
}
