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
import kotlinx.serialization.SerializationException

/**
 * Motorun havuzu. İçerik sürümü değişmediği sürece bellekte tutulur; her "NAPSAM?" dokunuşunda 500 satır
 * yeniden okunmaz. Sunucudan yeni içerik inince sürüm değiştiği için bir sonraki istekte yeniden okunur.
 */
@Singleton
internal class OfflineSuggestionRepository @Inject constructor(
    private val importer: ContentImporter,
    private val dao: ContentDao,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SuggestionRepository {
    private val mutex = Mutex()
    private var cache: Pair<String?, List<Suggestion>>? = null

    /**
     * İçe aktarma hatası dışarı sızmaz (spesifikasyon B3.8): gömülü içerik bozuksa veritabanındaki son
     * geçerli içerikle devam edilir. İçerik derleme sırasında web doğrulayıcısından geçtiği için beklenmez.
     */
    override suspend fun ensureContentImported() {
        try {
            importer.importIfNeeded()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            // Son geçerli içerikle devam.
        } catch (e: SerializationException) {
            // Aynı.
        }
    }

    override suspend fun suggestions(): List<Suggestion> {
        ensureContentImported()
        return mutex.withLock {
            val version = dao.info()?.version
            cache?.takeIf { it.first == version }?.second
                ?: withContext(defaultDispatcher) { dao.suggestions().map { it.toModel() } }
                    .also { cache = version to it }
        }
    }
}
