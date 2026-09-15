package com.kaaneneskpc.napsam.core.data.content

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaaneneskpc.napsam.core.data.FakeContentSource
import com.kaaneneskpc.napsam.core.data.contractText
import com.kaaneneskpc.napsam.core.data.database.NapsamDatabase
import com.kaaneneskpc.napsam.core.data.database.entity.UserStateEntity
import com.kaaneneskpc.napsam.core.data.embeddedMeta
import com.kaaneneskpc.napsam.core.data.inMemoryDatabase
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.repository.OfflineSuggestionRepository
import com.kaaneneskpc.napsam.core.model.ContractFixtures
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

/** İçerik içe aktarma (karar K3, spesifikasyon C5.4) — Robolectric üzerinde bellek içi Room. */
@RunWith(AndroidJUnit4::class)
class ContentImporterTest {
    private lateinit var database: NapsamDatabase

    @Before
    fun setUp() {
        database = inMemoryDatabase()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `ilk acilista gomulu icerik birebir aktarilir`() = runTest {
        assertTrue(importer().importIfNeeded())

        val imported = database.contentDao().suggestions().map { it.toModel() }.sortedBy { it.slug }
        assertEquals(ContractFixtures.suggestions.sortedBy { it.slug }, imported)
        assertEquals(embeddedMeta.contentVersion, database.contentDao().version())
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
    fun `surum degisince icerik yenilenir, kullanici kayitlari korunur, yetimler silinir`() = runTest {
        assertTrue(importer().importIfNeeded())
        val kept = ContractFixtures.suggestions[0].slug
        val removed = ContractFixtures.suggestions[1].slug
        listOf(kept, removed).forEach { database.userStateDao().upsert(state(it)) }

        val updated = FakeContentSource(
            suggestions = suggestionsJson { items -> items.filterNot { it.slug() == removed } },
            meta = metaJson(version = "sha256:yeni", count = 499),
        )
        assertTrue(importer(updated).importIfNeeded())

        assertEquals(499, database.contentDao().suggestions().size)
        assertEquals("sha256:yeni", database.contentDao().version())
        assertEquals(listOf(kept), database.userStateDao().all().map { it.slug })
    }

    @Test
    fun `bozuk icerik mevcut icerigi degistirmez`() = runTest {
        assertTrue(importer().importIfNeeded())
        val unknownCategory = FakeContentSource(
            suggestions = suggestionsJson { items ->
                listOf(JsonObject(items.first() + ("category" to JsonPrimitive("uydurma")))) + items.drop(1)
            },
            meta = metaJson(version = "sha256:bozuk", count = 500),
        )
        val wrongCount = FakeContentSource(meta = metaJson(version = "sha256:eksik", count = 501))

        listOf(unknownCategory, wrongCount).forEach { source ->
            val error = runCatching { importer(source).importIfNeeded() }.exceptionOrNull()
            assertTrue("$error", error is IllegalArgumentException)
            assertEquals(embeddedMeta.contentVersion, database.contentDao().version())
            assertEquals(500, database.contentDao().suggestions().size)
        }
    }

    @Test
    fun `havuz ilk istekte aktarilir, bozuk icerik istisna sizdirmaz`() = runTest {
        assertEquals(500, repository(FakeContentSource()).suggestions().size)

        val broken = FakeContentSource(meta = metaJson(version = "sha256:eksik", count = 501))
        assertEquals(500, repository(broken).suggestions().size)
    }

    private fun importer(source: FakeContentSource = FakeContentSource()) = ContentImporter(
        source = source,
        metaProvider = ContentMetaProvider(source, napsamJson, Dispatchers.Unconfined),
        dao = database.contentDao(),
        json = napsamJson,
        defaultDispatcher = Dispatchers.Unconfined,
    )

    private fun repository(source: FakeContentSource) =
        OfflineSuggestionRepository(importer(source), database.contentDao(), Dispatchers.Unconfined)

    private fun suggestionsJson(transform: (List<JsonObject>) -> List<JsonObject>): String {
        val items = napsamJson.parseToJsonElement(contractText("content/suggestions.json")).jsonArray.map {
            it.jsonObject
        }
        return JsonArray(transform(items)).toString()
    }

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
        shownCount = 1,
        updatedAt = 1L,
    )
}
