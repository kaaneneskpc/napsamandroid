package com.kaaneneskpc.napsam.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kaaneneskpc.napsam.core.designsystem.component.CardActions
import com.kaaneneskpc.napsam.core.designsystem.component.CardBand
import com.kaaneneskpc.napsam.core.designsystem.component.FallbackBlock
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBadge
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamCard
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamCommitButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamIconToggleButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamSecondaryButton
import com.kaaneneskpc.napsam.core.designsystem.component.NeedsRow
import com.kaaneneskpc.napsam.core.designsystem.component.SectionLabel
import com.kaaneneskpc.napsam.core.designsystem.component.StepList
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.badges
import kotlinx.collections.immutable.toImmutableList

/**
 * Öneri kartı (spesifikasyon A6.4, web `#card`): kategori bandı ve rozetler, başlık, kanca, adımlar,
 * gerekenler (varsa), olmazsa alternatifi ve eylemler (Tamam, yapıyorum · Başka fikir · kalp).
 */
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    saved: Boolean,
    onCommit: () -> Unit,
    onAnotherIdea: () -> Unit,
    onSaveToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val badges = remember(suggestion, resources) { suggestion.badges().toTexts(resources) }
    val steps = remember(suggestion.steps) { suggestion.steps.toImmutableList() }
    val requiredItems = remember(suggestion.requiredItems) { suggestion.requiredItems.toImmutableList() }
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    val typography = NapsamTheme.typography

    NapsamCard(modifier = modifier) {
        CardBand(categoryLabel = stringResource(suggestion.category.labelRes)) {
            NapsamBadge(text = badges.cost)
            NapsamBadge(text = badges.duration, icon = NapsamIcons.Clock)
            NapsamBadge(text = badges.place, icon = NapsamIcons.Pin)
            NapsamBadge(text = badges.companion, icon = NapsamIcons.Person)
        }
        Column(modifier = Modifier.padding(spacing.xl)) {
            Text(
                text = suggestion.title,
                style = typography.cardTitle,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = suggestion.hook,
                style = typography.cardHook,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = spacing.sm, bottom = spacing.xxl),
            )
            SectionLabel(
                text = pluralStringResource(R.plurals.card_steps_label, steps.size, steps.size),
                modifier = Modifier.padding(bottom = spacing.md),
            )
            StepList(steps = steps)
            if (requiredItems.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.card_needs_label),
                    modifier = Modifier.padding(top = spacing.xl, bottom = spacing.md),
                )
                NeedsRow(items = requiredItems)
            }
            FallbackBlock(
                title = stringResource(R.string.card_fallback_title),
                text = suggestion.fallback,
                modifier = Modifier.padding(top = spacing.xl, bottom = spacing.xxl),
            )
            CardActions(
                primary = { NapsamCommitButton(text = stringResource(R.string.card_commit), onClick = onCommit) },
                secondary = {
                    NapsamSecondaryButton(
                        text = stringResource(R.string.card_another_idea),
                        icon = NapsamIcons.Refresh,
                        onClick = onAnotherIdea,
                    )
                },
                toggle = {
                    NapsamIconToggleButton(
                        checked = saved,
                        onCheckedChange = { onSaveToggle() },
                        icon = NapsamIcons.Heart,
                        checkedIcon = NapsamIcons.HeartFilled,
                        contentDescription = stringResource(if (saved) R.string.card_unsave else R.string.card_save),
                    )
                },
            )
        }
    }
}
