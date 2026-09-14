package com.kaaneneskpc.napsam.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Aralık token'ları: Stitch mobil tasarımındaki Tailwind değerleri (0.25rem = 4dp). */
@Immutable
data class NapsamSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 6.dp,
    val md: Dp = 8.dp,
    val ml: Dp = 10.dp,
    val lg: Dp = 12.dp,
    val lgx: Dp = 14.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 20.dp,
    val xxxl: Dp = 24.dp,
    val section: Dp = 32.dp,
    /** Ekran yatay kenar boşluğu (`px-4`). */
    val screenHorizontal: Dp = 16.dp,
    /** Tüm dokunma hedeflerinin alt sınırı (spesifikasyon B12). */
    val minTouchTarget: Dp = 48.dp,
)

/** Köşe yarıçapları: `rounded` 4dp, `rounded-md` 6dp, `rounded-xl` 12dp, `rounded-2xl` 16dp. */
@Immutable
data class NapsamShapes(
    val tag: Shape = RoundedCornerShape(4.dp),
    val badge: Shape = RoundedCornerShape(6.dp),
    val control: Shape = RoundedCornerShape(12.dp),
    val panel: Shape = RoundedCornerShape(16.dp),
    val pill: Shape = CircleShape,
)
