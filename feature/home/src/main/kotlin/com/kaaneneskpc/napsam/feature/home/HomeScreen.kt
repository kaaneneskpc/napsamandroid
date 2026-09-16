package com.kaaneneskpc.napsam.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetSelector
import com.kaaneneskpc.napsam.core.designsystem.component.CommitMessage
import com.kaaneneskpc.napsam.core.designsystem.component.CtaNote
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamChip
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamConsole
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamDivider
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamFooter
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamPrimaryButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamTextField
import com.kaaneneskpc.napsam.core.designsystem.component.OverlinePill
import com.kaaneneskpc.napsam.core.designsystem.component.RefineTabs
import com.kaaneneskpc.napsam.core.designsystem.component.RefineToggle
import com.kaaneneskpc.napsam.core.designsystem.component.StageHeader
import com.kaaneneskpc.napsam.core.designsystem.component.ValuePropCard
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.core.ui.SuggestionCard

private const val CARD_ENTER_MS = 260
private val CardEnterEasing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)
private val CardEnterOffset = 8.dp

private const val KEY_STAGE = "stage"
private const val KEY_COMMIT_MESSAGE = "commit-message"

@Composable
fun HomeRoute(
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = uiState,
        onBudgetSelect = viewModel::onBudgetSelect,
        onNapsamClick = viewModel::onNapsamClick,
        onRefineToggle = viewModel::onRefineToggle,
        onRefineModeSelect = viewModel::onRefineModeSelect,
        onChipToggle = viewModel::onChipToggle,
        onTextChange = viewModel::onTextChange,
        onTextSubmit = viewModel::onTextSubmit,
        onRefineClear = viewModel::onRefineClear,
        onCommit = viewModel::onCommit,
        onAnotherIdea = viewModel::onAnotherIdea,
        onSaveToggle = viewModel::onSaveToggle,
        onCommitMessageClose = viewModel::onCommitMessageClose,
        onOpenPrivacy = onOpenPrivacy,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/** Ana ekran, web sırasıyla: başlık → karar paneli → canlı öneri → tanıtım kutuları → alt bilgi (K19). */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onBudgetSelect: (String) -> Unit,
    onNapsamClick: () -> Unit,
    onRefineToggle: () -> Unit,
    onRefineModeSelect: (RefineMode) -> Unit,
    onChipToggle: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onRefineClear: () -> Unit,
    onCommit: () -> Unit,
    onAnotherIdea: () -> Unit,
    onSaveToggle: () -> Unit,
    onCommitMessageClose: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = NapsamTheme.spacing
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current

    // Yeni öneri istendiğinde kart bölümünün başı görünür olmalı (web'de düzeltilen hata).
    LaunchedEffect(state.stageScrollRequest) {
        if (state.stageScrollRequest > 0) {
            val stageIndex = listState.layoutInfo.totalItemsCount.coerceAtMost(STAGE_INDEX + 1) - 1
            listState.animateScrollToItem(stageIndex)
        }
    }
    // "Tamam, yapıyorum" mesajı görünür alanın dışındaysa en yakın konuma kaydırılır (web `block: nearest`).
    LaunchedEffect(state.commitMessageScrollRequest) {
        if (state.commitMessageScrollRequest > 0) {
            withFrameNanos { }
            val layout = listState.layoutInfo
            val message = layout.visibleItemsInfo.firstOrNull { it.key == KEY_COMMIT_MESSAGE }
            val overflow = message?.let { it.offset + it.size - layout.viewportEndOffset } ?: 0
            if (overflow > 0) listState.animateScrollBy(overflow.toFloat())
        }
    }

    // Zemin uygulama kabugunda (NapsamApp) bir kez cizilir; ekranlar uzerine yerlesir.
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + spacing.screenHorizontal,
                end = contentPadding.calculateEndPadding(layoutDirection) + spacing.screenHorizontal,
                top = contentPadding.calculateTopPadding() + spacing.xxxl,
                bottom = contentPadding.calculateBottomPadding() + spacing.section,
            ),
        ) {
            item(key = "hero") { Hero() }
            item(key = "console") {
                DecisionConsole(
                    state = state,
                    onBudgetSelect = onBudgetSelect,
                    onNapsamClick = onNapsamClick,
                    onRefineToggle = onRefineToggle,
                    onRefineModeSelect = onRefineModeSelect,
                    onChipToggle = onChipToggle,
                    onTextChange = onTextChange,
                    onTextSubmit = onTextSubmit,
                    onRefineClear = onRefineClear,
                )
            }
            item(key = KEY_STAGE) {
                StageHeader(
                    accentText = stringResource(R.string.home_stage_accent),
                    secondaryText = stringResource(R.string.home_stage_secondary),
                    modifier = Modifier.padding(top = spacing.section, bottom = spacing.lg),
                )
            }
            item(key = "card") {
                CardSection(
                    state = state,
                    onCommit = onCommit,
                    onAnotherIdea = onAnotherIdea,
                    onSaveToggle = onSaveToggle,
                )
            }
            if (state.showCommitMessage) {
                item(key = KEY_COMMIT_MESSAGE) {
                    CommitMessage(
                        title = stringResource(R.string.home_commit_title),
                        body = stringResource(R.string.home_commit_body),
                        closeContentDescription = stringResource(R.string.home_commit_close),
                        onClose = onCommitMessageClose,
                        modifier = Modifier.padding(top = spacing.xl),
                    )
                }
            }
            item(key = "values") { ValueProps(modifier = Modifier.padding(vertical = spacing.section)) }
            item(key = "footer") {
                NapsamFooter(
                    text = stringResource(R.string.home_footer_text),
                    linkText = stringResource(R.string.home_footer_privacy),
                    onLinkClick = onOpenPrivacy,
                )
            }
        }
    }
}

/** LazyColumn'daki "Canlı Öneri" başlığının sırası (hero, panel, başlık). */
private const val STAGE_INDEX = 2

@Composable
private fun Hero(modifier: Modifier = Modifier) {
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OverlinePill(text = stringResource(R.string.home_overline))
        Text(
            text = stringResource(R.string.home_title),
            style = NapsamTheme.typography.heroTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = spacing.lg, bottom = spacing.md)
                .semantics { heading() },
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = NapsamTheme.typography.heroSubtitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DecisionConsole(
    state: HomeUiState,
    onBudgetSelect: (String) -> Unit,
    onNapsamClick: () -> Unit,
    onRefineToggle: () -> Unit,
    onRefineModeSelect: (RefineMode) -> Unit,
    onChipToggle: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onRefineClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = NapsamTheme.spacing
    val summary = when (val refineSummary = state.refineSummary) {
        is RefineSummary.Keyword -> refineSummary.keyword

        is RefineSummary.KeywordCount -> pluralStringResource(
            R.plurals.home_refine_keyword_count,
            refineSummary.count,
            refineSummary.count,
        )

        RefineSummary.Written -> stringResource(R.string.home_refine_written)

        null -> null
    }

    NapsamConsole(modifier = modifier) {
        Text(
            text = stringResource(R.string.home_budget_label).uppercase(TurkishLocale),
            style = NapsamTheme.typography.sectionLabel,
            color = NapsamTheme.colors.textTertiary,
        )
        BudgetSelector(
            options = state.budgetOptions,
            selectedKey = state.selectedBudget,
            onSelect = onBudgetSelect,
            modifier = Modifier.padding(top = spacing.ml),
        )
        NapsamPrimaryButton(
            text = stringResource(R.string.home_napsam),
            onClick = onNapsamClick,
            modifier = Modifier.padding(top = spacing.lgx),
        )
        CtaNote(text = stringResource(R.string.home_cta_note), modifier = Modifier.padding(top = spacing.ml))
        // Altta boşluk yok: daraltma düğmesinin 48dp dokunma alanı tasarımdaki boşluğu karşılıyor.
        NapsamDivider(modifier = Modifier.padding(top = spacing.lg))
        RefineToggle(
            text = stringResource(R.string.home_refine_toggle),
            expanded = state.refineExpanded,
            stateDescription = stringResource(
                if (state.refineExpanded) R.string.home_refine_expanded else R.string.home_refine_collapsed,
            ),
            onClick = onRefineToggle,
            summary = summary,
        )
        AnimatedVisibility(visible = state.refineExpanded) {
            RefinePanel(
                state = state,
                showClear = summary != null,
                onRefineModeSelect = onRefineModeSelect,
                onChipToggle = onChipToggle,
                onTextChange = onTextChange,
                onTextSubmit = onTextSubmit,
                onRefineClear = onRefineClear,
            )
        }
    }
}

@Composable
private fun RefinePanel(
    state: HomeUiState,
    showClear: Boolean,
    onRefineModeSelect: (RefineMode) -> Unit,
    onChipToggle: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onRefineClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = NapsamTheme.spacing
    val textFocus = remember { FocusRequester() }
    val textMode = state.refineMode == RefineMode.TEXT

    // Web: "Yaz" sekmesi seçilince veya panel bu sekmede açılınca yazı alanı odaklanır.
    LaunchedEffect(textMode) {
        if (textMode) textFocus.requestFocus()
    }

    Column(
        modifier = modifier.padding(top = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        RefineTabs(
            chipsLabel = stringResource(R.string.home_refine_tab_chips),
            textLabel = stringResource(R.string.home_refine_tab_text),
            textSelected = textMode,
            onTextSelectedChange = { text -> onRefineModeSelect(if (text) RefineMode.TEXT else RefineMode.CHIPS) },
            clearLabel = stringResource(R.string.home_refine_clear),
            showClear = showClear,
            onClear = onRefineClear,
        )
        if (textMode) {
            NapsamTextField(
                value = state.text,
                onValueChange = onTextChange,
                placeholder = stringResource(R.string.home_refine_text_placeholder),
                label = stringResource(R.string.home_refine_text_label),
                onSubmit = onTextSubmit,
                modifier = Modifier.focusRequester(textFocus),
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                state.chips.forEach { chip ->
                    NapsamChip(
                        text = chip,
                        selected = chip in state.selectedKeywords,
                        onSelectedChange = { onChipToggle(chip) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CardSection(
    state: HomeUiState,
    onCommit: () -> Unit,
    onAnotherIdea: () -> Unit,
    onSaveToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val offset = with(LocalDensity.current) { CardEnterOffset.roundToPx() }
    // Kart geçişi ~260 ms (web `cardIn`): yalnızca yeni öneri geldiğinde, önceki kart beklemeden kalkar.
    AnimatedContent(
        targetState = state.suggestion,
        contentKey = { it?.slug },
        transitionSpec = {
            val spec = tween<Float>(CARD_ENTER_MS, easing = CardEnterEasing)
            (fadeIn(spec) + slideInVertically(tween(CARD_ENTER_MS, easing = CardEnterEasing)) { offset })
                .togetherWith(ExitTransition.None)
        },
        modifier = modifier,
        label = "oneri-karti",
    ) { suggestion ->
        if (suggestion != null) {
            SuggestionCard(
                suggestion = suggestion,
                saved = state.isSaved,
                onCommit = onCommit,
                onAnotherIdea = onAnotherIdea,
                onSaveToggle = onSaveToggle,
            )
        }
    }
}

@Composable
private fun ValueProps(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(NapsamTheme.spacing.ml)) {
        ValuePropCard(
            kicker = stringResource(R.string.home_value_1_kicker),
            title = stringResource(R.string.home_value_1_title),
            text = stringResource(R.string.home_value_1_text),
        )
        ValuePropCard(
            kicker = stringResource(R.string.home_value_2_kicker),
            title = stringResource(R.string.home_value_2_title),
            text = stringResource(R.string.home_value_2_text),
        )
        ValuePropCard(
            kicker = stringResource(R.string.home_value_3_kicker),
            title = stringResource(R.string.home_value_3_title),
            text = stringResource(R.string.home_value_3_text),
        )
    }
}

/** İçerik Türkçe: büyük harf dönüşümü cihaz dilinden bağımsız ("Bütçe durumu" → "BÜTÇE DURUMU"). */
private val TurkishLocale = java.util.Locale.forLanguageTag("tr-TR")
