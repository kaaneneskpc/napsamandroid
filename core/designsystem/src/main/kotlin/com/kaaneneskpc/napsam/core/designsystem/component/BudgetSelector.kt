package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import kotlinx.collections.immutable.ImmutableList

/** Bütçe seçicideki bir kademe. [label] config'teki ad ("Bedava"); "₺" öneki bileşende (web şablonu gibi). */
@Immutable
data class BudgetOption(val key: String, val label: String, val amountText: String)

private val SelectedElevation = 3.dp

/**
 * Bütçe kademeleri: tasarımda tek satırda 5 sütun, tek dokunuşla seçim. Kelimeler bölünmeden
 * sığmıyorsa (dar ekran, büyük yazı ölçeği) sütun sayısı azalır. Seçim radyo grubu semantiğiyle okunur.
 */
@Composable
fun BudgetSelector(
    options: ImmutableList<BudgetOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = NapsamTheme.spacing.sm
    val minTouchTarget = NapsamTheme.spacing.minTouchTarget
    Layout(
        modifier = modifier.selectableGroup(),
        content = {
            options.forEach { option ->
                BudgetItem(
                    option = option,
                    selected = option.key == selectedKey,
                    onClick = { onSelect(option.key) },
                )
            }
        },
    ) { measurables, constraints ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) {}
        val gapPx = gap.roundToPx()
        val neededWidth = measurables.maxOf { it.minIntrinsicWidth(Constraints.Infinity) }
        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            neededWidth * measurables.size + gapPx * (measurables.size - 1)
        }
        val columns = (measurables.size downTo 1).firstOrNull { count ->
            (width - gapPx * (count - 1)) / count >= neededWidth
        } ?: 1
        val itemWidth = (width - gapPx * (columns - 1)) / columns
        val rows = measurables.chunked(columns)
        val rowHeights = rows.map { row ->
            maxOf(row.maxOf { it.minIntrinsicHeight(itemWidth) }, minTouchTarget.roundToPx())
        }
        val placeables = rows.mapIndexed { index, row ->
            row.map { it.measure(Constraints.fixed(itemWidth, rowHeights[index])) }
        }
        val height = rowHeights.sum() + gapPx * (rows.size - 1)
        layout(width, height) {
            var y = 0
            placeables.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { column, placeable ->
                    placeable.placeRelative(x = column * (itemWidth + gapPx), y = y)
                }
                y += rowHeights[rowIndex] + gapPx
            }
        }
    }
}

@Composable
private fun BudgetItem(option: BudgetOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.control
    val spacing = NapsamTheme.spacing
    val typography = NapsamTheme.typography
    Column(
        modifier = modifier
            .shadow(elevation = if (selected) SelectedElevation else 0.dp, shape = shape)
            .clip(shape)
            .background(if (selected) colors.selected else colors.budgetItem)
            .border(width = 1.dp, color = if (selected) colors.selected else colors.budgetItemBorder, shape = shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = spacing.xs, vertical = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            // Bölünmez boşluk: dar sütunda "₺" etiketten ayrı satıra düşmesin.
            text = "₺ ${option.label}",
            style = if (selected) {
                typography.budgetLabel
            } else {
                typography.budgetLabel.copy(
                    fontWeight = typography.actionSecondary.fontWeight,
                )
            },
            color = if (selected) colors.onSelected else colors.budgetItemLabel,
            textAlign = TextAlign.Center,
        )
        Text(
            text = option.amountText,
            style = typography.budgetAmount,
            color = if (selected) colors.onSelectedMuted else colors.budgetItemAmount,
            textAlign = TextAlign.Center,
        )
    }
}
