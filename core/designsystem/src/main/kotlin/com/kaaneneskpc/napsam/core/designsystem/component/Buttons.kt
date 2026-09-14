package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

private val CtaIconSize = 16.dp
private val ActionIconSize = 14.dp
private val ToggleIconSize = 16.dp
private val CtaElevation = 10.dp
private val RaisedElevation = 3.dp
private val SubtleElevation = 1.dp
private const val DISABLED_ALPHA = 0.7f

/**
 * Ekrandaki TEK birincil eylem: "NAPSAM?". Turuncu gradyan, gölge, büyük harf (tasarım: CTA).
 */
@Composable
fun NapsamPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.control
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NapsamTheme.spacing.minTouchTarget)
            .shadow(
                elevation = CtaElevation,
                shape = shape,
                ambientColor = colors.ctaShadow,
                spotColor = colors.ctaShadow,
            )
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(colors.ctaGradientStart, colors.ctaGradientEnd)))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = NapsamTheme.spacing.xl, vertical = NapsamTheme.spacing.lgx),
        horizontalArrangement = Arrangement.spacedBy(NapsamTheme.spacing.md, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = NapsamIcons.Bolt,
            contentDescription = null,
            tint = colors.onCta,
            modifier = Modifier.size(CtaIconSize),
        )
        Text(
            text = text.toUpperCase(LocaleList.current),
            style = NapsamTheme.typography.cta,
            color = colors.onCta,
            textAlign = TextAlign.Center,
        )
    }
}

/** "Tamam, yapıyorum": açık temada koyu, koyu temada beyaz zemin; büyük harf, onay ikonu. */
@Composable
fun NapsamCommitButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    ActionButtonLayout(
        text = text.toUpperCase(LocaleList.current),
        icon = NapsamIcons.Check,
        onClick = onClick,
        background = colors.selected,
        borderColor = colors.selected,
        textColor = colors.onSelected,
        iconColor = colors.onSelected,
        elevation = RaisedElevation,
        bold = true,
        modifier = modifier,
    )
}

/** İkincil eylem ("Başka fikir"). */
@Composable
fun NapsamSecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    ActionButtonLayout(
        text = text,
        icon = icon,
        onClick = onClick,
        background = colors.secondaryButton,
        borderColor = colors.secondaryButtonBorder,
        textColor = colors.secondaryButtonText,
        iconColor = colors.secondaryButtonIcon,
        elevation = if (colors.isDark) 0.dp else SubtleElevation,
        bold = false,
        modifier = modifier,
    )
}

@Composable
private fun ActionButtonLayout(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    background: Color,
    borderColor: Color,
    textColor: Color,
    iconColor: Color,
    elevation: Dp,
    bold: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = NapsamTheme.shapes.control
    val spacing = NapsamTheme.spacing
    Row(
        modifier = modifier
            .heightIn(min = spacing.minTouchTarget)
            .shadow(elevation = elevation, shape = shape)
            .clip(shape)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.ml),
        horizontalArrangement = Arrangement.spacedBy(
            if (bold) spacing.md else spacing.sm,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(ActionIconSize))
        Text(
            text = text,
            style = if (bold) NapsamTheme.typography.actionPrimary else NapsamTheme.typography.actionSecondary,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Kaydet (kalp). Görünür metni olmadığı için [contentDescription] zorunlu; durum TalkBack'e toggleable
 * semantiğiyle iletilir. Kaydedilmişken kalp dolu ve aksan renginde (web).
 */
@Composable
fun NapsamIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    checkedIcon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.control
    val spacing = NapsamTheme.spacing
    Box(
        modifier = modifier
            .heightIn(min = spacing.minTouchTarget)
            .widthIn(min = spacing.minTouchTarget)
            .shadow(elevation = if (colors.isDark) 0.dp else SubtleElevation, shape = shape)
            .clip(shape)
            .background(colors.secondaryButton)
            .border(width = 1.dp, color = if (checked) colors.accent else colors.secondaryButtonBorder, shape = shape)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (checked) checkedIcon else icon,
            contentDescription = null,
            tint = if (checked) colors.accent else colors.iconButtonIcon,
            modifier = Modifier.size(ToggleIconSize),
        )
    }
}

/**
 * Kart eylemleri: "Tamam, yapıyorum" kalan genişliği alır, yanında "Başka fikir" ve kalp; hepsi aynı
 * yükseklikte (tasarım). Birincil düğmenin en uzun kelimesi sığmıyorsa (büyük yazı ölçeği) birincil
 * düğme üst satıra tam genişlikte geçer, diğer ikisi alt satırda kalır.
 */
@Composable
fun CardActions(
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
    toggle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = NapsamTheme.spacing.md
    val minTouchTarget = NapsamTheme.spacing.minTouchTarget
    Layout(
        contents = listOf(primary, secondary, toggle),
        modifier = modifier.fillMaxWidth(),
    ) { (primaryMeasurables, secondaryMeasurables, toggleMeasurables), constraints ->
        val primaryMeasurable = primaryMeasurables.single()
        val secondaryMeasurable = secondaryMeasurables.single()
        val toggleMeasurable = toggleMeasurables.single()
        val width = constraints.maxWidth
        val gapPx = gap.roundToPx()
        val minHeight = minTouchTarget.roundToPx()
        val toggleWidth = toggleMeasurable.maxIntrinsicWidth(Constraints.Infinity)
        val secondaryWidth = secondaryMeasurable.maxIntrinsicWidth(Constraints.Infinity)
        val primaryInRow = width - secondaryWidth - toggleWidth - gapPx * 2
        val singleRow = primaryMeasurable.minIntrinsicWidth(Constraints.Infinity) <= primaryInRow

        if (singleRow) {
            val height = maxOf(
                primaryMeasurable.minIntrinsicHeight(primaryInRow),
                secondaryMeasurable.minIntrinsicHeight(secondaryWidth),
                toggleMeasurable.minIntrinsicHeight(toggleWidth),
                minHeight,
            )
            val primaryPlaceable = primaryMeasurable.measure(Constraints.fixed(primaryInRow, height))
            val secondaryPlaceable = secondaryMeasurable.measure(Constraints.fixed(secondaryWidth, height))
            val togglePlaceable = toggleMeasurable.measure(Constraints.fixed(toggleWidth, height))
            layout(width, height) {
                primaryPlaceable.placeRelative(0, 0)
                secondaryPlaceable.placeRelative(primaryInRow + gapPx, 0)
                togglePlaceable.placeRelative(width - toggleWidth, 0)
            }
        } else {
            val primaryHeight = maxOf(primaryMeasurable.minIntrinsicHeight(width), minHeight)
            val secondaryInRow = width - toggleWidth - gapPx
            val bottomHeight = maxOf(
                secondaryMeasurable.minIntrinsicHeight(secondaryInRow),
                toggleMeasurable.minIntrinsicHeight(toggleWidth),
                minHeight,
            )
            val primaryPlaceable = primaryMeasurable.measure(Constraints.fixed(width, primaryHeight))
            val secondaryPlaceable = secondaryMeasurable.measure(Constraints.fixed(secondaryInRow, bottomHeight))
            val togglePlaceable = toggleMeasurable.measure(Constraints.fixed(toggleWidth, bottomHeight))
            val bottomY = primaryHeight + gapPx
            layout(width, bottomY + bottomHeight) {
                primaryPlaceable.placeRelative(0, 0)
                secondaryPlaceable.placeRelative(0, bottomY)
                togglePlaceable.placeRelative(width - toggleWidth, bottomY)
            }
        }
    }
}

/** Metinsiz ikon düğmesi (tema, kapat). Görsel küçük, dokunma alanı en az 48dp. */
@Composable
fun NapsamIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = ToggleIconSize,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(NapsamTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}
