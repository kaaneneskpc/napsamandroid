package com.kaaneneskpc.napsam.core.data.content

import com.kaaneneskpc.napsam.core.data.database.dao.ContentDao
import com.kaaneneskpc.napsam.core.data.database.entity.ContentInfoEntity
import com.kaaneneskpc.napsam.core.data.di.DefaultDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Gömülü içeriği veritabanına aktarır (karar K3, spesifikasyon C5.4).
 *
 * Veritabanındaki sürüm `meta.json` contentVersion'ından farklıysa (ilk açılış veya uygulama güncellemesi)
 * içerik tek transaction'da yenilenir. Kontrol süreç başına bir kez yapılır; `Application.onCreate`'te
 * değil ilk öneri isteğinde çalışır (spesifikasyon B11.2).
 */
@Singleton
internal class ContentImporter @Inject constructor(
    private val source: ContentSource,
    private val metaProvider: ContentMetaProvider,
    private val dao: ContentDao,
    private val json: Json,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()
    private var verifiedVersion: String? = null

    /**
     * true: içerik bu çağrıda yenilendi. Bozuk içerik (sayı tutmuyor, yinelenen slug, bilinmeyen anahtar)
     * [IllegalArgumentException] fırlatır ve mevcut içeriğe dokunulmaz.
     */
    suspend fun importIfNeeded(): Boolean = mutex.withLock {
        val meta = metaProvider.get()
        if (verifiedVersion == meta.contentVersion) return@withLock false

        val outdated = dao.version() != meta.contentVersion
        if (outdated) {
            val entities = withContext(defaultDispatcher) {
                val items = json.decodeFromString<List<ContentSuggestionDto>>(source.readSuggestions())
                require(items.size == meta.count) { "İçerik ${items.size} öneri, meta ${meta.count} diyor" }
                require(items.distinctBy { it.slug }.size == items.size) { "İçerikte yinelenen slug var" }
                items.map { it.toEntity() }
            }
            dao.replaceContent(entities, ContentInfoEntity(version = meta.contentVersion, count = entities.size))
        }
        verifiedVersion = meta.contentVersion
        outdated
    }
}
