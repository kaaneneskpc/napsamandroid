package com.kaaneneskpc.napsam.feature.saved

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.model.Suggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SavedUiState(
    val items: ImmutableList<Suggestion> = persistentListOf(),
    /** İlk liste gelene kadar boş durum metni gösterilmez. */
    val loaded: Boolean = false,
)

/** Kaydedilenler (spesifikasyon A6.5): düz liste, en son kaydedilen önce. Kayıtlar yalnızca cihazda. */
@HiltViewModel
class SavedViewModel @Inject constructor(private val userStateRepository: UserStateRepository) : ViewModel() {

    val uiState: StateFlow<SavedUiState> = userStateRepository.observeSaved()
        .map { SavedUiState(items = it.toImmutableList(), loaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SavedUiState())

    fun onRemove(slug: String) {
        viewModelScope.launch { userStateRepository.unsave(slug) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
