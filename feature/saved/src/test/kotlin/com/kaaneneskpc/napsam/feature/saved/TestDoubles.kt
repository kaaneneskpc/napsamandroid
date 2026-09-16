package com.kaaneneskpc.napsam.feature.saved

import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Kaydedilenler yalnızca `observeSaved` ve `unsave` kullanır; kalanı bu ekranda çağrılmaz. */
internal class FakeUserStateRepository(saved: List<Suggestion> = emptyList()) : UserStateRepository {
    private val items = MutableStateFlow(saved)
    val removed = mutableListOf<String>()

    override suspend fun states(): List<UserSuggestionState> = emptyList()

    override fun observeSaved(): Flow<List<Suggestion>> = items

    override suspend fun dismiss(slug: String) = error("Kaydedilenler ekranı geçme yapmaz")

    override suspend fun save(slug: String) = error("Kaydedilenler ekranı kaydetme yapmaz")

    override suspend fun unsave(slug: String) {
        removed += slug
        items.update { list -> list.filterNot { it.slug == slug } }
    }

    override suspend fun markDone(slug: String) = error("Kaydedilenler ekranı yapıldı işaretlemez")
}

internal fun savedSuggestions(count: Int = 2): List<Suggestion> = ContractFixtures.suggestions.take(count)

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
