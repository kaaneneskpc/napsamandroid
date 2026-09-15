package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.datastore.SettingsDataSource
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.respondJson
import com.kaaneneskpc.napsam.core.data.testDataStore
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.domain.repository.GeneratedBadges
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestion
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestionResult
import com.kaaneneskpc.napsam.core.model.Category
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Place
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** "Yaz" modunda mobil API (spesifikasyon C3.4, B8). */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkRemoteSuggestionRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val requests = mutableListOf<HttpRequestData>()
    private lateinit var settings: SettingsDataSource

    private val context = SuggestionContext(
        now = LocalDateTime.of(2026, 9, 15, 20, 0),
        budgetKey = "az",
        keywords = (1..10).map { "kelime-$it" },
        durationMax = 60,
        companions = CompanionType.COUPLE,
        excluded = setOf("ev-cekmece-bosalt"),
    )

    @Test
    fun `istek govdesi web istemcisiyle ayni, kurulum kimligi kalici uuid v4`() = runTest {
        val repository = repository { respondJson(response(source = "seed")) }

        repository.suggest(context, "sevgilimle 1 saatim var")
        repository.suggest(context, "ikinci deneme")

        val request = requests.first()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://napsam.vercel.app/api/v1/suggest/", request.url.toString())
        val installIds = requests.map { requireNotNull(it.headers[NapsamApi.INSTALL_HEADER]) }.toSet()
        assertEquals(1, installIds.size)
        assertEquals(4, UUID.fromString(installIds.single()).version())
        assertEquals(settings.current().installId, installIds.single())

        val expected = JsonObject(
            mapOf(
                "mode" to JsonPrimitive("text"),
                "budget" to JsonPrimitive("az"),
                "keywords" to JsonArray((1..8).map { JsonPrimitive("kelime-$it") }),
                "text" to JsonPrimitive("sevgilimle 1 saatim var"),
                "filters" to JsonObject(
                    mapOf("duration" to JsonPrimitive(60), "companions" to JsonPrimitive("couple")),
                ),
                "excluded" to JsonArray(listOf(JsonPrimitive("ev-cekmece-bosalt"))),
            ),
        )
        assertEquals(expected, napsamJson.parseToJsonElement((request.body as TextContent).text))
    }

    @Test
    fun `havuzdan secilen oneri ve kota`() = runTest {
        val repository = repository { respondJson(response(source = "seed", quota = 0)) }

        assertEquals(
            Outcome.Success(RemoteSuggestionResult(RemoteSuggestion.Seed("ev-cekmece-bosalt"), aiQuotaLeft = 0)),
            repository.suggest(context, "param yok"),
        )
    }

    @Test
    fun `yapay zeka onerisi kart alanlariyla gelir`() = runTest {
        val repository = repository { respondJson(response(source = "ai", quota = 4)) }

        val expected = RemoteSuggestion.Generated(
            slug = "ev-cekmece-bosalt",
            title = "Bir çekmeceyi tamamen boşalt",
            hook = "Zihnindeki dağınıklığı en somut yerden hafiflet.",
            steps = listOf("Çekmeceyi boşalt.", "Ayıkla."),
            fallback = "Tek bir rafı yap.",
            category = Category.EV_ICI,
            categoryLabel = "Ev içi",
            badges = GeneratedBadges(
                cost = "₺0 Bedava",
                duration = "20-40 dk",
                place = "Evde",
                companion = "Tek başına",
            ),
            requiredItems = emptyList(),
            place = Place.HOME,
            venueType = "",
            isFree = true,
        )
        assertEquals(
            Outcome.Success(RemoteSuggestionResult(expected, aiQuotaLeft = 4)),
            repository.suggest(context, "x"),
        )
    }

    @Test
    fun `sunucu oneri bulamazsa bos sonuc`() = runTest {
        val repository = repository { respondJson("""{"suggestion": null}""") }

        assertEquals(
            Outcome.Success(RemoteSuggestionResult(null, aiQuotaLeft = null)),
            repository.suggest(context, "x"),
        )
    }

    @Test
    fun `hatalar tiplenir ve yeniden denenmez`() = runTest {
        val cases: List<Pair<DataError, suspend MockRequestHandleScope.() -> HttpResponseData>> = listOf(
            DataError.RATE_LIMITED to { respond("", HttpStatusCode.TooManyRequests) },
            DataError.SERVER to { respond("", HttpStatusCode.InternalServerError) },
            DataError.INVALID_RESPONSE to { respondJson("""{"error": "başlık eksik"}""", HttpStatusCode.BadRequest) },
            DataError.INVALID_RESPONSE to { respondJson(response(source = "bilinmeyen")) },
            DataError.INVALID_RESPONSE to {
                respond(
                    "<html></html>",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString()),
                )
            },
        )
        cases.forEachIndexed { index, (error, handler) ->
            requests.clear()
            val repository = repository(folderName = "durum-$index") { handler() }

            assertEquals("durum $index", Outcome.Failure(error), repository.suggest(context, "x"))
            assertEquals("durum $index", 1, requests.size)
        }
    }

    @Test
    fun `bes saniyede yanit yoksa zaman asimi`() = runTest {
        val repository = repository {
            delay(6_000)
            respondJson(response(source = "ai"))
        }

        assertEquals(Outcome.Failure(DataError.TIMEOUT), repository.suggest(context, "x"))
        assertEquals(5_000L, currentTime)
    }

    private fun TestScope.repository(
        folderName: String = "ayarlar",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): NetworkRemoteSuggestionRepository {
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = StandardTestDispatcher(testScheduler)
                addHandler { request ->
                    requests += request
                    handler(request)
                }
            },
        )
        settings = SettingsDataSource(testDataStore(folder.newFolder(folderName), backgroundScope))
        return NetworkRemoteSuggestionRepository(NapsamApi(NapsamApi.createClient(engine, napsamJson)), settings)
    }

    private fun response(source: String, quota: Int? = null): String = """
        {
          "suggestion": {
            "slug": "ev-cekmece-bosalt", "title": "Bir çekmeceyi tamamen boşalt",
            "hook": "Zihnindeki dağınıklığı en somut yerden hafiflet.",
            "steps": ["Çekmeceyi boşalt.", "Ayıkla."], "fallback": "Tek bir rafı yap.",
            "category": "ev_ici", "categoryLabel": "Ev içi",
            "badges": {"cost": "₺0 Bedava", "duration": "20-40 dk", "place": "Evde", "companion": "Tek başına"},
            "requiredItems": [], "place": "ev", "venueType": "", "isFree": true, "source": "$source"
          },
          "meta": {"mode": "text", "budget": "az", "keywords": [], "isNight": false, "aiQuotaLeft": ${quota ?: "null"}}
        }
    """.trimIndent()
}
