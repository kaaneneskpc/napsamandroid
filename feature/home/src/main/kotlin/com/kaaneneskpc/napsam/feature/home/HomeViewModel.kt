package com.kaaneneskpc.napsam.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetOption
import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.domain.chips.ChipCatalog
import com.kaaneneskpc.napsam.core.domain.engine.EngineInput
import com.kaaneneskpc.napsam.core.domain.engine.SuggestionEngine
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.domain.repository.SuggestionRepository
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.domain.text.TextParser
import com.kaaneneskpc.napsam.core.domain.theme.WeeklyThemeRule
import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.BudgetTier
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * Ana ekran (spesifikasyon A6, web `static/js/napsam.js`): açılışta hazır kart (K4), NAPSAM?, daraltma
 * paneli, kart eylemleri. Öneri yerel motordan gelir; internet gerekmez.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val userStateRepository: UserStateRepository,
    private val configRepository: ConfigRepository,
    private val engine: SuggestionEngine,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(chips = ChipCatalog.forHour(LocalDateTime.now(clock).hour).toImmutableList()),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Bu oturumda gösterilen öneriler (web `state.seen`); son 30'u tekrar gösterilmez. */
    private val seen = mutableListOf<String>()
    private var savedSlugs: Set<String> = emptySet()

    /** Web gibi bir istek sürerken yenisi başlamaz. */
    private val requestInProgress = Mutex()

    init {
        viewModelScope.launch {
            configRepository.observeConfig().collect(::applyConfig)
        }
        viewModelScope.launch {
            userStateRepository.observeSaved().collect { saved ->
                savedSlugs = saved.mapTo(mutableSetOf()) { it.slug }
                _uiState.update { state ->
                    state.copy(isSaved = state.suggestion?.slug?.let { it in savedSlugs } == true)
                }
            }
        }
        viewModelScope.launch { requestSuggestion() }
        // Config ve içerik yenilemesi uygulama kabuğunda (NapsamAppViewModel) yapılır.
    }

    fun onBudgetSelect(key: String) {
        _uiState.update { it.copy(selectedBudget = key) }
    }

    fun onNapsamClick() {
        requestStageScroll()
        viewModelScope.launch { requestSuggestion() }
    }

    fun onRefineToggle() {
        _uiState.update { it.copy(refineExpanded = !it.refineExpanded) }
    }

    fun onRefineModeSelect(mode: RefineMode) {
        _uiState.update { it.copy(refineMode = mode) }
    }

    fun onChipToggle(chip: String) {
        _uiState.update { state ->
            val keywords = state.selectedKeywords
            val updated = if (chip in keywords) keywords - chip else keywords + chip
            state.copy(selectedKeywords = updated.toImmutableList())
        }
    }

    fun onTextChange(text: String) {
        _uiState.update { it.copy(text = text) }
    }

    /** Klavyedeki "Gönder" (web'de Enter): yeni öneri, sayfa kaydırılmaz. */
    fun onTextSubmit() {
        viewModelScope.launch { requestSuggestion() }
    }

    fun onRefineClear() {
        _uiState.update { it.copy(selectedKeywords = persistentListOf(), text = "") }
    }

    /** Başka fikir: mevcut öneri 30 gün geçilir, yenisi gelir ve kartın başı görünür. */
    fun onAnotherIdea() {
        val slug = _uiState.value.suggestion?.slug ?: return
        if (slug !in seen) seen += slug
        requestStageScroll()
        viewModelScope.launch {
            userStateRepository.dismiss(slug)
            requestSuggestion()
        }
    }

    fun onSaveToggle() {
        val slug = _uiState.value.suggestion?.slug ?: return
        val wasSaved = _uiState.value.isSaved
        _uiState.update { it.copy(isSaved = !wasSaved) }
        viewModelScope.launch {
            if (wasSaved) userStateRepository.unsave(slug) else userStateRepository.save(slug)
        }
    }

    /** Tamam, yapıyorum: amaç uygulamayı bıraktırmak (web mesajı); uygulama kapatılmaz (K6). */
    fun onCommit() {
        val slug = _uiState.value.suggestion?.slug ?: return
        viewModelScope.launch { userStateRepository.markDone(slug) }
        _uiState.update {
            it.copy(
                showCommitMessage = true,
                commitMessageScrollRequest =
                    it.commitMessageScrollRequest + 1,
            )
        }
    }

    fun onCommitMessageClose() {
        _uiState.update { it.copy(showCommitMessage = false) }
    }

    private fun applyConfig(config: AppConfig) {
        val budget = BudgetCalculator(config.budgetTiers, config.wage)
        _uiState.update { state ->
            state.copy(
                budgetOptions = budget.tiers.map {
                    BudgetOption(it.key, it.label, budget.amountText(it))
                }.toImmutableList(),
                selectedBudget = state.selectedBudget.takeIf { budget.tier(it) != null } ?: BudgetTier.DEFAULT_KEY,
            )
        }
    }

    private fun requestStageScroll() {
        _uiState.update { it.copy(stageScrollRequest = it.stageScrollRequest + 1) }
    }

    /** Web `requestSuggestion` + motor bağlamı (web `views.build_context` sınırlarıyla). */
    private suspend fun requestSuggestion() {
        if (!requestInProgress.tryLock()) return
        try {
            val state = _uiState.value
            val config = configRepository.observeConfig().first()
            val budget = BudgetCalculator(config.budgetTiers, config.wage)
            val now = LocalDateTime.now(clock)
            val base = SuggestionContext(
                now = now,
                budgetKey = state.selectedBudget,
                keywords = if (state.refineMode == RefineMode.CHIPS) {
                    state.selectedKeywords.take(EngineConstants.MAX_KEYWORDS)
                } else {
                    emptyList()
                },
                excluded = seen.takeLast(EngineConstants.SESSION_EXCLUDED).toSet(),
            )
            val textMode = state.refineMode == RefineMode.TEXT
            val context = if (textMode) TextParser.applyTo(base, state.text, budget) else base
            val input = EngineInput(
                suggestions = suggestionRepository.suggestions(),
                budget = budget,
                userStates = userStateRepository.states(),
                themeKey = WeeklyThemeRule.activeThemeKey(now.toLocalDate(), config.weeklyTheme, config.themeRotation),
            )
            val picked = engine.pick(context, input) ?: return
            seen += picked.slug
            _uiState.update {
                it.copy(suggestion = picked, isSaved = picked.slug in savedSlugs, showCommitMessage = false)
            }
        } finally {
            requestInProgress.unlock()
        }
    }
}
