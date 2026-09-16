package com.kaaneneskpc.napsam

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.domain.repository.PreferencesRepository
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class NapsamAppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Üst bardaki sayı rozeti; 0 ise rozet gösterilmez (web). */
    val savedCount: Int = 0,
)

/** Uygulama kabuğu: tema tercihi, kayıt sayısı ve açılışta uzaktan yenileme. */
@HiltViewModel
class NapsamAppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val configRepository: ConfigRepository,
    userStateRepository: UserStateRepository,
) : ViewModel() {

    val uiState: StateFlow<NapsamAppUiState> = combine(
        preferencesRepository.observeThemeMode(),
        userStateRepository.observeSaved(),
    ) { themeMode, saved -> NapsamAppUiState(themeMode = themeMode, savedCount = saved.size) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NapsamAppUiState())

    init {
        // Config ve (Supabase'de değiştiyse) içerik arka planda yenilenir; hata kullanıcıya gösterilmez (B7).
        viewModelScope.launch { configRepository.refresh() }
    }

    /** Web gibi iki durum arasında gider gelir: açık ↔ koyu. */
    fun onThemeToggle(currentlyDark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
