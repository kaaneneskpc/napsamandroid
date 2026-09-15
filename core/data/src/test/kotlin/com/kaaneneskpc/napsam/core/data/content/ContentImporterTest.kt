package com.kaaneneskpc.napsam.core.data.content

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.data.FakeContentSource
import com.kaaneneskpc.napsam.core.data.contractText
import com.kaaneneskpc.napsam.core.data.database.NapsamDatabase
import com.kaaneneskpc.napsam.core.data.database.entity.UserStateEntity
import com.kaaneneskpc.napsam.core.data.embeddedMeta
import com.kaaneneskpc.napsam.core.data.inMemoryDatabase
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.repository.OfflineSuggestionRepository
import com.kaaneneskpc.napsam.core.data.respondJson
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** İçeriğin cihaza gelişi: gömülü kopya ve sunucudan güncelleme (karar K3) — Robolectric, bellek içi Room. */
@RunWith(AndroidJUnit4::class)
class ContentImporterTest {
    private lateinit var database: NapsamDatabase
    private val requests = mutableListOf<HttpRequestData>()
    private var serverHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        error("sunucuya istek atılmamalı")
    }

    @Before
    fun setUp() {
        database = inMemoryDatabase()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `ilk acilista gomulu icerik birebir kurulur`() = runTest {
        assertTrue(importer().importIfNeeded())

        val imported = database.contentDao().suggestions().map { it.toModel() }.sortedBy { it.slug }
        assertEquals(ContractFixtures.suggestions.sortedBy { it.slug }, imported)
        val info = requireNotNull(database.contentDao().info())
        assertEquals(embeddedMeta.contentVersion, info.version)
        assertEquals(embeddedMeta.contentVersion, info.embeddedVersion)
    }

    @Test
    fun `ayni surum yeniden okunmaz`() = runTest {
        val source = FakeContentSource()
        val importer = importer(source)
        assertTrue(importer.importIfNeeded())
        assertFalse(importer.importIfNeeded())

        // Yeni süreç: veritabanında aynı sürüm var, 500 öneri yeniden çözülmez.
        val restartedSource = FakeContentSource()
        assertFalse(importer(restartedSource).importIfNeeded())
        assertEquals(1, source.suggestionReads)
        assertEquals(0, restartedSource.suggestionReads)
    }

    @Test
    fun `uygulama guncellenince gomulu icerik yeniden kurulur, kayitlar korunur, yetimler silinir`() = runTest {
        assertTrue(importer().importIfNeeded())
        val kept = ContractFixtures.suggestions[0].slug
        val removed = ContractFixtures.suggestions[1].slug
        listOf(kept, removed).forEach { database.userStateDao().upsert(state(it)) }

        val updatedApp = FakeContentSource(
            suggestions = contentItems().filterNot { it.slug() == removed }.toJsonString(),
            meta = metaJson(version = "sha256:yeni-gomulu", count = 499),
        )
        assertTrue(importer(updatedApp).importIfNeeded())

        assertEquals(499, database.contentDao().suggestions().size)
        assertEquals("sha256:yeni-gomulu", database.contentDao().info()?.version)
        assertEquals(listOf(kept), database.userStateDao().all().map { it.slug })
    }

    @Test
    fun `bozuk gomulu icerik mevcut icerigi degistirmez`() = runTest {
        assertTrue(importer().importIfNeeded())
        val unknownCategory = FakeContentSource(
            suggestions = contentItems().withUnknownCategory().toJsonString(),
            meta = metaJson(version = "sha256:bozuk", count = 500),
        )
        val wrongCount = FakeContentSource(meta = metaJson(version = "sha256:eksik", count = 501))

        listOf(unknownCategory, wrongCount).forEach { source ->
            val error = runCatching { importer(source).importIfNeeded() }.exceptionOrNull()
            assertTrue("$error", error is IllegalArgumentException)
            assertEquals(embeddedMeta.contentVersion, database.contentDao().info()?.version)
            assertEquals(500, database.contentDao().suggestions().size)
        }
    }

    @Test
    fun `havuz ilk istekte kurulur, bozuk icerik istisna sizdirmaz`() = runTest {
        assertEquals(500, repository(importer()).suggestions().size)

        val broken = importer(FakeContentSource(meta = metaJson(version = "sha256:eksik", count = 501)))
        assertEquals(500, repository(broken).suggestions().size)
    }

    @Test
    fun `sunucu surumu farkliysa icerik indirilir, havuz yenilenir, gomulu kopya ezmez`() = runTest {
        val importer = importer()
        val repository = repository(importer)
        assertEquals(500, repository.suggestions().size)
        val removed = ContractFixtures.suggestions[1].slug
        serverHandler =
            { respondJson(serverContent("sha256:sunucu", contentItems().filterNot { it.slug() == removed })) }

        assertEquals(Outcome.Success(Unit), importer.syncWithServer("sha256:sunucu"))

        assertEquals("https://napsam.vercel.app/api/v1/content/", requests.single().url.toString())
        assertEquals(499, repository.suggestions().size)
        val info = requireNotNull(database.contentDao().info())
        assertEquals("sha256:sunucu", info.version)
        assertEquals(embeddedMeta.contentVersion, info.embeddedVersion)

        // Uygulama yeniden açılıyor: aynı uygulama sürümü, indirilen içerik yerinde kalır.
        assertFalse(importer().importIfNeeded())
        assertEquals(499, database.contentDao().suggestions().size)
    }

    @Test
    fun `surum ayniysa indirme yapilmaz`() = runTest {
        val importer = importer()
        importer.importIfNeeded()

        assertEquals(Outcome.Success(Unit), importer.syncWithServer(embeddedMeta.contentVersion))
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `bozuk sunucu icerigi yazilmaz`() = runTest {
        val importer = importer()
        importer.importIfNeeded()
        val bodies = listOf(
            serverContent("sha256:a", contentItems().withUnknownCategory()),
            serverContent("sha256:b", contentItems(), count = 501),
            serverContent("sha256:c", emptyList()),
            serverContent("sha256:d", contentItems(), schema = 2),
            """{"schema": 1}""",
        )
        bodies.forEach { body ->
            serverHandler = { respondJson(body) }
            assertEquals(Outcome.Failure(DataError.INVALID_RESPONSE), importer.syncWithServer("sha256:yeni"))
            assertEquals(embeddedMeta.contentVersion, database.contentDao().info()?.version)
            assertEquals(500, database.contentDao().suggestions().size)
        }
    }

    @Test
    fun `sunucu hatasinda mevcut icerik korunur`() = runTest {
        val importer = importer()
        importer.importIfNeeded()
        serverHandler = { respond("", HttpStatusCode.ServiceUnavailable) }

        assertEquals(Outcome.Failure(DataError.SERVER), importer.syncWithServer("sha256:yeni"))
        assertEquals(500, database.contentDao().suggestions().size)
    }

    private fun importer(source: FakeContentSource = FakeContentSource()): ContentImporter {
        val engine = MockEngine { request ->
            requests += request
            serverHandler(request)
        }
        return ContentImporter(
            source = source,
            metaProvider = ContentMetaProvider(source, napsamJson, Dispatchers.Unconfined),
            dao = database.contentDao(),
            api = NapsamApi(NapsamApi.createClient(engine, napsamJson)),
            json = napsamJson,
            defaultDispatcher = Dispatchers.Unconfined,
        )
    }

    private fun repository(importer: ContentImporter) =
        OfflineSuggestionRepository(importer, database.contentDao(), Dispatchers.Unconfined)

    private fun contentItems(): List<JsonObject> =
        napsamJson.parseToJsonElement(contractText("content/suggestions.json")).jsonArray.map { it.jsonObject }

    private fun List<JsonObject>.withUnknownCategory(): List<JsonObject> =
        listOf(JsonObject(first() + ("category" to JsonPrimitive("uydurma")))) + drop(1)

    private fun List<JsonObject>.toJsonString(): String = JsonArray(this).toString()

    private fun serverContent(version: String, items: List<JsonObject>, count: Int = items.size, schema: Int = 1) =
        JsonObject(
            mapOf(
                "schema" to JsonPrimitive(schema),
                "contentVersion" to JsonPrimitive(version),
                "count" to JsonPrimitive(count),
                "suggestions" to JsonArray(items),
            ),
        ).toString()

    private fun metaJson(version: String, count: Int): String {
        val meta = napsamJson.parseToJsonElement(contractText("content/meta.json")).jsonObject
        return JsonObject(
            meta + mapOf("contentVersion" to JsonPrimitive(version), "count" to JsonPrimitive(count)),
        ).toString()
    }

    private fun JsonObject.slug(): String = getValue("slug").jsonPrimitive.content

    private fun state(slug: String) = UserStateEntity(
        slug = slug,
        status = "saved",
        dismissedUntil = null,
        savedAt = 1L,
        doneAt = null,
        updatedAt = 1L,
    )
}
