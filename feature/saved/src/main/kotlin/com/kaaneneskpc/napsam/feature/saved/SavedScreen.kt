package com.kaaneneskpc.napsam.feature.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBackLink
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBadge
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamIconButton
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.badges
import com.kaaneneskpc.napsam.core.ui.toTexts

@Composable
fun SavedRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SavedScreen(
        state = uiState,
        onRemove = viewModel::onRemove,
        onBack = onBack,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/** Web `saved.html`: klasör yok, etiket yok, sıralama yok — yalnızca liste. */
@Composable
fun SavedScreen(
    state: SavedUiState,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    val typography = NapsamTheme.typography
    val layoutDirection = LocalLayoutDirection.current

    // Zemin uygulama kabugunda (NapsamApp) bir kez cizilir; ekranlar uzerine yerlesir.
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + spacing.screenHorizontal,
                end = contentPadding.calculateEndPadding(layoutDirection) + spacing.screenHorizontal,
                top = contentPadding.calculateTopPadding() + spacing.lg,
                bottom = contentPadding.calculateBottomPadding() + spacing.section,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item(key = "header") {
                Column {
                    NapsamBackLink(text = stringResource(R.string.saved_back), onClick = onBack)
                    Text(
                        text = stringResource(R.string.saved_title),
                        style = typography.cardTitle,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .padding(top = spacing.lg, bottom = spacing.xs)
                            .semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.saved_subtitle),
                        style = typography.valueText,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = spacing.lg),
                    )
                }
            }
            if (state.items.isEmpty()) {
                if (state.loaded) {
                    item(key = "empty") { SavedEmpty() }
                }
            } else {
                items(items = state.items, key = { it.slug }) { suggestion ->
                    SavedRow(suggestion = suggestion, onRemove = { onRemove(suggestion.slug) })
                }
            }
        }
    }
}

@Composable
private fun SavedRow(suggestion: Suggestion, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    val typography = NapsamTheme.typography
    val shape = NapsamTheme.shapes.control
    val resources = LocalResources.current
    val badges = remember(suggestion, resources) { suggestion.badges().toTexts(resources) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(width = 1.dp, color = colors.cardBorder, shape = shape)
            .padding(spacing.lgx),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = suggestion.title, style = typography.valueTitle, color = colors.textPrimary)
            Text(
                text = suggestion.hook,
                style = typography.valueText,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = spacing.xxs, bottom = spacing.md),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                NapsamBadge(text = badges.cost)
                NapsamBadge(text = badges.duration, icon = NapsamIcons.Clock)
                NapsamBadge(text = badges.place, icon = NapsamIcons.Pin)
            }
        }
        NapsamIconButton(
            icon = NapsamIcons.Close,
            contentDescription = stringResource(R.string.saved_remove),
            onClick = onRemove,
            tint = colors.iconButtonIcon,
        )
    }
}

@Composable
private fun SavedEmpty(modifier: Modifier = Modifier) {
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    val radius = 16.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = colors.separator,
                    cornerRadius = CornerRadius(radius.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = spacing.xl, vertical = spacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.saved_empty_title),
            style = NapsamTheme.typography.valueTitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.saved_empty_body),
            style = NapsamTheme.typography.valueText,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.md),
        )
    }
}
