package com.kaaneneskpc.napsam

import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.domain.repository.PreferencesRepository
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Uygulama kabuğu: tema düğmesi, kayıt sayısı rozeti ve açılışta uzaktan yenileme. */
@OptIn(ExperimentalCoroutinesApi::class)
class NapsamAppViewModelTest {
    private val preferences = FakePreferencesRepository()
    private val config = FakeConfigRepository()
    private val userStates = FakeUserStateRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `acilista config yenilenir, varsayilan tema sistem`() = runTest {
        val viewModel = viewModel()

        assertEquals(1, config.refreshCount)
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `tema dugmesi acik ve koyu arasinda gider gelir`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onThemeToggle(currentlyDark = false)
        runCurrent()
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)

        viewModel.onThemeToggle(currentlyDark = true)
        runCurrent()
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `kayit sayisi ust bara yansir`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(0, viewModel.uiState.value.savedCount)

        userStates.saved.value = ContractFixtures.suggestions.take(3)
        runCurrent()

        assertEquals(3, viewModel.uiState.value.savedCount)
    }

    private fun viewModel() = NapsamAppViewModel(preferences, config, userStates)

    private fun TestScope.subscribe(viewModel: NapsamAppViewModel) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }
}

private class FakePreferencesRepository : PreferencesRepository {
    private val mode = MutableStateFlow(ThemeMode.SYSTEM)

    override fun observeThemeMode(): Flow<ThemeMode> = mode

    override suspend fun setThemeMode(mode: ThemeMode) {
        this.mode.value = mode
    }
}

private class FakeConfigRepository : ConfigRepository {
    var refreshCount = 0
        private set

    override fun observeConfig(): Flow<AppConfig> = flowOf(
        AppConfig(
            wage = BigDecimal("28075"),
            currency = "TRY",
            budgetTiers = emptyList(),
            weeklyTheme = null,
            themeRotation = emptyList(),
            aiEnabled = false,
            contentVersion = "test",
            suggestionCount = 0,
            dismissCooldownDays = 30,
            androidMinVersionCode = 1,
        ),
    )

    override suspend fun refresh(): Outcome<Unit> {
        refreshCount++
        return Outcome.Success(Unit)
    }
}

private class FakeUserStateRepository : UserStateRepository {
    val saved = MutableStateFlow<List<Suggestion>>(emptyList())

    override suspend fun states(): List<UserSuggestionState> = emptyList()

    override fun observeSaved(): Flow<List<Suggestion>> = saved

    override suspend fun dismiss(slug: String) = Unit

    override suspend fun save(slug: String) = Unit

    override suspend fun unsave(slug: String) = Unit

    override suspend fun markDone(slug: String) = Unit
}
