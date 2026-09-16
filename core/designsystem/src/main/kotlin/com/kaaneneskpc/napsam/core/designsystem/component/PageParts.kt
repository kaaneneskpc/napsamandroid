package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

private val BackIconSize = 16.dp
private val PulseDotSize = 6.dp
private val ChevronSize = 12.dp
private val SeparatorHeight = 12.dp
private const val PULSE_DURATION_MS = 1000
private const val PULSE_MIN_ALPHA = 0.45f
private const val CHEVRON_ROTATION_MS = 200
private const val HERO_GLOW_CENTER_Y = 0.18f
private const val HERO_GLOW_RADIUS = 0.7f

/**
 * Sayfa zemini. Koyu temada üst bölümde hafif turuncu ışıma vardır (tasarım: `hero-glow`,
 * `radial-gradient(circle at 50% 18%, …)`).
 */
@Composable
fun NapsamBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = NapsamTheme.colors
    Box(
        modifier = modifier
            .background(colors.background)
            .drawBehind {
                if (colors.heroGlow.alpha > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.heroGlow, Color.Transparent),
                            center = Offset(size.width / 2f, size.height * HERO_GLOW_CENTER_Y),
                            radius = size.maxDimension * HERO_GLOW_RADIUS,
                        ),
                    )
                }
            },
        content = content,
    )
}

/**
 * Üst bar: "napsam." logosu, Kaydedilenler (kayıt varsa sayı rozeti, web), ayırıcı ve tema düğmesi.
 */
@Composable
fun NapsamTopBar(
    brand: String,
    savedLabel: String,
    savedCount: Int,
    themeToggleDescription: String,
    onSavedClick: () -> Unit,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    Column(modifier = modifier.fillMaxWidth()) {
        // Büyük yazı ölçeğinde dar ekranda sığmazsa sağ grup kelime bölünmeden alt satıra geçer.
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = buildAnnotatedString {
                    append(brand)
                    withStyle(SpanStyle(color = colors.accentDot)) { append(".") }
                },
                style = NapsamTheme.typography.logo,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(NapsamTheme.shapes.tag)
                        .clickable(role = Role.Button, onClick = onSavedClick)
                        .padding(horizontal = spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(text = savedLabel, style = NapsamTheme.typography.topBarAction, color = colors.textSecondary)
                    if (savedCount > 0) {
                        CountBadge(text = savedCount.toString())
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(SeparatorHeight)
                        .background(colors.topBarSeparator),
                )
                NapsamIconButton(
                    icon = if (colors.isDark) NapsamIcons.Moon else NapsamIcons.Sun,
                    contentDescription = themeToggleDescription,
                    onClick = onThemeToggle,
                    tint = colors.themeIcon,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.topBarDivider),
        )
    }
}

@Composable
private fun CountBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = NapsamTheme.typography.stepNumber,
        color = NapsamTheme.colors.onCta,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(NapsamTheme.shapes.pill)
            .background(NapsamTheme.colors.ctaGradientStart)
            .padding(horizontal = NapsamTheme.spacing.sm, vertical = NapsamTheme.spacing.xxs),
    )
}

/** Alt ekranların başındaki "← Ana ekran" bağlantısı (web `.navlink`). */
@Composable
fun NapsamBackLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = NapsamTheme.spacing
    Row(
        modifier = modifier
            .heightIn(min = spacing.minTouchTarget)
            .clip(NapsamTheme.shapes.tag)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = NapsamIcons.Back,
            contentDescription = null,
            tint = NapsamTheme.colors.textSecondary,
            modifier = Modifier.size(BackIconSize),
        )
        Text(text = text, style = NapsamTheme.typography.topBarAction, color = NapsamTheme.colors.textSecondary)
    }
}

/** Başlık üstündeki hap: nabız noktası + büyük harf metin (tasarım: "KARAR VERME MOTORU · …"). */
@Composable
fun OverlinePill(text: String, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val shape = NapsamTheme.shapes.pill
    val transition = rememberInfiniteTransition(label = "nabiz")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MS), RepeatMode.Reverse),
        label = "nabiz-alfa",
    )
    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.overline)
            .border(width = 1.dp, color = colors.overlineBorder, shape = shape)
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PulseDotSize)
                .alpha(pulse)
                .clip(shape)
                .background(colors.accentDot),
        )
        Text(
            text = text.toUpperCase(ContentLocale),
            style = NapsamTheme.typography.overline,
            color = colors.overlineText,
        )
    }
}

/** NAPSAM? altındaki not ("Tek dokunuş · Tek kesin karar · …"). */
@Composable
fun CtaNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(ContentLocale),
        style = NapsamTheme.typography.ctaNote,
        color = NapsamTheme.colors.textTertiary,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Panel içi ince ayırıcı çizgi. */
@Composable
fun NapsamDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NapsamTheme.colors.divider),
    )
}

/**
 * "İstersen daralt — kelime seç ya da yaz" (web): kapalı başlar; açıkken ok döner. Seçim varsa kısa
 * özet rozeti gösterilir (web `refine__count`). Durum TalkBack'e "açık/kapalı" olarak okunur.
 */
@Composable
fun RefineToggle(
    text: String,
    expanded: Boolean,
    stateDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(CHEVRON_ROTATION_MS),
        label = "daralt-ok",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.minTouchTarget)
            .clip(NapsamTheme.shapes.tag)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.stateDescription = stateDescription },
        horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = NapsamIcons.ChevronDown,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier
                .size(ChevronSize)
                .rotate(rotation),
        )
        Text(text = text, style = NapsamTheme.typography.refineToggle, color = colors.textTertiary)
        if (summary != null) {
            CountBadge(text = summary)
        }
    }
}

/** "CANLI ÖNERİ / HEMEN ŞİMDİ BAŞLA". */
@Composable
fun StageHeader(accentText: String, secondaryText: String, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val style = NapsamTheme.typography.stageHeader
    Row(
        modifier = modifier.padding(horizontal = NapsamTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(NapsamTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = accentText.toUpperCase(ContentLocale), style = style, color = colors.accent)
        Text(text = "/", style = style, color = colors.separator)
        Text(text = secondaryText.toUpperCase(ContentLocale), style = style, color = colors.textTertiary)
    }
}

/** Ana ekrandaki tanıtım kutusu ("01 / Kararsızlık yok · Tekil odak · …"). */
@Composable
fun ValuePropCard(kicker: String, title: String, text: String, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.control
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (colors.isDark) 0.dp else 1.dp, shape = shape)
            .clip(shape)
            .background(colors.valueCard)
            .border(width = 1.dp, color = colors.valueCardBorder, shape = shape)
            .padding(NapsamTheme.spacing.lgx),
        verticalArrangement = Arrangement.spacedBy(NapsamTheme.spacing.xs),
    ) {
        Text(
            text = kicker.toUpperCase(ContentLocale),
            style = NapsamTheme.typography.valueKicker,
            color = colors.accent,
        )
        Text(text = title, style = NapsamTheme.typography.valueTitle, color = colors.textPrimary)
        Text(text = text, style = NapsamTheme.typography.valueText, color = colors.textSecondary)
    }
}

/** Alt bilgi: üst çizgi, ortalı metin ve altı çizili "Gizlilik" bağlantısı. */
@Composable
fun NapsamFooter(text: String, linkText: String, onLinkClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.footerDivider),
        )
        Text(
            text = buildAnnotatedString {
                append(text)
                append(" ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "gizlilik",
                        styles = TextLinkStyles(
                            SpanStyle(color = colors.textSecondary, textDecoration = TextDecoration.Underline),
                        ),
                        linkInteractionListener = { onLinkClick() },
                    ),
                ) { append(linkText) }
            },
            style = NapsamTheme.typography.footer,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NapsamTheme.spacing.xl),
        )
    }
}
