package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.content.ContentMetaProvider
import com.kaaneneskpc.napsam.core.data.content.ContentSynchronizer
import com.kaaneneskpc.napsam.core.data.content.toDefaultConfig
import com.kaaneneskpc.napsam.core.data.datastore.SettingsDataSource
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.network.dto.ConfigDto
import com.kaaneneskpc.napsam.core.data.network.dto.toModelOrNull
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.model.AppConfig
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Uzaktan yapılandırma ve içerik güncellemesi (spesifikasyon B7, B8, C3). Her zaman gömülü bir varsayılanı
 * vardır; sunucu yanıtı doğrulanıp önbelleğe yazılınca onun yerine geçer.
 *
 * - Config önbelleği 12 saatten eskiyse çekilir.
 * - Ağ hatası, zaman aşımı ve 5xx: ilk istek + en fazla 2 yeniden deneme (1 sn, 2 sn).
 * - 429: yeniden deneme yok, sonraki istek (config ve içerik) en az 10 dakika ertelenir.
 * - Sunucunun bildirdiği içerik sürümü cihazdakinden farklıysa içerik indirilir; önceki indirme başarısız
 *   olduysa önbellek taze olsa bile bir sonraki yenilemede yeniden denenir.
 */
@Singleton
internal class NetworkConfigRepository @Inject constructor(
    private val api: NapsamApi,
    private val settings: SettingsDataSource,
    private val metaProvider: ContentMetaProvider,
    private val contentSynchronizer: ContentSynchronizer,
    private val clock: Clock,
) : ConfigRepository {

    override fun observeConfig(): Flow<AppConfig> = settings.settings
        .map { it.config?.toModelOrNull() ?: metaProvider.get().toDefaultConfig() }
        .distinctUntilChanged()

    override suspend fun refresh(): Outcome<Unit> {
        val pausedUntil = settings.current().configRetryAfterMillis
        if (pausedUntil != null && clock.millis() < pausedUntil) return Outcome.Success(Unit)

        val configOutcome = refreshConfigIfStale()
        if (configOutcome is Outcome.Failure && configOutcome.error == DataError.RATE_LIMITED) return configOutcome

        val serverConfig = settings.current().config?.takeIf { it.toModelOrNull() != null } ?: return configOutcome
        val contentOutcome = contentSynchronizer.syncWithServer(serverConfig.contentVersion)
        if (contentOutcome is Outcome.Failure && contentOutcome.error == DataError.RATE_LIMITED) pauseForRateLimit()

        return if (configOutcome is Outcome.Failure) configOutcome else contentOutcome
    }

    private suspend fun refreshConfigIfStale(): Outcome<Unit> {
        val current = settings.current()
        val fresh = current.config != null &&
            current.configFetchedAtMillis?.let { clock.millis() - it < STALE_AFTER.toMillis() } == true
        if (fresh) return Outcome.Success(Unit)

        return when (val result = fetchWithRetry()) {
            is Outcome.Success -> {
                if (result.value.toModelOrNull() == null) {
                    Outcome.Failure(DataError.INVALID_RESPONSE)
                } else {
                    settings.update {
                        it.copy(
                            config = result.value,
                            configFetchedAtMillis = clock.millis(),
                            configRetryAfterMillis = null,
                        )
                    }
                    Outcome.Success(Unit)
                }
            }

            is Outcome.Failure -> {
                if (result.error == DataError.RATE_LIMITED) pauseForRateLimit()
                result
            }
        }
    }

    private suspend fun pauseForRateLimit() {
        settings.update { it.copy(configRetryAfterMillis = clock.millis() + RATE_LIMIT_PAUSE.toMillis()) }
    }

    private suspend fun fetchWithRetry(): Outcome<ConfigDto> {
        var retries = 0
        while (true) {
            val result = api.config()
            val retryable = result is Outcome.Failure && result.error in RETRYABLE
            if (!retryable || retries == MAX_RETRIES) return result
            delay(INITIAL_BACKOFF.toMillis() shl retries)
            retries++
        }
    }

    private companion object {
        val STALE_AFTER: Duration = Duration.ofHours(12)
        val RATE_LIMIT_PAUSE: Duration = Duration.ofMinutes(10)
        val INITIAL_BACKOFF: Duration = Duration.ofSeconds(1)
        const val MAX_RETRIES = 2
        val RETRYABLE = setOf(DataError.NETWORK, DataError.TIMEOUT, DataError.SERVER)
    }
}
