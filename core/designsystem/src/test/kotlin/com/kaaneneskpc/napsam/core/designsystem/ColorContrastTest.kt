package com.kaaneneskpc.napsam.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.kaaneneskpc.napsam.core.designsystem.theme.DarkNapsamColors
import com.kaaneneskpc.napsam.core.designsystem.theme.LightNapsamColors
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Metin kontrastı (WCAG AA 4.5:1, spesifikasyon B12.4).
 *
 * Renkler Stitch son tasarımıyla birebir aynıdır. Tasarımdan gelen şu istisnalar bilinçli olarak
 * korunur (web birebirliği, proje sahibine bildirildi) ve [tasarim istisnalari AA altinda kaliyor]
 * testinde belgelenir; başka HER metin çifti AA olmak zorundadır:
 * - Açık temada turuncu (orange-600) metin: "CANLI ÖNERİ", tanıtım kutusu başlığı.
 * - Turuncu zemin üzerinde beyaz: NAPSAM? düğmesi, adım numarası, sayı rozeti.
 * - Açık temada bütçe kademesindeki küçük tutar metni (zinc-500 / zinc-100).
 */
class ColorContrastTest {
    private val light = LightNapsamColors
    private val dark = DarkNapsamColors

    @Test
    fun `basliklar ve govde metni tum yuzeylerde AA`() {
        listOf(light, dark).forEach { c ->
            val surfaces = listOf(c.background, c.console, c.card, c.valueCard, c.well.compositeOver(c.card))
            surfaces.forEach { surface ->
                assertAA("${c.isDark} textPrimary", c.textPrimary, surface)
                assertAA("${c.isDark} textSecondary", c.textSecondary, surface)
                assertAA("${c.isDark} textTertiary", c.textTertiary, surface)
            }
        }
    }

    @Test
    fun `kontrol metinleri AA`() {
        listOf(light, dark).forEach { c ->
            val t = c.isDark
            assertAA("$t adım metni", c.wellText, c.well.compositeOver(c.card))
            assertAA("$t seçili", c.onSelected, c.selected)
            assertAA("$t seçili tutar", c.onSelectedMuted.compositeOver(c.selected), c.selected)
            assertAA("$t bütçe etiketi", c.budgetItemLabel, c.budgetItem.compositeOver(c.console))
            assertAA("$t ikincil düğme", c.secondaryButtonText, c.secondaryButton)
            assertAA("$t chip", c.chipText, c.chip)
            assertAA("$t olmazsa başlık", c.fallbackTitle, c.fallback.compositeOver(c.card))
            assertAA("$t olmazsa metin", c.fallbackText, c.fallback.compositeOver(c.card))
            assertAA("$t başlık rozeti", c.overlineText, c.overline.compositeOver(c.background))
        }
        assertAA("koyu bütçe tutarı", dark.budgetItemAmount, dark.budgetItem.compositeOver(dark.console))
        assertAA("koyu aksan metni", dark.accent, dark.background)
    }

    @Test
    fun `kart bandi etiket ve rozetleri AA`() {
        listOf(light, dark).forEach { c ->
            val bandBrightest = c.bandOverlay.compositeOver(c.bandMiddle.compositeOver(c.bandStart))
            val bandDarkest = c.bandOverlay.compositeOver(c.bandEnd)
            listOf(bandBrightest, bandDarkest).forEach { band ->
                assertAA("${c.isDark} kategori", c.categoryChipText, c.categoryChip.compositeOver(band))
                assertAA("${c.isDark} rozet", c.badgeText, c.badge.compositeOver(band))
            }
        }
    }

    @Test
    fun `tasarim istisnalari AA altinda kaliyor`() {
        // Bu test, istisnaların farkında olunduğunu belgeler; tasarım değişip AA'ya çıkarsa güncellenir.
        assertTrue(contrast(light.accent, light.background) < AA)
        assertTrue(contrast(light.onCta, light.ctaGradientStart) < AA)
        assertTrue(contrast(light.onStepNumber, light.stepNumber) < AA)
        assertTrue(contrast(light.budgetItemAmount, light.budgetItem.compositeOver(light.console)) < AA)
    }

    private fun assertAA(label: String, foreground: Color, background: Color) {
        val ratio = contrast(foreground, background)
        assertTrue("$label kontrastı %.2f (en az $AA)".format(ratio), ratio >= AA)
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private companion object {
        const val AA = 4.5
    }
}
