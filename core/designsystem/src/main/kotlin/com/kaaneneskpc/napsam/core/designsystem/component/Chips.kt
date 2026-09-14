package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import kotlinx.collections.immutable.ImmutableList

private val BadgeIconSize = 12.dp
private val SubtleElevation = 1.dp

/**
 * Kelime chip'i (DESIGN.md "Chips & Activity Tags"): 4dp köşe; seçiliyken koyu mürekkep zemin.
 * Görsel küçük, dokunma alanı en az 48dp. Seçim onay kutusu semantiğiyle okunur.
 */
@Composable
fun NapsamChip(text: String, selected: Boolean, onSelectedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.tag
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = onSelectedChange),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = NapsamTheme.typography.chip,
            color = if (selected) colors.onSelected else colors.chipText,
            modifier = Modifier
                .clip(shape)
                .background(if (selected) colors.selected else colors.chip)
                .border(width = 1.dp, color = if (selected) colors.selected else colors.chipBorder, shape = shape)
                .padding(horizontal = NapsamTheme.spacing.sm, vertical = NapsamTheme.spacing.xs),
        )
    }
}

/** Kart bandındaki rozet (maliyet, süre, yer, kiminle): isteğe bağlı ikon + kısa metin. */
@Composable
fun NapsamBadge(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.badge
    val spacing = NapsamTheme.spacing
    Row(
        modifier = modifier
            .shadow(elevation = if (colors.isDark) 0.dp else SubtleElevation, shape = shape)
            .clip(shape)
            .background(colors.badge)
            .border(width = 1.dp, color = colors.badgeBorder, shape = shape)
            .padding(horizontal = spacing.md, vertical = spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.badgeIcon,
                modifier = Modifier.size(BadgeIconSize),
            )
        }
        Text(text = text, style = NapsamTheme.typography.badge, color = colors.badgeText, maxLines = 1)
    }
}

/** "Gerekenler" (web): kesik çizgili kenarlıkla en fazla 3 kısa öğe. */
@Composable
fun NeedsRow(items: ImmutableList<String>, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        items.forEach { item ->
            Text(
                text = item,
                style = NapsamTheme.typography.badge,
                color = colors.textSecondary,
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            color = colors.separator,
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                            ),
                        )
                    }
                    .padding(horizontal = spacing.md, vertical = spacing.xxs),
            )
        }
    }
}
