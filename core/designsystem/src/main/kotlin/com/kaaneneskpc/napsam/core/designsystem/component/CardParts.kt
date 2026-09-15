package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

private val BandMinHeight = 112.dp

// sp: numara dairesi yazı ölçeğiyle büyür, %200'de rakam kırpılmaz.
private val StepNumberSize = 20.sp
private val FallbackIconSize = 16.dp
private val LightPanelElevation = 1.dp
private val DarkPanelElevation = 16.dp

@Composable
private fun panelElevation(): Dp = if (NapsamTheme.colors.isDark) DarkPanelElevation else LightPanelElevation

/** Bütçe ve NAPSAM? düğmesini taşıyan panel (tasarım: `rounded-2xl p-3.5`). */
@Composable
fun NapsamConsole(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.panel
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = panelElevation(), shape = shape)
            .clip(shape)
            .background(colors.console)
            .border(width = 1.dp, color = colors.consoleBorder, shape = shape)
            .padding(NapsamTheme.spacing.lgx),
        content = content,
    )
}

/** Öneri kartı kabuğu (tasarım: `rounded-2xl overflow-hidden`). İçeriği özellik ekranı bileşimler. */
@Composable
fun NapsamCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.panel
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = panelElevation(), shape = shape)
            .clip(shape)
            .background(colors.card)
            .border(width = 1.dp, color = colors.cardBorder, shape = shape),
        content = content,
    )
}

/**
 * Kartın üst bandı: nötr gradyan, üstte kategori etiketi, altta rozetler (tasarım: `h-28`).
 * Yazı ölçeği büyüdüğünde rozetler taşmasın diye yükseklik en az değerdir.
 */
@Composable
fun CardBand(categoryLabel: String, modifier: Modifier = Modifier, badges: @Composable FlowRowScope.() -> Unit) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BandMinHeight)
            .background(Brush.linearGradient(listOf(colors.bandStart, colors.bandMiddle, colors.bandEnd)))
            .background(colors.bandOverlay)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawRect(
                    color = colors.bandBottomBorder,
                    topLeft = Offset(0f, size.height - stroke),
                    size = Size(size.width, stroke),
                )
            }
            .padding(spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.lg, Alignment.Top),
    ) {
        Text(
            text = categoryLabel.toUpperCase(ContentLocale),
            style = NapsamTheme.typography.categoryChip,
            color = colors.categoryChipText,
            modifier = Modifier
                .clip(NapsamTheme.shapes.tag)
                .background(colors.categoryChip)
                .border(width = 1.dp, color = colors.categoryChipBorder, shape = NapsamTheme.shapes.tag)
                .padding(horizontal = spacing.ml, vertical = spacing.xs),
        )
        Box(modifier = Modifier.weight(1f, fill = false))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            content = badges,
        )
    }
}

/** Blok başlığı ("Nasıl yapılır (3 adım)", "Gerekenler"): küçük, büyük harf. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(ContentLocale),
        style = NapsamTheme.typography.stepsLabel,
        color = NapsamTheme.colors.textTertiary,
        modifier = modifier,
    )
}

/** Numaralı adımlar. Her adım TalkBack'te numarasıyla tek düğüm olarak okunur. */
@Composable
fun StepList(steps: kotlinx.collections.immutable.ImmutableList<String>, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val shape = NapsamTheme.shapes.control
    val numberSize = with(LocalDensity.current) { StepNumberSize.toDp() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.well)
                    .border(width = 1.dp, color = colors.wellBorder, shape = shape)
                    .padding(spacing.ml)
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(numberSize)
                        .clip(NapsamTheme.shapes.pill)
                        .background(colors.stepNumber),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "${index + 1}", style = NapsamTheme.typography.stepNumber, color = colors.onStepNumber)
                }
                Text(text = step, style = NapsamTheme.typography.step, color = colors.wellText)
            }
        }
    }
}

/** "Olmazsa alternatifi": her kartta zorunlu blok. */
@Composable
fun FallbackBlock(title: String, text: String, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val shape = NapsamTheme.shapes.control
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.fallback)
            .border(width = 1.dp, color = colors.fallbackBorder, shape = shape)
            .padding(spacing.lg)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(spacing.ml),
    ) {
        Icon(
            imageVector = NapsamIcons.Swap,
            contentDescription = null,
            tint = colors.fallbackIcon,
            modifier = Modifier
                .padding(top = spacing.xxs)
                .size(FallbackIconSize),
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(
                text = title.toUpperCase(ContentLocale),
                style = NapsamTheme.typography.fallbackTitle,
                color = colors.fallbackTitle,
            )
            Text(text = text, style = NapsamTheme.typography.fallbackText, color = colors.fallbackText)
        }
    }
}

/**
 * "Tamam, yapıyorum" sonrası mesaj ve kapatma düğmesi (web). Kibar canlı bölge olarak TalkBack'e duyurulur.
 */
@Composable
fun CommitMessage(
    title: String,
    body: String,
    closeContentDescription: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val shape = NapsamTheme.shapes.control
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(width = 1.dp, color = colors.accent, shape = shape)
            .padding(start = spacing.lgx, top = spacing.xs, bottom = spacing.xs)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(text = title, style = NapsamTheme.typography.valueTitle, color = colors.textPrimary)
            Text(text = body, style = NapsamTheme.typography.valueText, color = colors.textSecondary)
        }
        NapsamIconButton(
            icon = NapsamIcons.Close,
            contentDescription = closeContentDescription,
            onClick = onClose,
            tint = colors.iconButtonIcon,
        )
    }
}
