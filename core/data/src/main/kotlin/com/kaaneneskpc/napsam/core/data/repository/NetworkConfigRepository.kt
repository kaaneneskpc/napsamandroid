package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.content.ContentMetaProvider
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
 * Uzaktan yapılandırma (spesifikasyon B7, B8). Her zaman gömülü bir varsayılanı vardır; sunucu yanıtı
 * doğrulanıp önbelleğe yazılınca onun yerine geçer.
 *
 * - Önbellek 12 saatten eskiyse çekilir.
 * - Ağ hatası, zaman aşımı ve 5xx: ilk istek + en fazla 2 yeniden deneme (1 sn, 2 sn).
 * - 429: yeniden deneme yok, sonraki istek en az 10 dakika ertelenir.
 */
@Singleton
internal class NetworkConfigRepository @Inject constructor(
    private val api: NapsamApi,
    private val settings: SettingsDataSource,
    private val metaProvider: ContentMetaProvider,
    private val clock: Clock,
) : ConfigRepository {

    override fun observeConfig(): Flow<AppConfig> = settings.settings
        .map { it.config?.toModelOrNull() ?: metaProvider.get().toDefaultConfig() }
        .distinctUntilChanged()

    override suspend fun refresh(): Outcome<Unit> {
        val now = clock.millis()
        val current = settings.current()
        val rateLimited = current.configRetryAfterMillis?.let { now < it } == true
        val fresh = current.config != null &&
            current.configFetchedAtMillis?.let { now - it < STALE_AFTER.toMillis() } == true
        if (rateLimited || fresh) return Outcome.Success(Unit)

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
                if (result.error == DataError.RATE_LIMITED) {
                    settings.update { it.copy(configRetryAfterMillis = clock.millis() + RATE_LIMIT_PAUSE.toMillis()) }
                }
                result
            }
        }
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
