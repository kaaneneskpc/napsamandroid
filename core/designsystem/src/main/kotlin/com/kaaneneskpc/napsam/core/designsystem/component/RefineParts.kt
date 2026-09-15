package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

private val TextFieldIconSize = 20.dp
private val FocusedBorderWidth = 1.5.dp

/**
 * Daraltma panelinin sekmeleri (web `.tabs`): "Kelime Seç / Yaz" ve yalnızca seçim varken görünen
 * "Temizle". Sekmeler seçilebilir grup olarak okunur.
 */
@Composable
fun RefineTabs(
    chipsLabel: String,
    textLabel: String,
    textSelected: Boolean,
    onTextSelectedChange: (Boolean) -> Unit,
    clearLabel: String,
    showClear: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = NapsamTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            RefineTab(text = chipsLabel, selected = !textSelected, onClick = { onTextSelectedChange(false) })
            RefineTab(text = textLabel, selected = textSelected, onClick = { onTextSelectedChange(true) })
        }
        if (showClear) {
            Text(
                text = clearLabel,
                style = NapsamTheme.typography.refineToggle,
                color = NapsamTheme.colors.textSecondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(NapsamTheme.shapes.tag)
                    .clickable(role = Role.Button, onClick = onClear)
                    .padding(horizontal = spacing.xs),
            )
        }
    }
}

@Composable
private fun RefineTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NapsamTheme.colors
    val shape = NapsamTheme.shapes.control
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (selected) NapsamTheme.typography.actionPrimary else NapsamTheme.typography.actionSecondary,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            modifier = Modifier
                .clip(shape)
                .background(if (selected) colors.budgetItem else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (selected) colors.budgetItemBorder else Color.Transparent,
                    shape = shape,
                )
                .padding(horizontal = NapsamTheme.spacing.lg, vertical = NapsamTheme.spacing.sm),
        )
    }
}

/**
 * "Yaz" modu metin alanı (DESIGN.md "Inputs": sade kenarlık, odakta aksan rengi, parıltı yok).
 * Klavyedeki "Gönder" [onSubmit]'i tetikler (web'de Enter).
 */
@Composable
fun NapsamTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NapsamTheme.colors
    val spacing = NapsamTheme.spacing
    val shape = NapsamTheme.shapes.control
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val textStyle = NapsamTheme.typography.body

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.minTouchTarget)
            .semantics { contentDescription = label },
        textStyle = textStyle.copy(color = colors.textPrimary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSubmit() }),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(colors.accent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(colors.chip)
                    .border(
                        width = if (focused) FocusedBorderWidth else 1.dp,
                        color = if (focused) colors.accent else colors.chipBorder,
                        shape = shape,
                    )
                    .padding(horizontal = spacing.lg, vertical = spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = NapsamIcons.Edit,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(TextFieldIconSize),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = colors.chipText)
                    }
                    innerTextField()
                }
            }
        },
    )
}
