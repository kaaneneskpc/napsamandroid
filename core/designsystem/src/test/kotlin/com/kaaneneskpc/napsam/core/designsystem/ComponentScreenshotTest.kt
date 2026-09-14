package com.kaaneneskpc.napsam.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tasarım sistemi ekran görüntüleri: Stitch mobil ana ekranı açık/koyu tema ve %200 yazı ölçeği,
 * ayrıca tasarımda görünmeyen web durumları (spesifikasyon B13). Referanslar `src/test/screenshots`
 * altında; `verifyRoborazziDebug` görsel farkı yakalar. Tasarım görselleriyle karşılaştırma için:
 * `docs/design/mobile-light.png`, `mobile-dark.png`.
 *
 * - Compose saati durdurulur: sonsuz animasyonlar (nabız, iskelet) beklemede hiç boşa çıkmıyor ve
 *   testi kilitliyordu; durdurulmuş saat ayrıca her çalıştırmada aynı kareyi verir.
 * - Robolectric SDK 36: Android 17 (SDK 37) çalışma zamanında Compose test altyapısının çağırdığı
 *   `InputManager.getInstance()` yok (NoSuchMethodException). Uygulamanın targetSdk'sı değişmez.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ROBOLECTRIC_SDK], qualifiers = "w393dp-h2000dp-xhdpi")
class ComponentScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun anaekran_acik() = capture("anaekran_acik", dark = false) { HomePageCatalog() }

    @Test
    fun anaekran_koyu() = capture("anaekran_koyu", dark = true) { HomePageCatalog() }

    // Yazı %200'de sayfa uzar; pencere kısa kalırsa alttaki tanıtım kutuları ve alt bilgi görüntüye girmez.
    @Test
    @Config(qualifiers = "w393dp-h3600dp-xhdpi")
    fun anaekran_yazi200() = capture("anaekran_yazi200", dark = false, fontScale = 2f) { HomePageCatalog() }

    @Test
    fun durumlar_acik() = capture("durumlar_acik", dark = false) { StatesCatalog() }

    @Test
    fun durumlar_koyu() = capture("durumlar_koyu", dark = true) { StatesCatalog() }

    private fun capture(name: String, dark: Boolean, fontScale: Float = 1f, content: @Composable () -> Unit) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density = density.density, fontScale = fontScale)) {
                NapsamTheme(darkTheme = dark, content = content)
            }
        }
        composeRule.onRoot().captureRoboImage("${roborazziSystemPropertyOutputDirectory()}/$name.png")
    }
}

private const val ROBOLECTRIC_SDK = 36
