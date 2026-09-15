package com.kaaneneskpc.napsam.feature.home

import androidx.compose.runtime.Immutable
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetOption
import com.kaaneneskpc.napsam.core.model.BudgetTier
import com.kaaneneskpc.napsam.core.model.Suggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Daraltma panelinin sekmesi (web `mode`: chips / text). */
enum class RefineMode { CHIPS, TEXT }

/** Daraltma başlığındaki özet (web `paintRefineSummary`). */
sealed interface RefineSummary {
    data class Keyword(val keyword: String) : RefineSummary

    data class KeywordCount(val count: Int) : RefineSummary

    data object Written : RefineSummary
}

@Immutable
data class HomeUiState(
    val budgetOptions: ImmutableList<BudgetOption> = persistentListOf(),
    val selectedBudget: String = BudgetTier.DEFAULT_KEY,
    /** Web gibi kapalı başlar: kullanıcı bir şey seçmek zorunda hissetmesin. */
    val refineExpanded: Boolean = false,
    val refineMode: RefineMode = RefineMode.CHIPS,
    /** Günün saatine göre chip seti (spesifikasyon C7). */
    val chips: ImmutableList<String> = persistentListOf(),
    val selectedKeywords: ImmutableList<String> = persistentListOf(),
    val text: String = "",
    val suggestion: Suggestion? = null,
    val isSaved: Boolean = false,
    val showCommitMessage: Boolean = false,
    /** Her artışta kart bölümünün başına kaydırılır (web `showStage`). */
    val stageScrollRequest: Int = 0,
    /** Her artışta "Tamam, yapıyorum" mesajı görünür alana getirilir. */
    val commitMessageScrollRequest: Int = 0,
) {
    val refineSummary: RefineSummary?
        get() = when {
            refineMode == RefineMode.TEXT && text.isNotBlank() -> RefineSummary.Written
            selectedKeywords.size == 1 -> RefineSummary.Keyword(selectedKeywords.single())
            selectedKeywords.size > 1 -> RefineSummary.KeywordCount(selectedKeywords.size)
            else -> null
        }
}
