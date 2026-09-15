package com.kaaneneskpc.napsam.core.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.data.FakeContentSource
import com.kaaneneskpc.napsam.core.data.MutableClock
import com.kaaneneskpc.napsam.core.data.content.ContentImporter
import com.kaaneneskpc.napsam.core.data.content.ContentMetaProvider
import com.kaaneneskpc.napsam.core.data.database.NapsamDatabase
import com.kaaneneskpc.napsam.core.data.inMemoryDatabase
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import com.kaaneneskpc.napsam.core.model.StateStatus
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Durum geçişleri web `views.api_state` ve `engine.mark_*` ile birebir (spesifikasyon C4.6). */
@RunWith(AndroidJUnit4::class)
class OfflineUserStateRepositoryTest {
    private lateinit var database: NapsamDatabase
    private lateinit var repository: OfflineUserStateRepository
    private val clock = MutableClock(Instant.parse("2026-09-15T09:00:00Z"))
    private val first = ContractFixtures.suggestions[0]
    private val second = ContractFixtures.suggestions[1]

    @Before
    fun setUp() = runTest {
        database = inMemoryDatabase()
        val source = FakeContentSource()
        ContentImporter(
            source = source,
            metaProvider = ContentMetaProvider(source, napsamJson, Dispatchers.Unconfined),
            dao = database.contentDao(),
            json = napsamJson,
            defaultDispatcher = Dispatchers.Unconfined,
        ).importIfNeeded()
        repository = OfflineUserStateRepository(database.userStateDao(), clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `gosterim durumu degistirmez, sayaci ve guncelleme zamanini arttirir`() = runTest {
        repository.markShown(first.slug)
        assertEquals(StateStatus.SHOWN, state(first.slug).status)

        repository.save(first.slug)
        clock.advance(Duration.ofMinutes(1))
        repository.markShown(first.slug)

        val entity = requireNotNull(database.userStateDao().find(first.slug))
        assertEquals(2, entity.shownCount)
        assertEquals(StateStatus.SAVED.key, entity.status)
        assertEquals(clock.millis(), entity.updatedAt)
    }

    @Test
    fun `gecilen oneri 30 gun bekler, kaydetmek beklemeyi kaldirir`() = runTest {
        repository.dismiss(first.slug)
        val dismissed = state(first.slug)
        assertEquals(StateStatus.DISMISSED, dismissed.status)
        assertEquals(
            clock.instant().plus(Duration.ofDays(EngineConstants.DISMISS_COOLDOWN_DAYS.toLong())),
            dismissed.dismissedUntil,
        )
        assertTrue(dismissed.isCoolingDown(clock.instant()))

        repository.save(first.slug)
        val saved = state(first.slug)
        assertEquals(StateStatus.SAVED, saved.status)
        assertEquals(clock.instant(), saved.savedAt)
        assertNull(saved.dismissedUntil)
    }

    @Test
    fun `kayittan cikarmak gosterildiye doner ve guncelleme zamanina dokunmaz`() = runTest {
        repository.save(first.slug)
        val savedAt = clock.instant()
        clock.advance(Duration.ofHours(1))

        repository.unsave(first.slug)
        val unsaved = state(first.slug)
        assertEquals(StateStatus.SHOWN, unsaved.status)
        assertNull(unsaved.savedAt)
        assertEquals(savedAt, unsaved.updatedAt)

        repository.unsave(second.slug)
        assertNull("kaydı olmayan öneri için satır açılmaz", database.userStateDao().find(second.slug))
    }

    @Test
    fun `yapildi isaretlenir, icerikte olmayan oneri icin kayit acilmaz`() = runTest {
        repository.markDone(first.slug)
        val done = state(first.slug)
        assertEquals(StateStatus.DONE, done.status)
        assertEquals(clock.instant(), done.doneAt)

        repository.save("icerikte-olmayan-oneri")
        assertEquals(listOf(first.slug), repository.states().map { it.slug })
    }

    @Test
    fun `kaydedilenler en son kaydedilen once`() = runTest {
        assertEquals(emptyList<Any>(), repository.observeSaved().first())

        repository.save(first.slug)
        clock.advance(Duration.ofMinutes(1))
        repository.save(second.slug)
        assertEquals(listOf(second, first), repository.observeSaved().first())

        repository.unsave(second.slug)
        assertEquals(listOf(first), repository.observeSaved().first())
    }

    private suspend fun state(slug: String) = repository.states().single { it.slug == slug }
}
