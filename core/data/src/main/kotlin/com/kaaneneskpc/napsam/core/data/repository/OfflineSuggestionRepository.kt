package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.content.ContentImporter
import com.kaaneneskpc.napsam.core.data.content.toModel
import com.kaaneneskpc.napsam.core.data.database.dao.ContentDao
import com.kaaneneskpc.napsam.core.data.di.DefaultDispatcher
import com.kaaneneskpc.napsam.core.domain.repository.SuggestionRepository
import com.kaaneneskpc.napsam.core.model.Suggestion
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Motorun havuzu. İçerik değişmediği sürece bellekte tutulur; her "NAPSAM?" dokunuşunda 500 satır
 * yeniden okunmaz.
 */
@Singleton
internal class OfflineSuggestionRepository @Inject constructor(
    private val importer: ContentImporter,
    private val dao: ContentDao,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SuggestionRepository {
    private val mutex = Mutex()
    private var cache: List<Suggestion>? = null

    /**
     * İçe aktarma hatası dışarı sızmaz (spesifikasyon B3.8): gömülü içerik bozuksa veritabanındaki son
     * geçerli içerikle devam edilir. İçerik derleme sırasında web doğrulayıcısından geçtiği için beklenmez.
     */
    override suspend fun ensureContentImported() {
        val refreshed = try {
            importer.importIfNeeded()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: kotlinx.serialization.SerializationException) {
            false
        }
        if (refreshed) mutex.withLock { cache = null }
    }

    override suspend fun suggestions(): List<Suggestion> {
        ensureContentImported()
        return mutex.withLock {
            cache ?: withContext(defaultDispatcher) { dao.suggestions().map { it.toModel() } }.also { cache = it }
        }
    }
}
