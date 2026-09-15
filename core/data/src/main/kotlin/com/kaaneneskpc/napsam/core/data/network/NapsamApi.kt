package com.kaaneneskpc.napsam.core.data.network

import com.kaaneneskpc.napsam.core.data.content.ContentResponseDto
import com.kaaneneskpc.napsam.core.data.network.dto.ConfigDto
import com.kaaneneskpc.napsam.core.data.network.dto.SuggestRequestDto
import com.kaaneneskpc.napsam.core.data.network.dto.SuggestResponseDto
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
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Django mobil API v1 (spesifikasyon C3, B8). Çerez ve CSRF yok; kimlik yalnızca `X-Napsam-Install`.
 * Hiçbir istisna dışarı sızmaz: her çağrı [Outcome] döner.
 */
@Singleton
internal class NapsamApi @Inject constructor(private val client: HttpClient) {

    suspend fun config(): Outcome<ConfigDto> = call { client.get("config/") }

    /** Veritabanındaki (Supabase) güncel içerik; yalnızca sürüm cihazdakinden farklıysa çağrılır. */
    suspend fun content(): Outcome<ContentResponseDto> = call { client.get("content/") }

    suspend fun suggest(installId: String, request: SuggestRequestDto): Outcome<SuggestResponseDto> = call {
        client.post("suggest/") {
            header(INSTALL_HEADER, installId)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

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
        /** Sunucu Faz 13 kararına kadar canlıda olmayabilir; uygulama sunucusuz tam çalışır. */
        const val BASE_URL = "https://napsam.vercel.app/api/v1/"
        const val INSTALL_HEADER = "X-Napsam-Install"
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
