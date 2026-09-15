package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.FakeContentSource
import com.kaaneneskpc.napsam.core.data.MutableClock
import com.kaaneneskpc.napsam.core.data.content.ContentMetaProvider
import com.kaaneneskpc.napsam.core.data.datastore.SettingsDataSource
import com.kaaneneskpc.napsam.core.data.embeddedMeta
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.respondJson
import com.kaaneneskpc.napsam.core.data.testDataStore
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import java.io.IOException
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Uzaktan yapılandırma (spesifikasyon B7, B8, C3.3). */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConfigRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val clock = MutableClock(Instant.parse("2026-09-15T09:00:00Z"))
    private val requests = mutableListOf<HttpRequestData>()

    @Test
    fun `sunucu yanit vermeden gomulu varsayilan gecerli`() = runTest {
        val repository = repository { error("istek atılmamalı") }

        val config = repository.observeConfig().first()
        assertEquals(BigDecimal(embeddedMeta.wage), config.wage)
        assertEquals(embeddedMeta.budgetTiers.map { it.key }, config.budgetTiers.map { it.key })
        assertEquals(embeddedMeta.themeRotation, config.themeRotation)
        assertEquals(embeddedMeta.contentVersion, config.contentVersion)
        assertFalse(config.aiEnabled)
        assertNull(config.weeklyTheme)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `basarili yanit dogrulanip onbellege yazilir`() = runTest {
        val repository = repository { respondJson(configJson()) }

        assertEquals(Outcome.Success(Unit), repository.refresh())

        val config = repository.observeConfig().first()
        assertEquals("ekransiz", config.weeklyTheme)
        assertTrue(config.aiEnabled)
        assertEquals(BigDecimal("30000.50"), config.wage)
        assertEquals(3, config.androidMinVersionCode)
        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://napsam.vercel.app/api/v1/config/", request.url.toString())
        assertNull("çerez gönderilmez", request.headers[HttpHeaders.Cookie])
    }

    @Test
    fun `taze onbellek icin istek atilmaz, 12 saat sonra atilir`() = runTest {
        val repository = repository { respondJson(configJson()) }

        repository.refresh()
        clock.advance(Duration.ofHours(12).minusMinutes(1))
        repository.refresh()
        assertEquals(1, requests.size)

        clock.advance(Duration.ofMinutes(1))
        repository.refresh()
        assertEquals(2, requests.size)
    }

    @Test
    fun `429 yeniden denenmez ve 10 dakika ertelenir`() = runTest {
        val repository = repository { respond("", HttpStatusCode.TooManyRequests) }

        assertEquals(Outcome.Failure(DataError.RATE_LIMITED), repository.refresh())
        assertEquals(1, requests.size)

        clock.advance(Duration.ofMinutes(9))
        assertEquals(Outcome.Success(Unit), repository.refresh())
        assertEquals(1, requests.size)

        clock.advance(Duration.ofMinutes(1))
        repository.refresh()
        assertEquals(2, requests.size)
    }

    @Test
    fun `sunucu hatasi iki kez artan beklemeyle yeniden denenir`() = runTest {
        val repository = repository { respond("", HttpStatusCode.ServiceUnavailable) }

        assertEquals(Outcome.Failure(DataError.SERVER), repository.refresh())
        assertEquals(3, requests.size)
        assertEquals("1 sn + 2 sn", 3_000L, currentTime)
    }

    @Test
    fun `ag hatasindan sonra yeniden denemede basari`() = runTest {
        val repository = repository {
            if (requests.size == 1) throw IOException("bağlantı koptu")
            respondJson(configJson())
        }

        assertEquals(Outcome.Success(Unit), repository.refresh())
        assertEquals(2, requests.size)
        assertEquals("ekransiz", repository.observeConfig().first().weeklyTheme)
    }

    @Test
    fun `gecersiz yanit onbellege yazilmaz ve yeniden denenmez`() = runTest {
        listOf("{}", configJson(schema = 2), configJson(wage = "yirmi bin")).forEach { body ->
            requests.clear()
            val repository = repository { respondJson(body) }

            assertEquals(body, Outcome.Failure(DataError.INVALID_RESPONSE), repository.refresh())
            assertEquals(1, requests.size)
            assertNull(repository.observeConfig().first().weeklyTheme)
        }
    }

    private fun TestScope.repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): NetworkConfigRepository {
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = StandardTestDispatcher(testScheduler)
                addHandler { request ->
                    requests += request
                    handler(request)
                }
            },
        )
        val source = FakeContentSource()
        return NetworkConfigRepository(
            api = NapsamApi(NapsamApi.createClient(engine, napsamJson)),
            settings = SettingsDataSource(testDataStore(folder.newFolder(), backgroundScope)),
            metaProvider = ContentMetaProvider(source, napsamJson, Dispatchers.Unconfined),
            clock = clock,
        )
    }

    private fun configJson(schema: Int = 1, wage: String = "30000.50"): String = """
        {
          "schema": $schema, "wage": "$wage", "currency": "TRY",
          "budgetTiers": [
            {"key": "bedava", "label": "Bedava", "hint": "", "order": 0, "ratioMin": "0", "ratioMax": "0",
             "amountMin": 0, "amountMax": 0, "amountText": "0 TL"},
            {"key": "bol", "label": "Bol", "hint": "etkinlik, aktivite", "order": 4, "ratioMin": "0.053428",
             "ratioMax": null, "amountMin": 1500, "amountMax": null, "amountText": "1.500 TL+"}
          ],
          "weeklyTheme": "ekransiz", "themeRotation": ["yuru", "elini_kullan", "ekransiz", "duzen"],
          "aiEnabled": true, "contentVersion": "sha256:abc", "suggestionCount": 500,
          "dismissCooldownDays": 30, "androidMinVersionCode": 3, "yeniAlan": "yok sayılır"
        }
    """.trimIndent()
}
