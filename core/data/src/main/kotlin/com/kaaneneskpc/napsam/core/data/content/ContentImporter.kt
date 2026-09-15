package com.kaaneneskpc.napsam.core.data.content

import com.kaaneneskpc.napsam.core.data.database.dao.ContentDao
import com.kaaneneskpc.napsam.core.data.database.entity.ContentInfoEntity
import com.kaaneneskpc.napsam.core.data.database.entity.SuggestionEntity
import com.kaaneneskpc.napsam.core.data.di.DefaultDispatcher
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Cihazdaki içeriği sunucudaki (Supabase) sürüme getirir; config yenilemesi çağırır. */
internal fun interface ContentSynchronizer {
    suspend fun syncWithServer(serverVersion: String): Outcome<Unit>
}

/**
 * İçeriğin cihaza gelişi (karar K3, spesifikasyon C5.4).
 *
 * 1. Uygulama Supabase içeriğinin gömülü kopyasıyla gelir: veritabanı boşsa veya uygulama yeni bir gömülü
 *    içerikle güncellendiyse bu kopya tek transaction'da kurulur. İnternet gerekmez.
 * 2. Sunucudaki içerik sürümü cihazdakinden farklıysa (Supabase'de içerik değişti) yeni içerik
 *    `GET /api/v1/content/`'ten indirilir, doğrulanır ve yine tek transaction'da yazılır. İndirilen içerik
 *    sonraki açılışlarda gömülü kopyayla ezilmez.
 *
 * Her iki yolda da kullanıcı kayıtları korunur; bozuk içerik hiç yazılmaz.
 */
@Singleton
internal class ContentImporter @Inject constructor(
    private val source: ContentSource,
    private val metaProvider: ContentMetaProvider,
    private val dao: ContentDao,
    private val api: NapsamApi,
    private val json: Json,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ContentSynchronizer {
    private val mutex = Mutex()
    private var verifiedEmbeddedVersion: String? = null

    /**
     * true: gömülü içerik bu çağrıda kuruldu. Bozuk gömülü içerik (sayı tutmuyor, yinelenen slug, bilinmeyen
     * anahtar) [IllegalArgumentException] fırlatır ve mevcut içeriğe dokunulmaz.
     */
    suspend fun importIfNeeded(): Boolean = mutex.withLock { importEmbedded() }

    override suspend fun syncWithServer(serverVersion: String): Outcome<Unit> = mutex.withLock {
        try {
            importEmbedded()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            // Gömülü kopya kurulamadıysa sunucu içeriği yine de denenir.
        } catch (e: SerializationException) {
            // Aynı.
        }

        val info = dao.info()
        if (info?.version == serverVersion) return@withLock Outcome.Success(Unit)

        when (val response = api.content()) {
            is Outcome.Failure -> response

            is Outcome.Success -> {
                val content = response.value
                if (content.schema != ContentResponseDto.SUPPORTED_SCHEMA) {
                    return@withLock Outcome.Failure(DataError.INVALID_RESPONSE)
                }
                val entities = try {
                    withContext(defaultDispatcher) { validated(content.suggestions, content.count) }
                } catch (e: IllegalArgumentException) {
                    return@withLock Outcome.Failure(DataError.INVALID_RESPONSE)
                }
                if (content.contentVersion != info?.version) {
                    dao.replaceContent(
                        entities,
                        ContentInfoEntity(
                            version = content.contentVersion,
                            embeddedVersion = info?.embeddedVersion ?: metaProvider.get().contentVersion,
                            count = entities.size,
                        ),
                    )
                }
                Outcome.Success(Unit)
            }
        }
    }

    private suspend fun importEmbedded(): Boolean {
        val meta = metaProvider.get()
        if (verifiedEmbeddedVersion == meta.contentVersion) return false

        val info = dao.info()
        val install = info == null || info.embeddedVersion != meta.contentVersion
        if (install) {
            val entities = withContext(defaultDispatcher) {
                validated(json.decodeFromString<List<ContentSuggestionDto>>(source.readSuggestions()), meta.count)
            }
            dao.replaceContent(
                entities,
                ContentInfoEntity(
                    version = meta.contentVersion,
                    embeddedVersion = meta.contentVersion,
                    count = entities.size,
                ),
            )
        }
        verifiedEmbeddedVersion = meta.contentVersion
        return install
    }

    private fun validated(items: List<ContentSuggestionDto>, expectedCount: Int): List<SuggestionEntity> {
        require(items.isNotEmpty()) { "İçerik boş" }
        require(items.size == expectedCount) { "İçerik ${items.size} öneri, beklenen $expectedCount" }
        require(items.distinctBy { it.slug }.size == items.size) { "İçerikte yinelenen slug var" }
        return items.map { it.toEntity() }
    }
}
