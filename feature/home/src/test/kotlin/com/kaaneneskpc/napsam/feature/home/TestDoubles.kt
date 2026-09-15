package com.kaaneneskpc.napsam.feature.home

import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.domain.engine.SuggestionEngine
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.domain.repository.SuggestionRepository
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.domain.theme.WeeklyThemeRule
import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.StateStatus
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** 10 Mart 2026 14:00, Türkiye saati (parite verisindeki öğle anı). */
internal val testClock: Clock = Clock.fixed(Instant.parse("2026-03-10T11:00:00Z"), ZoneId.of("Europe/Istanbul"))

internal fun testConfig(): AppConfig = AppConfig(
    wage = ContractFixtures.wage,
    currency = "TRY",
    budgetTiers = ContractFixtures.budgetTiers,
    weeklyTheme = null,
    themeRotation = WeeklyThemeRule.ROTATION,
    aiEnabled = false,
    contentVersion = "test",
    suggestionCount = ContractFixtures.suggestions.size,
    dismissCooldownDays = 30,
    androidMinVersionCode = 1,
)

internal class FakeSuggestionRepository(private val items: List<Suggestion> = ContractFixtures.suggestions) :
    SuggestionRepository {
    override suspend fun ensureContentImported() = Unit

    override suspend fun suggestions(): List<Suggestion> = items
}

/** Web `api_state` geçişlerinin bellekteki karşılığı. */
internal class FakeUserStateRepository(private val clock: Clock = testClock) : UserStateRepository {
    private val states = MutableStateFlow<Map<String, UserSuggestionState>>(emptyMap())
    val dismissed = mutableListOf<String>()
    val done = mutableListOf<String>()

    override suspend fun states(): List<UserSuggestionState> = states.value.values.toList()

    override fun observeSaved(): Flow<List<Suggestion>> = states.map { map ->
        map.values
            .filter { it.status == StateStatus.SAVED }
            .sortedByDescending { it.savedAt }
            .map { state -> ContractFixtures.suggestions.first { it.slug == state.slug } }
    }

    override suspend fun dismiss(slug: String) {
        dismissed += slug
        put(slug) {
            it.copy(status = StateStatus.DISMISSED, dismissedUntil = clock.instant().plus(Duration.ofDays(30)))
        }
    }

    override suspend fun save(slug: String) {
        put(slug) { it.copy(status = StateStatus.SAVED, savedAt = clock.instant(), dismissedUntil = null) }
    }

    override suspend fun unsave(slug: String) {
        if (slug in states.value) put(slug) { it.copy(status = StateStatus.SHOWN, savedAt = null) }
    }

    override suspend fun markDone(slug: String) {
        done += slug
        put(slug) { it.copy(status = StateStatus.DONE, doneAt = clock.instant()) }
    }

    private fun put(slug: String, transform: (UserSuggestionState) -> UserSuggestionState) {
        states.update { map ->
            val current = map[slug] ?: UserSuggestionState(slug, StateStatus.SHOWN, null, null, null, clock.instant())
            map + (slug to transform(current).copy(updatedAt = clock.instant()))
        }
    }
}

internal class FakeConfigRepository(initial: AppConfig = testConfig()) : ConfigRepository {
    val config = MutableStateFlow(initial)
    var refreshCount = 0
        private set

    override fun observeConfig(): Flow<AppConfig> = config

    override suspend fun refresh(): Outcome<Unit> {
        refreshCount++
        return Outcome.Success(Unit)
    }
}

internal class HomeTestFixture(seed: Int = 1) {
    val suggestions = FakeSuggestionRepository()
    val userStates = FakeUserStateRepository()
    val config = FakeConfigRepository()
    val viewModel = HomeViewModel(
        suggestionRepository = suggestions,
        userStateRepository = userStates,
        configRepository = config,
        engine = SuggestionEngine(Random(seed), testClock.zone),
        clock = testClock,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
