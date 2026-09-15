package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.content.toModel
import com.kaaneneskpc.napsam.core.data.database.dao.UserStateDao
import com.kaaneneskpc.napsam.core.data.database.entity.UserStateEntity
import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import com.kaaneneskpc.napsam.core.model.StateStatus
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Kullanıcı kayıtları (spesifikasyon C4.6). Durum geçişleri web `views.api_state` ve `engine.mark_*` ile
 * birebir aynıdır; motorun "son 5 kayıt" cezası `updated_at`'e baktığı için hangi eylemin bu alanı
 * güncellediği de web'deki gibidir.
 */
internal class OfflineUserStateRepository @Inject constructor(
    private val dao: UserStateDao,
    private val clock: Clock,
) : UserStateRepository {

    override suspend fun states(): List<UserSuggestionState> = dao.all().mapNotNull { it.toModelOrNull() }

    override fun observeSaved(): Flow<List<Suggestion>> = dao.observeSaved().map { list -> list.map { it.toModel() } }

    /** Web `mark_shown`: durum değişmez, gösterim sayısı artar. */
    override suspend fun markShown(slug: String) = modify(slug) { state, now ->
        val current = state ?: newState(slug, now)
        current.copy(shownCount = current.shownCount + 1, updatedAt = now)
    }

    /** Web `mark_dismissed`: bekleme süresi başlar; kayıt tarihi olduğu gibi kalır. */
    override suspend fun dismiss(slug: String) = modify(slug) { state, now ->
        (state ?: newState(slug, now)).copy(
            status = StateStatus.DISMISSED.key,
            dismissedUntil = now + DISMISS_COOLDOWN.toMillis(),
            updatedAt = now,
        )
    }

    /** Web `save`: kaydetmek bekleme süresini de kaldırır. */
    override suspend fun save(slug: String) = modify(slug) { state, now ->
        (state ?: newState(slug, now)).copy(
            status = StateStatus.SAVED.key,
            savedAt = now,
            dismissedUntil = null,
            updatedAt = now,
        )
    }

    /**
     * Web `unsave`: yalnızca var olan kayıt "gösterildi"ye döner. Web bunu `QuerySet.update` ile yaptığı
     * için `updated_at` değişmez; burada da değişmez.
     */
    override suspend fun unsave(slug: String) = modify(slug) { state, _ ->
        state?.copy(status = StateStatus.SHOWN.key, savedAt = null)
    }

    /** Web `done`. */
    override suspend fun markDone(slug: String) = modify(slug) { state, now ->
        (state ?: newState(slug, now)).copy(status = StateStatus.DONE.key, doneAt = now, updatedAt = now)
    }

    private suspend fun modify(slug: String, transform: (UserStateEntity?, Long) -> UserStateEntity?) {
        val now = clock.millis()
        dao.modify(slug) { transform(it, now) }
    }

    private fun newState(slug: String, now: Long) = UserStateEntity(
        slug = slug,
        status = StateStatus.SHOWN.key,
        dismissedUntil = null,
        savedAt = null,
        doneAt = null,
        shownCount = 0,
        updatedAt = now,
    )

    private fun UserStateEntity.toModelOrNull(): UserSuggestionState? {
        val status = StateStatus.fromKey(status) ?: return null
        return UserSuggestionState(
            slug = slug,
            status = status,
            dismissedUntil = dismissedUntil?.let(Instant::ofEpochMilli),
            savedAt = savedAt?.let(Instant::ofEpochMilli),
            doneAt = doneAt?.let(Instant::ofEpochMilli),
            updatedAt = Instant.ofEpochMilli(updatedAt),
        )
    }

    private companion object {
        val DISMISS_COOLDOWN: Duration = Duration.ofDays(EngineConstants.DISMISS_COOLDOWN_DAYS.toLong())
    }
}
