package com.kaaneneskpc.napsam.core.data.network

import com.kaaneneskpc.napsam.core.data.content.ContentResponseDto
import com.kaaneneskpc.napsam.core.data.network.dto.ConfigDto
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Django mobil API v1 (spesifikasyon C3, B8): yalnızca okuma uçları (config, içerik). İsteklerde çerez,
 * kimlik ya da kullanıcı verisi yoktur; AI önerisi v1'de kapalı (Faz 13 kararı), `suggest/` çağrılmaz.
 * Hiçbir istisna dışarı sızmaz: her çağrı [Outcome] döner.
 */
@Singleton
internal class NapsamApi @Inject constructor(private val client: HttpClient) {

    suspend fun config(): Outcome<ConfigDto> = call { client.get("config/") }

    /** Veritabanındaki (Supabase) güncel içerik; yalnızca sürüm cihazdakinden farklıysa çağrılır. */
    suspend fun content(): Outcome<ContentResponseDto> = call { client.get("content/") }

    private suspend inline fun <reified T> call(request: () -> HttpResponse): Outcome<T> = try {
        val response = request()
        when (response.status.value) {
            in 200..299 -> Outcome.Success(response.body<T>())
            429 -> Outcome.Failure(DataError.RATE_LIMITED)
            in 500..599 -> Outcome.Failure(DataError.SERVER)
            else -> Outcome.Failure(DataError.INVALID_RESPONSE)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        Outcome.Failure(DataError.TIMEOUT)
    } catch (e: ConnectTimeoutException) {
        Outcome.Failure(DataError.TIMEOUT)
    } catch (e: java.net.SocketTimeoutException) {
        Outcome.Failure(DataError.TIMEOUT)
    } catch (e: IOException) {
        Outcome.Failure(DataError.NETWORK)
    } catch (e: SerializationException) {
        Outcome.Failure(DataError.INVALID_RESPONSE)
    } catch (e: ContentConvertException) {
        Outcome.Failure(DataError.INVALID_RESPONSE)
    } catch (e: NoTransformationFoundException) {
        Outcome.Failure(DataError.INVALID_RESPONSE)
    } catch (e: IllegalArgumentException) {
        Outcome.Failure(DataError.INVALID_RESPONSE)
    }

    companion object {
        /** Sunucu yayınlanana kadar uçlar 404 döner; uygulama sunucusuz tam çalışır. */
        const val BASE_URL = "https://napsam.vercel.app/api/v1/"
        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val REQUEST_TIMEOUT_MS = 20_000L

        /** Tek istemci yapılandırması: uygulamada Android motoru, testlerde MockEngine. */
        fun createClient(engine: HttpClientEngine, json: Json): HttpClient = HttpClient(engine) {
            expectSuccess = false
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url(BASE_URL)
            }
        }
    }
}
