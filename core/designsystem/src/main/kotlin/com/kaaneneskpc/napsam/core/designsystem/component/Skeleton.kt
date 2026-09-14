package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

private const val SHIMMER_DURATION_MS = 1200

/**
 * Yükleme parıltısı (web `.sk`): 1 saniyeyi geçen beklemede kartın metinleri yerinde bu blokla
 * gösterilir. Ayrı bir iskelet kart düzeni yoktur (web birebirliği).
 */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val transition = rememberInfiniteTransition(label = "iskelet")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_DURATION_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "iskelet-parilti",
    )
    Box(
        modifier = modifier
            .clip(NapsamTheme.shapes.tag)
            .background(colors.divider)
            .drawBehind {
                val start = -size.width + size.width * 2 * progress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.divider, colors.well, colors.divider),
                        start = Offset(start, 0f),
                        end = Offset(start + size.width, 0f),
                    ),
                )
            },
    )
}
